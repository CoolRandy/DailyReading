package com.example.randy.dailyreading.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.model.Constant;
import com.example.randy.dailyreading.model.StartPicBean;
import com.example.randy.dailyreading.util.HttpUtils;
import com.example.randy.dailyreading.util.RequestCallBack;
import com.example.randy.dailyreading.util.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by randy on 2015/10/15.
 */
public class SplashActivity extends Activity {

    private ImageView mImageView;
    //请求启动图像回调
    private RequestCallBack startPicCallback;
    //启动图像bean
    private StartPicBean mStartPicBean;
    //
    protected ImageLoader imageLoader = ImageLoader.getInstance();

    public RequestQueue mRequestQueue;

    //网络标志位
    private boolean netIsAvaiable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_layout);
        mRequestQueue = Volley.newRequestQueue(this);
        mImageView = (ImageView)findViewById(R.id.image_start);
        initImage();
    }

    public void initImage(){

        //放大图片的动画效果
        File dir = getFilesDir();
        final File imgFile = new File(dir, "start.jpg");
        if(imgFile.exists()){
            //图片文件存在
            mImageView.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
        }else{
            mImageView.setImageResource(R.mipmap.start);
        }

        final ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setDuration(3000);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //请求接口获取最新的启动图像
                /*startPicCallback = new RequestCallBack() {
                    @Override
                    public void onFail(Exception e) {
                        startActivity();
                    }

                    @Override
                    public void onSuccess(Object o) {
                        mStartPicBean = (StartPicBean) o;
                        Log.e("TAG", "图片的url--->" + mStartPicBean.getImg());
                        imageLoader.loadImage(mStartPicBean.getImg(), new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                                //super.onLoadingComplete(imageUri, view, loadedImage);
                                //加载结束，将图片存到本地文件夹
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        saveBitmap(imgFile, loadedImage);
                                    }
                                };
                                startActivity();
                            }
                        });
                    }
                };
                //这块请求出了问题  NPE解决   但是会存在访问时间超时的问题
                HttpUtils.get(Constant.START, StartPicBean.class, startPicCallback, mRequestQueue, 50000);*/

                //先判断一下网络是否可得到
                if(Utils.networkIsAvailable(SplashActivity.this)) {
                    Log.e("TAG", "网络正常");
                    Constant.HAS_NETWORK = true;
                    AsyncHttpClient client = new AsyncHttpClient();
                    client.get(Constant.START, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {

                            try {
                                JSONObject jsonObject = new JSONObject(new String(bytes));
                                String url = jsonObject.getString("img");
                                Log.e("TAG", "图片的url--->" + url);
                                HttpUtils.get(url, new BinaryHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                                        saveImage(imgFile, bytes);
                                        startActivity();
                                    }

                                    @Override
                                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                                        //这里表示请求图片失败，但是有网
                                        startActivity();
                                    }
                                });

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            super.onFailure(statusCode, headers, responseBody, error);
                            //请求网络失败，也需要跳转
                            Constant.HAS_NETWORK = false;
                            startActivity();
                        }
                    });
                }else {
                    //没有网络直接跳转
                    Log.e("TAG", "没有网络");
                    Constant.HAS_NETWORK = false;
                    startActivity();
                }
                //采用自定义的GsonRequest请求json数据，然后采用UIL下载图片
                /*GsonRequest<StartPicBean> gsonRequest = new GsonRequest<StartPicBean>(Constant.START,
                        StartPicBean.class, new Response.Listener<StartPicBean>() {
                    @Override
                    public void onResponse(StartPicBean response) {

                        Log.e("TAG", "图片的url--->" + response.getImg());

                        imageLoader.loadImage(response.getImg(), new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                                //super.onLoadingComplete(imageUri, view, loadedImage);
                                //加载结束，将图片存到本地文件夹
                                new Runnable(){
                                    @Override
                                    public void run() {
                                        saveBitmap(imgFile, loadedImage);
                                    }
                                };
                                startActivity();
                            }
                        });

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                Log.e("TAG", "gsonRequest--->" + gsonRequest);
                mRequestQueue.add(gsonRequest);*/
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mImageView.startAnimation(scaleAnimation);

    }
    //TODO 这种方式好像没有起作用，还需研究原因
    public void saveBitmap(File file, Bitmap bitmap){
        try{
            if(file.exists()){
                file.delete();
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);//压缩处理
            outputStream.flush();
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void saveImage(File file, byte[] bytes) {
        try {
            file.delete();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //初始化动画后跳转主Activity
    public void startActivity(){

        Intent intent = new Intent(SplashActivity.this, MainContentActivity.class);
        //Intent intent = new Intent(SplashActivity.this, TestActivity.class);
        //intent.putExtra("hasNetwork", netIsAvaiable);
        startActivity(intent);
        //Activity切换效果，该方法必须放在startActivity或finish方法后
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

}
