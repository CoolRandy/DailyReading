package com.example.randy.dailyreading.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.randy.dailyreading.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by randy on 2015/12/12.
 */
public class HttpActivity extends Activity {

    private EditText nameTxt;
    private EditText pwdTxt;
    private Button button1;
    private Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.http_activity_layout);
        nameTxt = (EditText)findViewById(R.id.nameText);
        pwdTxt = (EditText)findViewById(R.id.pwdText);
        button1 = (Button)findViewById(R.id.button1);
        button2 = (Button)findViewById(R.id.button2);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameTxt.getText().toString();
                String pwd = pwdTxt.getText().toString();

                // 运行线程，使用GET方法向本地服务器发送数据
                GetThread getThread = new GetThread(name, pwd);
                getThread.start();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameTxt.getText().toString();
                String pwd = pwdTxt.getText().toString();

                // 运行线程，使用GET方法向本地服务器发送数据
                PostThread postThread = new PostThread(name, pwd);
                postThread.start();
            }
        });

    }

    /**
     * 采用get请求方法请求数据
     */
    class GetThread extends Thread{

        String name;
        String pwd;

        public GetThread(String name, String pwd){
            this.name = name;
            this.pwd = pwd;
        }

        @Override
        public void run() {
            //创建httpclient对象
            HttpClient httpClient = new DefaultHttpClient();
            //请求的url
            String url = "http://10.210.43.100:8080/test.jsp?name=" + name + "&password=" + pwd;
            //创建代表请求的对象
            HttpGet httpGet = new HttpGet(url);
            try{
                //执行httpGet请求，获取响应
                HttpResponse httpResponse = httpClient.execute(httpGet);
                //检查状态码
                if(200 == httpResponse.getStatusLine().getStatusCode()){
                    //从相应对象中取出数据，存放到entity中
                    HttpEntity httpEntity = httpResponse.getEntity();
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
                    String result = br.readLine();
                    Log.e("TAG", "get result: " + result);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 采用post方法请求数据
     */
    class PostThread extends Thread{

        String name;
        String pwd;

        public PostThread(String name, String pwd){
            this.name = name;
            this.pwd = pwd;
        }

        @Override
        public void run() {
            HttpClient httpClient = new DefaultHttpClient();
            String url = "http://10.210.43.100:8080/test.jsp";
            //生成post方法请求对象
            HttpPost httpPost = new HttpPost(url);
            //NameValuePaire对象代表发往服务器的键值对
            NameValuePair pair1 = new BasicNameValuePair("name", name);
            NameValuePair pair2 = new BasicNameValuePair("password", pwd);
            //将键值对放入到一个list中
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(pair1);
            pairs.add(pair2);
            //
            try{
                //创建代表请求体的对象
                HttpEntity httpEntity = new UrlEncodedFormEntity(pairs);
                //将请求体方锐请求对象中
                httpPost.setEntity(httpEntity);
                //执行请求对象
                try{
                    //获取server发回的响应对象
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    //检查状态码
                    if(200 == httpResponse.getStatusLine().getStatusCode()){
                        HttpEntity entity = httpResponse.getEntity();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
                        String result = reader.readLine();
                        Log.e("TAG", "post result: " + result);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
        }
    }
}
