package com.zys.weather.musicplayer.service;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.zys.weather.musicplayer.DownloadListener;
import com.zys.weather.musicplayer.DownloadTask;
import com.zys.weather.musicplayer.MainActivity;
import com.zys.weather.musicplayer.R;

import java.io.File;

public class DownloadService extends Service {

    private DownloadTask downloadTask;

    private String downloadUrl;

    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1,getNotification("Downloading...",progress));
        }

        @Override
        public void onSuccess() {
            downloadTask=null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download Success",-1));
            Toast.makeText(DownloadService.this,"Download Success",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask=null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download Failed",-1));
            Toast.makeText(DownloadService.this,"Download Failed",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            downloadTask=null;
            Toast.makeText(DownloadService.this,"Download Paused",Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onCanceled() {
            downloadTask=null;
            stopForeground(true);
            Toast.makeText(DownloadService.this,"Download Canceled",Toast.LENGTH_SHORT).show();
        }
    };

    public DownloadService() { }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("................", "onCreate: ");
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.remote_view_main);
        Intent intentStart = new Intent("start");
        PendingIntent startPendingIntent = PendingIntent.getBroadcast(this, 0, intentStart, PendingIntent.FLAG_CANCEL_CURRENT);
        Intent intentPause = new Intent("pause");
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 0, intentPause, PendingIntent.FLAG_CANCEL_CURRENT);
        Intent intentNext = new Intent("next");
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0, intentNext, PendingIntent.FLAG_CANCEL_CURRENT);
        Intent intentForward = new Intent("forward");
        PendingIntent forwardPendingIntent = PendingIntent.getBroadcast(this, 0, intentForward, PendingIntent.FLAG_CANCEL_CURRENT);

        remoteViews.setTextViewText(R.id.start, "播放");//通过id-内容的方式设置remoteview中控件的内容，底层实现是通过Binder跨进程通信
        remoteViews.setTextViewText(R.id.pause, "暂停");
        remoteViews.setTextViewText(R.id.forward_music, "上一首");
        remoteViews.setTextViewText(R.id.next_music,"下一首");
        remoteViews.setOnClickPendingIntent(R.id.start, startPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.pause, pausePendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.next_music, nextPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.forward_music, forwardPendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");
        builder.setSmallIcon(R.mipmap.ic_launcher)
               .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
               .setContent(remoteViews)
               .setContentIntent(pi);
        manager.notify(2,builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("................", "onStartCommand: ");

        return super.onStartCommand(intent, flags, startId);
    }

    private DownloadMusicBinder downloadMusicBinder = new DownloadMusicBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return downloadMusicBinder;
    }

    public class DownloadMusicBinder extends Binder {
        public void downloadMusic(String url) {
            if (downloadTask == null) {
                downloadUrl = url;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(downloadUrl);
                startForeground(1, getNotification("Downloading...", 0));
                Toast.makeText(DownloadService.this, "Downloading...", Toast.LENGTH_SHORT).show();
            }
        }

        public void pauseDownload() {
            if (downloadTask != null) {
                downloadTask.pauseDownload();
            }
        }

        public void cancelDownload() {

            if (downloadTask != null) {
                downloadTask.cancelDownload();
            } else {
                if (downloadUrl != null) {
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    File sdDir = Environment.getExternalStorageDirectory();
                    File musicDir = new File(sdDir+File.separator+"Music");
                    File file = new File(musicDir.getPath() + fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);

                }
            }
        }


    }

    private NotificationManager getNotificationManager(){
        return (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }


    private Notification getNotification(String title, int progress){
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.remote_view_downloadprogress);
        Intent intentStart = new Intent("start_download");
        PendingIntent startPendingIntent = PendingIntent.getBroadcast(this, 0, intentStart, PendingIntent.FLAG_CANCEL_CURRENT);
        Intent intentPause = new Intent("pause_download");
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 0, intentPause, PendingIntent.FLAG_CANCEL_CURRENT);
        Intent intentCancel = new Intent("cancel_download");
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, intentCancel, PendingIntent.FLAG_CANCEL_CURRENT);

        remoteViews.setTextViewText(R.id.start_download, "下载");//通过id-内容的方式设置remoteview中控件的内容，底层实现是通过Binder跨进程通信
        remoteViews.setTextViewText(R.id.pause_download, "暂停");
        remoteViews.setTextViewText(R.id.cancel_download, "取消");

        if (progress > 0) {
            remoteViews.setTextViewText(R.id.download_progress,progress+"%");
            remoteViews.setProgressBar(R.id.download_progressBar, 100, progress, false);
        } else {
            remoteViews.setTextViewText(R.id.download_progress,"0%");
            remoteViews.setProgressBar(R.id.download_progressBar,100,0,false);
        }
        remoteViews.setOnClickPendingIntent(R.id.start_download, startPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.pause_download, pausePendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.cancel_download, cancelPendingIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        builder.setContent(remoteViews);
        return builder.build();
    }
}
