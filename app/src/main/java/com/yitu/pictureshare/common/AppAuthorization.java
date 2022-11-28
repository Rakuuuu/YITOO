package com.yitu.pictureshare.common;

import android.content.Context;
import android.content.SharedPreferences;

public class AppAuthorization {

    public static String getAppId(SharedPreferences sp) {
        return sp.getString("appId",null);
    }

    public static String getAppSecret(SharedPreferences sp) {
        return sp.getString("appSecret",null);
    }
}
