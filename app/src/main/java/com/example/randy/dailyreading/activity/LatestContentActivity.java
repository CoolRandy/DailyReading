package com.example.randy.dailyreading.activity;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.model.Constant;
import com.example.randy.dailyreading.model.ContentBean;
import com.example.randy.dailyreading.model.StoryBean;
import com.example.randy.dailyreading.util.HttpUtils;
import com.example.randy.dailyreading.util.RequestCallBack;
import com.example.randy.dailyreading.util.UIUtil;
import com.example.randy.dailyreading.util.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

/**
 * Created by randy on 2015/10/16.
 */
public class LatestContentActivity extends AppCompatActivity {

    private AppBarLayout appBarLayout;
    private StoryBean storyBean;
    private boolean isLight;

    private WebView webView;
    private ImageView imageView;
    //接口回调
    private RequestCallBack newsCallBack;
    //实体bean
    private ContentBean contentBean;
    //网络请求队列以及imageloader配置信息   对于imageloader采用懒加载的方式处理
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.latest_content_layout);
        initView();
        initData();
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //浮动操作按钮
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Snackbar.make(v, "分享这篇文章^_^", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                showShare();
            }
        });

        //CollapsingToolbarLayout
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar_layout);
        //设置title，即ToolBar的标题
        collapsingToolbarLayout.setTitle(storyBean.getTitle());
        //设置ToolBar被滚动到顶部固定下来时的背景颜色
        collapsingToolbarLayout.setContentScrimColor(getResources().getColor(isLight? R.color.light_toolbar : R.color.dark_toolbar));
        //设置状态栏的背景色  保持和ToolBar的一致
        collapsingToolbarLayout.setStatusBarScrimColor(getResources().getColor(isLight? R.color.light_toolbar : R.color.dark_toolbar));
        //初始化WebView
        initWebview();
    }

    /**
     * 分享 TODO 这一块可以单独抽取出来
     */
    private void showShare() {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用  最多30个字符
        oks.setTitle(getString(R.string.share));
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl("http://daily.zhihu.com/story/" + storyBean.getId());  //网友点进链接后，可以看到分享的详情  redrict url
        // text是分享文本，所有平台都需要这个字段
        oks.setText(contentBean.getTitle() + "~http://daily.zhihu.com/story/" + storyBean.getId());
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片

        //网络图片的url：所有平台
        oks.setImageUrl(contentBean.getImage());//网络图片rul
        //TODO 分享到qq好友，微信好友以及朋友圈只能分享图片过去，还有问题？？已解决问题出在需要设置BypassApproval="false"
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://daily.zhihu.com/story/" + storyBean.getId());
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        //oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://daily.zhihu.com/story/" + storyBean.getId());
        // 启动分享GUI
        oks.show(this);
    }

    public void initView(){
        //设置AppBarLayout不可见
        appBarLayout = (AppBarLayout)findViewById(R.id.appbar_layout);
        //appBarLayout.setVisibility(View.INVISIBLE);
        requestQueue = Volley.newRequestQueue(this);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        imageView = (ImageView)findViewById(R.id.iv);
    }
    public void initData(){
        isLight = getIntent().getBooleanExtra("isLight", true);
        storyBean = (StoryBean)getIntent().getSerializableExtra("storyBean");
    }

    public void initWebview(){
        webView = (WebView)findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        // 开启Application Cache功能
        webView.getSettings().setAppCacheEnabled(true);

        //判断网络是否可得到，请求数据
        if(Utils.networkIsAvailable(LatestContentActivity.this)){

            newsCallBack = new RequestCallBack() {
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
                    Log.e("TAG", "contentBean--->" + contentBean.getImage());
                   /* new Thread(new Runnable() {
                        @Override
                        public void run() {
                            imageLoader.displayImage(contentBean.getImage(), imageView, options);
                        }
                    });*/
                    //UI操作应该放到UI线程
                    imageLoader.displayImage(contentBean.getImage(), imageView, options);
                    //TODO webview内容和上面的图片有些空隙，没有处理？
                    ((CoordinatorLayout)webView.getParent().getParent()).setBackgroundColor(getResources().getColor(isLight? R.color.white : R.color.color_1e3e4a));
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
                    webView.loadDataWithBaseURL("x-data://base", html, "text/html", "UTF-8", null);
                }

            };
            UIUtil.showLoadingDialog(LatestContentActivity.this);
            HttpUtils.get(Constant.CONTENT + storyBean.getId(), ContentBean.class, newsCallBack, requestQueue, 5000);
        }
        setStatusBarColor(getResources().getColor(isLight? R.color.light_toolbar : R.color.dark_toolbar));
    }

    @Override
    public void onBackPressed() {
        finish();
        //下面的方法必须位于finish后面
        overridePendingTransition(0, R.anim.slide_out_to_left_from_right);
    }

    @TargetApi(21)
    private void setStatusBarColor(int statusBarColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // If both system bars are black, we can remove these from our layout,
            // removing or shrinking the SurfaceFlinger overlay required for our views.
            Window window = this.getWindow();
            if (statusBarColor == Color.BLACK && window.getNavigationBarColor() == Color.BLACK) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            }
            window.setStatusBarColor(statusBarColor);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //结束SDK
        ShareSDK.stopSDK();
    }
}
