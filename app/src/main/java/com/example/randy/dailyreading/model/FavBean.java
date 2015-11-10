package com.example.randy.dailyreading.model;

/**
 * Created by randy on 2015/10/21.
 * 对应收藏文章的Bean
 */
public class FavBean {

    private long id;
    private String title;
    private String image;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
