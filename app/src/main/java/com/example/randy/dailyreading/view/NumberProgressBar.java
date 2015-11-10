package com.example.randy.dailyreading.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.example.randy.dailyreading.R;

/**
 * Created by randy on 2015/10/29.
 */
public class NumberProgressBar extends View {


    //已到达进度条的高度，也就是线条的粗细度
    private float mReachedBarHeight;
    private float mUnreachedBarHeight;

    private int mReachedBarColor;
    private int mUnreachedBarColor;
    //进度条text的尺寸和颜色
    private int mTextColor;
    private float mTextSize;

    //当前的进度
    private int mCurrentProgress = 0;
    //最大进度
    private int mMaxProgress = 100;

    //百分号
    private String mSuffix = "%";
    private String mPrefix = "";

    //注意：对于progress的进度条这里采用填充颜色的矩形来绘制
    private RectF mReachedRectF = new RectF(0, 0, 0, 0);//4个参数分别代表4个单精度浮点坐标
    private RectF mUnreachedRectF = new RectF(0, 0, 0, 0);

    private Paint mTextPaint;
    private Paint mReachedBarPaint;
    private Paint mUnreachedBarPaint;

    //当前绘制的进度text
    private String mCurrentDrawText;
    private float mDrawTextWidth;
    private float mDrawTextStart;
    private float mDrawTextEnd;

    //progress text offset 偏移量
    private float mOffset;

    //决定是否需要去绘制unreached area
    private boolean mDrawUnreachedBar = true;
    private boolean mDrawReachedBar = true;
    private boolean mIfDrawText = true;

    //监听器
    private OnProgressBarListener listener;

    //默认的未到达的进度条的粗度
    private float default_reached_bar_height;

    private float default_unreached_bar_height;

    //默认中间进度百分比字体显示大小
    private float default_text_size;

    private float default_progress_text_offset;//??

    private final int default_text_color = Color.rgb(66, 145, 240);
    private final int default_reached_color = Color.rgb(66, 145, 240);
    private final int default_unreached_color = Color.rgb(204, 204, 204);//灰色

    //常量
    /**
     * For save and restore instance of progressbar.
     */
    private static final String INSTANCE_STATE = "saved_instance";
    private static final String INSTANCE_TEXT_COLOR = "text_color";
    private static final String INSTANCE_TEXT_SIZE = "text_size";
    private static final String INSTANCE_REACHED_BAR_HEIGHT = "reached_bar_height";
    private static final String INSTANCE_REACHED_BAR_COLOR = "reached_bar_color";
    private static final String INSTANCE_UNREACHED_BAR_HEIGHT = "unreached_bar_height";
    private static final String INSTANCE_UNREACHED_BAR_COLOR = "unreached_bar_color";
    private static final String INSTANCE_MAX = "max";
    private static final String INSTANCE_PROGRESS = "progress";
    private static final String INSTANCE_SUFFIX = "suffix";
    private static final String INSTANCE_PREFIX = "prefix";
    private static final String INSTANCE_TEXT_VISIBILITY = "text_visibility";
    private static final int PROGRESS_TEXT_VISIBLE = 0;


    //文字可见与否
    public enum ProgressTextVisiblity {
        Visible, Invisible
    }


    public NumberProgressBar(Context context) {
        //super(context);
        this(context, null);
    }

    public NumberProgressBar(Context context, AttributeSet attrs) {
        //super(context, attrs);
        this(context, attrs, R.attr.numberProgressBarStyle);
    }

    public NumberProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        default_reached_bar_height = dp2px(1.5f);
        default_unreached_bar_height = dp2px(1.0f);
        default_text_size = dp2px(10);
        default_progress_text_offset = dp2px(3.0f);//设置默认text偏移量的px大小

        final TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.NumberProgressBar,
                defStyleAttr, 0);

        mReachedBarColor = a.getColor(R.styleable.NumberProgressBar_progress_reached_color, default_reached_color);
        mUnreachedBarColor = a.getColor(R.styleable.NumberProgressBar_progress_unreached_color, default_unreached_color);
        mTextSize = a.getDimension(R.styleable.NumberProgressBar_progress_text_size, default_text_size);
        mTextColor = a.getColor(R.styleable.NumberProgressBar_progress_text_color, default_text_color);

        mReachedBarHeight = a.getDimension(R.styleable.NumberProgressBar_progress_reached_bar_height, default_reached_bar_height);
        mUnreachedBarHeight = a.getDimension(R.styleable.NumberProgressBar_progress_unreached_bar_height, default_unreached_bar_height);
        mOffset = a.getFloat(R.styleable.NumberProgressBar_progress_text_offset, default_progress_text_offset);

        //是否可见
        int textVisible = a.getInt(R.styleable.NumberProgressBar_progress_text_visiblity, PROGRESS_TEXT_VISIBLE);
        if(textVisible != PROGRESS_TEXT_VISIBLE){

            mIfDrawText = false;
        }

        setProgress(a.getInt(R.styleable.NumberProgressBar_progress_current, 0));
        setMax(a.getInt(R.styleable.NumberProgressBar_progress_max, 100));

        a.recycle();
        //初始化画笔
        initPainter();

    }

    /**
     * 重写这个view建议的最小宽高，这里需要重写，我们自定义的view的高度最小值由text，未到达和已到达线条高度的最大值决定
     * 宽度为progress的text尺寸决定
     * @return
     */
    @Override
    protected int getSuggestedMinimumHeight() {
        //return super.getSuggestedMinimumHeight();
        return Math.max((int)mTextSize, Math.max((int)mReachedBarHeight, (int)mUnreachedBarHeight));
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        //return super.getSuggestedMinimumWidth();
        return (int) mTextSize;
    }

    /**
     * 重写onMeasure方法，用于测量view
     * 默认是调用的超类的onMeasure方法，这里我们需要采用自己的测量逻辑，所以注释掉超类的该方法
     * 注意自己实现测量逻辑，一定要调用setMeasuredDimension方法，不然或报异常,该方法需要传入宽高参数
     * 这里我们定义一个测量方法来返回宽和高measure
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measure(widthMeasureSpec, true), measure(heightMeasureSpec, false));
    }
    /**
     * 该方法可以仿照系统源码中的getDefaultSize方法来写
     * @param measureSpec
     * @param isWidth
     * @return
     */
    public int measure(int measureSpec, boolean isWidth){

        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        //getPaddingLeft表示控件内容距离该控件左边缘的距离，其他类似
        int padding = isWidth ? getPaddingLeft() + getPaddingRight() : getPaddingTop() + getPaddingBottom();

        //根据mode重新封装大小
        if(mode == MeasureSpec.EXACTLY){
            result = size;
        }else{
            result = isWidth ? getSuggestedMinimumWidth() : getSuggestedMinimumHeight();
            result += padding;
            if(mode == MeasureSpec.AT_MOST) {
                if (isWidth) {
                    result = Math.max(result, size);
                } else {
                    result = Math.min(result, size);
                }
            }
        }
        return result;
    }

    //绘制
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制分为两种情况：一种显示进度百分比  另一种不显示
        if(mIfDrawText){

            calculateDrawRextF();
        }else {
            calculateDrawRectFWithoutProgressText();
        }

        if(mDrawReachedBar){
            canvas.drawRect(mReachedRectF, mReachedBarPaint);
        }

        if(mDrawUnreachedBar){
            canvas.drawRect(mUnreachedRectF, mUnreachedBarPaint);
        }

        if(mIfDrawText){
            canvas.drawText(mCurrentDrawText, mDrawTextStart, mDrawTextEnd, mTextPaint);
        }
    }

    //初始化画笔
    public void initPainter(){
        //绘制已到达，百分比文字和未到达分别是不同的画笔
        mReachedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
        mReachedBarPaint.setColor(mReachedBarColor);

        mUnreachedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnreachedBarPaint.setColor(mUnreachedBarColor);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
    }

    /**
     * 计算已到达和未到达进度矩形条的左上角和右下角坐标
     */
    public void calculateDrawRectFWithoutProgressText(){

        mReachedRectF.left = getPaddingLeft();
        mReachedRectF.top = getHeight() / 2.0f - mReachedBarHeight / 2.0f;
        mReachedRectF.right = (getWidth() - getPaddingRight() - getPaddingLeft()) * getProgress() / (getMax() * 1.0f) + getPaddingLeft();
        mReachedRectF.bottom = getHeight() / 2.0f + mReachedBarHeight / 2.0f;

        mUnreachedRectF.left = mReachedRectF.right;
        mUnreachedRectF.top = getHeight() / 2.0f - mUnreachedBarHeight / 2.0f;
        //未到达的right坐标是固定的
        mUnreachedRectF.right = getWidth() - getPaddingRight();
        mUnreachedRectF.bottom = getHeight() / 2.0f + mUnreachedBarHeight / 2.0f;
    }


    //获取、设置已到达进度条的高度
    public float getmReachedBarHeight() {
        return mReachedBarHeight;
    }

    public void setmReachedBarHeight(float mReachedBarHeight) {
        this.mReachedBarHeight = mReachedBarHeight;
    }

    //获取、设置未到达进度条的高度
    public float getmUnreachedBarHeight() {
        return mUnreachedBarHeight;
    }

    public void setmUnreachedBarHeight(float mUnreachedBarHeight) {
        this.mUnreachedBarHeight = mUnreachedBarHeight;
    }

    //获取、设置已到达进度条的颜色
    public int getmReachedBarColor() {
        return mReachedBarColor;
    }

    public void setmReachedBarColor(int mReachedBarColor) {
        this.mReachedBarColor = mReachedBarColor;
        mReachedBarPaint.setColor(mReachedBarColor);
        invalidate();

    }

    //获取、设置未到达进度条的颜色
    public int getmUnreachedBarColor() {
        return mUnreachedBarColor;
    }

    public void setmUnreachedBarColor(int mUnreachedBarColor) {
        this.mUnreachedBarColor = mUnreachedBarColor;
        mUnreachedBarPaint.setColor(mReachedBarColor);
        invalidate();
    }

    //获取并设置文字颜色
    public int getmTextColor() {
        return mTextColor;
    }

    public void setmTextColor(int mTextColor) {
        this.mTextColor = mTextColor;
        mTextPaint.setColor(mTextColor);
        invalidate();
    }
    //获取、设置文字大小
    public float getmTextSize() {
        return mTextSize;
    }

    public void setmTextSize(float mTextSize) {
        this.mTextSize = mTextSize;
        mTextPaint.setTextSize(mTextSize);
        invalidate();//即请求重新draw
    }

    /**
     * 这地方有一个地方不太好理解：就是对于偏移量这块offset，
     */
    public void calculateDrawRextF(){

        mCurrentDrawText = String.format("%d", getProgress() * 100 / getMax());
        mCurrentDrawText = mPrefix + mCurrentDrawText + mSuffix;//eg:  35%
        //测量text的宽度
        mDrawTextWidth = mTextPaint.measureText(mCurrentDrawText);

        //考虑起始情况
        if(0 == getProgress()){
            mDrawReachedBar = false;
            mDrawTextStart = getPaddingLeft();
        }else {

            mDrawReachedBar = true;
            mReachedRectF.left = getPaddingLeft();
            mReachedRectF.top = getHeight() / 2.0f - mReachedBarHeight / 2.0f;
            mReachedRectF.right = (getWidth() - getPaddingRight() - getPaddingLeft()) * getProgress() / (getMax() * 1.0f) - mOffset + getPaddingLeft();
            mReachedRectF.bottom = getHeight() / 2.0f + mReachedBarHeight / 2.0f;
            mDrawTextStart = mReachedRectF.right + mOffset;
        }

        /**

         *
         */
        mDrawTextEnd = (int) ((getHeight() / 2.0f) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2.0f));
        if((mDrawTextStart + mDrawTextWidth) > getWidth() - getPaddingRight()){
            mDrawTextStart = getWidth() - getPaddingRight() - mDrawTextWidth;
            mReachedRectF.right = mDrawTextStart - mOffset;
        }

        float unReachedBarStart = mDrawTextStart + mDrawTextWidth + mOffset;
        if(unReachedBarStart > getWidth() - getPaddingRight()){
            mDrawUnreachedBar = false;
        }else{

            mDrawUnreachedBar = true;
            mUnreachedRectF.left = unReachedBarStart;
            mUnreachedRectF.top = getHeight() / 2.0f - mReachedBarHeight / 2.0f;
            mUnreachedRectF.right = getWidth() - getPaddingRight();
            mUnreachedRectF.bottom = getHeight() / 2.0f + mReachedBarHeight / 2.0f;
        }

    }

    /**
     * 获取、设置当前的进度
     * @return
     */
    public int getProgress(){

        return mCurrentProgress;
    }

    /**
     * 设置当前进度
     */
    public void setProgress(int progress){

        if(progress <= getMax() && progress >= 0){
            this.mCurrentProgress = progress;
            //重绘
            invalidate();
            //postInvalidate();
        }
    }

    /**
     * 获取、设置最大的progress
     * @return
     */
    public int getMax(){
        return mMaxProgress;
    }

    public void setMax(int maxProgress){

        if(maxProgress > 0){
            this.mMaxProgress = maxProgress;
            //重绘
            invalidate();
        }
    }

    public String getmPrefix() {
        return mPrefix;
    }

    public void setmPrefix(String mPrefix) {
        //this.mPrefix = mPrefix;
        if(null == mPrefix){
            mPrefix = null;
        }else {
            this.mPrefix = mPrefix;
        }
    }

    public String getmSuffix() {
        return mSuffix;
    }

    public void setmSuffix(String mSuffix) {
        this.mSuffix = mSuffix;
    }

    public boolean getmProgressTextVisibility() {
        return mIfDrawText;
    }

    public void setProgressTextVisibility(ProgressTextVisiblity visibility) {
        mIfDrawText = visibility == ProgressTextVisiblity.Visible;
        invalidate();
    }

    public void setOnProgressBarListener(OnProgressBarListener listener){
        this.listener = listener;
    }

    /**
     * 设置增加进度的步长
     * @param step
     */
    public void increaseProgressBy(int step){
        if(step > 0){
            setProgress(getProgress() + step);
        }
        //每次调用increaseProgressBy方法都调用onProgressChange来判断是否到达终点
        if(listener != null){
            listener.onProgressChange(getProgress(), getMax());
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(INSTANCE_TEXT_COLOR, getmTextColor());
        bundle.putFloat(INSTANCE_TEXT_SIZE, getmTextSize());
        bundle.putFloat(INSTANCE_REACHED_BAR_HEIGHT, getmReachedBarHeight());
        bundle.putFloat(INSTANCE_UNREACHED_BAR_HEIGHT, getmUnreachedBarHeight());
        bundle.putInt(INSTANCE_REACHED_BAR_COLOR, getmReachedBarColor());
        bundle.putInt(INSTANCE_UNREACHED_BAR_COLOR, getmUnreachedBarColor());
        bundle.putInt(INSTANCE_MAX, getMax());
        bundle.putInt(INSTANCE_PROGRESS, getProgress());
        bundle.putString(INSTANCE_SUFFIX, getmSuffix());
        bundle.putString(INSTANCE_PREFIX, getmPrefix());
        bundle.putBoolean(INSTANCE_TEXT_VISIBILITY, getmProgressTextVisibility());
        return bundle;
    }

    /**
     * 注：这里对于onRestoreInstanceState和onSaveInstanceState的使用原因和调用时机可以参见印象笔记
     * @param state
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            mTextColor = bundle.getInt(INSTANCE_TEXT_COLOR);
            mTextSize = bundle.getFloat(INSTANCE_TEXT_SIZE);
            mReachedBarHeight = bundle.getFloat(INSTANCE_REACHED_BAR_HEIGHT);
            mUnreachedBarHeight = bundle.getFloat(INSTANCE_UNREACHED_BAR_HEIGHT);
            mReachedBarColor = bundle.getInt(INSTANCE_REACHED_BAR_COLOR);
            mUnreachedBarColor = bundle.getInt(INSTANCE_UNREACHED_BAR_COLOR);
            initPainter();
            setMax(bundle.getInt(INSTANCE_MAX));
            setProgress(bundle.getInt(INSTANCE_PROGRESS));
            setmPrefix(bundle.getString(INSTANCE_PREFIX));
            setmSuffix(bundle.getString(INSTANCE_SUFFIX));
            setProgressTextVisibility(bundle.getBoolean(INSTANCE_TEXT_VISIBILITY) ? ProgressTextVisiblity.Visible : ProgressTextVisiblity.Invisible);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    /**
     * 根据手机分辨率从dp转换为px像素
     * @param dp
     * @return
     */
    public float dp2px(float dp){
        final float scale = getResources().getDisplayMetrics().density;
        return scale * dp + 0.5f;
    }

    /**
     * 根据手机分辨率从sp转换为px
     * @param sp
     * @return
     */
    public float sp2px(float sp){
        final float scale = getResources().getDisplayMetrics().density;
        return scale * sp;
    }

    /**
     * 根据手机分辨率从px转换为dp
     * @param px
     * @return
     */
    public float px2dp(float px){
        final float scale = getResources().getDisplayMetrics().density;
        return px / scale + 0.5f;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }
}
