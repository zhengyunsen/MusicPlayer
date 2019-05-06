package com.zys.weather.musicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int SELECT_MUSIC_RESULT_CODE = 1;
    private static final String TAG = "Media" ;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private int totalTime = mediaPlayer.getDuration();
//    private Button pause = null;
//    private Button start = null;
//    private Button forward = null;
//    private Button backward = null;
//    private Button select_file = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button select_file = (Button) findViewById(R.id.select_file);
        Button pause = (Button) findViewById(R.id.pause);
        Button start = (Button) findViewById(R.id.start);
        Button forward = (Button) findViewById(R.id.forward);
        Button backward = (Button) findViewById(R.id.backward);

        select_file.setOnClickListener(this);
        pause.setOnClickListener(this);
        start.setOnClickListener(this);
        forward.setOnClickListener(this);
        backward.setOnClickListener(this);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        initMusicPlayer();
    }

    private void initMusicPlayer(){
        try {
            File file = new File(Environment.getExternalStorageDirectory() , "2");
            Log.d(TAG, file.getPath());
            mediaPlayer.setDataSource(file.getPath());
//            totalTime = mediaPlayer.getDuration();
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        int currentPosition =mediaPlayer.getCurrentPosition();
        switch (v.getId()) {
            case R.id.select_file:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,SELECT_MUSIC_RESULT_CODE);
                break;
            case R.id.start:
                if (!mediaPlayer.isPlaying()) {
//                    startMusic();
                    mediaPlayer.start();
//                    if (start.isEnabled()) {
//                        start.setEnabled(false);
//                    }
//
//                    pause.setEnabled(true);
                }
                break;
            case R.id.pause:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
//                    pause.setEnabled(false);
//                    start.setEnabled(true);
                }
                break;
            case R.id.forward:
                if (mediaPlayer.isPlaying()) {
                    int forwardTime = 1000 * 2;

                    if (currentPosition > 0 && currentPosition < totalTime - forwardTime) {
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+forwardTime);
                    }

                }
                break;
            case R.id.backward:
                if (mediaPlayer.isPlaying()) {
                    int backwardTime = 1000 * 2;
                    if (currentPosition < totalTime && currentPosition - backwardTime > 0) {
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-backwardTime);
                    }

                }
                break;
            default:
                break;
        }
    }

//    private void startMusic(){
//        try {
//            File file = new File(Environment.getExternalStorageDirectory() , "2");
//            Log.d(TAG, file.getPath());
//            mediaPlayer.setDataSource(file.getPath());
//            mediaPlayer.prepareAsync();
//            totalTime = mediaPlayer.getDuration();
//            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                mediaPlayer.start();
//                start.setEnabled(false);
//                pause.setEnabled(true);
//            }
//        });
//
//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
////                start.setEnabled(true);
////                mediaPlayer.setLooping(true);
//                startMusic();
//            }
//        });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_MUSIC_RESULT_CODE && resultCode == RESULT_OK) {
            String musicPath = "";
            Uri uri = null;
            if (data != null && data.getData() != null) {// 有数据返回直接使用返回的地址
                uri = data.getData();
                try {
//                    mediaPlayer = MediaPlayer.create(this,uri);
//                    mediaPlayer.prepare();
//                    mediaPlayer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                Toast.makeText(this, "文件路径：" + uri.getPath().toString(), Toast.LENGTH_SHORT).show();
//                Log.d(TAG, uri.getPath().toString());
//                try {
//                    mediaPlayer.setDataSource(uri.getPath().toString());
//                    mediaPlayer.prepare();
//                }catch (Exception e){
//                    e.printStackTrace();
//                }

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
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
