package com.example.randy.dailyreading.util;

/**
 * Created by randy on 2015/9/13.
 */
public interface RequestCallBack {

    public void onFail(Exception e);

    public void onSuccess(Object o);
}
