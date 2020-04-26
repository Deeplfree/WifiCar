package com.aaaaaaaaaa.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import static com.aaaaaaaaaa.helloworld.Scoket.is;

public class MainActivity extends AppCompatActivity {

    private Button moveBtn;
    private Button connBtn;
    private TextView textView;
    private RelativeLayout layout;
    private TextView connTxt;
    Timer movetim = null;
    TimerTask movetimtask = null;

    float x = 0, y = 0;
    float dx = 0, dy = 0;
    float offsetx = 0, offsety = 0;
    float initx = 0, inity = 0;
    float lastx = 0, lasty = 0;

    float screenWidth = 0;
    float screenHeight = 0;

    public MainActivity() throws IOException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        moveBtn = findViewById(R.id.movebtn1);
        moveBtn.setOnTouchListener(new Move_btn_ontouchlistener());
        connBtn = findViewById(R.id.conn_btn);
        connBtn.setOnClickListener(new Conn_btn_onclicklistenew());
        textView = findViewById(R.id.text1);
        connTxt = findViewById(R.id.conn_txt);
        layout = findViewById(R.id.l1);
        layout.measure(0,0);
//        screenWidth = layout.getMeasuredWidth();
//        screenHeight = layout.getMeasuredHeight();
    }

    private class Move_btn_ontouchlistener implements View.OnTouchListener{
        @SuppressLint("SetTextI18n")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    initx = event.getRawX();
                    inity = event.getRawY();
                    lastx = v.getLeft();
                    lasty = v.getTop();

                    screenWidth = layout.getWidth();
                    screenHeight = layout.getHeight();
                    textView.setText(screenWidth+" "+screenHeight);

                    startMoveTim();
                    break;
                case MotionEvent.ACTION_MOVE:
                    x = event.getRawX();
                    y = event.getRawY();
                    dx = x - initx;
                    dy = y - inity;

                    v.setX(dx + lastx);
                    v.setY(dy + lasty);
                    if (Math.abs(dx) >= screenWidth/2 - v.getWidth()/2){
                        if (dx >= 0){
                            v.setX(screenWidth - v.getWidth());
                        }
                        else {
                            v.setX(0);
                        }
                    }
                    if (Math.abs(dy) >= screenHeight/2 - v.getHeight()/2){
                        if (dy >= 0){
                            v.setY(screenHeight - v.getHeight());
                        }
                        else {
                            v.setY(0);
                        }
                    }

//                    textView.setText(screenWidth+" "+screenHeight);
                    v.postInvalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    v.setX(lastx);
                    v.setY(lasty);
                    dx = 0;
                    dy = 0;
                    if(movetim != null){
                        movetim.cancel();
                        movetim = null;
                    }
                    stopMoveTim();
                    break;

            }
            return true;
        }
    }

    private class Conn_btn_onclicklistenew implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            int overtime = 0;
            new Scoket.SockConnThread().start();
            connTxt.setText("连接中...");
            while (!Scoket.connflag){
                overtime++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (overtime > 30) {
                    connTxt.clearComposingText();
                    return;
                }
            }
            connBtn.setText("已连接");
            connTxt.setText(" ");
        }
    }


    private void startMoveTim(){
        if (movetim == null){
            movetim = new Timer();
        }
        if (movetimtask == null){
            movetimtask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        Scoket.is.read(Scoket.buf);
                        if (Scoket.buf == null){
                            stopMoveTim();
                            connBtn.setText("未连接");
                            connTxt.setText(" ");
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (Scoket.connflag){
                        byte[] bt = {0,0,0};
                        /*bt数组
                        * [0]：0000 0001前进；0000 0010后退；0000 0100左转；0000 1000右转
                        * [1]：速度控制，范围100~200
                        * [2]：转向控制，范围100~200*/
                        if (dy >= 0){
                            bt[0] = (byte)(bt[0] | 2);
                        }else {
                            bt[0] = (byte)(bt[0] | 1);
                        }
                        if (dx >= 0){
                            bt[0] = (byte)(bt[0] | 8);
                        }else {
                            bt[0] = (byte)(bt[0] | 4);
                        }
                        if (Math.abs(dy) >= (screenHeight*0.3/2)){
                            bt[1] = (byte) 200;
                        }else {
                            bt[1] = (byte)(Math.abs(dy)*100/(screenHeight*0.3/2) + 100);
                        }
                        if (Math.abs(dx) >= (screenWidth*0.3/2)){
                            bt[2] = (byte) 200;
                        }else {
                            bt[2] = (byte)(Math.abs(dx)*100/(screenWidth*0.3/2) + 100);
                        }
                        Scoket.writeMsg(bt);
                        int b0 = 0,b1 = 0,b2 = 0;
                        textView.setText((bt[0]&0xff)+" "+(bt[1]&0xff)+" "+(bt[2]&0xff)+"\r\n"+dx+" "+dy);
                    }
                }
            };
        }
        if (movetim != null && movetimtask != null){
            movetim.schedule(movetimtask, 0, 100);
        }
    }

    private void stopMoveTim(){
        if (movetim != null){
            movetim.cancel();
            movetim = null;
        }
        if (movetimtask != null){
            movetimtask.cancel();
            movetimtask = null;
        }
    }
}
