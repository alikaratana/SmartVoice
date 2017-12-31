from socket import *
import config
import SpeakerRecognition
from Utils import XmlHelper
from Utils import LoggingHelper


def get_names_and_pass(users_array):
    usernames = []
    passwords = []
    for user in users_array:
        usernames.append(user[0])
        passwords.append(user[1])
    return usernames, passwords


def get_audio_file(conn):
    # create a new file for audio
    audio = open('audio_from_client.wav', 'wb')
    # start to receive data from client
    l = conn.recv(1024)
    LoggingHelper.log("Receiving the audio file....")
    # until STOP message is received, continue to receive data
    while l != b'STOP':
        audio.write(l)
        l = conn.recv(1024)
    # close file
    audio.close()
    LoggingHelper.log("Audio file has been downloaded successfully !")


def main():
    # initialize logger
    LoggingHelper.initialize_logger()
    # get the information of all users
    users_array = XmlHelper.parse_users_xml()
    usernames, passwords = get_names_and_pass(users_array)
    # create a socket
    soc = socket()
    soc.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
    # initialize the host IP address and port number
    HOST_IP = config.Server.IP_ADDR
    PORT = config.Server.PORT
    # open socket on the host and port number
    soc.bind((HOST_IP, PORT))
    # listen the socket
    soc.listen(10)
    # create alarm state
    alarm_state_str = ""

    while True:
        LoggingHelper.log("-------------------------------------------------")
        LoggingHelper.log("Server is now online on IP: " + HOST_IP + " Port No: " + str(PORT))
        # accept connection request from client
        conn, addr = soc.accept()
        LoggingHelper.log("Got connection from " + str(addr[0]) + "on Port No" + str(addr[1]))
        # get the client type
        type_msg = conn.recv(1024)
        # decode the message with UTF-8 decoding
        type_str = type_msg.decode('UTF-8', 'ignore')
        LoggingHelper.log("Client type is:" + type_str)

        # if the client type is alarm
        if type_msg == b'HSSYSTEM':
            # set the connection between server and alarm system
            secconn = conn
            # receive the alarm state from the alarm system
            alarm_state = secconn.recv(1024)
            # decode the message with UTF-8 decoding
            alarm_state_str = alarm_state.decode('UTF-8', 'ignore')
            LoggingHelper.log("Alarm state: " + alarm_state_str)

        # if the client type is android
        elif type_msg == b'ANDROID':
            # get the username from client
            usermsg = conn.recv(1024)
            # decode the message with UTF-8 decoding
            username = usermsg.decode('UTF-8', 'ignore')
            LoggingHelper.log("Client username: " + username)
            # send the alarm state to the client
            state = alarm_state_str + "\n"
            conn.send(state.encode())
            # get the command from the server
            cmd_msg = conn.recv(1024)
            # decode the message with UTF-8 decoding
            command = cmd_msg.decode('UTF-8', 'ignore')
            LoggingHelper.log("Command from client is: " + command)

            # check the command
            # if test command is received from the client, start to download the audio file
            if cmd_msg == b'TEST':
                get_audio_file(conn)
                # find speaker
                LoggingHelper.log("Finding speaker........")
                # send the verification information to the client and the alarm system
                speaker = str(SpeakerRecognition.main('./audio_from_client.wav'))
                # if the owner of the audio is correct person...
                if username == speaker:
                    # send accept message to the client
                    acp_ack_client = "ACCEPT\n"
                    conn.send(acp_ack_client.encode())
                    # send accept message to the alarm system
                    acp_ack_alarm = "ACTIVATE\n"
                    secconn.send(acp_ack_alarm.encode())
                    # set the alarm state to true
                    alarm_state_str = "true"
                # if the owner of the audio is not correct person...
                else:
                    acp_nack = "DENIED\n"
                    # send denied message to the client
                    conn.send(acp_nack.encode())

            # if turn off command is received from the client...
            elif cmd_msg == b'CLOSE':
                get_audio_file(conn)
                # find speaker
                LoggingHelper.log("Finding speaker........")
                # send the verification information to the client and the alarm system
                speaker = str(SpeakerRecognition.main('./audio_from_client.wav'))
                # if the owner of the audio is correct person...
                if username == speaker:
                    # send accept message to the client
                    acp_ack_client = "ACCEPT\n"
                    conn.send(acp_ack_client.encode())
                    # send the turn off command to the alarm system
                    acp_ack_alarm = "DISABLE\n"
                    secconn.send(acp_ack_alarm.encode())
                    # set the alarm state to false
                    alarm_state_str = "false"
                # if the owner of the audio is not correct person...
                else:
                    acp_nack = "DENIED\n"
                    # send denied message to the client
                    conn.send(acp_nack.encode())

            # if exit command is received from the client, close the connection between client and server
            elif cmd_msg == b'EXIT':
                conn.close()


if __name__ == '__main__':
    main()
