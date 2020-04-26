package com.aaaaaaaaaa.helloworld;

import android.util.Log;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Scoket {
    static Socket sok;
    static OutputStream os;
    static InputStream is;
    static private String Tag;

    static byte[] buf = new byte[1];
    static public boolean connflag = false;

    public Scoket() {
    }

    public static class SockConnThread extends Thread{

        @Override
        public void run() {
            try {
                sok = new Socket("192.168.4.1", 2333);
                if (sok != null){
                    os = sok.getOutputStream();
                    is = sok.getInputStream();
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
                connflag = false;
                return;
            } catch (IOException e) {
                e.printStackTrace();
                connflag = false;
                return;
            }
            int num = 0;
            while (buf == null){
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    connflag = false;
                    return;
                }
                try {
                    is.read(buf);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                num++;
                if (num >= 20){
                    Log.d(Tag, "Failed!");
                    connflag = false;
                    return;
                }
            }
            Log.d(Tag, "Success!");
            connflag = true;
            return;
        }
    }

    static public void writeMsg(byte[] bt){
        if (bt == null || bt[0] == 0){
            return;
        }
        try {
            os.write(bt);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
