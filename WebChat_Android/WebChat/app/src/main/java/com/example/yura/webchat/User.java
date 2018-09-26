package com.example.yura.webchat;

import java.io.Serializable;

public class User implements Serializable
{
    long id;
    String login;
    String nick;

    public long getId(){ return id; }
    public String getLogin(){ return login; }
    public String getNick(){ return nick; }

    public User(long id, String login, String nick)
    {
        this.id = id;
        this.login = login;
        this.nick = nick;
    }
}
