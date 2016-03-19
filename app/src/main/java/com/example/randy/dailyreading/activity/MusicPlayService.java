package com.example.randy.dailyreading.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by randy on 2016/2/18.
 */
public class MusicPlayService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {
    //url
    private String sntAudioLink;
    //base url
    private static final String BASE_URL = "http://abv.cn/music/";
    private MediaPlayer mediaPlayer = new MediaPlayer();
    //sd root dir
    private String MUSIC_PATH = null;
    //List
    private List<File> fileList = new ArrayList<>();
    //set up notification id
    private static final int NOTIFICATION_ID = 1;
    //对电话的控制
    private boolean isPausedInCall = false;
    private PhoneStateListener mPhoneStateListener;
    private TelephonyManager mTelephonyManager;

    //broadcast 控制在线音乐缓存
    public static final String BROADCAST_BUFFER = " com.example.randy.dailyreading.broadcastbuffer";
    Intent bufferIntent;
    //download intent
    private String loadMusic;

    public MyBinder mMyBinder = new MyBinder();


    @Override
    public void onCreate() {
        //
        bufferIntent = new Intent(BROADCAST_BUFFER);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.reset();//复位

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //get telephony manager
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneStateListener = new PhoneStateListener(){

            @Override
            public void onCallStateChanged(int state, String incomingNumber) {

                switch (state){

                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if(mediaPlayer != null){

                            pauseMedia();
                            isPausedInCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if(mediaPlayer != null){
                            if(isPausedInCall){
                                playMedia();
                                isPausedInCall = false;
                            }
                        }
                        break;
                }
            }
        };

        //使用telephony manager注册电话监听   是否处于拨打状态
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        //insert notification
        initNotification();
        sntAudioLink = intent.getStringExtra("sentAudioLink");
        mediaPlayer.reset();
        // Set up the MediaPlayer data source using the strAudioLink value
        if (!mediaPlayer.isPlaying()) {

            if(Utils.hasSdcard() != null) {
                MUSIC_PATH = Utils.hasSdcard() + "/musicLoad";
            }
            try {
                if(traverseFileIsExist(MUSIC_PATH, sntAudioLink)){//本地播放
                    Log.e("TAG", "play from local");
                    Log.e("TAG", "new path: " + MUSIC_PATH + "/" + sntAudioLink);
                    mediaPlayer.setDataSource(MUSIC_PATH + "/" + sntAudioLink);

                }else {//在线播放
                    Log.e("TAG", "play from network");
                    mediaPlayer.setDataSource(BASE_URL + sntAudioLink);
                    //send message to Activity to display progressBar
                    sendBufferingBroadcast();
                }
                // Prepare media player 采用异步的
                mediaPlayer.prepareAsync();//准备就绪之后，就会调用回调方法onPrepared方法
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        //准备就绪之后，开始播放
        sendBufferedBroadcast();
        playMedia();
        //这里暂时默认下载
//        downloadMusicAsync(BASE_URL + sntAudioLink);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Toast.makeText(this,
                        "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra,
                        Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Toast.makeText(this, "MEDIA ERROR SERVER DIED " + extra,
                        Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Toast.makeText(this, "MEDIA ERROR UNKNOWN " + extra,
                        Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        stopMedia();
        //手动关闭service
        stopSelf();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMyBinder;
    }

    public void initNotification(){

        //create notification
//        Notification notification = new Notification(R.mipmap.icon, "Music In Service", System.currentTimeMillis());
        //create intent
        Intent intent = new Intent(this, MusicPlayActivity.class);
        //intent wait util ready
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, 0);
        //set event info
        CharSequence contentTitle = "Music In Service App Tutorial";
        CharSequence contentText = "Listen To Music While Performing Other Tasks";
        CharSequence tickerText = "Music Message";
        //setLatestEventInfo has been removed in M(api 23)
//        notification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, pendingIntent);
        //Notification.Builder  require min API 16
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setTicker(tickerText)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.icon)
                .build();
        // 获取通知 manager 的实例
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }


    public class MyBinder extends Binder {

        private String url;

        public MyBinder() {
            this.url = BASE_URL;
        }

        public String getUrl() {
            return url;
        }

        /**
         * 异步下载音频文件
         */
        //TODO 测试没有下载成功
        public void downloadMusicAsync(final String musicUrl) {

            //首先遍历
//        if(null == Utils.hasSdcard()){//不存在根目录
//            return;
//        }
//        traverseFileIsExist(MUSIC_PATH);
            new Thread(new Runnable() {
                @Override
                public void run() {

                    String fileName = musicUrl.substring(musicUrl.lastIndexOf("/") + 1);
                    Log.e("TAG", "fileName is: " + fileName);
                    try {
                        //url 编码
                        fileName = URLEncoder.encode(fileName, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    String lastUrl = musicUrl.substring(0, musicUrl.lastIndexOf("/") + 1) + fileName;
                    Log.e("TAG", "lastUrl: " + lastUrl);
                    try {
                        URL url = new URL(lastUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setDoOutput(true);
                        connection.connect();
                        //sd 保存地址
                        if (Utils.hasSdcard() != null) {
                            MUSIC_PATH = Utils.hasSdcard() + "/musicLoad";
                        }
                        Log.e("TAG", "sdCard addr: " + MUSIC_PATH);//      /storage/emulated/0/musicLoad
                        File file = new File(MUSIC_PATH);
                        file.mkdirs();
                        //file absolute path: /storage/emulated/0/musicLoad
                        Log.e("TAG", "file absolute path: " + file.getAbsolutePath());
//                    String fileName = "life.mp3";
                        File outputFile = new File(file, sntAudioLink);
                        //outputFile absolute path: /storage/emulated/0/musicLoad/光辉岁月.mp3
                        Log.e("TAG", "outputFile absolute path: " + outputFile.getAbsolutePath());
//                    fileList.add(outputFile);
                        final FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                        InputStream inputStream = connection.getInputStream();
                        byte[] buffer = new byte[1024];
                        int len = 0;
                        while ((len = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        inputStream.close();

                        //下载完成  采用通知的方式


                    } catch (MalformedURLException e) {

                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

        }
    }

    public boolean traverseFileIsExist(String path, String song){

        if(null == path){
            Log.e("TAG", "path is not exist");
            return false;
        }
        String absolutePath = path + "/";
        Log.e("TAG", "absolutePath: " + absolutePath);
        File file_ = new File(absolutePath);
        File[] files = file_.listFiles();
        if(files != null){
            int count = files.length;
            for(int i = 0; i < count; i++){
                File file = files[i];
                Log.e("TAG", "file name: " + file.getName());
                String filePath = file.getAbsolutePath();
                Log.e("TAG", "each file path: " + filePath);
                if(filePath.endsWith("mp3")){

                    if(filePath.equals(absolutePath + song)){
                        //本地存在，直接从本地播放
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void playMedia() {

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void pauseMedia(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
    }

    public void stopMedia() {

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public void sendBufferingBroadcast(){
        //正在缓冲
        bufferIntent.putExtra("buffering", "1");
        sendBroadcast(bufferIntent);
    }

    public void sendBufferedBroadcast(){
        //缓冲完毕
        bufferIntent.putExtra("buffering", "0");
        sendBroadcast(bufferIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }

        if(mPhoneStateListener != null){
            mTelephonyManager.listen(mPhoneStateListener,
                    PhoneStateListener.LISTEN_NONE);
        }

        //cancel notification
        cancelNotification();
    }

    public void cancelNotification(){

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
