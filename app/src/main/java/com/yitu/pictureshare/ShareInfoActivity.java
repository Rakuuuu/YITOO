package com.yitu.pictureshare;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yitu.pictureshare.bean.CommentBean;
import com.yitu.pictureshare.bean.SaveImgTools;
import com.yitu.pictureshare.common.AppAuthorization;
import com.yitu.pictureshare.adapter.CommentAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShareInfoActivity extends AppCompatActivity {

    private static Handler handler;
    private ArrayList<CommentBean> comments;
    private String username;
    private String pUserId;
    private String userId;
    private String shareId;
    private String likeNum;
    private String pUserName;
    private String title;
    private String content;
    private String intentImageUrl;
    private Boolean hasLike;
    private Boolean hasFocus;
    private int commentSize;

    private TextView textView_title;
    private TextView textView_content;
    private TextView textView_author;
    private TextView textView_likeNum;
    private TextView textView_commentNum;
    private TextView textView_deleteComment;
    private EditText editText_comment;
    private ImageView imageView;
    private ImageView imageView_like;
    private ImageView imageView_save;
    private Button button_focus;
    private Button button_submitComment;
    private ListView listView;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_info);

        Button back_to_index = findViewById(R.id.share_info_button_to_index);

        textView_author = findViewById(R.id.share_user_name);
        textView_title = findViewById(R.id.share_info_title);
        textView_content = findViewById(R.id.share_info_content);
        textView_likeNum = findViewById(R.id.like_num);
        textView_commentNum = findViewById(R.id.comment_number);
        textView_deleteComment = findViewById(R.id.delete_share);
        editText_comment = findViewById(R.id.edit_text_comment);
        imageView = findViewById(R.id.share_info_image);
        imageView_like = findViewById(R.id.image_like);
        imageView_save = findViewById(R.id.image_save);
        listView = findViewById(R.id.list_comment);
        button_focus = findViewById(R.id.button_focus);
        button_submitComment = findViewById(R.id.button_submit_comment);

        Intent intent = getIntent();
        shareId = intent.getStringExtra("shareId");
        intentImageUrl = intent.getStringExtra("intentImageUrl");
        pUserName = intent.getStringExtra("pUserName");

        Context ctx = ShareInfoActivity.this;
        SharedPreferences sp = ctx.getSharedPreferences("SP", Context.MODE_PRIVATE);
        username = sp.getString("username",null);
        userId = sp.getString("id",null);



        handler = new Handler(Looper.getMainLooper());

        getShareInfo();
        getComments();

        textView_author.setText(pUserName);
        textView_title.setText(title);
        textView_content.setText(content);

        back_to_index.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Glide.with(ShareInfoActivity.this)
                .load(intentImageUrl) //加载地址
                .asBitmap()
                .placeholder(R.drawable.loading_img)//加载未完成时显示占位图
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView);//显示的位置

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent1 = new Intent(ShareInfoActivity.this, ShowPictureActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent1.putExtra("imageUrl", intentImageUrl);
                startActivity(intent1);
            }
        });

        imageView_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveImgTools.SaveImageToSysAlbum(ShareInfoActivity.this, imageView);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    public void getShareInfo(){

        String url = "http://47.107.52.7:88/member/photo/share/detail";
//        String url = "http://35.241.95.124:8081/user/login";
        OkHttpClient client = new OkHttpClient();

        SharedPreferences sp = ShareInfoActivity.this.getSharedPreferences("SP", Context.MODE_PRIVATE);
        String appId = AppAuthorization.getAppId(sp);
        String appSecret = AppAuthorization.getAppSecret(sp);

//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("userId",userId);
//        jsonObject.put("current",current);
//        jsonObject.put("size",10);

        //构建请求
//        RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));
        Request request=new Request.Builder()
                .addHeader("appId",appId)
                .addHeader("appSecret",appSecret)
                .url(url+"?userId="+userId+"&shareId="+shareId)
                .get()
                .build();

        //异步请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("register", "连接失败" + e.getLocalizedMessage());
                finish();
                Looper.prepare();
                Toast.makeText(ShareInfoActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                System.out.println(response.body().string());
                String result_string = response.body().string();
                Map result = JSON.parseObject(result_string);
                Map result_data = JSON.parseObject(result.get("data").toString());

                System.out.println("————————————响应信息————————————\n"+result_data.toString());
                System.out.println("————————————响应结束————————————");



                if (result.get("code").toString().equals("200")) {
                    title = result_data.get("title").toString();
                    content = result_data.get("content").toString();
                    pUserId = result_data.get("pUserId").toString();

                    if(result_data.get("likeNum") != null)
                        likeNum = result_data.get("likeNum").toString();
                    else
                        likeNum = "0";

                    System.out.println("+++++++++++++++++++++++++++++"+result_data.get("pUserId").toString());
                    System.out.println("+++++++++++++++++++++++++++++"+result_data.get("title").toString());
                    System.out.println("+++++++++++++++++++++++++++++"+result_data.get("content").toString());

                    handler.post(new Runnable() {
                        //handler用于post非ui线程向ui线程的操作
                        @Override
                        public void run() {
                            textView_author.setText(pUserName);
                            textView_title.setText(title);
                            textView_content.setText(content);
                            textView_likeNum.setText(likeNum);

                            hasLike = (Boolean) result_data.get("hasLike");
                            if(hasLike){
                                imageView_like.setBackground(getResources().getDrawable(R.drawable.yidianzan));
                            }

                            hasFocus = (Boolean) result_data.get("hasFocus");
                            if(hasFocus){
                                button_focus.setText("已关注");
                            }

                            imageView_like.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(!hasLike){
                                        toLike();
                                    }else {
                                        Toast.makeText(ShareInfoActivity.this, "点赞不能取消哦", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                            button_focus.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(!hasFocus){
                                        toFocus();
                                    } else {
                                        toCancelFocus();
                                    }
                                }
                            });
                        }
                    });

//                    Looper.prepare();
//                    Toast.makeText(ShareInfoActivity.this, "获取成功", Toast.LENGTH_SHORT).show();
//                    Looper.loop();

                }else{
                    finish();
                    Looper.prepare();
                    Toast.makeText(ShareInfoActivity.this, "分享详情获取失败", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

                response.body().close();
            }
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void toLike(){
        imageView_like.setBackground(getResources().getDrawable(R.drawable.yidianzan));
        likeNum = Integer.toString(Integer.parseInt(likeNum)+1);
        textView_likeNum.setText(likeNum);

        String url = "http://47.107.52.7:88/member/photo/like";
        OkHttpClient client = new OkHttpClient();

        SharedPreferences sp = ShareInfoActivity.this.getSharedPreferences("SP", Context.MODE_PRIVATE);
        String appId = AppAuthorization.getAppId(sp);
        String appSecret = AppAuthorization.getAppSecret(sp);

//        JSONObject jsonObject = new JSONObject();

//        jsonObject.put("shareId",Long.parseLong(shareId));
//        jsonObject.put("userId",Long.parseLong(userId));
//        //构建表单参数
//        //添加请求体
//        RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));

        RequestBody body = new MultipartBody.Builder()
                .addFormDataPart("shareId",shareId)
                .addFormDataPart("userId",userId)
                .build();

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
                finish();
                Looper.prepare();
                Toast.makeText(ShareInfoActivity.this, "无法连接服务器", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Map result = JSON.parseObject(response.body().string());
                System.out.println("————————————响应信息————————————\n"+result.toString());
                if (result.get("code").toString().equals("200")) {
                    hasLike = !hasLike;
                    imageView_like.setBackground(getResources().getDrawable(R.drawable.yidianzan));
                    Looper.prepare();
                    Toast.makeText(ShareInfoActivity.this, "点赞成功", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else{
                    finish();
                    Looper.prepare();
                    Toast.makeText(ShareInfoActivity.this, (String)result.get("msg"), Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

                response.body().close();
                getShareInfo();
            }
        });
    }

    public void getComments(){//先获取comment的数量，再一次获取所有comments

        String url = "http://47.107.52.7:88/member/photo/comment/first";
        OkHttpClient client = new OkHttpClient();

        SharedPreferences sp = ShareInfoActivity.this.getSharedPreferences("SP", Context.MODE_PRIVATE);
        String appId = AppAuthorization.getAppId(sp);
        String appSecret = AppAuthorization.getAppSecret(sp);

//        JSONObject jsonObject = new JSONObject();

//        jsonObject.put("shareId",Long.parseLong(shareId));
//        jsonObject.put("userId",Long.parseLong(userId));
//        //构建表单参数
//        //添加请求体
//        RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));

        Request request=new Request.Builder()
                .addHeader("appId",appId)
                .addHeader("appSecret",appSecret)
                .url(url+"?shareId="+shareId)
                .get()
                .build();

        System.out.println("——————————请求信息——————————\n"+request);
        //异步请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("register", "连接失败" + e.getLocalizedMessage());
                finish();
                Looper.prepare();
                Toast.makeText(ShareInfoActivity.this, "无法连接服务器", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String result_string = response.body().string();
                Map result = JSON.parseObject(result_string);
                System.out.println("————————————响应信息————————————\n"+result.toString());
                System.out.println("————————————响应信息————————————\n"+result.toString());
                if (result.get("code").toString().equals("200")) {
                    Map result_data = JSON.parseObject(result.get("data").toString());
                    List share_data_origin = (List) result_data.get("records");
                    System.out.println("______________comment_data_origin________________\n"+share_data_origin);
                    commentSize = Integer.parseInt(result_data.get("total").toString());

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView_commentNum.setText(Integer.toString(commentSize));
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    if(userId.equals(pUserId)){
                                        textView_deleteComment.setVisibility(View.VISIBLE);
                                    }else{
                                        System.out.println("----------------userId:"+userId);
                                        System.out.println("----------------pUserId"+pUserId);
                                    }

                                    button_submitComment.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if(editText_comment.getText()!=null || editText_comment.getText().toString()!="") {
                                                toSubmitComment();
                                            } else {
                                                Toast.makeText(ShareInfoActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                    textView_deleteComment.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            System.out.println("------------------------------toDelete");
                                            toDeleteShare();
                                        }
                                    });
                                }
                            });
                        }
                    });

                    if(commentSize > 0)
                        getAllComments();

                }else{
                    finish();
                    Looper.prepare();
                    Toast.makeText(ShareInfoActivity.this, (String)result.get("msg"), Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

                response.body().close();
            }
        });
    }

    public void getAllComments(){

        String url = "http://47.107.52.7:88/member/photo/comment/first";
        OkHttpClient client = new OkHttpClient();

        SharedPreferences sp = ShareInfoActivity.this.getSharedPreferences("SP", Context.MODE_PRIVATE);
        String appId = AppAuthorization.getAppId(sp);
        String appSecret = AppAuthorization.getAppSecret(sp);

//        JSONObject jsonObject = new JSONObject();

//        jsonObject.put("shareId",Long.parseLong(shareId));
//        jsonObject.put("userId",Long.parseLong(userId));
//        //构建表单参数
//        //添加请求体
//        RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));

        Request request=new Request.Builder()
                .addHeader("appId",appId)
                .addHeader("appSecret",appSecret)
                .url(url+"?shareId="+shareId+"&size="+commentSize)
                .get()
                .build();

        System.out.println("——————————请求信息——————————\n"+request);
        //异步请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("register", "连接失败" + e.getLocalizedMessage());
                finish();
                Looper.prepare();
                Toast.makeText(ShareInfoActivity.this, "无法连接服务器", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String result_string = response.body().string();
                Map result = JSON.parseObject(result_string);
                System.out.println("————————————响应信息————————————\n"+result.toString());
                if (result.get("code").toString().equals("200")) {
                    Map result_data = JSON.parseObject(result.get("data").toString());
                    List comment_data_origin = (List) result_data.get("records");
                    System.out.println("______________comment_data_origin________________\n"+comment_data_origin);

                    comments = new ArrayList<>();//初始化shares(List<Map<String,String>)

                    for(int i = 0; i < comment_data_origin.size();i++){
                        Map item = JSON.parseObject(comment_data_origin.get(i).toString());
                        System.out.println("userName:"+item.get("userName").toString());
                        System.out.println("content:"+item.get("content").toString());
                        System.out.println("commentSize:"+commentSize);

                        CommentBean commentBean = new CommentBean();
                        commentBean.setUsername(item.get("userName").toString());
                        commentBean.setContent(item.get("content").toString());

                        comments.add(commentBean);
                    }
                    ///////////////////////////////////////////////////////首页数据解析end/////////////////////////////////////////////////////
                    handler.post(new Runnable() {
                        //handler用于post非ui线程向ui线程的操作
                        @Override
                        public void run() {

                            setSharesAdapter(comments, listView);
                            CommentAdapter.fixListViewHeight(listView);//修复scrollView中listView高度不正常的问题

                        }
                    });
                }else{
                    System.out.println("msg在这里");
                    finish();
                    Looper.prepare();
                    Toast.makeText(ShareInfoActivity.this, (String)result.get("msg"), Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

                response.body().close();
            }
        });
    }

    public void toFocus(){
        String url = "http://47.107.52.7:88/member/photo/focus";
        OkHttpClient client = new OkHttpClient();

        SharedPreferences sp = ShareInfoActivity.this.getSharedPreferences("SP", Context.MODE_PRIVATE);
        String appId = AppAuthorization.getAppId(sp);
        String appSecret = AppAuthorization.getAppSecret(sp);

//        JSONObject jsonObject = new JSONObject();
//
//        jsonObject.put("focusUserId",Long.parseLong(pUserId));
//        jsonObject.put("userId",Long.parseLong(userId));
        //构建表单参数
        //添加请求体
//        RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));

        RequestBody body = new MultipartBody.Builder()
                .addFormDataPart("focusUserId",pUserId)
                .addFormDataPart("userId",userId)
                .build();

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
                Log.d("register", "连接失败" + e.getLocalizedMessage());
                finish();
                Looper.prepare();
                Toast.makeText(ShareInfoActivity.this, "无法连接服务器", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Map result = JSON.parseObject(response.body().string());
                System.out.println("————————————响应信息————————————\n"+result.toString());
                if (result.get("code").toString().equals("200")) {
                    button_focus.setText("已关注");
                    hasFocus = !hasFocus;
                    Looper.prepare();
                    Toast.makeText(ShareInfoActivity.this, "关注成功", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else{
                    Looper.prepare();
                    Toast.makeText(ShareInfoActivity.this, (String)result.get("msg"), Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

                response.body().close();
                getShareInfo();
            }
        });
    }

    public void toCancelFocus(){
        button_focus.setText("关注");

        String url = "http://47.107.52.7:88/member/photo/focus/cancel";
        OkHttpClient client = new OkHttpClient();

        SharedPreferences sp = ShareInfoActivity.this.getSharedPreferences("SP", Context.MODE_PRIVATE);
        String appId = AppAuthorization.getAppId(sp);
        String appSecret = AppAuthorization.getAppSecret(sp);

//        JSONObject jsonObject = new JSONObject();
//
//        jsonObject.put("focusUserId",Long.parseLong(pUserId));
//        jsonObject.put("userId",Long.parseLong(userId));
        //构建表单参数
        //添加请求体
//        RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));

        RequestBody body = new MultipartBody.Builder()
                .addFormDataPart("focusUserId",pUserId)
                .addFormDataPart("userId",userId)
                .build();

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
                Log.d("register", "连接失败" + e.getLocalizedMessage());
                finish();
                Looper.prepare();
                Toast.makeText(ShareInfoActivity.this, "无法连接服务器", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Map result = JSON.parseObject(response.body().string());
                System.out.println("————————————响应信息————————————\n"+result.toString());
                if (result.get("code").toString().equals("200")) {
                    button_focus.setText("关注");
                    hasFocus = !hasFocus;

                    System.out.println("------------------------------------from----------------------------------------------"+getIntent().getIntExtra("from",0));

                    if(getIntent().getIntExtra("from",0) == 1) {
                        Intent data = new Intent();
                        setResult(1, data);//resultCode=1是指取消关注成功后返回Focus列表时，提交给Focus列表刷新的指令
                        finish();
                    }

                    Looper.prepare();
                    Toast.makeText(ShareInfoActivity.this, "取消关注成功", Toast.LENGTH_SHORT).show();
                    Looper.loop();

                }else{
                    finish();
                    Looper.prepare();
                    Toast.makeText(ShareInfoActivity.this, (String)result.get("msg"), Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

                response.body().close();
                getShareInfo();
            }
        });
    }

    public void toSubmitComment(){


        String url = "http://47.107.52.7:88/member/photo/comment/first";
        OkHttpClient client = new OkHttpClient();

        SharedPreferences sp = ShareInfoActivity.this.getSharedPreferences("SP", Context.MODE_PRIVATE);
        String appId = AppAuthorization.getAppId(sp);
        String appSecret = AppAuthorization.getAppSecret(sp);

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("shareId",Long.parseLong(shareId));
        jsonObject.put("userId",Long.parseLong(userId));
        jsonObject.put("userName",username);
        jsonObject.put("content",editText_comment.getText());
//        构建表单参数
//        添加请求体
        RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));

//        RequestBody body = new MultipartBody.Builder()
//                .addFormDataPart("shareId",shareId)
//                .addFormDataPart("userId",userId)
//                .addFormDataPart("userName",username)
//                .addFormDataPart("content",editText_comment.getText().toString())
//                .build();

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
                Log.d("register", "连接失败" + e.getLocalizedMessage());
                finish();
                Looper.prepare();
                Toast.makeText(ShareInfoActivity.this, "无法连接服务器", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Map result = JSON.parseObject(response.body().string());
                System.out.println("————————————响应信息————————————\n"+result.toString());
                if (result.get("code").toString().equals("200")) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            editText_comment.setText(null);
                            getComments();
                        }
                    });
                    Looper.prepare();
                    Toast.makeText(ShareInfoActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else{
                    finish();
                    Looper.prepare();
                    Toast.makeText(ShareInfoActivity.this, (String)result.get("msg"), Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

                response.body().close();
                getShareInfo();
            }
        });
    }

    public void toDeleteShare(){

        String url = "http://47.107.52.7:88/member/photo/share/delete";
        OkHttpClient client = new OkHttpClient();

        SharedPreferences sp = ShareInfoActivity.this.getSharedPreferences("SP", Context.MODE_PRIVATE);
        String appId = AppAuthorization.getAppId(sp);
        String appSecret = AppAuthorization.getAppSecret(sp);

//        JSONObject jsonObject = new JSONObject();
//
//        jsonObject.put("shareId",Long.parseLong(shareId));
//        jsonObject.put("userId",Long.parseLong(userId));
//        构建表单参数
//        添加请求体
//        RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));

        RequestBody body = new MultipartBody.Builder()
                .addFormDataPart("shareId",shareId)
                .addFormDataPart("userId",userId)
                .build();

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
                Log.d("register", "连接失败" + e.getLocalizedMessage());
                finish();
                Looper.prepare();
                Toast.makeText(ShareInfoActivity.this, "无法连接服务器", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Map result = JSON.parseObject(response.body().string());
                System.out.println("————————————响应信息————————————\n"+result.toString());
                if (result.get("code").toString().equals("200")) {
                    if(getIntent().getIntExtra("from",0) == 2 || getIntent().getIntExtra("from",0) == 5) {
                        Intent data = new Intent();
                        setResult(2, data);//resultCode=2是指删除分享成功后，返回myShares/Shares列表时，提交给mySharesList/shareList列表刷新的指令
                    }

                    finish();

                    Looper.prepare();
                    Toast.makeText(ShareInfoActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else{
                    finish();
                    Looper.prepare();
                    Toast.makeText(ShareInfoActivity.this, (String)result.get("msg"), Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                response.body().close();
            }
        });
    }



    void setSharesAdapter(List<CommentBean> comments, ListView listView){
        CommentAdapter commentAdapter = new CommentAdapter(ShareInfoActivity.this,comments);
        listView.setAdapter(commentAdapter);
        CommentAdapter.fixListViewHeight(listView);//修复scrollView中listView高度不正常的问题
    }
}