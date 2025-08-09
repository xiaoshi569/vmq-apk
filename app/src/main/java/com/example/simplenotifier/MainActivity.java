package com.example.simplenotifier;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private EditText etHost;
    private EditText etKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etHost = findViewById(R.id.et_host);
        etKey = findViewById(R.id.et_key);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnPermission = findViewById(R.id.btn_permission);

        // Load saved config
        etHost.setText(Config.getHost(this));
        etKey.setText(Config.getKey(this));

        btnSave.setOnClickListener(v -> {
            String host = etHost.getText().toString();
            String key = etKey.getText().toString();

            if (TextUtils.isEmpty(host) || TextUtils.isEmpty(key)) {
                Toast.makeText(this, "地址和密钥不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            Config.setHost(this, host);
            Config.setKey(this, key);

            Toast.makeText(this, "保存成功!", Toast.LENGTH_SHORT).show();
            checkAndRestartService();
        });

        btnPermission.setOnClickListener(v -> {
            if (!isNotificationServiceEnabled()) {
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            } else {
                Toast.makeText(this, "通知权限已授予", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAndRestartService() {
        if (isNotificationServiceEnabled()) {
            // Restart the service to apply new config
            Intent intent = new Intent(this, NotificationService.class);
            stopService(intent);
            startService(intent);
            Toast.makeText(this, "服务已重启", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "请先授予通知权限", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNotificationServiceEnabled() {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(this);
        return packageNames.contains(getPackageName());
    }
}
