package com.example.testssh;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

    public static final String TAG = MainActivity.class.getSimpleName();

    TextView outputView;
    RecyclerView recyclerView;
    MyAdapter mAdapter;
    RecyclerView.LayoutManager layoutManager;

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

        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter();
        recyclerView.setAdapter(mAdapter);
    }

    // Use async method for starting the session
    public void startSessionTask(View view){
        Log.i(TAG, "Start session task");

        EditText usernameText = findViewById(R.id.username);
        EditText passwordText = findViewById(R.id.password);
        EditText ipText = findViewById(R.id.ipAddress);
        final String user = usernameText.getText().toString();
        final String password = passwordText.getText().toString();
        final String ip = ipText.getText().toString();

        new Thread(new Runnable() {
            public void run() {
                try {
                    startSession(user, password, ip);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Use async method for sending commands
    public void sendMessageTask(View view){
        Log.i(TAG, "Start message task");

        EditText commandText = findViewById(R.id.messageSSH);
        final String command = commandText.getText().toString();

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
    public void startSession(String user, String password, String ip) throws Exception {

        Log.i(TAG, "Start session");

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
        channelssh = (ChannelShell)
                session.openChannel("shell");
        input_for_the_channel = channelssh.getOutputStream();
        output_from_the_channel = channelssh.getInputStream();

        commander = new PrintStream(input_for_the_channel, true);
        br = new BufferedReader(new InputStreamReader(output_from_the_channel));

        // Connect to channel
        channelssh.connect();

        Thread.sleep(100);

        TextView outputView = findViewById(R.id.outputView);
        String line = br.readLine();

        while (line != null && channelssh.isConnected()) {
            Log.i(TAG, "looper session");

            /*line = line.replaceAll("[^\\x00-\\x7F]", "");
            line = line.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
            line = line.replaceAll("\\p{C}", "");
            line = line.replaceAll("\u001B\\[[;\\d]*m", "");*/

            // Remove ANSI control chars (Terminal VT 100)
            line = line.replaceAll("\u001B\\[[\\d;]*[^\\d;]","");

            final String finalLine = line;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.addItem(finalLine);
                    recyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                }
            });

            line = br.readLine();
        }


        Log.i(TAG, "looper ended");
    }

    public void sendMessage(String command){

        Log.i(TAG, "Send message " + command);

        // Send command
        commander.println(command);

        /*
        TextView outputView = findViewById(R.id.outputView);
        String line = br.readLine();
        while (line != null && channelssh.isConnected()) {
            Log.i(TAG, "looper session");
            outputView.setText(line + "\n");
            line = br.readLine();
        }

         */

        Log.i(TAG, "Sending finished");
    }
}


