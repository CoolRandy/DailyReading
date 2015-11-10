package com.example.randy.dailyreading.model;

/**
 * Created by ${randy} on 2015/9/19.
 */

import java.io.Serializable;

/**
 * title: "商场和很多人家里，竹制家具越来越多（多图）",
 * image: "http://p2.zhimg.com/9a/15/9a1570bb9e5fa53ae9fb9269a56ee019.jpg",
 * ga_prefix: "052315",
 * type: 0,
 * id: 3930883
 */
public class TopStoryBean implements Serializable{

    private long id;
    private int type;
    private String ga_prefix;
    private String image;
    private String title;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getGa_prefix() {
        return ga_prefix;
    }

    public void setGa_prefix(String ga_prefix) {
        this.ga_prefix = ga_prefix;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
