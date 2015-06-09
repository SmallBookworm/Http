package com.laboratory600.peng.http;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Peng on 2015/5/14.
 *
 */
public class MainActivity extends Activity {
    String airPhoneNumber = "9999";
    String motorPhoneNumber = "0000";
    SharedPreferences mySharedPreferences;
    private String SMS_SEND_ACTIOIN = "SMS_SEND_ACTIOIN";
    private String SMS_DELIVERED_ACTION = "SMS_DELIVERED_ACTION";
    mServiceReceiver mReceiver01 = new mServiceReceiver();
    mServiceReceiver mReceiver02 = new mServiceReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button monitorButton = (Button) findViewById(R.id.main_button_monitor);
        Button postfButton = (Button) findViewById(R.id.main_button_postf);
        Button postdButton = (Button) findViewById(R.id.main_button_postd);
        Button smsfButton = (Button) findViewById(R.id.main_button_smsf);
        Button smsdButton = (Button) findViewById(R.id.main_button_smsd);
        Button settingButton = (Button) findViewById(R.id.main_button_setting);
        /**提取sharedPreferences储存数据*/
        mySharedPreferences = getSharedPreferences("PhoneNumber", Activity.MODE_PRIVATE);
        String burl = mySharedPreferences.getString("airPhoneNumber", "");
        if (!burl.equals("")) {
            airPhoneNumber = burl;
        }
        burl = mySharedPreferences.getString("motorPhoneNumber", "");
        if (!burl.equals("")) {
            motorPhoneNumber = burl;
        }
        /****注册发送短信监听*/
            /* 自定义IntentFilter为SENT_SMS_ACTIOIN Receiver */
        IntentFilter mFilter01;
        mFilter01 = new IntentFilter(SMS_SEND_ACTIOIN);
        registerReceiver(mReceiver01, mFilter01);
    /* 自定义IntentFilter为DELIVERED_SMS_ACTION Receiver */
        IntentFilter mFilter02;
        mFilter02 = new IntentFilter(SMS_DELIVERED_ACTION);
        registerReceiver(mReceiver02, mFilter02);
        /***跳转界面和发送短信*/
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (v.getId()) {
                    case R.id.main_button_monitor:
                        Intent intent1 = new Intent();
                        intent1.setClass(MainActivity.this, MonitorActivity.class);
                        startActivity(intent1);
                        break;
                    case R.id.main_button_postf:
                        Intent intent2 = new Intent();
                        intent2.setClass(MainActivity.this, AirDoorPostActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.main_button_postd:
                        Intent intent3 = new Intent();
                        intent3.setClass(MainActivity.this, MotorPostActivity.class);
                        startActivity(intent3);
                        break;
                    case R.id.main_button_smsf:
                        sendsms("1#工位风门故障，请检修！",airPhoneNumber);
                        break;
                    case R.id.main_button_smsd:
                        sendsms("1#工位电机故障，请检修！",motorPhoneNumber);
                        break;
                    case R.id.main_button_setting:
                        Intent intent = new Intent();
                        intent.putExtra("airPhoneNumber",airPhoneNumber);
                        intent.putExtra("motorPhoneNumber",motorPhoneNumber);
                        intent.setClass(MainActivity.this, SmssettingActivity.class);
                        startActivityForResult(intent,200);
                        break;
                }
            }
        };
        settingButton.setOnClickListener(onClickListener);
        monitorButton.setOnClickListener(onClickListener);
        smsfButton.setOnClickListener(onClickListener);
        smsdButton.setOnClickListener(onClickListener);
        postfButton.setOnClickListener(onClickListener);
        postdButton.setOnClickListener(onClickListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            String airPhoneNumber1 = data.getStringExtra("airPhoneNumber");
            String motorPhoneNumber1 = data.getStringExtra("motorPhoneNumber");
            if (!airPhoneNumber1.equals("")){
                airPhoneNumber = airPhoneNumber1;
                mySharedPreferences.edit().putString("airPhoneNumber",airPhoneNumber).apply();
            }
            if (!motorPhoneNumber1.equals("")){
                motorPhoneNumber = motorPhoneNumber1;
                mySharedPreferences.edit().putString("motorPhoneNumber",motorPhoneNumber).apply();
            }
        }

    }

    private void sendsms(String context,String PhoneNumber) {
                /*发送*/
        if (PhoneNumber.equals("")) {
            Toast.makeText(MainActivity.this, "请设置电话号码", Toast.LENGTH_LONG).show();
        } else {

            SmsManager manager = SmsManager.getDefault();
            ArrayList<String> list = manager.divideMessage(context);  //因为一条短信有字数限制，因此要将长短信拆分
                              /* 建立自定义Action常数的Intent(给PendingIntent参数之用) */
            Intent itSend = new Intent(SMS_SEND_ACTIOIN);
            Intent itDeliver = new Intent(SMS_DELIVERED_ACTION);

          /* sentIntent参数为传送后接受的广播信息PendingIntent */
            PendingIntent mSendPI = PendingIntent.getBroadcast(getApplicationContext(), 0, itSend, 0);

          /* deliveryIntent参数为送达后接受的广播信息PendingIntent */
            PendingIntent mDeliverPI = PendingIntent.getBroadcast(getApplicationContext(), 0, itDeliver, 0);
            for (String text : list) {
          /* 发送SMS短信，注意倒数的两个PendingIntent参数 */
                manager.sendTextMessage(PhoneNumber, null, text, mSendPI, mDeliverPI);
                Toast.makeText(getApplicationContext(), "发送中", Toast.LENGTH_SHORT).show();
            }

        }


    }

    /**
     * *自定义mServiceReceiver重写BroadcastReceiver监听短信状态信息 *
     */
    public class mServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {


            if (intent.getAction().equals(SMS_SEND_ACTIOIN)) {

                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "发送短信成功", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(MainActivity.this, "发送短信失败", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(MainActivity.this, "发送短信失败,RADIO_OFF", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(MainActivity.this, "发送短信失败,NULL_PDU", Toast.LENGTH_SHORT).show();
                        break;
                }
            } else if (intent.getAction().equals(SMS_DELIVERED_ACTION)) {

                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "短信被接收", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(MainActivity.this, "短信未送达", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(MainActivity.this, "短信未送达,RADIO_OFF", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(MainActivity.this, "短信未送达,NULL_PDU", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver01);
        unregisterReceiver(mReceiver02);
    }
}
