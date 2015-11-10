package com.example.randy.dailyreading.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;
import com.example.randy.dailyreading.R;

/**
 * Created by randy on 2015/10/18.
 * 自定义WebView，实现滑动webView实现动态改变顶部栏title的透明度颜色变化
 */
public class ScrollWebView extends WebView {

    private OnBoardListener onBoardListener;
    private boolean isFling = true;
    private int titleHeight;

    public ScrollWebView(Context context) {
        super(context);
        titleHeight = this.getResources().getDimensionPixelSize(R.dimen.title_bar_height);
    }

    public ScrollWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        titleHeight = this.getResources().getDimensionPixelSize(R.dimen.title_bar_height);
    }

    public ScrollWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        titleHeight = this.getResources().getDimensionPixelSize(R.dimen.title_bar_height);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    public void setOnBoardListener(final OnBoardListener onBoardListener){

        this.onBoardListener = onBoardListener;
        if(null == onBoardListener){
            return;
        }
    }

    /**
     * 定义一个监听器接口
     */
    public static interface OnBoardListener{
        /**
         * Called when scroll to bottom
         */
        public void onBottom();
        /**
         * Called when scroll to top
         */
        public void onTop();
        /**
         * change the alpha
         */
        public void onAlphaChanged(float alpha);
        /**
         * override onScrollChanged
         */
        public void onScrollChanged(int l, int t, int oldl, int oldt);
    }

    /**
     * 执行滑动到顶部或底部监听事件
     */
    public void doBorderListener(int l, int t, int oldl, int oldt){
        //webview的总高度
        float webviewContentHeight = getContentHeight() * getScale();
        //当前webview的高度
        float webviewCurrentHeight = getHeight() + getScrollY();
        //判断滑动到底部
        if (Math.abs(webviewContentHeight-webviewCurrentHeight) < 1){
            if(null != onBoardListener){
                onBoardListener.onBottom();
            }
        }else if(0 == getScrollY()){
            //纵轴向下滑动的距离为0，表示滑动到了顶部
            if(null != onBoardListener) {
                onBoardListener.onTop();
            }
        }else {
            if (null != onBoardListener) {
                onBoardListener.onScrollChanged(l, t, oldl, oldt);
            }
        }

        int pos = this .getScrollY();
        if (isFling && null != onBoardListener) {//监听器不为空且正在滑动，根据滑动点的位置变化动态改变title的透明度
            float alpha = 0;
            if (pos < titleHeight && pos > 0) {
                alpha = 1.0f * t / titleHeight;
            }
            if (pos >= titleHeight ) {
                alpha = 1;
            }
            // title alpha
            onBoardListener.onAlphaChanged(alpha);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        switch(ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                isFling = false;
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                if(onBoardListener != null){
                    float alpha = 1.0f * getScrollY() / titleHeight ;
                    float value = titleHeight > getScrollY() ? alpha : 1.0f;

                    onBoardListener.onAlphaChanged(value);
                }
                break;
        }
        return super.onTouchEvent(ev);
    }
}
