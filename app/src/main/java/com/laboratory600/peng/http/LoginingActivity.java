package com.laboratory600.peng.http;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by wanwan on 15-6-3.
 */
public class LoginingActivity extends Activity {
    MainMyHandler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logining);
        handler=new MainMyHandler(this);
        Intent intent =this.getIntent();
        String password=intent.getStringExtra("password");
        String username=intent.getStringExtra("username");
        String url="http://172.31.138.70:8080/first/servlet/login";
        new Thread(new login(password,username,url)).start();
    }

    static class MainMyHandler extends Handler {
        WeakReference<LoginingActivity> mActivity;

        MainMyHandler(LoginingActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginingActivity theActivity = mActivity.get();
            switch (msg.what) {
                case 2:
                    Toast.makeText(theActivity, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    public class login implements Runnable{
        String password;
        String username;
        String url;
        public login(String password,String username,String url){
            this.password=password;
            this.username=username;
            this.url=url;
        }
        @Override
        public void run() {
            if(!username.equals("")&&!password.equals("")){
                //请求数据
                HttpPost httpRequest  = new HttpPost(url);
                //创建参数
                List<NameValuePair> params=new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("password", password));
                try {
                    //对提交数据进行编码
                    httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                    HttpResponse httpResponse=new DefaultHttpClient().execute(httpRequest);
                    //获取响应服务器的数据
                    if (httpResponse.getStatusLine().getStatusCode()==200) {
                        //利用字节数组流和包装的绑定数据
                        byte[] data =new byte[2048];
                        //先把从服务端来的数据转化成字节数组
                        data = EntityUtils. toByteArray(httpResponse.getEntity());
                        //再创建字节数组输入流对象
                        ByteArrayInputStream bais = new ByteArrayInputStream(data);
                        //绑定字节流和数据包装流
                        DataInputStream dis = new DataInputStream(bais);
                        //将字节数组中的数据还原成原来的各种数据类型，代码如下：
                        String result= dis.readUTF();
                        handler.obtainMessage(2, "服务器返回信息:"+result).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
