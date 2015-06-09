package com.laboratory600.peng.http;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 *
 * Created by Peng on 2015/5/17.
 */
public class SmssettingActivity extends Activity {
    EditText airEditText;
    EditText motorEditText;
    String airPhoneNumber = "";
    String motorPhoneNumber = "";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smssetting);
        Button finishButton = (Button) findViewById(R.id.smssetting_button_finish);
        Button airButton = (Button) findViewById(R.id.smssetting_button_airPhone);
        Button motorButton = (Button) findViewById(R.id.smssetting_button_motorPhone);
        airEditText = (EditText) findViewById(R.id.smssetting_ediText_airPhone);
        motorEditText = (EditText) findViewById(R.id.smssetting_ediText_motorPhone);
        Intent intent = getIntent();
        airEditText.setText(intent.getStringExtra("airPhoneNumber"));
        motorEditText.setText(intent.getStringExtra("motorPhoneNumber"));
        View.OnClickListener OnClickListener = new View.OnClickListener() {
            Intent intent = new Intent();
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.smssetting_button_airPhone:
                        airPhoneNumber = String.valueOf(airEditText.getText());
                        break;
                    case R.id.smssetting_button_motorPhone:
                        motorPhoneNumber = String.valueOf(motorEditText.getText());
                        break;
                    case R.id.smssetting_button_finish:
                        intent.putExtra("airPhoneNumber", airPhoneNumber);
                        intent.putExtra("motorPhoneNumber", motorPhoneNumber);
                        setResult(1, intent);
                        finish();
                        break;
                }
            }
        };
        finishButton.setOnClickListener(OnClickListener);
        airButton.setOnClickListener(OnClickListener);
        motorButton.setOnClickListener(OnClickListener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {


        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Intent intent=getIntent();
            intent.putExtra("airPhoneNumber", airPhoneNumber);
            intent.putExtra("motorPhoneNumber", motorPhoneNumber);
            setResult(1, intent);
            finish();

        }
        return super.onKeyDown(keyCode, event);
    }
}
