package com.zys.weather.musicplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MyAppWidgetProvider extends AppWidgetProvider {

    public static final String CLICK_ACTION = "study.chenj.chapter5.CLICK";
    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("......", "onReceive : action = " + intent.getAction());
        Log.d("........", "...........");
//        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        //这里判断是自己的action，左自己的事情，比如小部件被单击了要干什么，这里是左一个动画效果
        if (intent.getAction().equals(CLICK_ACTION)) {
            Toast.makeText(context, "clicked it", Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
//                    new MainActivity().startMusic();

                    AppWidgetManager manager = AppWidgetManager.getInstance(context);

                    //重新构造RemoteViews，加载旋转后的图片
                    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.remote_view_main);
                    remoteViews.setTextViewText(R.id.pause, "点击");
                    remoteViews.setTextViewText(R.id.start, "播放");//通过id-内容的方式设置remoteview中控件的内容，底层实现是通过Binder跨进程通信
                    remoteViews.setTextViewText(R.id.forward_music, "上一首");
                    remoteViews.setTextViewText(R.id.next_music,"下一首");
                    //重新设置监听
                        Intent onClickIntent = new Intent();
                        onClickIntent.setAction(CLICK_ACTION);
                        PendingIntent pi = PendingIntent.getBroadcast(context,0,onClickIntent,0);
                        remoteViews.setOnClickPendingIntent(R.id.remote_view_layout,pi);
                    //更新AppWidgetProvider
                    manager.updateAppWidget(new ComponentName(context, MyAppWidgetProvider.class), remoteViews);
                }

            }).start();
        }
    }
}
