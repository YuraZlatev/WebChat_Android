package com.example.yura.webchat;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class WebChat extends AppCompatActivity
{
    private final int web_chat_code = 8888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_chat);
        //----------------------------------------
    }


    public void btnClick(View view)
    {
        switch(view.getId())
        {
            case R.id.btnLogin:
            {
                String login = ((EditText)this.findViewById(R.id.etName)).getText().toString().trim();
                String pass= ((EditText)this.findViewById(R.id.etPass)).getText().toString().trim();
                if(login.isEmpty())
                {
                    Toast.makeText(this, Errors.emptyLogin, Toast.LENGTH_LONG).show();
                    return;
                }
                if(pass.isEmpty())
                {
                    Toast.makeText(this, Errors.emptyPass, Toast.LENGTH_LONG).show();
                    return;
                }

                DBConnector conn = new DBConnector(this, login, pass);
                conn.start();
            }break;
            case R.id.btnRegister:
            {
                Intent intent = new Intent();
                intent.setAction("WebChat_Registration");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                this.startActivityForResult(intent, web_chat_code);
            }break;
        }
    }


    protected void answer(String ans, boolean flag)
    {
        if(flag)
        {
            String id = ans.substring(0, ans.indexOf("|"));
            String login = ans.substring(ans.indexOf("|") + 1, ans.lastIndexOf("|"));
            String nick = ans.substring(ans.lastIndexOf("|") + 1);
            User user = new User(Long.valueOf(id), login, nick);
            DBConnector.setUser(user);

            Intent intent = new Intent();
            intent.putExtra("user", user);
            intent.setAction("Chat");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            this.startActivityForResult(intent, web_chat_code);
        }
        else
            Toast.makeText(this, ans, Toast.LENGTH_LONG).show();
    }

    //-------------------------------------------------------------------

    @Override
    public void onActivityResult(int code, int resCode, Intent data)
    {
        if(code == web_chat_code && resCode == Activity.RESULT_OK)
        {
            String str = data.getStringExtra("status");
            if(str.contentEquals("OK"))
                Toast.makeText(this, "Registration success", Toast.LENGTH_SHORT).show();
        }
    }


}
