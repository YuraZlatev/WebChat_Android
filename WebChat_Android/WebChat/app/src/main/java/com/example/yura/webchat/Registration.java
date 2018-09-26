package com.example.yura.webchat;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Registration extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        //----------------------------------------------

    }

    public void click(View view)
    {
        switch(view.getId())
        {
            case R.id.btnCancel: sendIntent("cancel"); break;
            case R.id.btnOk:
            {
                EditText login = (EditText)this.findViewById(R.id.tvLogin);
                EditText pass = (EditText)this.findViewById(R.id.tvPassword);
                EditText nick = (EditText)this.findViewById(R.id.tvNick);

                if(!isLogin(login.getText().toString().trim()))
                    return;
                if(!isPassword(pass.getText().toString().trim()))
                    return;
                if(!isNick(nick.getText().toString().trim()))
                    return;

                String nLogin = login.getText().toString().trim();
                String nPass = pass.getText().toString().trim();
                String nNick = nick.getText().toString().trim();

                DBConnector con = new DBConnector(this, nLogin, nPass, nNick);
                con.start();
            }break;
        }
    }

    protected void answer(String str, boolean flag)
    {
        if(flag)
            sendIntent("OK");
        else
            Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }
    //------------------------------
    private boolean isLogin(String str)
    {
        if(str.isEmpty())
        {
            Toast.makeText(this, Errors.emptyLogin, Toast.LENGTH_LONG).show();
            return false;
        }

        if(str.length() <= 3)
        {
            Toast.makeText(this, Errors.loginLength, Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
    private boolean isPassword(String str)
    {
        if(str.isEmpty())
        {
            Toast.makeText(this, Errors.emptyPass, Toast.LENGTH_LONG).show();
            return false;
        }

        if(str.length() <= 5)
        {
            Toast.makeText(this, Errors.passLength, Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
    private boolean isNick(String str)
    {
        if(str.isEmpty())
        {
            Toast.makeText(this, Errors.emptyNick, Toast.LENGTH_LONG).show();
            return false;
        }

        if(str.length() <= 3)
        {
            Toast.makeText(this, Errors.nickLength, Toast.LENGTH_LONG).show();
            return false;
        }

        try
        {
            Double.valueOf(str);
            Toast.makeText(this, Errors.isNick, Toast.LENGTH_LONG).show();
            return false;
        }catch(Exception ex){}

        return true;
    }
    //-------------------------------
    private void sendIntent(String str)
    {
        Intent intent = new Intent();
        intent.putExtra("status", str);
        Registration.this.setResult(Activity.RESULT_OK, intent);
        Registration.this.finish();
    }
}
