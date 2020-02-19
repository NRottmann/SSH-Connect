package com.example.testssh;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {
    JSch jsch;
    Session session;
    ChannelExec channelssh;
    ByteArrayOutputStream outputSSH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Define Async Task for handle the SSH connection
        /* new AsyncTask<Integer, Void, Void>(){
            @Override
            protected Void doInBackground(Integer... params) {
                try {
                    executeRemoteCommand("root", "myPW", "192.168.1.1", 22);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(1); */
    }

    // Use async method for starting the session
    public void startSessionTask(View view){

        EditText usernameText = findViewById(R.id.username);
        EditText passwordText = findViewById(R.id.password);
        EditText ipText = findViewById(R.id.ipAddress);
        final String user = usernameText.getText().toString();
        final String password = passwordText.getText().toString();
        final String ip = ipText.getText().toString();

        // Define Async Task for handle the SSH connection
        new AsyncTask<Integer, Void, Void>(){
            @Override
            protected Void doInBackground(Integer... params) {
                try {
                    startSession(user, password, ip);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(1);
    }

    // Use async method for sending commands
    public void sendMessageTask(View view){

        EditText commandText = findViewById(R.id.messageSSH);
        final String command = commandText.getText().toString();

        // Define Async Task for handle the SSH connection
        new AsyncTask<Integer, Void, Void>(){
            @Override
            protected Void doInBackground(Integer... params) {
                try {
                    sendMessage(command);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(1);
    }

    // Start SSH connection
    public void startSession(String user, String password, String ip) throws Exception {

        // Create new session
        jsch = new JSch();
        session = jsch.getSession(user, ip, 22);
        session.setPassword(password);

        // Avoid asking for key confirmation
        java.util.Properties prop = new java.util.Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        // Start connection
        session.connect(30000);

        // SSH Channel
        channelssh = (ChannelExec)
                session.openChannel("exec");
        outputSSH = new ByteArrayOutputStream();
        channelssh.setOutputStream(outputSSH);
    }

    public void sendMessage(String command) throws Exception {

        // Execute command
        channelssh.setCommand(command);
        channelssh.connect();
        channelssh.disconnect();

        // Print SSH output to screen
        TextView outputView = findViewById(R.id.outputView);
        String output = outputSSH.toString();
        outputView.setText(output);
    }
}


