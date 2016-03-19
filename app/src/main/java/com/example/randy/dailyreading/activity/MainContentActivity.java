package com.example.randy.dailyreading.activity;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.fragment.MainFragment;
import com.example.randy.dailyreading.fragment.MenuFragment;
import com.example.randy.dailyreading.fragment.OtherNewsFragment;
import com.example.randy.dailyreading.util.Utils;
import com.example.randy.dailyreading.view.VpSwipeRefreshLayout;

import java.io.FileNotFoundException;

/**
 * Created by randy on 2015/10/16.
 * 实际上比较好的架构是做一个BaseActivity
 */
public class MainContentActivity extends AppCompatActivity {

    //SwipeRefreshLayout
    private VpSwipeRefreshLayout srLayout;
    //content
    private FrameLayout content;
    //DrawerLayout
    private DrawerLayout drawerLayout;
    //ToolBar
    private Toolbar toolbar;
    //阅读模式标志位 全局变量，保证获取的是最新的
    private boolean isLight;
    //沙盒存储
    private SharedPreferences sp;
    //当前fragment的id
    private String currId;
    private static final String LATEST_TAG = "latest";
    private static final String NEWS_TAG = "news";
    //首次点击back键的时间
    private long firstClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_content_layout);
        //通过SharedPreference来存储阅读模式标志
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        isLight = sp.getBoolean("isLight", true);//默认为日间模式
        Log.e("TAG", "初始时isLight==>" + isLight);
        //初始化view
        initView();
        //初始化数据，渲染界面
        initData();
        //开始请求数据，渲染界面
        //requestData();
    }

    public void initView(){
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        //根据isLight调整toolbar的颜色
        toolbar.setBackgroundColor(getResources().getColor(isLight ? R.color.light_toolbar : R.color.dark_toolbar));
        setSupportActionBar(toolbar);
        setStatusBarColor(getResources().getColor(isLight ? R.color.light_toolbar : R.color.dark_toolbar));
        //初始化SwipeRefreshLayout
        srLayout = (VpSwipeRefreshLayout)findViewById(R.id.sr_layout);
        //设置加载进度条的颜色
        srLayout.setColorSchemeResources(android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_red_dark);
        //设置下拉监听
        srLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //采用左右切换fragment的效果
                replaceFragment();
                //停止刷新
                srLayout.setRefreshing(false);
            }
        });
        //初始化content
        content = (FrameLayout)findViewById(R.id.content);
        //初始化DrawerLayout  这里会初始化调用MenuFragment
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        //采用toolbar构建一个新的ActionBarDrawerToggle，采用这个构建方法可以在点击toolbar导航键时开关抽屉
        final ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    //对类中的一些私有域做一些共有方法处理，开放访问
    //获取阅读模式标志
    public boolean isLight(){
        return isLight;
    }
    //设置id
    public void setCurrID(String id){
        currId = id;
    }
    //设置toolbar的title
    public void setToolbarTitle(String title){
        toolbar.setTitle(title);
    }
    //关闭抽屉
    public void closeDrawer(){
        drawerLayout.closeDrawers();
    }
    //使能SwipeRefresh
    public void enableSwipRefresh(boolean enable){
        srLayout.setEnabled(enable);
    }
    //刷新加载切换fragment
    //Attempt to invoke virtual method 'boolean java.lang.String.equals(java.lang.Object)' on a null object reference
    public void replaceFragment(){
        Log.e("TAG", "currId--->" + currId);
        if(currId.equals(LATEST_TAG)){
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                   .replace(R.id.content, new MainFragment(), LATEST_TAG).commit();
        }else {
            //TODO 扩展
        }

    }

    //初始化渲染界面
    public void initData(){
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                .replace(R.id.content, new MainFragment(), LATEST_TAG).commit();
        currId = LATEST_TAG;
    }
    /**
     * 网络加载请求数据，这一块是放在fragment中进行的
     */
    public void requestData(){

    }

    //设置菜单项

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //加载菜单布局
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //根据islight设置阅读模式文字显示
        menu.getItem(0).setTitle(sp.getBoolean("isLight", true)? R.string.night_mode : R.string.day_mode);
        return true;
    }

    //菜单点击监听

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //获取id
        int id = item.getItemId();
        if(R.id.action_mode == id){//如果点击的是自定义的菜单项，自行处理并返回；否则，交给超类处理
            isLight = !isLight;
            Log.e("TAG", "点击之后isLight的值为： " + isLight);
            //更新sp存储的值  这里保存有点问题？
            //sp.edit().putBoolean("isLight", isLight);
            Utils.setBooleanPreferences(MainContentActivity.this, "isLight", isLight);
            //设置title
            item.setTitle(isLight? R.string.night_mode : R.string.day_mode);
            //设置toolbar背景色  设置toolbar的颜色为白色
            toolbar.setBackgroundColor(getResources().getColor(isLight? R.color.light_toolbar : R.color.dark_toolbar));
            //toolbar.setTitleTextColor(getResources().getColor(isLight? R.color.black_color : R.color.white));
            //设置状态栏颜色  保持和toolbar一致
            setStatusBarColor(getResources().getColor(isLight? R.color.light_toolbar : R.color.dark_toolbar));
            //该应用主要包含两类新闻：一个是首页，另一个是其他新闻  更新内容页
            if(currId.equals(LATEST_TAG)){
                if(null == getSupportFragmentManager().findFragmentByTag(LATEST_TAG)){
                    new MainFragment();
                }else {
                    ((MainFragment) getSupportFragmentManager().findFragmentByTag(LATEST_TAG)).updateTheme();
                }
            }else{
                if(null == getSupportFragmentManager().findFragmentByTag(NEWS_TAG)){
                    new OtherNewsFragment();
                }else {
                    ((OtherNewsFragment) getSupportFragmentManager().findFragmentByTag(NEWS_TAG)).updateTheme();
                }
            }
            //更新侧边栏列表页
            ((MenuFragment)getSupportFragmentManager().findFragmentById(R.id.menu_fragment)).updateTheme();
            return true;
        }

        if(id == R.id.action_relax){
            //点击放松一下，跳转音乐播放界面
            Intent intent = new Intent(MainContentActivity.this, MusicPlayActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    //对状态栏的处理
    @TargetApi(21)
    private void setStatusBarColor(int statusBarColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // If both system bars are black, we can remove these from our layout,
            // removing or shrinking the SurfaceFlinger overlay required for our views.
            Window window = this.getWindow();
            if (statusBarColor == Color.BLACK &&/**/ window.getNavigationBarColor() == Color.BLACK) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            }
            window.setStatusBarColor(statusBarColor);
        }
    }

    /**
     * Android平台上捕获Back键事件有两种方式1、常规方法：直接获取按钮按下事件直接重写Activity的onKeyDown方法
     * 2、Android 2.0以后，对于Activity 可以单独获取Back键的按下事件，直接重写onBackPressed方法即可
     */
    @Override
    public void onBackPressed() {
        //如果抽屉打开，则执行关闭;否则提示两次关闭应用
        if(drawerLayout.isDrawerOpen(Gravity.LEFT)){
            closeDrawer();
        }else{
            //处理方法，第一次点击和第二次点击间隔不大于2s,则直接退出
            long secondClick = System.currentTimeMillis();
            if(secondClick - firstClick > 2000){
                Toast.makeText(this, "再点击一次退出", Toast.LENGTH_SHORT).show();
                firstClick = secondClick;
            }else{
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            Uri uri = data.getData();
            Log.e("TAG", "uri: " + uri.toString());
            ContentResolver cr = getContentResolver();
            try{
                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                //TODO 压缩处理
                ((MenuFragment)getSupportFragmentManager().findFragmentById(R.id.menu_fragment)).setImage(bitmap);
                //同时保存到sd卡或数据库


            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
