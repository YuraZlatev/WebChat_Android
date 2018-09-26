package com.example.yura.webchat;

import android.animation.AnimatorInflater;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Chat extends AppCompatActivity
{
    private User user = null;
    private boolean isLeft_panelHidden = true;
    private MessageUpdater updater = null;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_chat);
        //--------------------------------------
        user = (User)getIntent().getSerializableExtra("user");
        TextView ac = (TextView)this.findViewById(R.id.tvAccount);
        ac.setText(user.getNick());
        //--------------------------------------
        updater = new MessageUpdater(this);
        if(b != null)
            getMessage(b.getString("all_messages"));
        updater.start();
    }

    public void click_refresh(View view)
    {
        DBConnector con = new DBConnector(this, false);
        con.start();
        Toast.makeText(this, "updating", Toast.LENGTH_SHORT).show();
    }

    /**
     * получаем список пользователей
     */
    protected void answer(String str, boolean flag)
    {
        int indexSlash = 0;
        int indexAmpersant = 0;

        ArrayList<String> nicks = new ArrayList<>();
        final ArrayList<String> status = new ArrayList<>();
        while(true)
        {
            if(str.indexOf("|", indexSlash) == -1)
                break;

            indexSlash = str.indexOf("|", indexSlash);
            indexAmpersant = str.indexOf("|", indexSlash + 1);
            String us = str.substring(indexSlash + 1, indexAmpersant);

            indexSlash = indexAmpersant + 1;

            String isSelf_nick = us.substring(0, us.indexOf("&"));
            if(isSelf_nick.contentEquals(user.getNick()))
                continue;

            nicks.add(isSelf_nick);
            status.add(us.substring(us.indexOf("&") + 1));
        }
        //-----------------
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.contact_item, R.id.tvContact, nicks)
        {
            @Override
            public View getView(int pos, View v, ViewGroup group)
            {
                v = super.getView(pos, v, group);
                if(status.get(pos).contentEquals("1"))
                {
                    TextView tv = (TextView) ((LinearLayout)v).getChildAt(1);
                    tv.setBackground(getResources().getDrawable(R.drawable.online, Chat.this.getTheme()));
                }
                return v;
            }
        };
        ListView contacts = (ListView)this.findViewById(R.id.contacts);
        contacts.setAdapter(adapter);
    }

    /**
     * получаем сообщение с сервера
     */
    protected void getMessage(String msg)
    {
        final LinearLayout list = (LinearLayout)this.findViewById(R.id.list_messages);
        final ScrollView scroll = (ScrollView)this.findViewById(R.id.main_scroll);
        scroll.postDelayed(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 100L);

        int index = 0;
        while(msg.indexOf("|", index) != -1)
        {
            index = msg.indexOf("|", index);
            String us = msg.substring(index + 1, msg.indexOf("|", index + 1));
            index = msg.indexOf("|", index + 1) + 1;

            String[] user_info = us.split("&");
            long curId = Integer.valueOf(user_info[0]);
            if(updater.lastAddedMessage >= curId)
                continue;
            else
                updater.lastAddedMessage = curId;

            updater.all_messages.add("|" + us + "|");
            TextView tv = new TextView(this);
            user_info[1] = user_info[1].replace("\\n", "\r\n");
            tv.setText(user_info[2]+"\n"+user_info[1]);
            tv.setGravity(Gravity.LEFT);
            tv.setPadding(10,10,10,10);
            tv.setTextColor(Color.rgb(255,255,255));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(10,10,10,10);
            if(user_info[2].contentEquals(user.getNick()))
            {
                params.gravity = Gravity.RIGHT;
                tv.setBackground(getResources().getDrawable(R.drawable.user_message, getTheme()));
            }
            else
            {
                params.gravity = Gravity.LEFT;
                tv.setBackground(getResources().getDrawable(R.drawable.contact_message, getTheme()));
            }
            tv.setLayoutParams(params);
            list.addView(tv);
        }

        scroll.fullScroll(ScrollView.FOCUS_DOWN);
    }

    public void sendMessage(View view)
    {
        EditText field = (EditText)this.findViewById(R.id.etSend);
        String msg = field.getText().toString().trim();
        if(msg.isEmpty())
            return;

        updater.updateMessage(msg);
        field.setText("");
        Toast.makeText(this, "sending...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle b)
    {
        super.onSaveInstanceState(b);
        //--------------------------
        String messages = "";
        for(String s : updater.all_messages)
            messages += s;
        b.putString("all_messages", messages);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //-------------
        updater.isAlive = true;
    }

    @Override
    public void onStop()
    {
        super.onStop();
        //-------------
        updater.isAlive = false;
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        //-------------------
        DBConnector con = new DBConnector(this, true);
        con.start();
    }

    /**
     * анимирование появления left_panel
     */
    public void click(View v)
    {
        final LinearLayout left_panel = (LinearLayout)this.findViewById(R.id.panel_left);
        ValueAnimator anime = null;
        if(isLeft_panelHidden)
            anime = (ValueAnimator)AnimatorInflater.loadAnimator(this, R.animator.left_panel_visible);
        else
            anime = (ValueAnimator)AnimatorInflater.loadAnimator(this, R.animator.left_panel_hidden);

        anime.setInterpolator(new AccelerateDecelerateInterpolator());
        anime.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int x = (int)valueAnimator.getAnimatedValue();
                left_panel.setX(x);
            }
        });
        anime.start();
        isLeft_panelHidden = !isLeft_panelHidden;
    }
}
