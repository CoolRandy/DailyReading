package com.example.randy.dailyreading.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.fragment.MusicDialogFragment;
import com.example.randy.dailyreading.util.MusicPlayManager;
import com.example.randy.dailyreading.util.Utils;

import co.mobiwise.library.MusicPlayerView;

/**
 * Created by randy on 2015/12/20.
 */
public class MusicPlayActivity extends AppCompatActivity {

    MusicPlayerView mpv;
    private ImageView downloadImg;
    Intent serviceIntent;
    private MediaPlayer mediaPlayer;
    //是否有网标志
    private boolean isOnline;
    // -- PUT THE NAME OF YOUR AUDIO FILE HERE...URL GOES IN THE SERVICE
    String strAudioLink = "光辉岁月.mp3";

    //加载dialog
    // Progress dialogue and broadcast receiver variables
    boolean mBufferBroadcastIsRegistered;
    private ProgressDialog pdBuff = null;
    //下面的接收器对象需要分别注册和取消
    //设置广播接收器
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //显示progress dialog
            showPD(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_music_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
                showMusicDialog();
            }
        });

        try {
            serviceIntent = new Intent(this, MusicPlayService.class);
            initViews();
            initData();
//            setListeners();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getClass().getName() + " " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.star);

        mpv.setCoverURL("https://upload.wikimedia.org/wikipedia/en/b/b3/MichaelsNumberOnes.JPG");
        mpv.setTimeColor(Color.BLUE);

        mpv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 这一块的判断逻辑有点问题？？？
                if (mpv.isRotating()) {//正在转动，即点击前为播放状态
                    mpv.stop();//对应的view显示暂停
                    //音频的播放由service执行
//                    mediaPlayer.pause();
                    stopMusicPlayService();
//                    pauseAudio();
                    MusicPlayManager.setIsPlaying(false);

                } else {
                    mpv.start();
//                    mediaPlayer.start();
                    playAudio();
                    MusicPlayManager.setIsPlaying(true);
                }
            }
        });
    }

    private MusicPlayService.MyBinder mBinderService;
    private boolean isBinded = false;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinderService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            mBinderService = (MusicPlayService.MyBinder) service;

            mBinderService.downloadMusicAsync(mBinderService.getUrl() + strAudioLink);
        }
    };

    public void doBindService(){

        if(!isBinded){
            Intent bindIntent = new Intent(MusicPlayActivity.this, MusicPlayService.class);
            //绑定service
            bindService(bindIntent, connection, BIND_AUTO_CREATE);
            isBinded = true;
        }
    }

    public void doUnBindService(){

        if(isBinded){
            unbindService(connection);
            isBinded = false;
        }
    }




    //初始化播放、暂停按钮，这里暂时用不到
    public void initViews() {

        mpv = (MusicPlayerView) findViewById(R.id.mpv);
        downloadImg = (ImageView) findViewById(R.id.download);
    }

    public void initData() {
        //
//        MusicPlayManager.setIsPlaying(false);
        downloadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("TAG", "click to download");
                Intent intent = new Intent(getApplicationContext(), DownloadActivity.class);
                startActivity(intent);
//                doBindService();
            }
        });
    }

    public void setListeners() {

    }

    public void showMusicDialog(){

        MusicDialogFragment dialog = new MusicDialogFragment();
        dialog.show(getSupportFragmentManager(), "exitMusicDialog");
        Dialog dialog1 = new Dialog(this);

    }

    //播放音频  service
    public void playAudio() {

        Log.e("TAG", "play audio...");

        if (Utils.networkIsAvailable(this)) {
            //onLine
            isOnline = true;
            serviceIntent.putExtra("sentAudioLink", strAudioLink);
            try {
                startService(serviceIntent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getClass().getName() + " " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            //offLine
            isOnline = false;
            //播放本地音乐或提示没有网络
            mpv.stop();//停止转动
            Toast.makeText(getApplicationContext(), "亲，请检查你的网络。。。",
                    Toast.LENGTH_LONG).show();
        }
    }

    //暂停播放
    public void pauseAudio(){


    }

    //停止播放
    public void stopMusicPlayService() {

        Log.e("TAG", "stop service");

        try {
            stopService(serviceIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getClass().getName() + " " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // Handle progress dialogue for buffering...
    private void showPD(Intent bufferIntent) {
        String bufferValue = bufferIntent.getStringExtra("buffering");
        int bufferIntValue = Integer.parseInt(bufferValue);

        // When the broadcasted "buffering" value is 1, show "Buffering"
        // progress dialogue.
        // When the broadcasted "buffering" value is 0, dismiss the progress
        // dialogue.

        switch (bufferIntValue) {
            case 0:
                // Log.v(TAG, "BufferIntValue=0 RemoveBufferDialogue");
                // txtBuffer.setText("");
                if (pdBuff != null) {
                    pdBuff.dismiss();
                }
                break;

            case 1:
                BufferDialogue();
                break;

            // Listen for "2" to reset the button to a play button
            case 2:
//                buttonPlayStop.setBackgroundResource(R.drawable.playbuttonsm);
                break;

        }
    }

    // Progress dialogue...
    private void BufferDialogue() {

        pdBuff = ProgressDialog.show(MusicPlayActivity.this, "Buffering...",
                "Acquiring song...", true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!mBufferBroadcastIsRegistered) {
            registerReceiver(mBroadcastReceiver, new IntentFilter(
                    MusicPlayService.BROADCAST_BUFFER));
            mBufferBroadcastIsRegistered = true;
        }
    }

    @Override
    protected void onPause() {
        if(mBufferBroadcastIsRegistered) {
            unregisterReceiver(mBroadcastReceiver);
            mBufferBroadcastIsRegistered = false;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer = null;
        Log.e("TAG", "connection: " + connection);//这里不为null
        if(connection != null){
//            unbindService(connection);
            doUnBindService();
            connection = null;
        }
    }
}
