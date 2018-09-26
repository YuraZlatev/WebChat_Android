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

public class DBConnector extends Thread {
 //   private static final String IP = "10.3.60.132:8888";
 //   public static final String connectionPath = "http://" + IP + "/WebChat/index.php";
    public static final String connectionPath = "http://n99314il.beget.tech";

    private final String tag = "WebChat";
    private Object room = null;
    private boolean isExit = false;
    private String nick = "";
    private String login = "";
    private String pass = "";

    private static User user = null;
    public static void setUser(User us){ user = us; }
    public static long getUserId(){ return user.getId(); }

    public DBConnector(WebChat chat, String login, String pass) {
        this.setDaemon(true);
        room = chat;
        this.login = login;
        this.pass = pass;
    }

    public DBConnector(Registration reg, String login, String pass, String nick) {
        this.setDaemon(true);
        room = reg;
        this.login = login;
        this.pass = pass;
        this.nick = nick;
    }

    public DBConnector(Chat chat, boolean exit) {
        room = chat;
        isExit = exit;
        if (!exit)
            this.setDaemon(true);
    }

    @Override
    public void run()
    {
        if(room instanceof WebChat)
            webchat((WebChat) room);
        else if(room instanceof Registration)
            registration((Registration)room);
        else if(room instanceof Chat && !isExit)
            chat((Chat)room);
        else if(room instanceof Chat && isExit)
            exitFromChat((Chat)room);
    }

    //--------------------------------------------------------
    private void webchat(final WebChat web)
    {
        ConnectivityManager cm = (ConnectivityManager)web.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network n = cm.getActiveNetwork();
        HttpURLConnection con = null;
        try
        {
            URL url = new URL(connectionPath);
            con = (HttpURLConnection)n.openConnection(url);
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setConnectTimeout(5000);

            String postData = "action=select_user&login="+this.login+"&pass="+this.pass;
            byte[] a = postData.getBytes("UTF8");
            con.getOutputStream().write(a, 0, a.length);

            con.connect();

            BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(bis));

            String line = "";
            String str = "";
            while( (str = reader.readLine()) != null)
                line += str;

            Log.d(tag, "Answer: \n"+line);
            reader.close();

            final String data = line;
            web.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(data.indexOf("db_select_failed") != -1)
                        web.answer("Wrong login or password", false);
                    else if(data.indexOf("db_select_success") != -1)
                    {
                        String json_user = data.substring( data.indexOf("&&") + 2, data.lastIndexOf("&&") );
                        web.answer(json_user, true);
                    }
                }
            });
        } catch (Exception e) { Log.d(tag, "=======================  "+e.getMessage()); e.printStackTrace(); }
        finally {
            if(con != null) con.disconnect();
        }
    }
    private void registration(final Registration reg)
    {
        ConnectivityManager cm = (ConnectivityManager)reg.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network n = cm.getActiveNetwork();
        HttpURLConnection con = null;
        try
        {
            URL url = new URL(connectionPath);
            con = (HttpURLConnection)n.openConnection(url);
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setConnectTimeout(5000);

            String postData = "action=insert_user&login="+this.login+"&pass="+this.pass+"&nick="+this.nick;
            byte[] a = postData.getBytes("UTF8");
            con.getOutputStream().write(a, 0, a.length);

            con.connect();

            BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(bis));

            String line = "";
            String str = "";
            while( (str = reader.readLine()) != null)
                line += str;

            Log.d(tag, "Answer: \n"+line);
            reader.close();

            final String data = line;
            reg.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(data.indexOf("db_insert_nick_exists") != -1)
                        reg.answer(Errors.nickAlreadyExists, false);
                    else if(data.indexOf("db_insert") != -1)
                        reg.answer("Success", true);
                    else
                        reg.answer("Error", false);
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
        finally {
            if(con != null) con.disconnect();
        }
    }
    private void chat(final Chat chat)
    {
        ConnectivityManager cm = (ConnectivityManager)chat.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network n = cm.getActiveNetwork();
        HttpURLConnection con = null;
        try
        {
            URL url = new URL(connectionPath);
            con = (HttpURLConnection)n.openConnection(url);
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setConnectTimeout(5000);

            String postData = "action=select_all_users&user_id=" + String.valueOf(DBConnector.getUserId());
            byte[] a = postData.getBytes("UTF8");
            con.getOutputStream().write(a, 0, a.length);

            con.connect();

            BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(bis));

            String line = "";
            String str = "";
            while( (str = reader.readLine()) != null)
                line += str;

            Log.d(tag, "Answer: \n"+line);
            reader.close();

            final String data = line;
            chat.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chat.answer(data, true);
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
        finally {
            if(con != null) con.disconnect();
        }
    }
    private void exitFromChat(Chat chat)
    {
        ConnectivityManager cm = (ConnectivityManager)chat.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network n = cm.getActiveNetwork();
        HttpURLConnection con = null;
        try
        {
            URL url = new URL(connectionPath);
            con = (HttpURLConnection)n.openConnection(url);
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setConnectTimeout(3000);

            String id = String.valueOf(DBConnector.getUserId());
            String postData = "action=exit_user&user_id="+id;
            byte[] a = postData.getBytes("UTF8");
            con.getOutputStream().write(a, 0, a.length);

            con.connect();

            BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(bis));

            String line = "";
            String str = "";
            while( (str = reader.readLine()) != null)
                line += str;

            reader.close();
        } catch (Exception e) { e.printStackTrace(); }
        finally {
            if(con != null) con.disconnect();
        }
    }
}
