package com.example.randy.dailyreading.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.activity.LatestContentActivity;
import com.example.randy.dailyreading.activity.MainContentActivity;
import com.example.randy.dailyreading.adapter.MainNewsItemAdapter;
import com.example.randy.dailyreading.model.BeforeNewBean;
import com.example.randy.dailyreading.model.Constant;
import com.example.randy.dailyreading.model.LastedNewBean;
import com.example.randy.dailyreading.model.StoryBean;
import com.example.randy.dailyreading.model.TopStoryBean;
import com.example.randy.dailyreading.util.HttpUtils;
import com.example.randy.dailyreading.util.RequestCallBack;
import com.example.randy.dailyreading.util.UIUtil;
import com.example.randy.dailyreading.util.Utils;
import com.example.randy.dailyreading.view.Kanner;
import com.google.gson.Gson;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.util.List;

/**
 * Created by randy on 2015/10/16.
 * 新闻主界面: 包含顶部的轮播图片以及下面的新闻列表  Kanner采用动态添加的方式
 * 关于图片轮播器采用两种方式实现
 * 点击新闻item跳转LatestContentActivity
 */
public class MainFragment extends BaseFragment {

    //接口回调
    private RequestCallBack callBack;
    private RequestQueue requestQueue;
    //listview
    private ListView listView;
    //Kanner
    private Kanner kanner;
    //适配器
    private MainNewsItemAdapter mainNewsItemAdapter;
    //LastedNewBean
    private LastedNewBean mLastedNewBean;
    //获取新闻的日期
    private String date;
    //正在加载标志位
    private boolean isLoading = false;
    //Handler
    private Handler handler = new Handler();
    //BeforeNewBean
    private BeforeNewBean mBeforeNewBean;
    //gotop
    private ImageView gotop_iv;
    //headerview的高度
    private int headerHeight;

    private String topstoryString;

    enum PictureType {
        KANNER, BANNDER
    }

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_news_layout, container, false);
        requestQueue = Volley.newRequestQueue(mActivity);
        gotop_iv = (ImageView)view.findViewById(R.id.image_gotop);
        listView = (ListView) view.findViewById(R.id.listview);
        //设置图片轮播layout
        initKanner(inflater);
        //初始化适配器
        mainNewsItemAdapter = new MainNewsItemAdapter(mActivity);
        listView.setAdapter(mainNewsItemAdapter);

        //listview设置滑动监听
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if(getNewScrollY() > listView.getChildAt(0).getHeight()){
                    //如果垂直滑动距离超过一个item的高度，让gotop图标显示
                    gotop_iv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (listView != null && listView.getChildCount() > 0) {
                    //首先判断listview不为null且子view数大于0
                    //然后根据第一个可见的item是否为第0个等来判断listview是否滑到顶部
                    boolean enable = (0 == firstVisibleItem) && (0 == view.getChildAt(firstVisibleItem).getTop());
                    if(0 == firstVisibleItem){//TODO 考虑到listview中还动态添加了haderView,同时也计算headerview的高度
                        gotop_iv.setVisibility(View.GONE);
                    }
                    //使能下拉刷新
                    ((MainContentActivity) mActivity).enableSwipRefresh(enable);

                    if (firstVisibleItem + visibleItemCount == totalItemCount && !isLoading) {
                        Log.e("TAG", "上拉加载自动刷新:" + date);
                        loadMore(Constant.BEFORE + date);//上拉加载
                    }

                }
            }
        });
        return view;
    }

    //TODO 获取listview滑动的垂直高度
    public int getNewScrollY(){
        View view = listView.getChildAt(0);//获取第一个item
        if(null == view) {
            return 0;
        }
        //获取第一个可视item的位置 position的值为0、1、2。。。。
        int firstVisibleItemPos = listView.getFirstVisiblePosition();
        //Top position of this view relative to its parent
        int top = view.getTop();
        if(0 == firstVisibleItemPos){
            return top;
        }else {
            return -top + firstVisibleItemPos * view.getHeight();
        }
    }

    public void initKanner(final LayoutInflater inflater) {
        View headerView = inflater.inflate(R.layout.kanner, listView, false);
        headerHeight = headerView.getHeight();
        kanner = (Kanner) headerView.findViewById(R.id.kanner_header);
        //监听
        kanner.setOnItemClickListener(new Kanner.OnItemClickListener() {
            @Override
            public void click(View v, TopStoryBean topStoryBean) {
                //TODO 这里由于TopStoryBean和StoryBean的内容格式是一样的，所以新闻详情的显示也设计为一样
                //即跳转LatestContentActivity，所以这里获取TopStoryBean的id和Ttitle来重新设置storyBean
                StoryBean storyBean = new StoryBean();
                storyBean.setId(topStoryBean.getId());
                storyBean.setTitle(topStoryBean.getTitle());
                Intent intent = new Intent(mActivity, LatestContentActivity.class);
                intent.putExtra("storyBean", storyBean);
                intent.putExtra("isLight", ((MainContentActivity) mActivity).isLight());
                //跳转
                startActivity(intent);
            }
        });
        kanner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){

                    case MotionEvent.ACTION_MOVE:
                        ((MainContentActivity)getActivity()).getSrLayout().setEnabled(false);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        ((MainContentActivity)getActivity()).getSrLayout().setEnabled(true);
                        break;
                }
                return false;
            }
        });
        //将headerView添加进listview
        listView.addHeaderView(headerView);
    }

    @Override
    protected void initData() {
        super.initData();
        //监听回到顶部的操作
        gotop_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] locations = new int[2];
                v.getLocationOnScreen(locations);
                int x = locations[0];
                int y = locations[1];
                Log.e("TAG", "x = " + x + " y = " + y);
                //listView.scrollTo(x, getNewScrollY());
                //TODO 这一块采用scrollTo滑动还有问题？采用setSelection正常
                //TODO scrollTo的效果是针对可视屏幕内的控件或视图，作相对可视屏幕大小的一个移动
                /*int width = listView.getWidth();
                int height = listView.getHeight();
                listView.scrollTo(0, height/2);*/
                //如果这里直接调用setSelection(0)则会瞬间回到顶部，没有滚动的效果，用户体验不好
                //listView.setSelection(0);
                //scrollToListviewTop(listView);
                /**
                 * Smoothly scroll to the specified adapter position. The view will
                 * scroll such that the indicated position is displayed.
                 * @param position Scroll to this adapter position.
                 */
                listView.smoothScrollToPosition(0);

                //TODO 采用smoothScrollBy  这个方法滑动还有问题,点击按钮不能直接滑到顶部
                //TODO 有可能跟headerView有关！！需要点击一次才能回到顶部，好像跟滑动几次有关？？
                //listView.smoothScrollBy(-(getNewScrollY() + headerHeight), 500);
                gotop_iv.setVisibility(View.GONE);
            }
        });
        callBack = new RequestCallBack() {
            @Override
            public void onFail(Exception e) {

                UIUtil.cancleLoadingDialog();
                Log.e("TAG", "请求网络失败");
            }

            @Override
            public void onSuccess(Object o) {
                //请求数据成功取消加载动画
                UIUtil.cancleLoadingDialog();
                mLastedNewBean = (LastedNewBean) o;

                if (null == mLastedNewBean) {
                    UIUtil.cancleLoadingDialog();
                    Log.e("TAG", "请求数据为空");
                    return;
                }
                date = mLastedNewBean.getDate();
                Log.e("TAG", "mLastedNewBean--->" + mLastedNewBean.getDate());
                Log.e("TAG", "请求数据成功");
                //将图片设置到kanner中,并缓存到文件中
                //Constant.topStoryBeans = mLastedNewBean.getTop_stories();
                try {
                    String list = Utils.BeanListToString(mLastedNewBean.getTop_stories());
                    Utils.setPreferences(mActivity, "topStoryList", list);
                    kanner.setTopPictures(mLastedNewBean.getTop_stories());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                List<StoryBean> storyBeanList = mLastedNewBean.getStories();
                StoryBean topic = new StoryBean();
                topic.setType(Constant.TOPIC);
                topic.setTitle("今日热闻");
                storyBeanList.add(0, topic);
                //这种将数据保存在常量中不行，每次app退出后都会清空
                //Constant.storyBeanList = storyBeanList;
                try{
                    String storyList = Utils.BeanListToString(storyBeanList);
                    Utils.setPreferences(mActivity, "storyList", storyList);
                }catch (Exception e){
                    e.printStackTrace();
                }
                //mListView.setAdapter(new NewsItemAdapter(mActivity, storyBeanList));
                mainNewsItemAdapter.addItemToList(storyBeanList);
                isLoading = false;
            }
        };
        if(Constant.HAS_NETWORK) {//有网络的情况下，每次都重新请求接口刷新一下数据
            //加载动画
            UIUtil.showLoadingDialog(getActivity());
            HttpUtils.get(Constant.LATESTNEWS, LastedNewBean.class, callBack, requestQueue, 50000);
        }else {//处理当没有网络时，从缓存文件中离线加载数据
           /* if(null != Constant.topStoryBeans){
                kanner.setTopPictures(Constant.topStoryBeans);
            }*/
            try{
                List<TopStoryBean> topStoryList = (List<TopStoryBean>)Utils.StringToBeanList(Utils.getPreferences(mActivity, "topStoryList", ""));
                if(topStoryList != null) {
                    kanner.setTopPictures(topStoryList);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            if(null != Constant.storyBeanList) {
                try {
                    List<StoryBean> storyBeanList = (List<StoryBean>) Utils.StringToBeanList(Utils.getPreferences(mActivity, "storyList", ""));
                    mainNewsItemAdapter.addItemToList(storyBeanList);
                    //mainNewsItemAdapter.addItemToList(Constant.storyBeanList);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        }

    }

    //TODO 滑动效果改进  效果不好
    public static void scrollToListviewTop(final AbsListView listView)
    {
        //listView.smoothScrollToPosition(0);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (listView.getFirstVisiblePosition() > 0) {
                    //listView.smoothScrollToPosition(0);
                    listView.setSelection(0);
                  /*  try {
                        Thread.sleep(300);
                    }catch (Exception e){
                        e.printStackTrace();
                    }*/
                    handler.postDelayed(this, 100);
                }
            }
        }, 500);
    }

    //格式化日期
    public String formatDate(String date){

        StringBuffer buffer = new StringBuffer();
        buffer.append(date.substring(0, 4)).append("年").append(date.substring(4, 6))
                .append("月").append(date.substring(6, 8)).append("日");
        return buffer.toString();
    }

    /**
     * 上拉加载更多过往新闻内容
     */
    public void loadMore(String url) {
        isLoading = true;
        //采用AnyncHttp
        HttpUtils.get(url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseBody) {
                //super.onSuccess(statusCode, headers, responseBody);
                Log.e("TAG", "返回的数据为: " + responseBody);
                Gson gson = new Gson();
                mBeforeNewBean = gson.fromJson(responseBody, BeforeNewBean.class);
                if (null == mBeforeNewBean) {
                    Log.e("TAG", "请求数据为空");
                    isLoading = false;
                    return;
                }
                date = mBeforeNewBean.getDate();
                Log.e("TAG", "当前新闻日期为:" + date);//20151002
                //下面的代码出现NPE？？？
                //mKanner.setTopPictures(mLastedNewBean.getTop_stories());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        List<StoryBean> storiesEntities = mBeforeNewBean.getStories();
                        Log.e("TAG", "storiesEntities--->" + storiesEntities);
                        StoryBean topic = new StoryBean();
                        topic.setType(Constant.TOPIC);
                        topic.setTitle(formatDate(date));
                        storiesEntities.add(0, topic);
                        //mListView.setAdapter(new NewsItemAdapter(mActivity, storiesEntities));
                        mainNewsItemAdapter.addItemToList(storiesEntities);
                        isLoading = false;
                    }
                });
            }
        });
    }

    public void updateTheme(){
        mainNewsItemAdapter.setStoryBean();
    }
}
