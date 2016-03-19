package com.example.randy.dailyreading.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.model.Constant;
import com.example.randy.dailyreading.model.ContentBean;
import com.example.randy.dailyreading.model.FavBean;
import com.example.randy.dailyreading.model.StoryBean;
import com.example.randy.dailyreading.util.HttpUtils;
import com.example.randy.dailyreading.util.RequestCallBack;
import com.example.randy.dailyreading.util.UIUtil;
import com.example.randy.dailyreading.util.Utils;
import com.example.randy.dailyreading.view.ScrollWebView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by randy on 2015/10/21.
 */
public class OtherNewsActivtiy extends AppCompatActivity{

    RequestQueue requestQueue;
    //回调
    private RequestCallBack requestCallBack;
    //ScrollWebView
    private ScrollWebView scrollWebView;
    //imageloader
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    //isLight
    private boolean isLight;
    //布局
    private RelativeLayout titleLayout;
    //back
    private LinearLayout backLayout;
    //storyBean
    private StoryBean storyBean;
    //ContentBean
    private ContentBean contentBean;
    //favourite
    private ImageView favourite_iv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.othernews_content_layout);

        initView();
        initData();
    }

    public void initView(){
        requestQueue = Volley.newRequestQueue(OtherNewsActivtiy.this);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        isLight = getIntent().getBooleanExtra("isLight", true);
        storyBean = (StoryBean)getIntent().getSerializableExtra("storyBean");
        //title 布局
        titleLayout = (RelativeLayout)findViewById(R.id.content_title);
        backLayout = (LinearLayout)findViewById(R.id.back_layout);
        favourite_iv = (ImageView)findViewById(R.id.icon_favourite);
        //TODO 这里对webView的使用存在内存泄露的情况
        scrollWebView = (ScrollWebView)findViewById(R.id.scrollWebview);
    }

    public void initData(){
        //初始设置title背景透明
        titleLayout.setAlpha(0);
        //back 监听
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //favourite 点击监听
        favourite_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 这里采用的是常量的方式保存收藏文章，不是恒久保存，卸载后重装会丢失，这个也正常类似未登陆的情况
                //TODO 后面添加登陆之后，对于登陆收藏的文章存放到数据库中，非登录保存在内存
                //TODO 一个小bug，跳转收藏页后，下拉刷新，会重新加载首页数据，这个是由于在MainContentActivity中每次刷新都执行replace MainFragment，改一下就好
                Log.e("TAG", "是否收藏: " + contentBean.isFavourite());
                if(contentBean.isFavourite()){
                    contentBean.setFavourite(false);//取消收藏
                    //从列表中删除相应的bean
                    for(int i = 0; i < Constant.favBeanList.size(); i++){
                        final FavBean favBean = Constant.favBeanList.get(i);
                        if(favBean.getId() == contentBean.getId()){
                            Constant.favBeanList.remove(favBean);
                        }
                        Log.e("TAG", "favBean---->" + favBean);
                    }
                }else{
                    contentBean.setFavourite(true);//添加收藏
                    FavBean favBean = new FavBean();
                    favBean.setTitle(contentBean.getTitle());
                    favBean.setId(contentBean.getId());
                    favBean.setImage(contentBean.getImage());
                    Constant.favBeanList.add(favBean);
                }

            }
        });

        //TODO 点击webview页面中的链接跳转了其他浏览器，这个体验不好，可优化
        //配置wbview信息
        //支持js，表示让java与js交互或本身希望js完成一定功能
        scrollWebView.getSettings().setJavaScriptEnabled(true);
        scrollWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        scrollWebView.getSettings().setDomStorageEnabled(true);
        scrollWebView.getSettings().setDatabaseEnabled(true);
        scrollWebView.getSettings().setAppCacheEnabled(true);

        scrollWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //将webview横向纵向的scrollbar全部禁掉，避免与ScrollView冲突  放弃了缩放的效果
        scrollWebView.setVerticalScrollBarEnabled(false);
        scrollWebView.setVerticalScrollbarOverlay(false);
        scrollWebView.setHorizontalScrollBarEnabled(false);//避免内容左右滑动
        scrollWebView.setHorizontalScrollbarOverlay(false);
        scrollWebView.getSettings().setDefaultFontSize(18);

        //设置滑动监听
        scrollWebView.setOnBoardListener(new ScrollWebView.OnBoardListener() {
            @Override
            public void onBottom() {
                Toast.makeText(OtherNewsActivtiy.this, "没有更多了^-^", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTop() {

            }

            @Override
            public void onAlphaChanged(float alpha) {
                titleLayout.setAlpha(alpha);
            }

            @Override
            public void onScrollChanged(int l, int t, int oldl, int oldt) {

            }
        });

        //请求网络数据
        if(Utils.networkIsAvailable(OtherNewsActivtiy.this)){

            requestCallBack = new RequestCallBack() {
                @Override
                public void onFail(Exception e) {
                    UIUtil.cancleLoadingDialog();
                    Log.e("TAG", "网络请求失败");
                }

                @Override
                public void onSuccess(Object o) {
                    UIUtil.cancleLoadingDialog();
                    contentBean = (ContentBean)o;
                    if(null == contentBean){
                        UIUtil.cancleLoadingDialog();
                        Log.e("TAG", "请求数据失败");
                        return;
                    }
                    if(contentBean.isFavourite()){
                        favourite_iv.setImageDrawable(getResources().getDrawable(R.drawable.collected));
                    }else{
                        favourite_iv.setImageDrawable(getResources().getDrawable(R.drawable.collect));
                    }

                    Log.e("TAG", "contentBean--->" + contentBean.getImage());
                    //推荐者绑定有问题？？？
                    // Log.e("TAG", "contentBean1--->" + contentBean.getRecommenders().get(0).getAvatar());
                    //contentLayout.setBackgroundColor(getResources().getColor(isLight? R.color.light_news_item : R.color.dark_news_item));
                    /*if(!isLight){
                        Log.e("TAG", "加载模式控制文件:夜间");
                        mWebView.loadUrl("file:///android_asset/html/mode_change.html");
                        mWebView.loadUrl("javascript:load_night()");
                    }else{
                        Log.e("TAG", "加载模式控制文件:日间");
                        mWebView.loadUrl("file:///android_asset/mode_change.html");
                        mWebView.loadUrl("javascript:load_day()");
                    }*/
                    //imageLoader.displayImage(contentBean.getImage(), iv, options);
                    //imageLoader.displayImage(contentBean.getRecommenders().get(0).getAvatar(), re_iv, options);
                    //头部内容采用css处理  关于webview的夜间模式基本解决，关键就是在css文件中对页面背景，内容，字体等进行颜色的一些处理
                    String css = "";
                    if(isLight) {
                        css = "<link rel=\"stylesheet\" href=\"file:///android_asset/css/news.css\" type=\"text/css\">";
                    }else {
                        css = "<link rel=\"stylesheet\" href=\"file:///android_asset/css/news_night.css\" type=\"text/css\">";
                    }
                    //String css = "<link rel=\"stylesheet\" href=\"{style}\" type=\"text/css\">";
                    String html = "<html><head>" + css + "</head><body>" + contentBean.getBody() + "</body></html>";
                    html = html.replace("<div class=\"img-place-holder\">", "");
                    //html = html.replace("{style}", isLight? "file:///android_asset/css/style_day.css" : "file:///android_asset/css/style_night.css");
                    //mWebView.loadUrl("javascript:MyApp.resize(document.body.getBoundingClientRect().height)");
                    scrollWebView.loadDataWithBaseURL("x-data://base", html, "text/html", "UTF-8", null);
                    //下面方法利用反射并没有实现夜间模式
                    /*try{
                        Class webSettings = getClassLoader().loadClass("android.webkit.WebSettingsClassic");
                        Method md = webSettings.getMethod("setProperty", String.class, String.class);
                        md.invoke(mWebView.getSettings(), "inverted", "true");
                        md.invoke(mWebView.getSettings(), "inverted_contrast", "1");
                    }catch(Exception e){
                        e.printStackTrace();
                    }*/
                }
            };
            UIUtil.showLoadingDialog(OtherNewsActivtiy.this);
            HttpUtils.get(Constant.CONTENT + storyBean.getId(), ContentBean.class, requestCallBack, requestQueue, 5000);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
