package com.ipcamera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int PORT = 8080;
    
    private Button startStopButton;
    private TextView ipAddressText;
    private TextView statusText;
    private boolean isStreaming = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startStopButton = findViewById(R.id.startStopButton);
        ipAddressText = findViewById(R.id.ipAddressText);
        statusText = findViewById(R.id.statusText);

        startStopButton.setOnClickListener(v -> {
            if (!isStreaming) {
                if (checkPermissions()) {
                    startStreaming();
                } else {
                    requestPermissions();
                }
            } else {
                stopStreaming();
            }
        });

        updateIPAddress();
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStreaming();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startStreaming() {
        Intent serviceIntent = new Intent(this, StreamingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        isStreaming = true;
        startStopButton.setText("Stop Streaming");
        statusText.setText("Streaming Active");
        updateIPAddress();
        Toast.makeText(this, "Streaming started on port " + PORT, Toast.LENGTH_SHORT).show();
    }

    private void stopStreaming() {
        Intent serviceIntent = new Intent(this, StreamingService.class);
        stopService(serviceIntent);
        
        isStreaming = false;
        startStopButton.setText("Start Streaming");
        statusText.setText("Streaming Stopped");
        ipAddressText.setText("Waiting...");
        Toast.makeText(this, "Streaming stopped", Toast.LENGTH_SHORT).show();
    }

    private void updateIPAddress() {
        String ipAddress = getLocalIpAddress();
        if (ipAddress != null && isStreaming) {
            String url = "http://" + ipAddress + ":" + PORT + "/video";
            ipAddressText.setText("VLC URL:\n" + url);
        } else if (ipAddress != null) {
            ipAddressText.setText("Local IP: " + ipAddress + "\nPress Start to begin streaming");
        } else {
            ipAddressText.setText("Not connected to WiFi");
        }
    }

    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipInt = wifiInfo.getIpAddress();
            
            try {
                return InetAddress.getByAddress(new byte[]{
                    (byte) (ipInt & 0xff),
                    (byte) (ipInt >> 8 & 0xff),
                    (byte) (ipInt >> 16 & 0xff),
                    (byte) (ipInt >> 24 & 0xff)
                }).getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isStreaming) {
            stopStreaming();
        }
    }
}
