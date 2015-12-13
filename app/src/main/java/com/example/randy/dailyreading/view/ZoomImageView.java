package com.example.randy.dailyreading.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by randy on 2015/11/15.
 * 对于自定义view，运行过程首先要明确：构造方法--》onMeasure--》onLayout--》onDraw
 * 这三个回调方法，需要根据自定义view的需要进行重写，这一点需要明确！！！
 * 然后根据需要重写一些触摸事件等
 */
public class ZoomImageView extends View {

    /**
     * 记录图片缩放的几种状态
     * @param context
     */
    public static final int STATUS_INIT = 1;//初始化
    public static final int STATUS_ZOOM_OUT = 2;//放大状态
    public static final int STATUS_ZOOM_IN = 3;//缩小状态
    public static final int STATUS_MOVE = 4;//移动状态

    private int currentStatus;//记录当前的状态

    private double lastFigureDis;//上次两点之间的距离
    //private int currFigureDis;//当前两点之间的距离

    //自定义view控件的宽高
    private int width;
    private int height;

    //待展示的bitmap对象
    private Bitmap bitmap;



    public ZoomImageView(Context context) {
        //super(context);
        this(context, null);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        currentStatus = STATUS_INIT;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()){

            case MotionEvent.ACTION_POINTER_DOWN:
                //出现第一点之后，又出现了额外的点
                if(2 == event.getPointerCount()){
                    //两点触摸，计算两点之间的距离,作为上一次触摸点之间的距离，当两个手指移动时，重新计算两点之间的距离
                    //如果大于这个值，则表明放大；若小于该值，则表明缩小
                    lastFigureDis = distanceBetweenFigures(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //手指移动,这里只考虑两种情况：单点触控和两点触控
                if(1 == event.getPointerCount()){
                    //一个手指滑动，即为移动图片，这里需要考虑移除边界的情况



                }else if(2 == event.getPointerCount()){

                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_UP:
                break;

        }
        return super.onTouchEvent(event);
    }

    /**
     * 初始化bitmap
     * @param canvas
     */
    private void initBitmap(Canvas canvas){

        if (bitmap != null){
        }
    }

    /**
     * 计算两点之间的距离
     * @param event
     * @return
     */
    public double distanceBetweenFigures(MotionEvent event){

        float xDis = Math.abs(event.getX(0) + event.getX(1));
        float yDis = Math.abs(event.getY(0) - event.getY(1));
        return Math.sqrt(xDis * xDis + yDis + yDis);
    }
}
