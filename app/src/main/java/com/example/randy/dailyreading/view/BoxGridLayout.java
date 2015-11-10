package com.example.randy.dailyreading.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.example.randy.dailyreading.R;

/**
 * Created by randy on 2015/10/30.
 * 具体的代码分析详见印象笔记“自定义view”
 */
public class BoxGridLayout extends ViewGroup {

    private static final int DEFAULT_COUNT = 3;

    private Paint mGridPaint;

    private int mColumnCount;
    private int mMaxChildren;

    public BoxGridLayout(Context context) {
        this(context, null);
    }

    public BoxGridLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BoxGridLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BoxGridLayout, 0, defStyle);

        int strokeWidth = a.getDimensionPixelSize(R.styleable.BoxGridLayout_separatorWidth, 0);
        int strokeColor = a.getColor(R.styleable.BoxGridLayout_separatorColor, Color.WHITE);
        mColumnCount = a.getInteger(R.styleable.BoxGridLayout_numColumns, DEFAULT_COUNT);
        mMaxChildren = mColumnCount * mColumnCount;

        a.recycle();

        mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGridPaint.setStyle(Paint.Style.STROKE);
        mGridPaint.setColor(strokeColor);
        mGridPaint.setStrokeWidth(strokeWidth);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize, heightSize;

        //Get the width based on the measure specs
        widthSize = getDefaultSize(0, widthMeasureSpec);

        //Get the height based on measure specs
        heightSize = getDefaultSize(0, heightMeasureSpec);

        int majorDimension = Math.min(widthSize, heightSize);
        //Measure all child views
        int blockDimension = majorDimension / mColumnCount;
        int blockSpec = MeasureSpec.makeMeasureSpec(blockDimension, MeasureSpec.EXACTLY);
        measureChildren(blockSpec, blockSpec);

        //MUST call this to save our own dimensions
        setMeasuredDimension(majorDimension, majorDimension);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int row, col, left, top;
        for (int i=0; i < getChildCount(); i++) {
            row = i / mColumnCount;
            col = i % mColumnCount;
            View child = getChildAt(i);
            left = col * child.getMeasuredWidth();
            top = row * child.getMeasuredHeight();

            child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        //Let the framework do its thing
        super.dispatchDraw(canvas);

        //Draw the grid lines
        for (int i=0; i <= getWidth(); i += (getWidth() / mColumnCount)) {
            canvas.drawLine(i, 0, i, getHeight(), mGridPaint);
        }
        for (int i=0; i <= getHeight(); i += (getHeight() / mColumnCount)) {
            canvas.drawLine(0, i, getWidth(), i, mGridPaint);
        }
    }

    @Override
    public void addView(View child) {
        if (getChildCount() > mMaxChildren-1) {
            throw new IllegalStateException("BoxGridLayout cannot have more than "+mMaxChildren+" direct children");
        }

        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (getChildCount() > mMaxChildren-1) {
            throw new IllegalStateException("BoxGridLayout cannot have more than "+mMaxChildren+" direct children");
        }

        super.addView(child, index);
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        if (getChildCount() > mMaxChildren-1) {
            throw new IllegalStateException("BoxGridLayout cannot have more than "+mMaxChildren+" direct children");
        }

        super.addView(child, index, params);
    }

    @Override
    public void addView(View child, LayoutParams params) {
        if (getChildCount() > mMaxChildren-1) {
            throw new IllegalStateException("BoxGridLayout cannot have more than "+mMaxChildren+" direct children");
        }

        super.addView(child, params);
    }

    @Override
    public void addView(View child, int width, int height) {
        if (getChildCount() > mMaxChildren-1) {
            throw new IllegalStateException("BoxGridLayout cannot have more than "+mMaxChildren+" direct children");
        }

        super.addView(child, width, height);
    }
}
