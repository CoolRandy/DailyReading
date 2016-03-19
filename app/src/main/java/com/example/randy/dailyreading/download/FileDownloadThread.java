package com.example.randy.dailyreading.download;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by randy on 2016/2/27.
 * 自定义文件下载线程
 */
public class FileDownloadThread extends Thread {

    private static final String TAG = FileDownloadThread.class.getSimpleName();

    //当前文件是否下载完毕 标志位
    private boolean downloadComplete = false;
    //当前下载文件的长度
    private int downloadLength = 0;
    //文件保存的路径
    private File filePath;
    //文件下载的url
    private URL downloadUrl;
    //下载文件的大小或长度
    private int fileLength;
    //采用唯一的id来标识不同的下载线程
    private int threadId;

    /**
     * 因为每次需要创建不同的线程实例，所以这里采用构造器，而不是静态工厂方法
     * @param downloadUrl 文件下载的地址
     * @param filePath   文件保存的路径
     * @param fileLength 文件的长度
     * @param threadId    下载线程的id
     */
    public FileDownloadThread(URL downloadUrl, File filePath, int fileLength, int threadId){

        this.downloadUrl = downloadUrl;
        this.filePath = filePath;
        this.fileLength = fileLength;
        this.threadId = threadId;
    }

    @Override
    public void run() {
        BufferedInputStream bis = null;
        RandomAccessFile raf = null;

        try {
            URLConnection connection = downloadUrl.openConnection();
            connection.setAllowUserInteraction(true);
            //threadId从1开始
            int startPos = fileLength * (threadId - 1);
            int endPos = fileLength * threadId -1;

            //设置下载的范围
            connection.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
            connection.setRequestProperty("Charset", "UTF-8");

            bis = new BufferedInputStream(connection.getInputStream());
            raf = new RandomAccessFile(filePath, "rwd");
            //将指针定位到起始位置
            raf.seek(startPos);

            //开始读取数据流
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bis.read(buffer, 0, 1024)) != -1){
                raf.write(buffer, 0, len);
                downloadLength += len;
            }
            downloadComplete = true;
            Log.e(TAG, "current download task has completed,the total size is: " + downloadLength);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(bis != null){
                try {
                    bis.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            if(raf != null){
                try {
                    raf.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    //添加几个getter

    public boolean isDownloadComplete() {
        return downloadComplete;
    }

    public int getDownloadLength() {
        return downloadLength;
    }
}
