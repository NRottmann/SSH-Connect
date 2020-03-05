package com.example.testssh;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;


public class MainActivity extends AppCompatActivity {

    // Define the output views
    TextView outputView;
    RecyclerView recyclerView;
    MyAdapter mAdapter;
    RecyclerView.LayoutManager layoutManager;

    // Define required variables for the SSH shell
    JSch jsch;
    Session session;
    ChannelShell channelssh;
    OutputStream input_for_the_channel;
    InputStream output_from_the_channel;
    PrintStream commander;
    BufferedReader br;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputView = findViewById(R.id.outputView);
        recyclerView = findViewById(R.id.outputRV);

        // Set recycler view to a maximum size
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        mAdapter = new MyAdapter();
        recyclerView.setAdapter(mAdapter);
    }

    // Start session in a new thread
    public void startSessionTask(View view){
        // Get the login data for the SSH connection
        EditText usernameText = findViewById(R.id.username);
        EditText passwordText = findViewById(R.id.password);
        EditText ipText = findViewById(R.id.ipAddress);
        EditText portText = findViewById(R.id.port);
        final String user = usernameText.getText().toString();
        final String password = passwordText.getText().toString();
        final String ip = ipText.getText().toString();
        final String port = portText.getText().toString();
        // Start the new thread
        new Thread(new Runnable() {
            public void run() {
                try {
                    startSession(user, password, ip, port);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Send message in a new thread
    public void sendMessageTask(View view){
        // Get message to send
        EditText commandText = findViewById(R.id.messageSSH);
        final String command = commandText.getText().toString();
        // Start the new thread
        new Thread(new Runnable() {
            public void run() {
                try {
                    sendMessage(command);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Start SSH connection
    public void startSession(String user, String password, String ip, String port) throws Exception {
            // Transform String port to integer
            int portNum = 22;
            try {
                portNum = Integer.parseInt(port);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }

            // Create new session
            jsch = new JSch();
            session = jsch.getSession(user, ip, portNum);
            session.setPassword(password);

            // Avoid asking for key confirmation
            java.util.Properties prop = new java.util.Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);

            // Start connection
            session.connect(30000);

            // SSH Channel
            channelssh = (ChannelShell)
                    session.openChannel("shell");
            input_for_the_channel = channelssh.getOutputStream();
            output_from_the_channel = channelssh.getInputStream();

            commander = new PrintStream(input_for_the_channel, true);
            br = new BufferedReader(new InputStreamReader(output_from_the_channel));

            // Connect to channel
            channelssh.connect();
            Thread.sleep(100);

            String line = br.readLine();
            while (line != null && channelssh.isConnected()) {
                // Remove ANSI control chars (Terminal VT 100)
                line = line.replaceAll("\u001B\\[[\\d;]*[^\\d;]", "");
                final String finalLine = line;
                // Run and print output in UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.addItem(finalLine);
                        recyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                    }
                });
                line = br.readLine();
            }
    }

    // Print a message via the open shell
    public void sendMessage(String command){
        // Send command
        commander.println(command);
    }

    // Close the session
    public void closeSession() {
            channelssh.disconnect();
            session.disconnect();
    }
}


