package com.zys.weather.musicplayer;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zys.weather.musicplayer.service.DownloadService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int SELECT_MUSIC_RESULT_CODE = 1;

    private static final String TAG = "Media" ;

    private MediaPlayer mediaPlayer;

    List<String> musicLists = new ArrayList<>();

    private int musicIndex = 0;

    private int totalTime;

    private Button pause;

    private Button start;

    private Button forward;

    private Button backward;

    private Button select_file;

    private Button connect_network;

    private Button next_music;

    private Button forward_music;

    private EditText music_url;

    private DownloadService.DownloadMusicBinder downloadMusicBinder;

    private BroadcastReceiver playerReceiver;

    private ServiceConnection serviceConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: ");
            downloadMusicBinder = (DownloadService.DownloadMusicBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        select_file = (Button) findViewById(R.id.select_file);
        pause = (Button) findViewById(R.id.pause);
        start = (Button) findViewById(R.id.start);
        forward = (Button) findViewById(R.id.forward);
        backward = (Button) findViewById(R.id.backward);
        connect_network = (Button)findViewById(R.id.connect_network);
        forward_music = (Button)findViewById(R.id.forward_music);
        next_music = (Button)findViewById(R.id.next_music);
        music_url = (EditText) findViewById(R.id.music_url);


        select_file.setOnClickListener(this);
        pause.setOnClickListener(this);
        start.setOnClickListener(this);
        forward.setOnClickListener(this);
        backward.setOnClickListener(this);
        connect_network.setOnClickListener(this);
        forward_music.setOnClickListener(this);
        next_music.setOnClickListener(this);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        initMusicPlayer();
        // 注册广播
        playerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("action", intent.getAction());
                switch (intent.getAction()) {
                    case "start":
                        startMusic();
                        break;
                    case "pause":
                        pause();
                        break;
                    case "next":
                        nextMusic();
                        break;
                    case "forward":
                        forwardMusic();
                    break;
                    default:
                        break;
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("start");
        intentFilter.addAction("pause");
        intentFilter.addAction("next");
        intentFilter.addAction("forward");
        registerReceiver(playerReceiver, intentFilter);
    }

    private void initMusicPlayer(){
        try {

            // 判断SD卡是否存在，并且是否具有读写权限
            if (Environment.getExternalStorageState().
                    equals(Environment.MEDIA_MOUNTED)) {
                File sdDir = Environment.getExternalStorageDirectory();
                File path = new File(sdDir+File.separator+"Music");
                File[] files = path.listFiles();// 读取文件夹下文件
                Log.d(TAG, files.toString());
                for (int i = 0; i < files.length; i++) {
                    Log.d(TAG, files[i].getPath());
                    musicLists.add(files[i].getPath());
                }
                Log.d(TAG, musicLists.toString());
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(musicLists.get(0));
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
//                    mediaPlayer.start();
                    totalTime = mediaPlayer.getDuration();
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                Log.d(TAG, intent.toString());
                bindService(intent,serviceConnection,BIND_AUTO_CREATE);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.select_file:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,SELECT_MUSIC_RESULT_CODE);
                break;
            case R.id.start:
                    startMusic();
                break;
            case R.id.pause:
                    pause();
                break;
            case R.id.forward:
                forward();
                break;
            case R.id.backward:
                backward();
            case R.id.connect_network:
                onlineMusic();
                break;
            case R.id.forward_music:
                forwardMusic();
                break;
            case R.id.next_music:
                nextMusic();
            default:
                break;
        }
    }

    private void forwardMusic() {
        if (musicIndex > 0) {
            musicIndex = musicIndex - 1;
            songplay();
        }else {
            musicIndex = musicLists.size()-1;
            songplay();
        }
    }

    private void nextMusic(){
        if (musicIndex < musicLists.size() - 1) {
            musicIndex = musicIndex + 1;
            songplay();
        }else {
            musicIndex = 0;
            songplay();
        }
    }

    private void songplay() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(musicLists.get(musicIndex));
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    private void onlineMusic() {
//        Log.d(TAG, "downloadMusic: ");
        Log.d(TAG, serviceConnection.toString());
//        Intent intent = new Intent(this, DownloadService.class);
//        Log.d(TAG, intent.toString());
//        bindService(intent,serviceConnection,BIND_AUTO_CREATE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = music_url.getText().toString();
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(url); // 设置数据源
                    mediaPlayer.prepare(); // prepare自动播放
                    mediaPlayer.start();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }



    private void pause(){
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private void forward(){
        int currentPosition =mediaPlayer.getCurrentPosition();
        if (mediaPlayer.isPlaying()) {
            int forwardTime = 1000 * 5;

            if (currentPosition > 0 && currentPosition < totalTime - forwardTime) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+forwardTime);
            }

        }
    }

    private void backward(){
        int currentPosition =mediaPlayer.getCurrentPosition();
        if (mediaPlayer.isPlaying()) {
            int backwardTime = 1000 * 5;
            if (currentPosition < totalTime && currentPosition - backwardTime > 0) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-backwardTime);
            }

        }
    }

    public void startMusic() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    nextMusic();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_MUSIC_RESULT_CODE && resultCode == RESULT_OK) {
            String musicPath = "";
            Uri uri = null;
            if (data != null && data.getData() != null) {// 有数据返回直接使用返回的地址
                uri = data.getData();
                Log.d(TAG, uri.getPath().toString());
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(MainActivity.this,uri);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
               }catch (Exception e){
                    e.printStackTrace();
               }

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initMusicPlayer();
                }
                Toast.makeText(this,"拒绝权限将无法使用程序",Toast.LENGTH_SHORT).show();
                finish();
                break;
            default:
                break;
        }

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        unbindService(serviceConnection);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
