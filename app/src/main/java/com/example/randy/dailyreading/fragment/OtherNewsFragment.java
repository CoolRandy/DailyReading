package com.example.randy.dailyreading.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.activity.MainContentActivity;
import com.example.randy.dailyreading.activity.OtherNewsActivtiy;
import com.example.randy.dailyreading.adapter.NewsItemAdapter;
import com.example.randy.dailyreading.model.Constant;
import com.example.randy.dailyreading.model.NewsBean;
import com.example.randy.dailyreading.model.StoryBean;
import com.example.randy.dailyreading.util.HttpUtils;
import com.example.randy.dailyreading.util.RequestCallBack;
import com.example.randy.dailyreading.util.UIUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by randy on 2015/10/18.
 * 点击新闻item，跳转OtherNewsActivity
 */
public class OtherNewsFragment extends BaseFragment {

    private RequestQueue mRequestQueue;
    private ListView listView;
    private RequestCallBack mRequestCallBack;
    //关于imageloader
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    //顶部图片和文字
    private ImageView iv_title;
    private TextView tv_title;
    //主题日报的id 表明是菜单栏的哪一项
    private int id;
    //
    private NewsBean newsBean;
    private List<StoryBean> storyBeanList;
    private Handler handler;
    private NewsItemAdapter newsItemAdapter;
    private boolean isLoading = false;

    public OtherNewsFragment() {

    }
    //TODO 这里采用fragment实例的方法传递参数，而不是采用构造方法，是为了避免设备横竖屏切换后数据丢失
    public static Fragment newInstance(int arg){
        OtherNewsFragment fragment = new OtherNewsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("itemId", arg);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_layout, container, false);
        id = getArguments().getInt("itemId");
        Log.e("TAG", "相应新闻类型ID： " + id);
        mRequestQueue = Volley.newRequestQueue(mActivity);//创建请求队列
        //配置下载信息
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        listView = (ListView)view.findViewById(R.id.lv_news);

        View headerView = inflater.inflate(R.layout.news_header, listView, false);

        iv_title = (ImageView) headerView.findViewById(R.id.iv_title);
        tv_title = (TextView) headerView.findViewById(R.id.tv_title);
        listView.addHeaderView(headerView);
        //添加点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //这里注意由于headview占据了第一个position，所以需要减一
                position = position - 1;
                StoryBean storyBean = storyBeanList.get(position);
                if(!storyBean.isClicked()){
                    storyBean.setClicked(true);
                    newsItemAdapter.setStoryBean();
                }
                //Utils.setPreferences(context, HAVE_READ, storyBean.getId() + "");
                //Log.e("TAG", "点击item--->" + storyBean.getTitle());
                Intent intent = new Intent(mActivity, OtherNewsActivtiy.class);
                //intent.putExtra(Constant.START_LOCATION, startingLocation);
                intent.putExtra("storyBean", storyBean);//此处StoryBean在Activity间通信需要实现序列化
                intent.putExtra("isLight", ((MainContentActivity) mActivity).isLight());
                mActivity.startActivity(intent);
            }
        });
        //滚动事件
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (listView != null && listView.getChildCount() > 0) {
                    //首先判断listview不为null且子view数大于0
                    //然后根据第一个可见的item是否为第0个等来判断listview是否滑到顶部
                    boolean enable = (0 == firstVisibleItem) && (0 == absListView.getChildAt(firstVisibleItem).getTop());
                    //使能下拉刷新
                    ((MainContentActivity) mActivity).enableSwipRefresh(enable);
                    //下拉加载
                    /*if (firstVisibleItem + visibleItemCount == totalItemCount && !isLoading) {
                        Log.e("TAG", "上拉加载自动刷新:" + date);
                        loadMore(Constant.BEFORE + date);//上拉加载
                    }*/
                }
            }
        });
        return view;
    }

    @Override
    protected void initData() {
        super.initData();
        handler = new Handler();
        mRequestQueue = Volley.newRequestQueue(getActivity());

        mRequestCallBack = new RequestCallBack() {
            @Override
            public void onFail(Exception e) {
                UIUtil.cancleLoadingDialog();
                Toast.makeText(getActivity(), "网络连接失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(Object o) {
                UIUtil.cancleLoadingDialog();
                newsBean = (NewsBean)o;
                if(null == newsBean){
                    UIUtil.cancleLoadingDialog();
                    Toast.makeText(getActivity(), "请求数据失败", Toast.LENGTH_SHORT).show();
                }
                storyBeanList = newsBean.getStories();
                tv_title.setText(newsBean.getDescription());

                imageLoader.displayImage(newsBean.getImage(), iv_title, options);

                handler.post(new Runnable() {
                    @Override
                    public void run() {//适配数据
                        newsItemAdapter = new NewsItemAdapter(mActivity, storyBeanList);
                        listView.setAdapter(newsItemAdapter);
                    }
                });
            }
        };
        //加载动画
        UIUtil.showLoadingDialog(mActivity);
        HttpUtils.get(Constant.THEMENEWS + id, NewsBean.class, mRequestCallBack, mRequestQueue, 50000);
    }

    public void updateTheme(){
        newsItemAdapter.setStoryBean();
    }
}
