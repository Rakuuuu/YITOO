package com.yitu.pictureshare.fragments.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yitu.pictureshare.AboutActivity;
import com.yitu.pictureshare.FocusActivity;
import com.yitu.pictureshare.MyPictureActivity;
import com.yitu.pictureshare.R;
import com.yitu.pictureshare.LoginActivity;
import com.yitu.pictureshare.SettingActivity;
import com.yitu.pictureshare.VersionActivity;
import com.yitu.pictureshare.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    String username;
    String userId;
    private FragmentProfileBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        //读取数据
        Context ctx = getActivity();
        SharedPreferences sp = ctx.getSharedPreferences("SP", Context.MODE_PRIVATE);
        username = sp.getString("username",null);
        TextView user_title = root.findViewById(R.id.biaoti);
        TextView textView_userId = root.findViewById(R.id.textView_id);
        userId = "ID:"+sp.getString("id",null);
        user_title.setText(username);
        textView_userId.setText(userId);

        LinearLayout to_focus = root.findViewById(R.id.guanzhu);
        to_focus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FocusActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });

        LinearLayout to_my = root.findViewById(R.id.fabu);
        to_my.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MyPictureActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });

        Button exit_login_button = root.findViewById(R.id.btn_exit_login);
        exit_login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        LinearLayout to_version_button = root.findViewById(R.id.version);
        to_version_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), VersionActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        LinearLayout to_setting_button = root.findViewById(R.id.app_setting);
        to_setting_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SettingActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("from",4);
                startActivityForResult(intent,444);//444代表从个人信息界面进入设置携带的requestCode
            }
        });

        LinearLayout to_about_button = root.findViewById(R.id.contact);
        to_about_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AboutActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 4){
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }
}