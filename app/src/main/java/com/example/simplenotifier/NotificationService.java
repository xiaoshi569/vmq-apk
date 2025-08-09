package com.example.simplenotifier;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NotificationService extends NotificationListenerService {

    private static final String TAG = "NotificationService";
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        String packageName = sbn.getPackageName();
        Notification notification = sbn.getNotification();
        if (notification == null) {
            return;
        }

        Bundle extras = notification.extras;
        if (extras == null) {
            return;
        }

        String title = extras.getString(NotificationCompat.EXTRA_TITLE, "");
        String content = extras.getString(NotificationCompat.EXTRA_TEXT, "");

        Log.d(TAG, "Package: " + packageName + ", Title: " + title + ", Content: " + content);

        if ("com.tencent.mm".equals(packageName)) { // WeChat
            if (title.equals("微信支付") || title.equals("微信收款助手")) {
                String money = getMoney(content);
                if (money != null) {
                    Log.d(TAG, "WeChat Pay detected, amount: " + money);
                    sendNotification(1, Double.parseDouble(money));
                }
            }
        } else if ("com.eg.android.AlipayGphone".equals(packageName)) { // Alipay
            if (title.contains("收钱到账") || content.contains("成功收款")) {
                String money = getMoney(content);
                if (money != null) {
                    Log.d(TAG, "Alipay detected, amount: " + money);
                    sendNotification(2, Double.parseDouble(money));
                }
            }
        }
    }

    private String getMoney(String content) {
        Pattern pattern = Pattern.compile("([0-9]+\\.[0-9]{2})");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private void sendNotification(int type, double price) {
        String host = Config.getHost(this);
        String key = Config.getKey(this);

        if (host.isEmpty() || key.isEmpty()) {
            Log.e(TAG, "Host or Key is not configured.");
            return;
        }

        String time = String.valueOf(new Date().getTime());
        String sign = md5(type + "" + price + time + key);
        String url = "http://" + host + "/appPush?t=" + time + "&type=" + type + "&price=" + price + "&sign=" + sign;

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Request successful: " + response.body().string());
                } else {
                    Log.e(TAG, "Request unsuccessful: " + response.code());
                }
            }
        });
    }

    private String md5(String string) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
