package com.example.randy.dailyreading.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.lang.reflect.Method;

/**
 * Created by randy on 2015/11/4.
 * 关于网络相关的工具类  需要添加网络权限 <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
 */
public class NetWork {


    private static final String TAG = NetWork.class.getSimpleName();

    //采用枚举分类不同的网络类型
    public enum NetType{

        None(1),
        Mobile(2),
        Wifi(4),
        Other(8);

        NetType(int value){
            this.value = value;
        }
        public int value;
    }

    /**
     * 获取ConnectivityManager
     */
    public static ConnectivityManager getConnManager(Context context){
        return (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }


    /**
     * 判断网络连接状态
     * @param context
     * @return
     */
    public static boolean isConnected(Context context){
        NetworkInfo networkInfo = getConnManager(context).getActiveNetworkInfo();
        /*if(null != networkInfo){
            return networkInfo.isAvailable();
        }*/
        return networkInfo != null && networkInfo.isAvailable();
    }

    /**
     * 判断当前是否有网络正在连接或已连接
     * @param context
     * @return
     */
    public static boolean isConnectedOrConnecting(Context context){
        //获取设备支持的所有网络类型的连接信息 This method does not support multiple connected networks of the same type
        NetworkInfo[] infos = getConnManager(context).getAllNetworkInfo();
        if(null != infos){
            for(NetworkInfo info: infos){
                if(info.isConnectedOrConnecting()){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取网络连接类型
     */
    public static NetType getConnectedType(Context context){
        NetworkInfo net = getConnManager(context).getActiveNetworkInfo();
        if(null != net){
            switch (net.getType()){
                case ConnectivityManager.TYPE_WIFI:
                    return NetType.Wifi;
                case ConnectivityManager.TYPE_MOBILE:
                    return NetType.Mobile;
                default:
                    return NetType.Other;
            }
        }
        return NetType.None;
    }

    /**
     * 是否存在有效的wifi连接
     */
    public static boolean isWifiConnected(Context context){
        NetworkInfo networkInfo = getConnManager(context).getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected();
    }

    /**
     * 是否存在有效的移动连接
     * @param context
     * @return boolean
     */
    public static boolean isMobileConnected(Context context) {
        NetworkInfo net = getConnManager(context).getActiveNetworkInfo();
        return net != null && net.getType() == ConnectivityManager.TYPE_MOBILE && net.isConnected();
    }

    /**
     * 检测网络是否为可用状态
     */
    public static boolean isAvailable(Context context) {
        return isWifiAvailable(context) || (isMobileAvailable(context) && isMobileEnabled(context));
    }

    /**
     * 判断是否有可用状态的Wifi，以下情况返回false：
     *  1. 设备wifi开关关掉;
     *  2. 已经打开飞行模式；
     *  3. 设备所在区域没有信号覆盖；
     *  4. 设备在漫游区域，且关闭了网络漫游。
     *
     * @param context
     * @return boolean wifi为可用状态（不一定成功连接，即Connected）即返回ture
     */
    public static boolean isWifiAvailable(Context context) {
        NetworkInfo[] nets = getConnManager(context).getAllNetworkInfo();
        if (nets != null) {
            for (NetworkInfo net : nets) {
                if (net.getType() == ConnectivityManager.TYPE_WIFI) { return net.isAvailable(); }
            }
        }
        return false;
    }

    /**
     * 判断有无可用状态的移动网络，注意关掉设备移动网络直接不影响此函数。
     * 也就是即使关掉移动网络，那么移动网络也可能是可用的(彩信等服务)，即返回true。
     * 以下情况它是不可用的，将返回false：
     *  1. 设备打开飞行模式；
     *  2. 设备所在区域没有信号覆盖；
     *  3. 设备在漫游区域，且关闭了网络漫游。
     *
     * @param context
     * @return boolean
     */
    public static boolean isMobileAvailable(Context context) {
        NetworkInfo[] nets = getConnManager(context).getAllNetworkInfo();
        if (nets != null) {
            for (NetworkInfo net : nets) {
                if (net.getType() == ConnectivityManager.TYPE_MOBILE) { return net.isAvailable(); }
            }
        }
        return false;
    }

    /**
     * 设备是否打开移动网络开关
     * @param context
     * @return boolean 打开移动网络返回true，反之false
     */
    public static boolean isMobileEnabled(Context context) {
        try {
            Method getMobileDataEnabledMethod = ConnectivityManager.class.getDeclaredMethod("getMobileDataEnabled");
            getMobileDataEnabledMethod.setAccessible(true);
            return (Boolean) getMobileDataEnabledMethod.invoke(getConnManager(context));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 反射失败，默认开启
        return true;
    }




}
