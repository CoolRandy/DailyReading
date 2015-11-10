package com.example.randy.dailyreading.widgets;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.randy.dailyreading.R;


/**
 * Created by ${randy} on 2015/10/8.
 * 这里还有一种方式可以采用继承自ProgressDialog，这样可以使用其中的一些特性，比如点击窗口外区域可以取消dialog
 */
public class LoadingDialog extends Dialog{

    public LoadingDialog(Context context) {
        super(context);
    }
    public LoadingDialog(Context context, int theme){
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_loading_layout);
        ImageView imageView = (ImageView)findViewById(R.id.loading_image);

        //通过ImageView对象拿到背景显示的AnimationDrawable
        final AnimationDrawable animationDrawable = (AnimationDrawable)imageView.getBackground();
        //为防止动画只执行一次
        imageView.post(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        });
    }
}
