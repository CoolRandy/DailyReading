package com.example.randy.dailyreading.model;

import java.util.List;

/**
 * Created by ${randy} on 2015/10/3.
 * 过往消息
 */
public class BeforeNewBean {

    private String date;
    private List<StoryBean> stories;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<StoryBean> getStories() {
        return stories;
    }

    public void setStories(List<StoryBean> stories) {
        this.stories = stories;
    }
}
