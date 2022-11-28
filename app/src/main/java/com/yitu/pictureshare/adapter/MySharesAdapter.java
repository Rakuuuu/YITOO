package com.yitu.pictureshare.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yitu.pictureshare.R;
import com.yitu.pictureshare.bean.ShareBean;
import com.yitu.pictureshare.common.AppAuthorization;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MySharesAdapter extends BaseAdapter {
    private CardView cardview;
    private Context mContext;
    private List<ShareBean> mList;

    public MySharesAdapter(Context mContext, List<ShareBean> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ShareViewHolder shareViewHolder;

        shareViewHolder = new ShareViewHolder();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        cardview = (CardView) inflater.inflate(R.layout.card_view_mypicture, null);
        //获取id
        shareViewHolder.textTitle = cardview.findViewById(R.id.textview_mypicture_title);
        shareViewHolder.imageView = cardview.findViewById(R.id.imageview_mypicture);

        //设置数据
        shareViewHolder.textTitle.setText(mList.get(i).getTitle());
        Glide.with(mContext)//使用glide加载图片
                .load(mList.get(i).getImageUrl()) //加载地址
                .placeholder(R.drawable.customer_selected)//加载未完成时显示占位图
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(shareViewHolder.imageView);//显示的位置
        //标记当前view
        cardview.setTag(shareViewHolder);

        return cardview;
    }

    //使用viewHolder缓存数据
    static class ShareViewHolder {
        TextView textTitle;
        ImageView imageView;

    }
}

