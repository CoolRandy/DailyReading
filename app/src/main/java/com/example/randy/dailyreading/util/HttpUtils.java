package com.example.randy.dailyreading.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.randy.dailyreading.frame.MyApplication;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.ResponseHandlerInterface;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.List;

//import com.coolrandy.dailyreading.MyApplication;

/**
 * Created by ${randy} on 2015/9/13.
 * desc: 网络请求工具
 */
public class HttpUtils {

    private static final String NET_TAG = "net_tag";

    /**
     * get 不带参数 不带缓存时间
     *
     * @param <T>
     */
   /* public static <T> void get(String url, final Class<T> bean, final RequestCallBack callback) {
        get(url, null, bean, callback);

    }*/

    /**
     * get 带参数 不带缓存时间
     *
     * @param <T>
     */
    public static <T> void get(String url, final List<String> urlParam, final Class<T> bean, final RequestCallBack callback) {
        get(url, urlParam, bean, callback, 0);
    }

    /**
     * get 带参数 带缓存时间
     *
     * @param <T>
     */
    public static <T> void get(String url, final List<String> urlParam, final Class<T> bean, final RequestCallBack callback, long cacheTime) {
        get(url, urlParam, bean, callback, cacheTime, null);
    }

    /**
     * get 带参数 带缓存时间 带typeToken
     *
     * @param <T>
     */
    public static <T> void get(String url, final List<String> urlParam, final Class<T> bean, final RequestCallBack callback, long cacheTime,
                               final Type typeToken) {
        get(url, urlParam, bean, callback, cacheTime, typeToken, 0);
    }

    /**
     * get 带参数 带缓存时间 带typeToken 带超时时间
     *
     * @param <T>
     */
    public static <T> void get(String url, final List<String> urlParam, final Class<T> bean, final RequestCallBack callback, long cacheTime,
                               final Type typeToken, int connectTimeout) {

        try {
            Log.i(NET_TAG, "/***********************网络请求 start************************/");
            Log.i(NET_TAG, "请求方式--Get");
            //处理url参数  这里不需要额外处理
            //url = urlGetParam(urlParam, url);
            Log.i(NET_TAG, "请求Url--" + url);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i(NET_TAG, "onResponse:" + response);
                    Log.i(NET_TAG, "/***********************网络请求 end success ************************/");
                    if (typeToken != null) {
                        Object responseBeanObject = null;
                        try {
                            responseBeanObject = new Gson().fromJson(response, typeToken);
                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();
                        }
                        if (responseBeanObject == null) {
                            if (callback != null) {
                                callback.onFail(new VolleyError());
                                return;
                            }
                        }

                        if (callback != null) {
                            callback.onSuccess(responseBeanObject);
                        }
                    } else {
                        Object responseBeanObject = null;
                        try {
                            responseBeanObject = new Gson().fromJson(response, bean);
                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();
                        }

                        if (responseBeanObject == null) {
                            if (callback != null) {
                                callback.onFail(new VolleyError());
                                return;
                            }
                        }
                        if (callback != null) {
                            callback.onSuccess(responseBeanObject);
                        }
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(NET_TAG, "onErrorResponse:" + error.toString());
                    Log.i(NET_TAG, "/***********************网络请求 end fail************************/");
                    error.printStackTrace();
                    if (callback != null) {
                        callback.onFail(error);
                    }
                }
            });
            if (connectTimeout != 0 && connectTimeout != DefaultRetryPolicy.DEFAULT_TIMEOUT_MS) {// 默认10000
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(connectTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            }
            //将请求添加到队列中
            MyApplication.getmInstance().mRequestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> void get(String url,final Class<T> bean, final RequestCallBack callback, RequestQueue requestQueue, int connectTimeout){
        try {
            Log.i(NET_TAG, "/***********************网络请求 start************************/");
            Log.i(NET_TAG, "请求方式--Get");
            //处理url参数  这里不需要额外处理
            //url = urlGetParam(urlParam, url);
            Log.i(NET_TAG, "请求Url--" + url);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i(NET_TAG, "onResponse:" + response);
                    Log.i(NET_TAG, "/***********************网络请求 end success ************************/");

                    Object responseBeanObject = null;
                    try {
                        //TODO 可以看到这里采用的是Gson来解析
                        responseBeanObject = new Gson().fromJson(response, bean);
                        Log.e("TAG", "responseBeanObject : " + responseBeanObject.toString());
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }

                    if (responseBeanObject == null) {
                        if (callback != null) {
                            callback.onFail(new VolleyError());
                            return;
                        }
                    }
                    if (callback != null) {
                        callback.onSuccess(responseBeanObject);
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(NET_TAG, "onErrorResponse:" + error.toString());
                    Log.i(NET_TAG, "/***********************网络请求 end fail************************/");
                    error.printStackTrace();
                    if (callback != null) {
                        callback.onFail(error);
                    }
                }
            });
            if (connectTimeout != 0 && connectTimeout != DefaultRetryPolicy.DEFAULT_TIMEOUT_MS) {// 默认10000
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(connectTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            }
            //将请求添加到队列中  NPE
            Log.e("TAG", "请求数据成功: " + stringRequest.toString());//请求获取成功了
            //MyApplication.getmInstance().mRequestQueue.add(stringRequest);
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get 处理url参数
     *
     * @param urlParam
     * @param url
     */
    public static String urlGetParam(final List<String> urlParam, final String url) {
        if (urlParam != null && urlParam.size() > 0) {

            try {
                //采用StringBuilder速度更快，不考虑线程安全问题
                final StringBuffer sBuffer = new StringBuffer();
                final StringBuffer urlBuffer = new StringBuffer(url);
                for (int i = 0; i < urlParam.size(); i++) {
                    sBuffer.append("{" + i + "}");
                    final int start = urlBuffer.indexOf(sBuffer.toString());
                    if (start == -1) {
                        sBuffer.delete(0, sBuffer.toString().length());
                        continue;
                    }
                    final int len = sBuffer.length();
                    if (urlParam != null && urlParam.size() > i) {
                        String s = urlParam.get(i);
                        if (s != null) {
                            s = URLEncoder.encode(String.valueOf(s), "utf-8");
                            urlBuffer.replace(start, start + len, s);
                            sBuffer.delete(0, sBuffer.toString().length());
                        }
                    }
                }
                Log.i(NET_TAG, "URL Param 处理之后:" + urlBuffer.toString());
                return urlBuffer.toString();
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        return url;
    }


    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, ResponseHandlerInterface responseHandler) {
        client.get(url, responseHandler);
    }

    public static void getImage(String url, ResponseHandlerInterface responseHandler) {
        client.get(url, responseHandler);
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
}