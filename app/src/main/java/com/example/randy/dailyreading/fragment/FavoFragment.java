package com.example.randy.dailyreading.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.model.Constant;
import com.example.randy.dailyreading.model.FavBean;
import com.example.randy.dailyreading.util.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by randy on 2015/10/21.
 */
public class FavoFragment extends BaseFragment {

    private ListView listView;
    private FavBean favBean;
    //isLight
    private boolean isLight;

    public FavoFragment() {
    }

    //TODO 这里采用fragment实例的方法传递参数，而不是采用构造方法，是为了避免设备横竖屏切换后数据丢失
   /* public static Fragment newInstance(int arg){
       FavoFragment fragment = new FavoFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("itemId", arg);
        fragment.setArguments(bundle);
        return fragment;
    }*/

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.favourite_layout, container, false);
        listView = (ListView)view.findViewById(R.id.list_view);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //
                Toast.makeText(mActivity, "跳转详情页", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    @Override
    protected void initData() {
        super.initData();
        isLight = Utils.getBooleanPreferences(mActivity, "isLight", true);
        FavContentAdapter favContentAdapter = new FavContentAdapter(mActivity, Constant.favBeanList);
        listView.setAdapter(favContentAdapter);
    }

    public class FavContentAdapter extends BaseAdapter{

        private Context context;
        private List<FavBean> favBeanList;
        private ImageLoader imageLoader;
        private DisplayImageOptions options;
        private static final String HAVE_READ = "have_read";

        public FavContentAdapter(Context context, List<FavBean> favBeanList){
            this.context = context;
            this.favBeanList = favBeanList;
            imageLoader = ImageLoader.getInstance();
            options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();
        }

        @Override
        public int getCount() {
            return favBeanList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return favBeanList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(null == convertView){
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mActivity).inflate(R.layout.fav_item, parent, false);
                holder.iv_title = (ImageView)convertView.findViewById(R.id.iv_title);
                holder.tv_title = (TextView)convertView.findViewById(R.id.tv_title);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }

            favBean = favBeanList.get(position);
            ((LinearLayout)holder.tv_title.getParent().getParent()).setBackgroundColor(getResources().getColor(isLight? R.color.light_news_item : R.color.dark_news_item));
            //设置数据和内容
            holder.tv_title.setText(favBean.getTitle());
            holder.tv_title.setTextColor(getResources().getColor(isLight? R.color.black_color : R.color.white));
            //有图就显示，没图则隐藏
            if (favBean.getImage() != null) {
                holder.iv_title.setVisibility(View.VISIBLE);
                imageLoader.displayImage(favBean.getImage(), holder.iv_title, options);
            } else {
                holder.iv_title.setVisibility(View.GONE);
            }
            return convertView;
        }
    }
    public static class ViewHolder {
        TextView tv_title;
        ImageView iv_title;
    }
}
