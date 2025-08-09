package com.example.simplenotifier;

import android.content.Context;
import android.content.SharedPreferences;

public class Config {
    private static final String PREFS_NAME = "SimpleNotifierPrefs";
    private static final String KEY_HOST = "host";
    private static final String KEY_KEY = "key";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static String getHost(Context context) {
        return getPrefs(context).getString(KEY_HOST, "");
    }

    public static void setHost(Context context, String host) {
        getPrefs(context).edit().putString(KEY_HOST, host).apply();
    }

    public static String getKey(Context context) {
        return getPrefs(context).getString(KEY_KEY, "");
    }

    public static void setKey(Context context, String key) {
        getPrefs(context).edit().putString(KEY_KEY, key).apply();
    }
}
