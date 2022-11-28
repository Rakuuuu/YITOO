package com.yitu.pictureshare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.yitu.pictureshare.R;
import com.yitu.pictureshare.bean.CommentBean;

import java.util.List;

public class CommentAdapter extends BaseAdapter {
    private LinearLayout linearLayout;
    private Context mContext;
    private List<CommentBean> mList;

    public CommentAdapter(Context mContext, List<CommentBean> mList) {
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
        CommentViewHolder commentViewHolder;
        if (view == null) {//判断view是否可以重载
            commentViewHolder = new CommentViewHolder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            linearLayout = (LinearLayout) inflater.inflate(R.layout.comment_item, null);
            //获取id
            commentViewHolder.textUserName = linearLayout.findViewById(R.id.comment_username);
            commentViewHolder.textContent = linearLayout.findViewById(R.id.comment_content);

            System.out.println(commentViewHolder.textContent.getText());

            System.out.println(commentViewHolder.textUserName.getText());

            System.out.println(mList.get(i).getUsername());
            System.out.println(mList.get(i).getContent());
            //设置数据
            commentViewHolder.textUserName.setText(mList.get(i).getUsername());
            commentViewHolder.textContent.setText(mList.get(i).getContent());


            //标记当前view
            linearLayout.setTag(commentViewHolder);
        } else {//可以重载则直接使用原来的view
            linearLayout = (LinearLayout) view;
            commentViewHolder = (CommentViewHolder) linearLayout.getTag();

            //获取id
            commentViewHolder.textUserName = linearLayout.findViewById(R.id.comment_username);
            commentViewHolder.textContent = linearLayout.findViewById(R.id.comment_content);



            //设置数据
            commentViewHolder.textUserName.setText(mList.get(i).getUsername());
            commentViewHolder.textContent.setText(mList.get(i).getContent());
        }

        return linearLayout;
    }

    public static void fixListViewHeight(ListView listView) {
        // 如果没有设置数据适配器，则ListView没有子项，返回。
        ListAdapter listAdapter = listView.getAdapter();
        int totalHeight = 0;
        if (listAdapter == null) {
            return;
        }
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            View listViewItem = listAdapter.getView(i , null, listView);
            // 计算子项View 的宽高
            listViewItem.measure(0, 0);
            // 计算所有子项的高度和
            totalHeight += listViewItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        // listView.getDividerHeight()获取子项间分隔符的高度
        // params.height设置ListView完全显示需要的高度
        params.height = totalHeight+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }


    //使用viewHolder缓存数据
    static class CommentViewHolder {
        TextView textUserName;
        TextView textContent;
    }
}

