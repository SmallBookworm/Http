package com.laboratory600.peng.http;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * Created by Peng on 2015/5/14.
 */
public class MonitorActivity extends Activity {
    String url = "http://172.31.138.70:8080/myServlet/servlet/MyServlet?secret=12580";
    TextView monitorTextView;
    MonitorMyHandler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        monitorTextView = (TextView) findViewById(R.id.monitor_textView_dutyshow);
        Button monitorButton = (Button) findViewById(R.id.monitor_button);
        myHandler = new MonitorMyHandler(MonitorActivity.this);
        monitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new HttpConnect()).start();
            }
        });

    }

    static class MonitorMyHandler extends Handler {
        WeakReference<MonitorActivity> mActivity;

        MonitorMyHandler(MonitorActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MonitorActivity theActivity = mActivity.get();
            switch (msg.what) {
                case 0:
                    String text = (String) msg.obj;
                    theActivity.monitorTextView.setText(text);
                    break;
            }
        }
    }

    class HttpConnect implements Runnable {

        @Override
        public void run() {


            try {
                URL urll = new URL(url);


                HttpURLConnection urlConnection = (HttpURLConnection) urll.openConnection();

                InputStreamReader in = new InputStreamReader(urlConnection.getInputStream(), "UTF-8");//读取字节

                BufferedReader bufferedReader = new BufferedReader(in);
                StringBuilder buffer = new StringBuilder();//缓冲
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line);
                }
                myHandler.obtainMessage(0, buffer.toString()).sendToTarget();
                in.close();
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
