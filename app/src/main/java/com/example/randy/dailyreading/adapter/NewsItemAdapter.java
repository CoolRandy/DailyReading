package com.example.randy.dailyreading.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.activity.MainContentActivity;
import com.example.randy.dailyreading.model.LastedNewBean;
import com.example.randy.dailyreading.model.StoryBean;
import com.example.randy.dailyreading.util.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by ${randy} on 2015/10/3.
 * 新闻适配器，不显示今日热闻字样
 */
public class NewsItemAdapter extends BaseAdapter {

    //StoryBean
    private LastedNewBean mLastedNewBean;
    private List<StoryBean> mStoryBeanList;
    private StoryBean mStoryBean;
    //
    private Context context;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    private boolean isLight;
    private static final String HAVE_READ = "have_read";

    public NewsItemAdapter(Context context, List<StoryBean> stories) {

        this.context = context;
        mStoryBeanList = stories;
        mImageLoader = ImageLoader.getInstance();
        mOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        isLight = ((MainContentActivity) context).isLight();
    }

    //更新adapter
    public void setStoryBean(){

        isLight = ((MainContentActivity) context).isLight();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(null == convertView){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.news_item, parent, false);
            holder.iv_title = (ImageView)convertView.findViewById(R.id.iv_title);
            holder.tv_title = (TextView)convertView.findViewById(R.id.tv_title);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }

        mStoryBean = mStoryBeanList.get(position);
        //渲染界面
        //对于点击过的item，也即阅读过的新闻对title做灰色处理以区别未阅读过的新闻，提升用户体验，这里采用SharedPreferences来记录读取信息
        //每一条item都有唯一的id，可以通过存取id来区别item
        String newsId = Utils.getPreferences(context, HAVE_READ, "");
        //判断item是否包含该id
        /*if(newsId.contains(mStoryBeanList.get(position).getId() + "")){
            //title置为灰色
            holder.tv_title.setTextColor(context.getResources().getColor(R.color.gray_normal));
        }else{
            //未阅读过，如果为日间模式，title置黑色，为夜间模式，title置白色
            holder.tv_title.setTextColor(context.getResources().getColor(isLight? R.color.black_color : R.color.white));
        }*/
        if(mStoryBean.isClicked()){
            //title置为灰色
            holder.tv_title.setTextColor(context.getResources().getColor(R.color.gray_normal));
        }else{
            //未阅读过，如果为日间模式，title置黑色，为夜间模式，title置白色
            holder.tv_title.setTextColor(context.getResources().getColor(isLight? R.color.black_color : R.color.white));
        }

        //设置整个item的背景
        ((LinearLayout)holder.tv_title.getParent().getParent().getParent()).setBackgroundColor(context.getResources().getColor(isLight? R.color.light_news_item : R.color.dark_news_item));
        //设置framelayout颜色
        ((FrameLayout) holder.tv_title.getParent().getParent()).setBackgroundResource(isLight ? R.drawable.item_background_selector_light : R.drawable.item_background_selector_dark);
        holder.tv_title.setText(mStoryBean.getTitle());
        //有图就显示，没图则隐藏
        if (mStoryBean.getImages() != null) {
            holder.iv_title.setVisibility(View.VISIBLE);
            mImageLoader.displayImage(mStoryBean.getImages().get(0), holder.iv_title, mOptions);
        } else {
            holder.iv_title.setVisibility(View.GONE);
        }
        return convertView;
    }

    public static class ViewHolder {
        TextView tv_title;
        ImageView iv_title;
    }
}
