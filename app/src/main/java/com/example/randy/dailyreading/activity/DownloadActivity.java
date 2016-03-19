package com.example.randy.dailyreading.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.download.FileDownloadThread;
import com.example.randy.dailyreading.util.Utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by randy on 2016/2/28.
 */
public class DownloadActivity extends Activity implements View.OnClickListener {

    private ProgressBar mProgressBar;
    private TextView mTextView;

    private static final String TAG = DownloadActivity.class.getSimpleName();

    private static final String FILE_URL = "http://gdown.baidu.com/data/wisegame/91319a5a1dfae322/baidu_16785426.apk";
    private static final int THREAD_NUM = 5;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_layout);
        mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
        mTextView = (TextView)findViewById(R.id.textView);
        findViewById(R.id.download_button).setOnClickListener(this);
    }

    //创建Handler
    @SuppressWarnings("HandlerLeak")
    Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {

            mProgressBar.setProgress(msg.getData().getInt("size"));

            float temp = (float)mProgressBar.getProgress() / (float)mProgressBar.getMax();
            int progress = (int)(temp * 100);
            if (progress == 100) {
                Toast.makeText(getApplicationContext(), "下载完成！", Toast.LENGTH_LONG).show();
            }
            mTextView.setText("下载进度:" + progress + " %");
        }
    };

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.download_button) {
            //执行下载
            Log.e(TAG, "begin to download" );
            doDownload();
        }
    }

    public void doDownload(){

        //每次下载都要判断sdCard是否存在
        if (Utils.hasSdcard() != null){
            filePath = Utils.hasSdcard() + "/baidu_16785426.apk";
        }else {
            Toast.makeText(getApplicationContext(), "请检查sdka是否插好", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "download file  path:" + filePath);
        //初始化progressbar
        mProgressBar.setProgress(0);
        //开启下载
        new DownloadTask(FILE_URL, THREAD_NUM, filePath).start();
    }


    //下载任务，对文件下载进行封装
    class DownloadTask extends Thread{

        //下载地址
        private String downloadUrl;
        //开启的线程数
        private int threadNums;
        //保存文件的路径
        private String filePath;
        //每个线程下载文件的大小
        private int fileSize;

        public DownloadTask(String downloadUrl, int threadNums, String filePath){

            this.downloadUrl = downloadUrl;
            this.threadNums = threadNums;
            this.filePath = filePath;
        }

        //开启5个线程
        FileDownloadThread[] mThreads = new FileDownloadThread[threadNums];

        @Override
        public void run() {

            try {
                URL url = new URL(downloadUrl);
                Log.e(TAG, "download file http path:" + downloadUrl);
                URLConnection connection = url.openConnection();
                //获取文件长度
                int fileLength = connection.getContentLength();
                if(fileLength <= 0){
                    Log.e(TAG, "文件获取失败");
                    return;
                }

                mProgressBar.setMax(fileLength);

                fileSize = (fileLength % threadNums == 0) ? fileLength / threadNums : fileLength / threadNums + 1;
                Log.e(TAG, "file size for each thread: " + fileSize);

                for (int i = 0; i < mThreads.length; i++){
                    Log.e(TAG, "start thread: " + i);
                    mThreads[i] = new FileDownloadThread(url, new File(filePath), fileSize, i+1);
                    mThreads[i].setName("thread-" + i);
                    mThreads[i].start();
                }//循环结束之后，所有线程均已启动

                //实时更新每个线程的下载进度，汇总计算总的文件下载长度，进而更新进度条状态
                boolean isFinished = false;
                int loadedTotalSize = 0;

                while (!isFinished){
                    isFinished = true;
                    for (int i = 0; i < mThreads.length; i++) {
                        loadedTotalSize += mThreads[i].getDownloadLength();
                        if(!mThreads[i].isDownloadComplete()){//判断当前线程是否下载完成
                            isFinished = false;
                        }
                    }

                    //通过handler更新
                    Message message = new Message();
                    message.getData().putInt("size", loadedTotalSize);
                    mHandler.sendMessageDelayed(message, 1000);
                }


            }catch (MalformedURLException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }
}
