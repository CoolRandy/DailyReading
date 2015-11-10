package com.example.randy.dailyreading.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.activity.MainContentActivity;
import com.example.randy.dailyreading.model.Constant;
import com.example.randy.dailyreading.model.MenuItemBean;
import com.example.randy.dailyreading.model.ThemeBean;
import com.example.randy.dailyreading.util.HttpUtils;
import com.example.randy.dailyreading.util.UIUtil;
import com.example.randy.dailyreading.util.Utils;
import com.google.gson.Gson;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by randy on 2015/10/16.
 * 侧边栏 菜单列表界面
 */
public class MenuFragment extends BaseFragment implements View.OnClickListener{

    private LinearLayout ll_layout;
    private LinearLayout login_layout;
    private ImageView login_iv;
    private TextView login_tv;
    private TextView favourite_tv;
    private TextView download_tv;
    private TextView main_tv;
    private ListView listView;

    private SharedPreferences sp;
    private boolean isLight;

    //bean
    private ThemeBean themeBean;
    private List<MenuItemBean> itemListBean = new ArrayList<>();
    //handler
    private android.os.Handler handler = new android.os.Handler();
    //adapter
    private NewTypeAdapter newTypeAdapter;

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sliding_menu_layout, container, false);

        //main layout
        ll_layout = (LinearLayout)view.findViewById(R.id.ll_menu);
        //login
        login_layout = (LinearLayout)view.findViewById(R.id.layout_login);
        login_iv = (ImageView)view.findViewById(R.id.iv_login);
        login_tv = (TextView)view.findViewById(R.id.tv_login);
        login_layout.setOnClickListener(this);
        // favourite
        favourite_tv = (TextView)view.findViewById(R.id.tv_favourite);
        download_tv = (TextView)view.findViewById(R.id.tv_download);
        main_tv = (TextView)view.findViewById(R.id.tv_main);
        favourite_tv.setOnClickListener(this);
        download_tv.setOnClickListener(this);
        main_tv = (TextView)view.findViewById(R.id.tv_main);
        main_tv.setOnClickListener(this);
        //listview
        listView = (ListView)view.findViewById(R.id.lv_item);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                        .replace(R.id.content,
                                OtherNewsFragment.newInstance(itemListBean.get(position).getId()), "news").commit();
                ((MainContentActivity)mActivity).setCurrID(itemListBean.get(position).getId() + "");
                ((MainContentActivity)mActivity).closeDrawer();
            }
        });
        return view;
    }

    /*public static Fragment newInstance(String arg){
        MenuFragment menuFragment = new MenuFragment();
        Bundle bundle = new Bundle();
        bundle.putString("itemId", arg);
        menuFragment.setArguments(bundle);
        return menuFragment;
    }*/

    @Override
    protected void initData() {
        super.initData();
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //isLight = sp.getBoolean("isLight", true);
        isLight = Utils.getBooleanPreferences(mActivity, "isLight", true);

        //请求数据
        UIUtil.showLoadingDialog(mActivity);
        HttpUtils.get(Constant.THEMES, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                UIUtil.cancleLoadingDialog();
                Log.e("TAG", "网络请求失败");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                UIUtil.cancleLoadingDialog();
                Gson gson = new Gson();
                if(null == responseString){
                    UIUtil.cancleLoadingDialog();
                    Log.e("TAG", "请求数据失败");
                    return;
                }
                themeBean = gson.fromJson(responseString, ThemeBean.class);
                itemListBean = themeBean.getOthers();
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        newTypeAdapter = new NewTypeAdapter();
                        listView.setAdapter(newTypeAdapter);
                        updateTheme();
                    }
                });
            }

        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.tv_main:
                ((MainContentActivity)mActivity).initData();
                ((MainContentActivity)mActivity).closeDrawer();
                break;
            case R.id.tv_favourite:
                Log.e("TAG", "点击收藏");
                //跳转收藏页 FavoFragment
                getFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                        .replace(R.id.content,
                                new FavoFragment(), "favourites").commit();
                //((MainContentActivity)mActivity).setCurrID(itemListBean.get(position).getId() + "");
                ((MainContentActivity)mActivity).closeDrawer();
                break;
            default:
                break;
        }
    }

    public class NewTypeAdapter extends BaseAdapter {

        public void setStoryBean(){

            //isLight = ((MainActivity) mActivity).isLight();
            //isLight = sp.getBoolean("isLight", true);
            isLight = Utils.getBooleanPreferences(mActivity, "isLight", true);
            Log.e("TAG", "isLight====>" + isLight);
            ll_layout.setBackgroundColor(getResources().getColor(isLight ? R.color.light_menu_header : R.color.dark_menu_header));
            login_tv.setTextColor(getResources().getColor(isLight ? R.color.light_menu_header : R.color.dark_menu_header));
            login_layout.setBackgroundColor(getResources().getColor(isLight ? R.color.light_menu_header : R.color.dark_menu_header));
            download_tv.setTextColor(getResources().getColor(isLight ? R.color.light_menu_header : R.color.dark_menu_header));
            main_tv.setBackgroundColor(getResources().getColor(isLight ? R.color.light_menu_index_background : R.color.dark_menu_listview_background));
            listView.setBackgroundColor(getResources().getColor(isLight ? R.color.light_menu_listview_background : R.color.dark_menu_listview_background));
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return itemListBean.size();
        }

        @Override
        public Object getItem(int i) {
            return itemListBean.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(null == convertView){
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.menu_item, parent, false);
            }
            TextView textView = (TextView)convertView.findViewById(R.id.tv_item);
            textView.setTextColor(getResources().getColor(isLight? R.color.light_menu_listview_textcolor : R.color.dark_menu_listview_textcolor));
            textView.setText(itemListBean.get(position).getName());
            return convertView;
        }
    }

    public void updateTheme(){
        Log.e("TAG", "改变侧边栏的阅读模式");
        newTypeAdapter.setStoryBean();
    }
}
;