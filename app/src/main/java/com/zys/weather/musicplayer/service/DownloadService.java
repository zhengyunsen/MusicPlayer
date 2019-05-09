package com.zys.weather.musicplayer.service;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.zys.weather.musicplayer.FirstActivity;
import com.zys.weather.musicplayer.MainActivity;
import com.zys.weather.musicplayer.MyAppWidgetProvider;
import com.zys.weather.musicplayer.R;

public class DownloadService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("................", "onCreate: ");
        Intent intent = new Intent(this, FirstActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.remote_view_main);
        Intent intentStart = new Intent("start");
        PendingIntent startPendingIntent = PendingIntent.getBroadcast(this, 0, intentStart, PendingIntent.FLAG_CANCEL_CURRENT);
        Intent intentPause = new Intent("pause");
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 0, intentPause, PendingIntent.FLAG_CANCEL_CURRENT);
        Intent intentNext = new Intent("next");
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0, intentNext, PendingIntent.FLAG_CANCEL_CURRENT);
        Intent intentForward = new Intent("foreard");
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
        manager.notify(1,builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("................", "onStartCommand: ");

        return super.onStartCommand(intent, flags, startId);
    }

    private DownloadMusicBinder downloadMusicBinder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return downloadMusicBinder;
    }

    public class DownloadMusicBinder extends Binder{

        private void downloadMusic(String url){

        }
    }
}
