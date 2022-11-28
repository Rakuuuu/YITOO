package com.yitu.pictureshare.fragments.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSON;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yitu.pictureshare.R;
import com.yitu.pictureshare.databinding.FragmentHomeBinding;
import com.yitu.pictureshare.common.AppAuthorization;
import com.yitu.pictureshare.ShareActivity;
import com.yitu.pictureshare.ShareInfoActivity;
import com.yitu.pictureshare.bean.ShareBean;
import com.yitu.pictureshare.adapter.SharesAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private long userId;
    private int current;
    private List<ShareBean> shares;
    private SharesAdapter sharesAdapter;
    private ListView listView;
    private static Handler handler;
    private int resultCode;


    /*请注意，当适配器中的List<ShareBean> shares产生变动时，必须令该适配器产生数据变化提醒*/
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//
//        homeViewModel =
//                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        listView = root.findViewById(R.id.home_list);

        handler = new Handler(Looper.getMainLooper());

        shares = new ArrayList<>();//初始化shares(List<Map<String,String>)

        Context ctx = getActivity();
        SharedPreferences sp = ctx.getSharedPreferences("SP", Context.MODE_PRIVATE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ShareBean shareBean = (ShareBean) adapterView.getItemAtPosition(i);//获取该条目的ShareBean
                Intent intent = new Intent(getActivity(), ShareInfoActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("shareId", shareBean.getShareId());
                intent.putExtra("intentImageUrl", shareBean.getImageUrl());
                intent.putExtra("pUserName",shareBean.getUsername());
                intent.putExtra("likeNum",shareBean.getLikeNum());
                intent.putExtra("hasLike",shareBean.getHasLike());
                intent.putExtra("hasCollect",shareBean.getHasCollect());
                intent.putExtra("hasFocus",shareBean.getHasFocus());
                intent.putExtra("from",2);//2代表从HomeFragment中传入shareInfo的信息
                System.out.println("以userId="+sp.getString("id",null)+"进入title为"+shareBean.getTitle()+"的图文分享详情");
                startActivityForResult(intent, 222);//222指从HomeFragment发出的Intent
            }
        });


        FloatingActionButton button_to_share = root.findViewById(R.id.share_button);

        button_to_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ShareActivity.class);
                startActivity(intent);
            }
        });

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(resultCode == 2 || resultCode == 3 || sharesAdapter == null) {
            //如果是来自share成功的返回，或者是删除分享成功，或者是新加载主页，就将current重置
            current = 1;    //初始化current
            getPicture();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.resultCode = resultCode;//传参给全局变量this.resultCode，用来给onResume进行判断是否进行刷新
    }

    public void getPicture(){

        String url = "http://47.107.52.7:88/member/photo/share";
//        String url = "http://35.241.95.124:8081/user/login";
        OkHttpClient client = new OkHttpClient();
        //获取已登录的userId
        Context ctx = getActivity();
        SharedPreferences sp = ctx.getSharedPreferences("SP", Context.MODE_PRIVATE);
        userId = Long.parseLong(sp.getString("id",null));

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
                .url(url+"?userId="+userId+"&current="+current+"&size=10")
                .get()
                .build();

        //异步请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("register", "连接失败" + e.getLocalizedMessage());
                Looper.prepare();
                Toast.makeText(getActivity(), "连接失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                System.out.println(response.body().string());
                String result_string = response.body().string();
                Map result = JSON.parseObject(result_string);
                System.out.println("————————————响应信息————————————\n"+result.toString());



                if (result.get("code").toString().equals("200")) {

                    ///////////////////////////////////////////////////////首页数据解析/////////////////////////////////////////////////////
                    Map result_data = JSON.parseObject(result.get("data").toString());
                    List share_data_origin = (List) result_data.get("records");
                    System.out.println("______________share_data_origin________________\n"+share_data_origin);

                    ArrayList<ShareBean> shares_temp = new ArrayList<>();


                    for(int i = 0; i < share_data_origin.size();i++) {
                        Map item = JSON.parseObject(share_data_origin.get(i).toString());
//                        Share item_share = new Share();
//                        item_share.setId(item.get("id").toString());
//                        item_share.setImageUrlList((List<String>)item.get("imageUrlList"));
//                        item_share.setpUserId(item.get("pUserId").toString());
//                        item_share.setUsername(item.get("username").toString());
                        ShareBean shareBean = new ShareBean();
                        shareBean.setShareId(item.get("id").toString());
                        List<String> temp_list = (List<String>) item.get("imageUrlList");
                        if (temp_list.size() != 0) {
                            shareBean.setImageUrl(temp_list.get(0));
                        } else {
                            shareBean.setImageUrl("");
                        }

                        shareBean.setTitle(item.get("title").toString());
                        shareBean.setUserId(item.get("pUserId").toString());
                        shareBean.setUsername(item.get("username").toString());
                        shareBean.setLikeNum(item.get("likeNum").toString());
                        shareBean.setHasLike((Boolean) item.get("hasLike"));
                        shareBean.setHasFocus((Boolean) item.get("hasFocus"));

                        shares_temp.add(shareBean);
                    }

                    ///////////////////////////////////////////////////////首页数据解析end/////////////////////////////////////////////////////
                    handler.post(new Runnable() {
                        //handler用于post非ui线程向ui线程的操作
                        @Override
                        public void run() {
                            System.out.println(("--------------------------current="+current+"------------------------"));
                            System.out.println(("--------------------------current="+current+"-----------------------"));
                            System.out.println(("--------------------------current="+current+"----------------------"));
                            System.out.println(("--------------------------current="+current+"---------------------"));
                            System.out.println(("--------------------------current="+current+"--------------------"));
                            System.out.println(("--------------------------current="+current+"-------------------"));
                            System.out.println(("--------------------------current="+current+"------------------"));
                            System.out.println(("--------------------------current="+current+"-----------------"));
                            System.out.println(("--------------------------current="+current+"----------------"));
                            System.out.println(("--------------------------current="+current+"---------------"));
                            System.out.println(("--------------------------current="+current+"--------------"));
                            System.out.println(("--------------------------current="+current+"-------------"));
                            if(current == 1) {
                                shares.clear();
                                shares.addAll(shares_temp);

                                if(sharesAdapter != null)
                                    sharesAdapter.notifyDataSetChanged();

                                sharesAdapter = new SharesAdapter(getActivity(),shares);
                                listView.setAdapter(sharesAdapter);
                            } else {
                                ArrayList<ShareBean> shares_temp_old = new ArrayList<>(shares);
                                shares.clear();
                                shares_temp_old.addAll(shares_temp);
                                shares.addAll(shares_temp_old);
                                sharesAdapter.notifyDataSetChanged();
                            }

                            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(AbsListView view, int scrollState) {
                                    switch (scrollState) {
                                        // 当不滚动时
                                        case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                                            // 判断滚动到底部
                                            if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                                                current++;
                                                getPicture();
                                            }else if (view.getFirstVisiblePosition() == 0) {
                                                Toast.makeText(getActivity(), "已经到顶部了，可点击主页图标刷新", Toast.LENGTH_SHORT).show();
                                            }
                                            break;
                                    }
                                }

                                @Override
                                public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                                }
                            });

                        }
                    });
                }else{
                    Looper.prepare();
                    Toast.makeText(getActivity(), (String)result.get("msg"), Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                response.body().close();
            }
        });
    }

}