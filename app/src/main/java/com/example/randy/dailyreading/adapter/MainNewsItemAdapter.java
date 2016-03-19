package com.example.randy.dailyreading.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BitmapCache;
import com.android.volley.toolbox.Volley;
import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.activity.LatestContentActivity;
import com.example.randy.dailyreading.activity.MainContentActivity;
import com.example.randy.dailyreading.model.Constant;
import com.example.randy.dailyreading.model.LastedNewBean;
import com.example.randy.dailyreading.model.StoryBean;
import com.example.randy.dailyreading.util.Utils;
import com.example.randy.dailyreading.view.RippleView;
import com.example.randy.dailyreading.view.RoundImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ${randy} on 2015/10/2.
 */
public class MainNewsItemAdapter extends BaseAdapter {

    private LastedNewBean mLastedNewBean;
    private List<StoryBean> mStoryBeanList;
    private StoryBean mStoryBean;

    private Context context;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    //阅读模式标志位
    private boolean isLight;
    private static final int TOP_TOPIC = 0;
    private static final String HAVE_READ = "have_read";
    //Volley
    private RequestQueue mQueue;
    private com.android.volley.toolbox.ImageLoader volleyImageLoader;
    private com.android.volley.toolbox.ImageLoader.ImageListener imageListener;

    public MainNewsItemAdapter(Context context) {
        this.context = context;
        this.mStoryBeanList = new ArrayList<>();
        mQueue = Volley.newRequestQueue(context);
        mImageLoader = ImageLoader.getInstance();
        mOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        //TODO 设置MainActivity是否为夜间模式
        isLight = ((MainContentActivity)context).isLight();

        volleyImageLoader = new com.android.volley.toolbox.ImageLoader(mQueue, new BitmapCache());

    }

    //更新adapter
    public void setStoryBean(){
        isLight = ((MainContentActivity) context).isLight();
        notifyDataSetChanged();
    }

    //添加item到list中，刷新数据
    public void addItemToList(List<StoryBean> list){
        this.mStoryBeanList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mStoryBeanList.size();
    }

    @Override
    public Object getItem(int i) {
        return mStoryBeanList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if(null == convertView){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.main_news_item, viewGroup, false);
            holder.todayNewsTxt = (TextView)convertView.findViewById(R.id.tv_topic);
            holder.titleTxt = (TextView)convertView.findViewById(R.id.tv_title);
            holder.newsImg = (RoundImageView)convertView.findViewById(R.id.iv_title);
            holder.rippleView = (RippleView)convertView.findViewById(R.id.itemRippleView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }

        mStoryBean = mStoryBeanList.get(position);
        //渲染界面
        //对于点击过的item，也即阅读过的新闻对title做灰色处理以区别未阅读过的新闻，提升用户体验，这里采用SharedPreferences来记录读取信息
        //每一条item都有唯一的id，可以通过存取id来区别item  此处还有bug，就是当已点击过的新闻列表滑出屏幕后再滑回来又重新恢复为未点击状态
        String newsId = Utils.getPreferences(context, HAVE_READ, "");
        if(mStoryBean.isClicked()){
            //title置为灰色
            holder.titleTxt.setTextColor(context.getResources().getColor(R.color.gray_normal));
        }else{
            //未阅读过，如果为日间模式，title置黑色，为夜间模式，title置白色
            holder.titleTxt.setTextColor(context.getResources().getColor(isLight? R.color.black_color : R.color.white));
        }
        //判断item是否包含该id
        /*if(newsId.contains(mStoryBeanList.get(position).getId() + "")){
            //title置为灰色
            holder.titleTxt.setTextColor(context.getResources().getColor(R.color.gray_normal));
        }else{
            //未阅读过，如果为日间模式，title置黑色，为夜间模式，title置白色
            holder.titleTxt.setTextColor(context.getResources().getColor(isLight? R.color.black_color : R.color.white));
        }*/

        //设置整个item的背景
        ((LinearLayout)holder.titleTxt.getParent().getParent().getParent()).setBackgroundColor(context.getResources().getColor(isLight? R.color.light_news_item : R.color.dark_news_item));
        //设置今日热闻字体颜色
        holder.todayNewsTxt.setTextColor(context.getResources().getColor(isLight? R.color.light_news_topic : R.color.dark_news_topic));

        //处理方式1
        if(mStoryBean.getType() == Constant.TOPIC){
            //只显示TOPIC，通过server返回的数据来控制显示，无需客户端做隐藏处理
            ((RippleView)holder.todayNewsTxt.getParent()).setBackgroundColor(Color.TRANSPARENT);
            holder.titleTxt.setVisibility(View.GONE);
            holder.newsImg.setVisibility(View.GONE);
            holder.todayNewsTxt.setVisibility(View.VISIBLE);
            holder.todayNewsTxt.setText(mStoryBean.getTitle());
        }else{
            ((RippleView)holder.todayNewsTxt.getParent()).setBackgroundResource(isLight? R.drawable.item_background_selector_light : R.drawable.item_background_selector_dark);
            holder.todayNewsTxt.setVisibility(View.GONE);
            holder.newsImg.setVisibility(View.VISIBLE);
            holder.titleTxt.setVisibility(View.VISIBLE);
            holder.titleTxt.setText(mStoryBean.getTitle());
            //TODO 这里如果不采用UIL，即开子线程自己该如何实现？？或者说UIL是如何实现在子线程下载图片，并切换到UI线程将图片显示到ImageView上的
            //mImageLoader.displayImage(mStoryBean.getImages().get(0), holder.newsImg, mOptions);//取第一张图显示

            //TODO 采用Volley的imageLoader  有点问题：滑出屏幕再划回来会先显示ic_launcher图片，然后显示下载的图片，没有缓存下来？？
            //TODO 将下面注释的这句新建imageloader注释掉，然后移到构造方法中就可以了：分析原因：由于将item划入屏幕每次都需要新建imageloader，这样就会出现重新加载显示的过程
            //volleyImageLoader = new com.android.volley.toolbox.ImageLoader(mQueue, new BitmapCache());
            imageListener = com.android.volley.toolbox.ImageLoader.getImageListener(holder.newsImg, R.mipmap.ic_launcher, R.mipmap.ic_launcher);
            volleyImageLoader.get(mStoryBean.getImages().get(0), imageListener);
        }
        //TODO 为了使用自定义的点击波纹效果，这里将listview的item点击监听写到了适配器里
        holder.rippleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, "波纹效果", Toast.LENGTH_SHORT).show();
                //holder.titleTxt.setTextColor(context.getResources().getColor(R.color.gray_normal));
                ((TextView)view.findViewById(R.id.tv_title)).setTextColor(context.getResources().getColor(R.color.gray_normal));
                //TODO 这一块没有绑定，点击过后的item在上滑出可视区域后会恢复到未点击状态，需调整
                StoryBean storyBean = mStoryBeanList.get(position);
                if(!storyBean.isClicked()){
                    storyBean.setClicked(true);
                }
                Utils.setPreferences(context, HAVE_READ, storyBean.getId() + "");
                //Log.e("TAG", "点击item--->" + storyBean.getTitle());
                Intent intent = new Intent(context, LatestContentActivity.class);
                //intent.putExtra(Constant.START_LOCATION, startingLocation);
                intent.putExtra("storyBean", storyBean);//此处StoryBean在Activity间通信需要实现序列化
                intent.putExtra("isLight", ((MainContentActivity)context).isLight());
                context.startActivity(intent);
            }
        });

        //对于第一条数据，今日热闻显示，其他隐藏
      /*  if(position == TOP_TOPIC){
            holder.todayNewsTxt.setVisibility(View.VISIBLE);
        }else{
            holder.todayNewsTxt.setVisibility(View.GONE);
        }

        holder.titleTxt.setText(mStoryBean.getTitle());*/
        //mImageLoader.displayImage(mStoryBean.getImages(), holder.newsImg, mOptions);
        return convertView;
    }

    private class ViewHolder{
        //今日热闻
        TextView todayNewsTxt;
        //title
        TextView titleTxt;
        //picture
        RoundImageView newsImg;
        //
        RippleView rippleView;

    }
}
