package com.example.randy.dailyreading.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.Toast;

import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.view.NumberProgressBar;
import com.example.randy.dailyreading.view.OnProgressBarListener;

import java.util.Timer;

/**
 * Created by randy on 2015/10/30.
 * 该Activity是用来测试自定义View的
 */
public class CustomViewActivity extends Activity implements OnProgressBarListener{

    private NumberProgressBar npb;
    private Timer timer;

    private int index = 0;

    /**
     * 这里第二种方式handler是在主线程中运行的
     */
    Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            npb.increaseProgressBy(1);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //测试DoubleImageView
        //setContentView(R.layout.double_imageview);
        //测试BoxGridLayout
        //setContentView(R.layout.box);


        /**
         *测试NumberProgressBar
         */

        setContentView(R.layout.activity_main);
        npb = (NumberProgressBar)findViewById(R.id.default_progress_bar);
        npb.setOnProgressBarListener(this);

        /**
         * 方式1、采用定时器的方式让绘制运行在UI线程上
         */
        /*timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                //Looper.prepare();

                runOnUiThread(new Runnable() {//让绘制跑在UI线程上
                    @Override
                    public void run() {
                        npb.increaseProgressBy(1);
                    }
                });

            }
        }, 1000, 100);*/
        /**
         * 方式2、采用Thread
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                /*while(true) {
                    //Looper.prepare();
                    //npb.increaseProgressBy(1);
                    handler.sendEmptyMessageDelayed(100, 10);
                    //Looper.loop();
                }*/

                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //while(true) {
                            try {
                                Thread.sleep(1000);
                                handler.obtainMessage().sendToTarget();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        //}

                    }
                });*/

                while(true){
                    try{
                        handler.obtainMessage().sendToTarget();
                        Thread.sleep(100);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        /**
         * 方式3：handler在一个异步线程中执行，也就是说新开一个子线程  采用HandlerThread
         * 这时调用setProgress方法，内部重绘需要使用postInvalidate  因为这里是运行在子线程中
         */
        //startWorkerHandler();

    }

    public void startWorkerHandler(){
        HandlerThread handlerThread = new HandlerThread("handler-thread");
        handlerThread.start();

        final Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                npb.increaseProgressBy(1);
                handler.postDelayed(this, 100);
            }
        });
    }

    @Override
    public void onProgressChange(int current, int max) {

        if(current == max){
            Toast.makeText(getApplicationContext(), getString(R.string.finish), Toast.LENGTH_SHORT).show();
            npb.setProgress(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //timer.cancel();

    }
}
