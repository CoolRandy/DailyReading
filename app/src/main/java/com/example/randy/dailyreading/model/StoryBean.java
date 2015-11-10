package com.example.randy.dailyreading.model;

/**
 * Created by ${randy} on 2015/9/19.
 */

import java.io.Serializable;
import java.util.List;

/**
 * title: "中国古代家具发展到今天有两个高峰，一个两宋一个明末（多图）",
 * ga_prefix: "052321",
 * images: [ "http://p1.zhimg.com/45/b9/45b9f057fc1957ed2c946814342c0f02.jpg" ],
 * 注意这里images是数组
 * type: 0,
 * id: 3930445
 */
public class StoryBean implements Serializable{

    private long id;
    private int type;
    private String title;
    private String ga_prefix;
    private List<String> images;
    //用于保存点击过的item灰色处理效果
    private boolean clicked;

    public boolean isClicked() {
        return clicked;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGa_prefix() {
        return ga_prefix;
    }

    public void setGa_prefix(String ga_prefix) {
        this.ga_prefix = ga_prefix;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
