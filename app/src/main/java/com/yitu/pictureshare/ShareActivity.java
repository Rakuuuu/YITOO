package com.yitu.pictureshare;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yitu.pictureshare.common.AppAuthorization;
import com.yitu.pictureshare.common.DcimUriget;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShareActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imageViewUploadPicture;
    private EditText editTextUploadTitle;
    private EditText editTextUploadInfo;
    private Bitmap picture;
    private static String filePath;
    private long imageCode;
    private long userId;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        editTextUploadTitle = findViewById(R.id.editTextUploadTitle);
        editTextUploadInfo = findViewById(R.id.editTextUploadInfo);
        Button buttonSelectPicture = findViewById(R.id.button_select_picture);
        imageViewUploadPicture = findViewById(R.id.imageViewUploadPicture);
        Button buttonUpload = findViewById(R.id.buttonUpload);
        buttonSelectPicture.setOnClickListener(this);
        imageViewUploadPicture.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
        progressBar = findViewById(R.id.progressBar_share);
        Context ctx = ShareActivity.this;
        SharedPreferences sp = ctx.getSharedPreferences("SP", MODE_PRIVATE);
        //存入数据
        SharedPreferences.Editor editor = sp.edit();

        userId = Long.parseLong(sp.getString("id",null));
        System.out.println("_____________________________userId"+userId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.button_select_picture == id || R.id.imageViewUploadPicture == id) {
            gallery();
        } else if (R.id.buttonUpload == id) {
            if(picture != null){
                uploadPicture();
            }else{
                Toast toast = Toast.makeText(ShareActivity.this, "未选择图片", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    private void gallery() {
        /// 激活系统图库，选择一张图片
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            try {
                picture = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                imageViewUploadPicture.setImageBitmap(picture);
                filePath = DcimUriget.getFilePathByUri(ShareActivity.this, uri);//调用根据uri获得图片路径的方法
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void uploadPicture(){
        progressBar.setVisibility(View.VISIBLE);
        ActivityCompat.requestPermissions(ShareActivity.this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        String url_upload = "http://47.107.52.7:88/member/photo/image/upload";
//        String url = "http://35.241.95.124:8081/user/login";
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType=MediaType.Companion.parse("image/*; charset=utf-8");
        SharedPreferences sp = ShareActivity.this.getSharedPreferences("SP", Context.MODE_PRIVATE);
        String appId = AppAuthorization.getAppId(sp);
        String appSecret = AppAuthorization.getAppSecret(sp);

        File fileer = new File(filePath);

        //构建表单参数
        //添加请求体
        RequestBody fileBody=RequestBody.Companion.create(fileer,mediaType);

        //第二层，指明服务表单的键名，文件名，文件体
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("fileList", fileer.getName(),fileBody)
                .build();

        Request request = new Request.Builder()
                .addHeader("appId",appId)
                .addHeader("appSecret",appSecret)
                .url(url_upload)
                .post(requestBody)
                .build();

        System.out.println("——————————请求信息——————————\n"+request);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("upload", "上传图片失败" + e.getLocalizedMessage());
                Looper.prepare();
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(ShareActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Map result = JSON.parseObject(response.body().string());
                System.out.println("————————响应数据———————"+result);
                if(result.get("msg").equals("成功")){
                    Log.d("upload", result.toString());
                    imageCode = Long.parseLong(JSON.parseObject(result.get("data").toString()).get("imageCode").toString());
                    publishPicture(imageCode, userId,editTextUploadTitle.getText().toString(), editTextUploadInfo.getText().toString());
                }else
                    Log.d("upload", result.toString());
                response.body().close();
            }
        });
    }

    public void publishPicture(long imageCode, long userId,String title, String content){
        String url = "http://47.107.52.7:88/member/photo/share/add";
//        String url = "http://35.241.95.124:8081/user/login";
        OkHttpClient client = new OkHttpClient();
        SharedPreferences sp = ShareActivity.this.getSharedPreferences("SP", Context.MODE_PRIVATE);
        String appId = AppAuthorization.getAppId(sp);
        String appSecret = AppAuthorization.getAppSecret(sp);

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("pUserId",userId);
        jsonObject.put("title",title);
        jsonObject.put("imageCode",imageCode);
        jsonObject.put("content",content);
        //构建表单参数
        //添加请求体
        RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));

        Request request=new Request.Builder()
                .addHeader("appId",appId)
                .addHeader("appSecret",appSecret)
                .url(url)
                .post(body)
                .build();
        System.out.println("——————————请求信息——————————\n"+request);
        System.out.println("id:"+userId);
        System.out.println("imageCode:"+ imageCode);
        System.out.println("title:"+title);
        System.out.println("Content:"+content);

        //异步请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("register", "连接失败" + e.getLocalizedMessage());
                progressBar.setVisibility(View.INVISIBLE);
                Looper.prepare();
                Toast.makeText(ShareActivity.this, "分享失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Map result = JSON.parseObject(response.body().string());
                System.out.println("————————————响应信息————————————\n"+result.toString());
                progressBar.setVisibility(View.INVISIBLE);
                if (result.get("code").toString().equals("200")) {
                    Intent intent = new Intent(ShareActivity.this, IndexActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("toFresh", true);
                    startActivity(intent);
                    finish();
                    Looper.prepare();
                    Toast.makeText(ShareActivity.this, "分享成功", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else{
                    Looper.prepare();
                    Toast.makeText(ShareActivity.this, (String)result.get("msg"), Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

                response.body().close();
            }
        });
    }
}

