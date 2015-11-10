package com.example.randy.dailyreading.view;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.model.TopStoryBean;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于实现图片轮播效果
 */
public class Kanner extends FrameLayout implements View.OnClickListener{
    private int count;
    private ImageLoader mImageLoader;
    private List<ImageView> imageViews;
    //view包含图片和文字
    private List<View> views;
    private Context context;
    private ViewPager vp;
    private boolean isAutoPlay;
    private int currentItem;
    private int delayTime;
    private LinearLayout ll_dot;
    private List<ImageView> iv_dots;
    private Handler handler = new Handler();
    private DisplayImageOptions options;
    //新闻列表
    private List<TopStoryBean> topStoriesList;
    //点击图片监听
    private OnItemClickListener mOnItemClickListener;

    public Kanner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        //initImageLoader(context);
        mImageLoader = ImageLoader.getInstance();
        //设置图片缓存
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        initData();
    }

    public Kanner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Kanner(Context context) {
        this(context, null);
    }

    private void initData() {
        imageViews = new ArrayList<ImageView>();
        views = new ArrayList<>();
        iv_dots = new ArrayList<ImageView>();
        delayTime = 2000;
    }

    public void setImagesUrl(String[] imagesUrl) {
        initLayout();
        initImgFromNet(imagesUrl);
        showTime();
    }

    public void setImagesRes(int[] imagesRes) {
        initLayout();
        initImgFromRes(imagesRes);
        showTime();
    }

    //设置图片到viewPager中
    public void setTopPictures(List<TopStoryBean> topStoriesList){
        Log.e("TAG", "设置图片");
        this.topStoriesList = topStoriesList;
        initLayout();
        initNewsView();
        //showTime();
    }

    private void initLayout() {
        imageViews.clear();
        views.clear();
        View view = LayoutInflater.from(context).inflate(
                R.layout.kanner_layout, this, true);
        vp = (ViewPager) view.findViewById(R.id.vp);
        ll_dot = (LinearLayout) view.findViewById(R.id.ll_dot);
        ll_dot.removeAllViews();
    }

    private void initImgFromRes(int[] imagesRes) {
        count = imagesRes.length;
        for (int i = 0; i < count; i++) {
            ImageView iv_dot = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 5;
            params.rightMargin = 5;
            iv_dot.setImageResource(R.mipmap.dot_blur);
            ll_dot.addView(iv_dot, params);
            iv_dots.add(iv_dot);
        }
        iv_dots.get(0).setImageResource(R.mipmap.dot_focus);

        for (int i = 0; i <= count + 1; i++) {
            ImageView iv = new ImageView(context);
            iv.setScaleType(ScaleType.FIT_XY);
//            iv.setBackgroundResource(R.drawable.loading);
            if (i == 0) {
                iv.setImageResource(imagesRes[count - 1]);
            } else if (i == count + 1) {
                iv.setImageResource(imagesRes[0]);
            } else {
                iv.setImageResource(imagesRes[i - 1]);
            }
            imageViews.add(iv);
        }
    }

    //初始化view
    public void initNewsView(){

        //采用动态添加的方式加入指示点
        int length = topStoriesList.size();
        for(int i = 0; i < length; i++){//动态添加
            ImageView iv_dot = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.leftMargin = 5;
            params.rightMargin = 5;
            iv_dot.setImageResource(R.mipmap.dot_blur);
            ll_dot.addView(iv_dot, params);
            iv_dots.add(iv_dot);
        }
        iv_dots.get(0).setImageResource(R.mipmap.dot_focus);
        for(int j = 0; j <= length + 1; j++){
            //kanner内容
            View content = LayoutInflater.from(context).inflate(R.layout.kanner_content_layout, null);
            ImageView imageView = (ImageView)content.findViewById(R.id.top_iv);
            TextView textView = (TextView)content.findViewById(R.id.top_tv);

            imageView.setScaleType(ScaleType.CENTER_CROP);
            if(0 == j){
                mImageLoader.displayImage(topStoriesList.get(length-1).getImage(), imageView, options);
                textView.setText(topStoriesList.get(length - 1).getTitle());
            }else if(length + 1 == j){
                mImageLoader.displayImage(topStoriesList.get(0).getImage(), imageView, options);
                textView.setText(topStoriesList.get(0).getTitle());
            }else{
                mImageLoader.displayImage(topStoriesList.get(j-1).getImage(), imageView, options);
                textView.setText(topStoriesList.get(j - 1).getTitle());
            }
            content.setOnClickListener(this);
            views.add(content);
        }
        vp.setAdapter(new MyPagerAdapter());
        vp.setFocusable(true);
        vp.setCurrentItem(1);
        currentItem = 1;
        vp.addOnPageChangeListener(new MyOnPageChangeListener());
        startPlay();
    }

    private void initImgFromNet(String[] imagesUrl) {
        count = imagesUrl.length;
        for (int i = 0; i < count; i++) {
            ImageView iv_dot = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 5;
            params.rightMargin = 5;
            iv_dot.setImageResource(R.mipmap.dot_blur);
            ll_dot.addView(iv_dot, params);
            iv_dots.add(iv_dot);
        }
        iv_dots.get(0).setImageResource(R.mipmap.dot_focus);

        for (int i = 0; i <= count + 1; i++) {
            ImageView iv = new ImageView(context);
            iv.setScaleType(ScaleType.FIT_XY);
//            iv.setBackgroundResource(R.mipmap.loading);
            if (i == 0) {
                mImageLoader.displayImage(imagesUrl[count - 1], iv, options);
            } else if (i == count + 1) {
                mImageLoader.displayImage(imagesUrl[0], iv, options);
            } else {
                mImageLoader.displayImage(imagesUrl[i - 1], iv, options);
            }
            imageViews.add(iv);
        }

    }

    private void showTime() {
        vp.setAdapter(new KannerPagerAdapter());
        vp.setFocusable(true);
        vp.setCurrentItem(1);
        currentItem = 1;
        vp.addOnPageChangeListener(new MyOnPageChangeListener());
        startPlay();
    }

    private void startPlay() {
        isAutoPlay = true;
        handler.postDelayed(task, 2000);
    }

    public void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs().build();
        ImageLoader.getInstance().init(config);
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        mImageLoader = ImageLoader.getInstance();
    }

   private final Runnable task = new Runnable() {

       @Override
       public void run() {
           if (isAutoPlay) {
               currentItem = currentItem % (topStoriesList.size() + 1) + 1;
               if (currentItem == 1) {
                   vp.setCurrentItem(currentItem, false);
                   handler.post(task);
               } else {
                   vp.setCurrentItem(currentItem);
                   handler.postDelayed(task, 5000);
               }
           } else {
               handler.postDelayed(task, 5000);
           }
       }
   };

    class MyPagerAdapter extends PagerAdapter{

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            //return super.instantiateItem(container, position);
            container.addView(views.get(position));
            return views.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //super.destroyItem(container, position, object);//自定义删除item
            container.removeView((View)object);
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    class KannerPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return imageViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(imageViews.get(position));
            return imageViews.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(imageViews.get(position));
        }

    }

    class MyOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            switch (arg0) {
                case ViewPager.SCROLL_STATE_IDLE:
                    //当前页面处于空闲状态 Indicates that the pager is in an idle, settled state. The current page is fully in view and no animation is in progress.
                    if(0 == vp.getCurrentItem()){
                        vp.setCurrentItem(topStoriesList.size(), false);//立即过渡
                    }else if(topStoriesList.size() + 1 == vp.getCurrentItem()){
                        vp.setCurrentItem(1, false);
                    }
                    currentItem = vp.getCurrentItem();
                    isAutoPlay = true;
                    break;
                case ViewPager.SCROLL_STATE_DRAGGING:
                    //当前页面处于拖拽状态，设置自动播放为false
                    isAutoPlay = false;
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    //表明pager处于设置为最终位置的过程，可以设置自动播放为true
                    isAutoPlay = true;
                    break;
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            for (int i = 0; i < iv_dots.size(); i++) {
                if (i == arg0 - 1) {
                    iv_dots.get(i).setImageResource(R.mipmap.dot_focus);
                } else {
                    iv_dots.get(i).setImageResource(R.mipmap.dot_blur);
                }
            }
        }

    }

    @Override
    public void onClick(View view) {

        if(mOnItemClickListener != null){
            TopStoryBean topStoryBean = topStoriesList.get(vp.getCurrentItem() - 1);
            mOnItemClickListener.click(view, topStoryBean);
        }
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener){
        this.mOnItemClickListener = mOnItemClickListener;
    }
    //定义一个item点击监听接口，用于响应图片点击事件，在其他类中实现该接口
    public interface OnItemClickListener {
        public void click(View v, TopStoryBean topStoryBean);
    }
}
