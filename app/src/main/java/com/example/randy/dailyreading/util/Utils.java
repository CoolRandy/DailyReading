package com.example.randy.dailyreading.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;

import com.example.randy.dailyreading.model.TopStoryBean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.List;

/**
 * Created by ${randy} on 2015/10/2.
 */
public class Utils {

    private static final String PREFS_NAME = "myPreefsFile";
    //read preferences
    public static void setPreferences(Context context, String key, String value){

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    //restore preferences
    public static String getPreferences(Context context, String key, String defValue){
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(key, defValue);
    }

    public static void setBooleanPreferences(Context context, String key, boolean value){

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBooleanPreferences(Context context, String key, boolean defValue){
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(key, defValue);
    }

    //这里采用泛型来扩展，下面两个方法就是对数据类型的一种转换
    public static String BeanListToString(List<? extends Object> beanList)
                throws IOException {
        //实例化一个字节数组流，用于装载压缩后的字节文件
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //对象输出流，封装一下字节数组流
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        //将beanList写入到对象输出流中
        objectOutputStream.writeObject(beanList);
        //用Base64.encode将字节文件转换为Base64编码保存在String中
        String topstoryListString = new String(Base64.encode(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
        //关闭输出流
        objectOutputStream.close();
        return topstoryListString;
    }

    public static List<? extends Object> StringToBeanList(String topstoryListString)
            throws StreamCorruptedException, IOException, ClassNotFoundException{
        //采用Base64解码为字节
        byte[] mobileBytes = Base64.decode(topstoryListString.getBytes(),
                Base64.DEFAULT);
        //将字节数组封装成字节数组输入流
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                mobileBytes);
        //封装为对象输入流
        ObjectInputStream objectInputStream = new ObjectInputStream(
                byteArrayInputStream);
        //从输入流中读取列表数据
        List<TopStoryBean> TopstoryList = (List<TopStoryBean>) objectInputStream
                .readObject();
        //关闭输入流
        objectInputStream.close();
        return TopstoryList;
    }

    //保存图片
    public void writeImageToPreference(Context context, int drawable){

        SharedPreferences sharedPreferences = context.getSharedPreferences("complex", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ByteArrayOutputStream byteArrayOutputStream =new ByteArrayOutputStream();
        BitmapFactory.decodeResource(context.getResources(), drawable).compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        String imageString = new String(Base64.encode(byteArrayOutputStream.toByteArray(),Base64.DEFAULT));
        editor.putString("image", imageString);
        editor.commit();
        try {
            if(byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //获取图片
    public Drawable readImageFromPreference(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("complex", Activity.MODE_PRIVATE);
        String str = sharedPreferences.getString("image", "");
        byte[] imageBytes = Base64.decode(str.getBytes(), Base64.DEFAULT);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
        return Drawable.createFromStream(byteArrayInputStream, "image");
    }

    //此外SharePrefrences可以存储序列化对象


    /**
     * 判断网络是否可获得
     */
    public static boolean networkIsAvailable(Context context){
        if(context != null){
            ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connectivityManager != null){
                NetworkInfo mNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (mNetworkInfo != null) {
                    return mNetworkInfo.isAvailable();
                }
            }
        }
        return false;
    }

}
