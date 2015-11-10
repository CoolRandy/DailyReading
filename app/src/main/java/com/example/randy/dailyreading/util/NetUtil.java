package com.example.randy.dailyreading.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * Created by randy on 2015/11/3.
 * 自定义网络请求 这里采用URLConnection
 */
public class NetUtil {

    /**
     * 向指定的URL发送get请求  从server获取一份文档
     * @param url  发送请求的url
     * @param params  请求参数，请求参数应该是name1=value1&name2=value2
     * @return  URL所代表远程资源的响应
     */
    public static String sendGetRequest(String url, String params){

        String result = "";
        BufferedReader in = null;

        try{
            String urlName = url + "?" + params;
            //将url字符串封装成URL类型
            URL realUrl = new URL(urlName);
            //打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            //设置一些通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            //User-Agent:Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36
            //conn.setRequestProperty("user-agent", "");

            //建立实际连接
            conn.connect();
            //获取所有响应头字段
            Map<String, List<String>> map = conn.getHeaderFields();
            //遍历所有的头字段
            for (String key: map.keySet()){
                Log.d("Headers--->", map.get(key) + "");
            }

            //定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            //读取输入流
            String line;
            while((line = in.readLine()) != null){
                result += "\n" + line;
            }
            return result;
        }catch (MalformedURLException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }finally {//使用finally块来关闭输入流
            try{
                if(null != in){
                    in.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 向指定的URL发送POST请求 向服务器发送需要处理的数据
     * @param url 发送请求的url
     * @param params 请求参数，请求参数应该是name1=value1&name2=value2
     * @return URL所代表远程资源的响应
     */
    public static String sendPostRequest(String url, String params){
        String result = "";
        PrintWriter out = null;
        BufferedReader in = null;

        try{
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            //设置一些通用的请求属性
            conn.setRequestProperty("accept", "*/*");//告知server，client会接受与其请求相符的任意媒体类型
            conn.setRequestProperty("connection", "Keep-Alive");//keep-alive首部请求将一条连接保持在打开状态，如果server愿意为下一条连接保持在打开状态，就在响应中保持相同的首部
            //User-Agent:Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36
            //conn.setRequestProperty("user-agent", "");

            //发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);

            //获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            //发送请求参数
            out.print(params);
            //flush输出流的缓冲
            out.flush();

            //定义BufferedReader输入流来读取URL响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while((line = in.readLine()) != null){
                result += "\n" + line;
            }
            return result;
        }catch (MalformedURLException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                if(null != out){
                    out.close();
                }
                if(null != in){
                    in.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return result;
    }

    //如果采用传统的JSONObject来解析json数据，代码量比较繁琐
}
