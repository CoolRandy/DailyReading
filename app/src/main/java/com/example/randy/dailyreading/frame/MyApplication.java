package com.example.randy.dailyreading.frame;

import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

/**
 * Created by randy on 2015/10/15.
 */
public class MyApplication extends Application {

    //使用单例
    private static MyApplication mInstance;
    public RequestQueue mRequestQueue;


    @Override
    public void onCreate() {
        super.onCreate();
        //初始化UIL
        initImageLoader(getApplicationContext());
        mRequestQueue = Volley.newRequestQueue(this);
    }

    public static MyApplication getmInstance(){
        if(null == mInstance){
            mInstance = new MyApplication();
        }
        return mInstance;
    }

    public void initImageLoader(Context context){

        File cacheDir = StorageUtils.getCacheDirectory(context);
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(3)
                .threadPriority(Thread.NORM_PRIORITY -2)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(configuration);
    }
}
