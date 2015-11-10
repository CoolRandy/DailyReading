package com.example.randy.dailyreading.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by randy on 2015/9/13.
 * 常量类
 */
public class Constant {

    //url
    public static final String BASEURL = "http://news-at.zhihu.com/api/4/";
    public static final String START = BASEURL + "start-image/1080*1776";
    public static final String THEMES = BASEURL + "themes";
    public static final String LATESTNEWS = BASEURL + "news/latest";
    public static final String BEFORE = BASEURL + "news/before/";
    public static final String THEMENEWS = BASEURL + "theme/";
    public static final String CONTENT = BASEURL + "news/";//后面拼接获得的id，获取内容
    public static final int TOPIC = 131;//代表是今日要闻？？
    public static final String START_LOCATION = BASEURL + "start_location";//离线下载？？
    public static final String CACHE = "cache";
    public static final int LATEST_COLUMN = Integer.MAX_VALUE;
    public static final int BASE_COLUMN = 100000000;

    //收藏列表
    public static List<FavBean> favBeanList = new ArrayList<>();

    public static boolean HAS_NETWORK = false;

    public static List<StoryBean> storyBeanList = new ArrayList<>();

    public static List<TopStoryBean> topStoryBeans;
}
