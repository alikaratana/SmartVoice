package com.example.ali.speakerrecognition;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import omrecorder.AudioChunk;
import omrecorder.AudioSource;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.Recorder;
import omrecorder.WriteAction;


public class MainScreen extends AppCompatActivity {

    // create ip and port variables to connect to the server
    private static String HOST_IP="192.168.0.12";
    private static int PORT_NO=6666;

    // create the buttons
    Button record;
    Button recognize;
    Button close_alarm;
    Button reconn;
    TextView usr_lbl;
    TextView alarm_state;

    // create a socket
    Socket socket=null;
    // create InputStream to get data from server
    BufferedReader inFromServer=null;

    // create a flag for checking if recording is active
    boolean isRecording_flag=false;
    // create a flag for checking if recording is done
    boolean isRecorded_flag=false;
    // create a flag for checking if connection with the server is available or not
    boolean connected_flag=false;
    // create a flag for checking if sending the audio to the server is done or not
    boolean sended_flag =false;
    // create a flag for checking if closing the alarm system is done or not
    boolean closed_flag=false;
    // create a flag for alarm state (true:active, false:disabled)
    String alarm_state_flag="";


    // create username variable
    String username="";
    String password="";
    // create admin password variable
    String admin_password="";

    // create recorder
    Recorder recorder;


    // function that sets listeners for buttons
    protected void SetListeners()
    {
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isRecording_flag) {
                    isRecording_flag = true;
                    record.setBackgroundResource(R.drawable.button_green);
                    record.setText("STOP");
                    initialize_recorder();
                    recorder.startRecording();
                }
                else {
                    isRecording_flag = false;
                    record.setBackgroundResource(R.drawable.button_red);
                    record.setText("RECORD");
                    try {
                        recorder.stopRecording();
                        isRecorded_flag = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    record.post(new Runnable() {
                        @Override public void run() {
                            animateVoice(0);
                        }
                    });
                }
            }
        });

        recognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected_flag) {
                    if(isRecorded_flag) {
                        new SendAndActivateThread().execute();
                        isRecorded_flag = false;
                    }
                    else
                        Toast.makeText(v.getContext(), "Please record your voice first !", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(v.getContext(), "Please connect to server first !", Toast.LENGTH_SHORT).show();

            }
        });


        close_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRecorded_flag) {
                    new TurnOffThread().execute();
                    isRecorded_flag = false;
                }
                else
                    Toast.makeText(v.getContext(), "Please record your voice first !", Toast.LENGTH_SHORT).show();
            }
        });

        reconn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog=create_login_dialog();
                dialog.show();
            }
        });


    }


    // function that creates login dialog
    protected AlertDialog create_login_dialog()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainScreen.this);
        final View view=getLayoutInflater().inflate(R.layout.login_dialog, null);
        final EditText username_field = (EditText) view.findViewById(R.id.txt_usr);
        final EditText password_field = (EditText) view.findViewById(R.id.txt_pass);
        Button login = (Button) view.findViewById(R.id.login);
        Button cancel = (Button) view.findViewById(R.id.close_dialog);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usr=String.valueOf(username_field.getText());
                username = usr;
                String pass = String.valueOf(password_field.getText());
                password=pass;
                if((username.equals("ali_karatana") && password.equals("1234"))
                        ||(username.equals("berkay_ugur"))&& password.equals("5678")) {
                    dialog.dismiss();
                    new ConnectThread().execute();

                }
                else
                {
                    Toast.makeText(MainScreen.this,"Wrong username or password !",Toast.LENGTH_SHORT).show();
                    reconn.setEnabled(true);
                    reconn.setBackgroundResource(R.drawable.button_grey);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                reconn.setEnabled(true);
                reconn.setBackgroundResource(R.drawable.button_grey);
            }
        });


        return  dialog;
    }


    // function that creates admin dialog
    protected AlertDialog create_admin_dialog()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainScreen.this);
        final View view=getLayoutInflater().inflate(R.layout.admin_dialog, null);
        final EditText password_field = (EditText) view.findViewById(R.id.txt_pass);
        Button login = (Button) view.findViewById(R.id.login);
        Button cancel = (Button) view.findViewById(R.id.close_dialog);
        builder.setView(view);
        final AlertDialog dialog = builder.create();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String admin_pass = String.valueOf(password_field.getText());
                admin_password=admin_pass;
                if(admin_password.equals("admin")) {
                    dialog.dismiss();
                    Intent intent = new Intent(MainScreen.this,CreateUserActivity.class);
                    startActivity(intent);

                }
                else
                {
                    Toast.makeText(MainScreen.this,"Wrong admin password !",Toast.LENGTH_SHORT).show();
                    reconn.setEnabled(true);
                    reconn.setBackgroundResource(R.drawable.button_grey);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return  dialog;
    }


    // thread class that connects to the server
    private class ConnectThread extends AsyncTask<Void,Void,Void>
    {
        private ProgressDialog dialog = new ProgressDialog(MainScreen.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Connecting to the Server...");
            this.dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // connect to server and get the socket
                socket=connect_to_server();
                connected_flag=true;
                // get the alarm state from the server
                alarm_state_flag=inFromServer.readLine();
                return null;

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (connected_flag) {
                Toast.makeText(MainScreen.this, "Connected to the server !", Toast.LENGTH_SHORT).show();
                usr_lbl.setText(username);
                usr_lbl.setTextColor(Color.GREEN);
                reconn.setEnabled(false);
                reconn.setBackgroundResource(R.drawable.disabled_button);
                if(alarm_state_flag.equals("true")){
                    alarm_state.setText("ON");
                    alarm_state.setTextColor(Color.GREEN);
                    recognize.setEnabled(false);
                    recognize.setBackgroundResource(R.drawable.disabled_button);
                    close_alarm.setEnabled(true);
                    close_alarm.setBackgroundResource(R.drawable.button_grey);
                }
                else if(alarm_state_flag.equals("false")) {
                    alarm_state.setText("OFF");
                    alarm_state.setTextColor(Color.RED);
                    close_alarm.setEnabled(false);
                    close_alarm.setBackgroundResource(R.drawable.disabled_button);
                    recognize.setEnabled(true);
                    recognize.setBackgroundResource(R.drawable.button_grey);
                }

            }
            else {
                Toast.makeText(MainScreen.this, "Failed to connect the server !", Toast.LENGTH_SHORT).show();
                reconn.setBackgroundResource(R.drawable.button_grey);
                reconn.setEnabled(true);
            }
        }

    }



    // function that connects to the server
    protected Socket connect_to_server() throws IOException, InterruptedException {
        // create a socket
        Socket socket = new Socket(HOST_IP,PORT_NO);
        // initialize the InputStream
        inFromServer=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // send the client type to the server
        socket.getOutputStream().write("ANDROID".getBytes("UTF-8"));
        Thread.sleep(500);
        // send the username to the server
        socket.getOutputStream().write(username.getBytes("UTF-8"));
        Thread.sleep(500);
        return socket;
    }


    // thread class that sends the audio to the server
    private class SendAndActivateThread extends AsyncTask<Void,Void,Void>
    {
        private ProgressDialog dialog = new ProgressDialog(MainScreen.this);
        private String response;

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Verification is in progress to activate the alarm system. Please wait !");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String command="TEST";
                // send the message TEST to server to start sending audio file
                socket.getOutputStream().write(command.getBytes("UTF-8"));
                response=main_job();
                sended_flag =true;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (sended_flag) {
                if (response.equals("ACCEPT")) {
                    Toast.makeText(MainScreen.this, "Speaker is verified !", Toast.LENGTH_SHORT).show();
                    new ConnectThread().execute();
                } else if (response.equals("DENIED")) {
                    Toast.makeText(MainScreen.this, "Access denied !", Toast.LENGTH_SHORT).show();
                    new ConnectThread().execute();
                }
            }
            else
                Toast.makeText(MainScreen.this, "Failed to send audio !", Toast.LENGTH_SHORT).show();
        }

    }


    // function that sends the audio to the server
    protected void send_audio() throws IOException, InterruptedException {
        // create a new file
        File file=new File(Environment.getExternalStorageDirectory() + "/SpeakerRecognition/test_record.wav");
        // create FileInputStream for file
        FileInputStream fileInputStream = new FileInputStream(file);
        // send the audio file
        int x;
        do {
            x = fileInputStream.read();
            socket.getOutputStream().write(x);

        } while (x != -1);
        Thread.sleep(500);
        // send STOP message to server
        socket.getOutputStream().write("STOP".getBytes("UTF-8"));
        // close FileInputStream
        fileInputStream.close();

    }


    // function that does the main job (sends audio, gets the response)
    protected String main_job() throws IOException, InterruptedException {
        // send the audio file to server
        send_audio();
        // get the verification information from the server
        String response = inFromServer.readLine();
        return response;
    }


    // thread class that closes the alarm system
    private class TurnOffThread extends AsyncTask<Void,Void,Void>
    {
        private ProgressDialog dialog = new ProgressDialog(MainScreen.this);
        private String response;

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Verification is in progress to turn off the alarm system. Please wait...");
            this.dialog.setCancelable(false);
            this.dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // send the message TEST to server to start sending audio file
                String command="CLOSE";
                socket.getOutputStream().write(command.getBytes("UTF-8"));
                // get the verification information from server
                response=main_job();
                closed_flag=true;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if(closed_flag) {
                if (response.equals("ACCEPT")) {
                    Toast.makeText(MainScreen.this, "Alarm is turned off !", Toast.LENGTH_SHORT).show();
                    new ConnectThread().execute();
                }else if (response.equals("DENIED")) {
                    Toast.makeText(MainScreen.this, "Access denied !", Toast.LENGTH_SHORT).show();
                    new ConnectThread().execute();
                }
            }
            else {
                Toast.makeText(MainScreen.this, "Failed to turn off the alarm system !", Toast.LENGTH_SHORT).show();
                new ConnectThread().execute();
            }
        }

    }


    // function that initializes the recorder
    private void initialize_recorder()
    {
        // simple recorder
        recorder = OmRecorder.wav(
                new PullTransport.Default(mic(), new PullTransport.OnAudioChunkPulledListener() {
                    @Override public void onAudioChunkPulled(AudioChunk audioChunk) {
                        animateVoice((float) (audioChunk.maxAmplitude() / 150.0));
                    }
                }), file());

        // recorder with silence removal
        /*recorder = OmRecorder.wav(
                new PullTransport.Noise(mic(), new PullTransport.OnAudioChunkPulledListener() {
                    @Override public void onAudioChunkPulled(AudioChunk audioChunk) {
                        animateVoice((float) (audioChunk.maxAmplitude() / 150.0));
                    }
                }, new WriteAction.Default(), new Recorder.OnSilenceListener() {
                    @Override public void onSilence(long silenceTime) {
                        Log.e("silenceTime", String.valueOf(silenceTime));
                    }
                }, 200), file());*/
    }


    // function creates the animation for record button
    private void animateVoice(final float maxPeak) {
        record.animate().scaleX(1 + maxPeak).scaleY(1 + maxPeak).setDuration(10).start();
    }


    // function that initializes the audio source and its preferences
    private AudioSource mic() {
        return new AudioSource.Smart(MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                AudioFormat.CHANNEL_IN_MONO, 16000);
    }


    // function that creates the test_record.wav file
    @NonNull
    private File file() {
        File directory=new File(Environment.getExternalStorageDirectory(),"/SpeakerRecognition");
        directory.mkdirs();
        return new File(directory,"test_record.wav");
    }


    // function that is called when activity starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        recognize = (Button) findViewById(R.id.recognize);
        record = (Button) findViewById(R.id.record);
        close_alarm = (Button) findViewById(R.id.close);
        reconn = (Button) findViewById(R.id.reconnect);
        usr_lbl = (TextView) findViewById(R.id.usr_lbl);
        alarm_state = (TextView) findViewById(R.id.alarm_state);

        close_alarm.setEnabled(false);
        reconn.setEnabled(false);
        SetListeners();

        AlertDialog login_dialog= create_login_dialog();
        login_dialog.show();

    }


    // function that is called when back is pressed
    @Override
    public void onBackPressed() {
        String exit_msg="EXIT";
        if(socket!=null && socket.isConnected()) {
            try {
                socket.getOutputStream().write(exit_msg.getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                //------------------- settings of the app -----------------
                return true;
            case R.id.about:
                //------------------- information about the app -----------------
                AlertDialog.Builder builder = new AlertDialog.Builder(MainScreen.this);
                TextView about=new TextView(MainScreen.this);
                about.setText("SrAndro v 1.2\nSpeaker Verification Home-Security System\n" +
                        "Ali Karatana & Berkay Uğur\n2017");
                about.setGravity(Gravity.CENTER_HORIZONTAL);
                builder.setView(about);
                Dialog dialog=builder.create();
                dialog.show();

                return true;
            case R.id.create_user:
                Dialog admin_dialog=create_admin_dialog();
                admin_dialog.show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
