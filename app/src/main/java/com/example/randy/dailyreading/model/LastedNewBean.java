package com.example.randy.dailyreading.model;

import java.util.List;

/**
 * Created by ${randy} on 2015/9/19.
 */
public class LastedNewBean {

    private String date;
    private List<StoryBean> stories;
    private List<TopStoryBean> top_stories;

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

    public List<TopStoryBean> getTop_stories() {
        return top_stories;
    }

    public void setTop_stories(List<TopStoryBean> top_stories) {
        this.top_stories = top_stories;
    }
}
