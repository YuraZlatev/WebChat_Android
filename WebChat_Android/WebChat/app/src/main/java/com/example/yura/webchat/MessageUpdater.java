package com.example.yura.webchat;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MessageUpdater extends Thread
{
    private final String connectionPath = DBConnector.connectionPath;
    private final String tag = "WebChat";
    private Chat room = null;
    private String message = "";
    protected boolean isAlive = true;
    public long lastAddedMessage = 0;
    public ArrayList<String> all_messages = new ArrayList<>();

    public MessageUpdater(Chat chat)
    {
        room = chat;
        this.setDaemon(true);
    }

    @Override
    public void run()
    {
        ConnectivityManager cm = (ConnectivityManager)room.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network n = cm.getActiveNetwork();
        HttpURLConnection con = null;
        try
        {
            while(true)
            {
                if(isAlive) {
                    URL url = new URL(connectionPath);
                    con = (HttpURLConnection) n.openConnection(url);
                    con.setRequestMethod("POST");
                    con.setDoInput(true);
                    con.setDoOutput(true);
                    con.setConnectTimeout(5000);

                    String postData = "action=";
                    if (!message.isEmpty()) {
                        postData += "new_message&message=" + message + "&user_id=" + String.valueOf(DBConnector.getUserId()) + "&last_message=" + String.valueOf(lastAddedMessage);
                        message = "";
                    } else
                        postData += "update_messages&last_message=" + String.valueOf(lastAddedMessage) + "&user_id=" + String.valueOf(DBConnector.getUserId());
                    byte[] a = postData.getBytes("UTF8");
                    con.getOutputStream().write(a, 0, a.length);

                    con.connect();

                    BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(bis));

                    String line = "";
                    String str = "";
                    while ((str = reader.readLine()) != null)
                        line += str + "\n";

                    if (line.indexOf("|") != -1) {
                        int index = line.indexOf("|");
                        int lastIndex = line.lastIndexOf("|");
                        line = line.substring(index, lastIndex + 1);

                        final String data = line;
                        room.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                room.getMessage(data);
                            }
                        });
                    }
                    reader.close();
                }
                Thread.sleep(5000);
            }
        } catch (Exception e) { e.printStackTrace(); }
        finally {
            if(con != null) con.disconnect();
        }
    }

    public void updateMessage(String msg)
    {
        message =  msg;
    }
}
