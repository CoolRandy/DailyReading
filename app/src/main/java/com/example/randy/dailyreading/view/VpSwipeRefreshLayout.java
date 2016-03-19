package com.example.randy.dailyreading.view;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by randy on 2016/3/19.
 */
public class VpSwipeRefreshLayout extends SwipeRefreshLayout {

    //非法按键
    private static final int INVALID_POINTER = -1;
    private float lastXPos;
    private float lastYPos;
    //系统判断滑动的最小距离
    private int mTouchSlop;

    //dispatch方法记录的手指
    private int mActiveDispatchPointerId = INVALID_POINTER;
    //是否请求拦截
    private boolean hasRequestDisallowIntercept = false;

    public VpSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){

            case MotionEvent.ACTION_DOWN:
                mActiveDispatchPointerId = MotionEventCompat.getPointerId(ev, 0);
                final float initDownX = getMotionEventX(ev, mActiveDispatchPointerId);
                if (initDownX != INVALID_POINTER){
                    lastXPos = initDownX;
                }
                final float initDownY = getMotionEventY(ev, mActiveDispatchPointerId);
                if (initDownY != INVALID_POINTER){
                    lastYPos = initDownY;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e("TAG", "滑动layout");

                final float x = getMotionEventX(ev, mActiveDispatchPointerId);
                final float y = getMotionEventY(ev, mActiveDispatchPointerId);
                if(lastXPos != INVALID_POINTER && lastYPos != INVALID_POINTER
                        && x != INVALID_POINTER && y != INVALID_POINTER) {
                    final float x_dis = Math.abs(x - lastXPos);
                    final float y_dis = Math.abs(y - lastYPos);
                    if (x_dis > mTouchSlop && x_dis * 0.7 > y_dis) {
                        //告诉父类横向滑动不拦截
//                        super.requestDisallowInterceptTouchEvent(true);
                        return false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP){
                    hasRequestDisallowIntercept = false;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /* @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        hasRequestDisallowIntercept = b;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){

            case MotionEvent.ACTION_DOWN:
                mActiveDispatchPointerId = MotionEventCompat.getPointerId(ev, 0);
                final float initDownX = getMotionEventX(ev, mActiveDispatchPointerId);
                if (initDownX != INVALID_POINTER){
                    lastXPos = initDownX;
                }
                final float initDownY = getMotionEventY(ev, mActiveDispatchPointerId);
                if (initDownY != INVALID_POINTER){
                    lastYPos = initDownY;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e("TAG", "滑动layout");
                if (hasRequestDisallowIntercept){
                    final float x = getMotionEventX(ev, mActiveDispatchPointerId);
                    final float y = getMotionEventY(ev, mActiveDispatchPointerId);
                    if(lastXPos != INVALID_POINTER && lastYPos != INVALID_POINTER
                            && x != INVALID_POINTER && y != INVALID_POINTER){
                        final float x_dis = Math.abs(x - lastXPos);
                        final float y_dis = Math.abs(y - lastYPos);
                        if(x_dis > mTouchSlop && x_dis * 0.7 > y_dis){
                            //告诉父类横向滑动不拦截
                            super.requestDisallowInterceptTouchEvent(true);
                        }else {
                            super.requestDisallowInterceptTouchEvent(false);
                        }
                    }else {
                        super.requestDisallowInterceptTouchEvent(false);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP){
                    hasRequestDisallowIntercept = false;
                }
                break;

        }
        return super.dispatchTouchEvent(ev);
    }*/

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    private float getMotionEventX(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getX(ev, index);
    }
}
