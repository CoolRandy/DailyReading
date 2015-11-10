package com.example.randy.dailyreading.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ${randy} on 2015/10/1.
 */

/**
 * {
     "limit": 1000,
     "subscribed": [ ],
     "others": [
         {
         "color": 8307764,
         "thumbnail": "http://pic4.zhimg.com/2c38a96e84b5cc8331a901920a87ea71.jpg",
         "description": "内容由知乎用户推荐，海纳主题百万，趣味上天入地",
         "id": 12,
         "name": "用户推荐日报"
         },
         ...
     ]
 }
 */
public class ThemeBean {

    private int limit;
    private String[] subscribed;
    private List<MenuItemBean> others = new ArrayList<>();

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String[] getSubscribed() {
        return subscribed;
    }

    public void setSubscribed(String[] subscribed) {
        this.subscribed = subscribed;
    }

    public List<MenuItemBean> getOthers() {
        return others;
    }

    public void setOthers(List<MenuItemBean> others) {
        this.others = others;
    }
}
