package com.yitu.pictureshare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.yitu.pictureshare.common.AppAuthorization;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText accountEditText;
    private EditText passwordEditText;
    private CheckBox passwordSave;
    private Button button_login_to_setting;
    ImageView pwdVisibility;
    private Boolean pwdSwitch = false;
    private Boolean isSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        Context ctx = LoginActivity.this;
        SharedPreferences sp = ctx.getSharedPreferences("SP", MODE_PRIVATE);

        //存入数据，如果不存在appId和appSecret则默认填入以下值
        SharedPreferences.Editor editor = sp.edit();
        if(sp.getString("appId",null) == null || sp.getString("appSecret",null) == null) {
            editor.putString("appId", "79a8c1a3c4f44ce39fb9feb179debb74");
            editor.putString("appSecret", "731153635a9226b4a42ce8b80dc45e18ae770");
            editor.apply();
            System.out.println("已写入已写入已写入已写入已写入已写入已写入已写入已写入已写入已写入已写入已写入已写入已写入已写入已写入已写入");
            System.out.println(sp.getString("appId",null));
            System.out.println(sp.getString("appSecret",null));
        }else {
            System.out.println(sp.getString("appId",null));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        accountEditText = findViewById(R.id.edittext_account);
        passwordEditText = findViewById(R.id.edittext_password);
        passwordSave = findViewById(R.id.save_password);
        button_login_to_setting = findViewById(R.id.button_login_to_setting);

        if(sp.getString("username", null) != null && sp.getString("password",null)!=null){
            accountEditText.setText(sp.getString("username",null));
            passwordEditText.setText(sp.getString("password",null));
            passwordSave.setChecked(true);
        }else if(sp.getString("username", null) != null && sp.getString("password",null)==null){
            accountEditText.setText(sp.getString("username",sp.getString("username",null)));
            passwordEditText.setText(sp.getString("password",null));
            passwordSave.setChecked(false);
        }else{
            accountEditText.setText(sp.getString("username",null));
            passwordEditText.setText(sp.getString("password",null));
            passwordSave.setChecked(false);
        }

        pwdVisibility = findViewById(R.id.password_visible);
        Button loginButton = findViewById(R.id.button_login);
        Button registerButton = findViewById(R.id.btn_register);
        loginButton.setOnClickListener(this);
        pwdVisibility.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        passwordSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!passwordSave.isChecked()){
                    System.out.println("_____________________unchecked______________________");
                    editor.remove("password");
                    editor.apply();
                }
            }
        });

        button_login_to_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SettingActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });
    }

    public boolean verifyLogin(String username, String password) {//登录

        String url = "http://47.107.52.7:88/member/photo/user/login";
        OkHttpClient client = new OkHttpClient();

        Context ctx = LoginActivity.this;
        SharedPreferences sp = ctx.getSharedPreferences("SP", Context.MODE_PRIVATE);
        String appId = AppAuthorization.getAppId(sp);
        String appSecret = AppAuthorization.getAppSecret(sp);

        //构建表单参数
        //添加请求体
        RequestBody body = new MultipartBody.Builder()
                .addFormDataPart("username",username)
                .addFormDataPart("password",password)
                .build();
//        formBody=RequestBody.create(IndexActivity.JSON,data);
        Request request=new Request.Builder()
                .addHeader("appId",appId)
                .addHeader("appSecret",appSecret)
                .url(url)
                .post(body)
                .build();
        System.out.println("——————————请求信息——————————\n"+request);
        //异步请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("MainActivityPost---", "连接失败" + e.getLocalizedMessage());
                Looper.prepare();
                Toast.makeText(LoginActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Map result = JSON.parseObject(response.body().string());
                System.out.println("————————————响应信息————————————\n"+result.toString());
                if(result.get("msg") != null){
                    if (result.get("msg").equals("登录成功")) {
                        Log.d("登录信息---", (String)result.get("msg"));

                        Context ctx = LoginActivity.this;
                        SharedPreferences sp = ctx.getSharedPreferences("SP", MODE_PRIVATE);
                        //存入数据
                        SharedPreferences.Editor editor = sp.edit();

                        if(passwordSave.isChecked()){
                            editor.putString("id", JSON.parseObject(result.get("data").toString()).get("id").toString());
                            editor.putString("username", username);
                            editor.putBoolean("isLogin",true);
                            editor.putString("password",password);
                            editor.apply();
                        }else{
                            editor.putString("id", JSON.parseObject(result.get("data").toString()).get("id").toString());
                            editor.putString("username", username);
                            editor.apply();
                        }

                        Intent intent = new Intent(LoginActivity.this, IndexActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("userId", JSON.parseObject(result.get("data").toString()).get("id").toString());
                        startActivity(intent);
                        finish();
                    } else
                        Log.d("登录信息---", (String)result.get("msg"));
                }else if(result.get("msg") == null){
                    Looper.prepare();
                    Toast.makeText(LoginActivity.this, result.get("errorMsg").toString(), Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                response.body().close();
                Looper.prepare();
                Toast.makeText(LoginActivity.this, result.get("msg").toString(), Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.button_login == id) {
            String account = accountEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            Log.d("Login", account);
            Log.d("Login", password);
            if (account.length() != 0 && password.length() != 0) {
                verifyLogin(account, password);
            } else {
                Toast.makeText(LoginActivity.this, "请输入账号和密码", Toast.LENGTH_SHORT).show();
            }

        } else if (R.id.btn_register == id) {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        } else if (R.id.password_visible == id) {
            pwdSwitch = !pwdSwitch;
            if (pwdSwitch) {
                pwdVisibility.setImageResource(
                        R.drawable.ic_outline_visibility_24);
                passwordEditText.setInputType(
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                pwdVisibility.setImageResource(
                        R.drawable.ic_outline_visibility_off_24);
                passwordEditText.setInputType(
                        InputType.TYPE_TEXT_VARIATION_PASSWORD |
                                InputType.TYPE_CLASS_TEXT);
                passwordEditText.setTypeface(Typeface.DEFAULT);
            }

        }
    }
}