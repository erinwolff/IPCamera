package com.ipcamera;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class StreamingService extends Service {
    private static final String TAG = "StreamingService";
    private static final String CHANNEL_ID = "IPCameraChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final int PORT = 8080;
    
    private MJPEGServer mjpegServer;
    private CameraHandler cameraHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        
        try {
            cameraHandler = new CameraHandler();
            cameraHandler.openCamera();
            
            mjpegServer = new MJPEGServer(PORT, cameraHandler);
            mjpegServer.start();
            
            Log.d(TAG, "Streaming service started on port " + PORT);
        } catch (Exception e) {
            Log.e(TAG, "Error starting service", e);
            stopSelf();
        }
        
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (mjpegServer != null) {
            mjpegServer.stop();
        }
        
        if (cameraHandler != null) {
            cameraHandler.closeCamera();
        }
        
        Log.d(TAG, "Streaming service stopped");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "IP Camera Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Running IP Camera streaming server");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("IP Camera Active")
            .setContentText("Streaming on port " + PORT)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }
}
