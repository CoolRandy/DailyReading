package com.example.randy.dailyreading.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.TypedValue;
import android.view.WindowManager;

import com.example.randy.dailyreading.model.TopStoryBean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

    /**
     * 保存图片
     * @param context
     * @param drawable
     */
    public void writeImageToPreference(Context context, int drawable){

        SharedPreferences sharedPreferences = context.getSharedPreferences("complex", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ByteArrayOutputStream byteArrayOutputStream =new ByteArrayOutputStream();
        //解码并压缩
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

    /**
     * 获取图片
     * @param context
     * @return
     */
    public Drawable readImageFromPreference(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("complex", Activity.MODE_PRIVATE);
        String str = sharedPreferences.getString("image", "");
        byte[] imageBytes = Base64.decode(str.getBytes(), Base64.DEFAULT);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
        return Drawable.createFromStream(byteArrayInputStream, "image");
    }

    /**
     * 读取资源图片
     * @return
     */
    private Bitmap readBitMap(Context context, int resId){
        BitmapFactory.Options opt=new BitmapFactory.Options();
        /*
         * 设置让解码器以最佳方式解码
         */
        opt.inPreferredConfig=Bitmap.Config.RGB_565;
        //下面两个字段需要组合使用
        opt.inPurgeable=true;
        opt.inInputShareable=true;
        /*
         * 获取资源图片
         */
        InputStream is=context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }


    /**
     * 缩放图片  1、采用矩阵类进行缩放   2、原有的bitamp对象是不可编辑的，需重新create一个bitmap返回
     * @param bitmap
     * @param w
     * @param h
     * @return
     */
    public Bitmap zoomBitmap(Bitmap bitmap, int w, int h){

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        float scale_w = ((float)w / width);
        float scale_h = ((float)h / height);
        matrix.postScale(scale_w, scale_h);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return newBitmap;
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


    public static int dp2px(int dpVal, Context context)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    /**
     * 根据手机分辨率从dp转换为px像素
     * @param dp
     * @return
     */
    public static float dp2px(float dp, Context context){
        final float scale = context.getResources().getDisplayMetrics().density;
        return scale * dp + 0.5f;
    }

    /**
     * 根据手机分辨率从sp转换为px
     * @param sp
     * @return
     */
    public static float sp2px(float sp, Context context){
        final float scale = context.getResources().getDisplayMetrics().density;
        return scale * sp;
    }

    /**
     * 根据手机分辨率从px转换为dp
     * @param px
     * @return
     */
    public static float px2dp(float px, Context context){
        final float scale = context.getResources().getDisplayMetrics().density;
        return px / scale + 0.5f;
    }

    /**
     * 获取当前设备的宽高
     * @param context
     * @return
     */
    @SuppressWarnings("deprecation")
    public static int getDeviceWidth(Context context){

        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        return manager.getDefaultDisplay().getWidth();
    }

    @SuppressWarnings("deprecation")
    public static int getDeviceHeight(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        return manager.getDefaultDisplay().getHeight();
    }

    /**
     * 判断设备是否为手机
     * @param context
     * @return
     */
    public static boolean isPhone(Context context){

        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 获取当前设备的IMEI(唯一的设备号)，需要与上面的isPhone()一起使用
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public static String getDeviceIMEI(Context context) {
        String deviceId;
        if (isPhone(context)) {
            TelephonyManager telephony = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = telephony.getDeviceId();
        } else {
            deviceId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        }
        return deviceId;
    }

    /**
     * 判断sd卡是否存在，若存在返回sd卡根目录，否则返回null
     * @return
     */
    public static String hasSdcard(){

        File sdcardDir = null;
        boolean sdcardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if(sdcardExist){
            sdcardDir = Environment.getExternalStorageDirectory();
            return sdcardDir.toString();
        }else {
            return null;
        }
    }
}
