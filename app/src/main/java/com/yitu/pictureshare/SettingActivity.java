package com.yitu.pictureshare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity {

    private EditText editText_app_id;
    private EditText editText_app_secret;
    private Button button_setting_back;
    private Button button_apply_setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        editText_app_id = findViewById(R.id.edittext_app_id);
        editText_app_secret = findViewById(R.id.edittext_app_secret);

        button_setting_back = findViewById(R.id.button_setting_back);
        button_apply_setting = findViewById(R.id.button_apply_setting);

        button_setting_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        SharedPreferences sp = SettingActivity.this.getSharedPreferences("SP", Context.MODE_PRIVATE);
        editText_app_id.setText(sp.getString("appId",null));
        editText_app_secret.setText(sp.getString("appSecret",null));
        System.out.println(sp.getString("appId",null));
        System.out.println(sp.getString("appSecret",null));

        button_apply_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editText_app_id.getText().toString().isEmpty() || editText_app_secret.getText().toString().isEmpty()){
                    Toast.makeText(SettingActivity.this, "请输入完整内容再保存设置", Toast.LENGTH_SHORT).show();
                }else{
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("appId", editText_app_id.getText().toString());
                    editor.putString("appSecret", editText_app_secret.getText().toString());
                    editor.apply();

                    if(getIntent().getIntExtra("from",0) == 4){
                        setResult(4);
                    }
                    finish();
                }
            }
        });
    }
}