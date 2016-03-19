package com.example.randy.dailyreading.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.example.randy.dailyreading.R;

/**
 * Created by randy on 2016/2/20.
 * 自定义一个表盘view
 */
public class DialView extends View {

    //画笔
    private Paint mPaint;
    //矩形区域
    private RectF mBounds;
    //绘制圆形的半径
    private float mRadius;
    //短指针
    private float sLength;
    //长指针
    private float bLength;
    //定义属性变量以及控件对象
    private float mBorderWidth;
    private int mBorderColor;

    private float width;
    private float height;

    //通过new View(context)的方式创建实例
    public DialView(Context context) {
        super(context);
    }

    public DialView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.DialView, 0, 0);

        mBorderColor = a.getColor(R.styleable.DialView_dial_border_color, 0xff000000);
        mBorderWidth = a.getDimension(R.styleable.DialView_dial_border_width, 2);

        a.recycle();

        init();
    }

    public void init(){

        //抗锯齿
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mBorderWidth);
        mPaint.setColor(mBorderColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBounds = new RectF(getLeft(), getTop(), getRight(), getBottom());
        width = getRight() - getLeft();
        height = getBottom() - getTop();

        if(width < height){
            mRadius = width / 4;
        }else {
            mRadius = height / 4;
        }

        sLength = 10;
        bLength = 20;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0xff000000);
        mPaint.setColor(0x66555555);
        canvas.drawRoundRect(new RectF(mBounds.centerX() - (float) 0.9 * width / 2, mBounds.centerY() - (float) 0.9 * height / 2,
                mBounds.centerX() + (float) 0.9 * width / 2, mBounds.centerY() - (float) 0.9 * height / 2), 30, 30, mPaint);
        mPaint.setColor(mBorderColor);
        //然后绘制一个圆
        canvas.drawCircle(mBounds.centerX(), mBounds.centerY(), mRadius, mPaint);
        //起点和终点
        float start_x, start_y;
        float end_x, end_y;
        for(int i=0;i<60;++i){
            start_x= mRadius *(float)Math.cos(Math.PI/180 * i * 6);
            start_y= mRadius *(float)Math.sin(Math.PI/180 * i * 6);
            if(i%5==0){
                end_x = start_x+bLength*(float)Math.cos(Math.PI / 180 * i * 6);
                end_y = start_y+bLength*(float)Math.sin(Math.PI/180 * i * 6);
            }else{
                end_x = start_x+sLength*(float)Math.cos(Math.PI/180 * i * 6);
                end_y = start_y+sLength*(float)Math.sin(Math.PI/180 * i * 6);
            }
            start_x+=mBounds.centerX();
            end_x+=mBounds.centerX();
            start_y+=mBounds.centerY();
            end_y+=mBounds.centerY();
            canvas.drawLine(start_x, start_y, end_x, end_y, mPaint);
        }
        canvas.drawCircle(mBounds.centerX(),mBounds.centerY(),20,mPaint);
        canvas.rotate(60,mBounds.centerX(),mBounds.centerY());
        canvas.drawLine(mBounds.centerX(),mBounds.centerY(),mBounds.centerX(),mBounds.centerY()-mRadius,mPaint);



        /*mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.translate(canvas.getWidth()/2, 200); //将位置移动画纸的坐标点:150,150
        canvas.drawCircle(0, 0, 100, mPaint); //画圆圈

        //使用path绘制路径文字
        canvas.save();
        canvas.translate(-75, -75);
        Path path = new Path();
        path.addArc(new RectF(0,0,150,150), -180, 180);
        Paint citePaint = new Paint(mPaint);
        citePaint.setTextSize(14);
        citePaint.setStrokeWidth(1);
        canvas.drawTextOnPath("http://www.android777.com", path, 28, 0, citePaint);
        canvas.restore();

        Paint tmpPaint = new Paint(mPaint); //小刻度画笔对象
        tmpPaint.setStrokeWidth(1);

        float  y=100;
        int count = 60; //总刻度数

        for(int i=0 ; i <count ; i++){
            if(i%5 == 0){
                canvas.drawLine(0f, y, 0, y+12f, mPaint);
                canvas.drawText(String.valueOf(i/5+1), -4f, y+25f, tmpPaint);

            }else{
                canvas.drawLine(0f, y, 0f, y +5f, tmpPaint);
            }
            canvas.rotate(360/count,0f,0f); //旋转画纸
        }

        //绘制指针
        tmpPaint.setColor(Color.GRAY);
        tmpPaint.setStrokeWidth(4);
        canvas.drawCircle(0, 0, 7, tmpPaint);
        tmpPaint.setStyle(Paint.Style.FILL);
        tmpPaint.setColor(Color.YELLOW);
        canvas.drawCircle(0, 0, 5, tmpPaint);
        canvas.drawLine(0, 10, 0, -65, mPaint);*/
    }
}
