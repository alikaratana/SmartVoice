import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class SecuritySystem {

	// create ip and port variables to connect to the server
	private static String HOST_IP="192.168.43.108";
	private static int PORT_NO=6666;

	// create a flag that keeps the alarm state
    public static boolean isActive =false;

	private JFrame frame;
	private static JLabel lblAlarm = new JLabel("DISABLED");



	// function that creates the GUI
    public SecuritySystem() throws IOException {
		frame = new JFrame("HOME-SECURITY SYSTEM");
		frame.setBounds(100, 100, 589, 389);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		lblAlarm.setHorizontalAlignment(SwingConstants.CENTER);
		lblAlarm.setFont(new Font("Tahoma", Font.BOLD, 32));
		lblAlarm.setBounds(151, 110, 248, 95);
        lblAlarm.setForeground(Color.RED);
		frame.getContentPane().add(lblAlarm);

    }


	// main function
	public static void main(String[] args) {
		try {
			SecuritySystem window = new SecuritySystem();
			window.frame.setVisible(true);
			Thread t = new Thread(main_job);
			t.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


    // function that does the main job (creates the connection, waits for messages)
    private static Runnable main_job = () -> {
        try {
            // create a socket
            Socket socket=new Socket(HOST_IP,PORT_NO);
            System.out.println("Connected to server!");
            // send the ALARM message to the server
            socket.getOutputStream().write("HSSYSTEM".getBytes("UTF-8"));
            Thread.sleep(500);
            // send the alarm state to the server
            socket.getOutputStream().write(String.valueOf(isActive).getBytes("UTF-8"));
            // start to wait for a command
            wait(socket);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    };


    // function that waits for verification and close message
	private static void wait(Socket socket) throws IOException {
		// create an InputStream to get data from server
		BufferedReader sockIn=new BufferedReader(new InputStreamReader(socket.getInputStream()));
		while(true) {
            System.out.println("Waiting for a command...");
            // get command from server
			String msg_from_server = sockIn.readLine();
			System.out.println("Command from server: "+msg_from_server);
			// if the command is ACTIVATE
			if (msg_from_server.equals("ACTIVATE")) {
				if (!isActive) {
				    // activate the alarm system
					activate();
					isActive = true;
				}
			}
			// if the command is TURN_OFF
			else if(msg_from_server.equals("DISABLE"))
			{
			    if(isActive) {
			        // turn off the alarm system
                    close();
                    isActive = false;
                }
			}
		}

	}


	// function that activates the alarm system
	private static void activate() throws IOException{
		lblAlarm.setText("ACTIVATED");
		lblAlarm.setForeground(Color.GREEN);
	}


	// function that turns off the alarm
	private static void close()
	{
		lblAlarm.setText("DISABLED");
		lblAlarm.setForeground(Color.RED);
	}


}

