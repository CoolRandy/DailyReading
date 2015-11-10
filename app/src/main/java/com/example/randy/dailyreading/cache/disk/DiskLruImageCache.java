package com.example.randy.dailyreading.cache.disk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader;
import com.example.randy.dailyreading.BuildConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by randy on 2015/11/4.
 * 硬盘缓存：主要用于缓存图片到硬盘中，对比Volley中的DiskBasedCache实现
 * 这里是在Jake Wharton写的DiskLruCache基础上进行封装的，Jake Wharton写的DiskLruCache用于UIL
 */
/**
 * Implementation of DiskLruCache by Jake Wharton
 * modified from http://stackoverflow.com/questions/10185898/using-disklrucache-in-android-4-0-does-not-provide-for-opencache-method
 */
public class DiskLruImageCache implements ImageLoader.ImageCache{

    private final String TAG = this.getClass().getSimpleName();

    private DiskLruCache mDiskCache;
    private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;//图片压缩格式

    private static int IO_BUFFER_SIZE = 8 * 1024;//8 BYTE
    private int mCompressQuality = 70;//压缩质量，这个对应百分比，即70%
    private static final int APP_VERSION = 1; //这里指定应用程序版本固定为1，实际上可以获取
    private static final int VALUE_COUNT = 1;//指定一个key可以对应多少个缓存文件，一般就一个

    public DiskLruImageCache(Context context, String uniqueName, int diskCacheSize,
                             Bitmap.CompressFormat compressFormat, int quality) {

        try{
            final File diskCacheDir = getDiskCacheDir(context, uniqueName);//获取硬盘缓存索引目录
            if(!diskCacheDir.exists()){
                diskCacheDir.mkdirs();
            }
            Log.e(TAG, "硬盘缓存目录");
            //在目录中打开cache，如果不存在则创建一个cache  DiskLruCache实例只能通过调用open方法来获取
            mDiskCache = DiskLruCache.open(diskCacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize);
            mCompressQuality = quality;
            this.compressFormat = compressFormat;
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //获取硬盘缓存目录
    /**
     * getCacheDir
     * Returns the absolute path to the application specific cache directory
     * on the filesystem. These files will be ones that get deleted first when the
     * device runs low on storage.
     * There is no guarantee when these files will be deleted.
     *
     * <strong>Note: you should not <em>rely</em> on the system deleting these
     * files for you; you should always have a reasonable maximum, such as 1 MB,
     * for the amount of space you consume with cache files, and prune those
     * files when exceeding that space.</strong>
     */

    //TODO 前者获取的路径是：/sdcard/Android/data/<application package>/cache
    //TODO 后者获取的路径是：  /data/data/<application package>/cache
    public File getDiskCacheDir(Context context, String uniqueName){
        String cachePath;
        //final String cachePath = context.getCacheDir().getPath();
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()){//如果sd卡存在，则将文件缓存到外设上
            cachePath = context.getExternalCacheDir().getPath();
        }else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     *
     * 关于DiskLruCache存数据：
     * String key = generateKey(url);  这里对url进行二次封装成新的key
     * DiskLruCache.Editor editor = mDiskLruCache.edit(key);
     * OuputStream os = editor.newOutputStream(0);
     * @param bitmap
     * @param editor
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    //将Bitmap对象写入到文件中
    public boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor)
                throws IOException, FileNotFoundException{

        OutputStream outputStream = null;
        try{
            //创建一个新的BufferedOutputStream，提供IO_BUFFER_SIZE大小的缓冲区给editor.newOutputStream(0)
            outputStream = new BufferedOutputStream(editor.newOutputStream(0), IO_BUFFER_SIZE);
            return bitmap.compress(compressFormat, mCompressQuality, outputStream);
        }finally {
            if (outputStream != null){
                outputStream.close();
            }
        }
    }

    /**
     * 获取应用程序版本
     * 注：每当版本号改变，缓存路径下的数据都会被清掉
     * @param context
     * @return
     */
    public int getAppVersion(Context context){
        try{
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            String versionName = info.versionName;//版本名称
            return info.versionCode;//返回版本号
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        return 1;
    }

    //这里最好不直接使用url作为key，因为url可能会包含一些特殊字符，这些字符可能在命名时不合法,如果传入的是图片的url，可以先将其转换为MD5

    @Override
    public void putBitmap(String key, Bitmap bitmap) {

        DiskLruCache.Editor editor = null;
        try{
            editor = mDiskCache.edit(key);
            if(null == editor){
                return;
            }
            if(writeBitmapToFile(bitmap, editor)){
                mDiskCache.flush();
                editor.commit();
                if(BuildConfig.DEBUG){
                    Log.d(TAG, "image put on disk cache: " + key );
                }
            }else {
                editor.abort();
                if(BuildConfig.DEBUG){
                    Log.d(TAG, "Error on: image put on disk cache " + key);
                }

            }
        }catch (IOException e){
            //e.printStackTrace();
            if ( BuildConfig.DEBUG ) {
                Log.d( "TAG", "ERROR on: image put on disk cache " + key );
            }
            try {
                if ( editor != null ) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }
        }

    }

    @Override
    public Bitmap getBitmap(String key) {
        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot = null;
        try{
            snapshot = mDiskCache.get(key);
            if(null == snapshot){
                return null;
            }
            final InputStream in = snapshot.getInputStream(0);
            if(null != in){
                final BufferedInputStream bufferedInputStream = new BufferedInputStream(in, IO_BUFFER_SIZE);
                bitmap = BitmapFactory.decodeStream(bufferedInputStream);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(null != snapshot){
                snapshot.close();
            }
        }
        if ( BuildConfig.DEBUG ) {
            Log.d( "cache_test_DISK_", bitmap == null ? "" : "image read from disk " + key);
        }

        return bitmap;
    }

    /**
     * 将字符串进行MD5编码
     * @param key
     * @return
     */
    public String hashKeyForDisk(String key){

        String cacheKey;
        try{
            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(key.getBytes());
            cacheKey = bytesToHexString(messageDigest.digest());

        }catch (NoSuchAlgorithmException e){
            //e.printStackTrace();
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    /**
     * 将字节数组转换为16进制字符串形式
     * @param bytes
     * @return
     */
    private String bytesToHexString(byte[] bytes){

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < bytes.length; i++){
            String hex = Integer.toHexString(bytes[i] & 0xff);
            if(1 == hex.length()){
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }


    //判断是否包含key
    public boolean containKey(String key){
        boolean contained = false;
        DiskLruCache.Snapshot snapshot = null;
        try{
            snapshot = mDiskCache.get(key);
            contained = snapshot != null;
        }catch (IOException e){
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }
        return contained;
    }

    /**
     * 清除缓存
     */
    public void clearCache(){
        if ( BuildConfig.DEBUG ) {
            Log.d( "cache_test_DISK_", "disk cache CLEARED");
        }
        try {
            mDiskCache.delete();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public File getCacheFolder() {
        return mDiskCache.getDirectory();
    }
}
