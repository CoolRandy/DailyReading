package com.example.randy.dailyreading.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.widgets.MeasureUtils;

/**
 * Created by randy on 2015/10/29.
 */
public class DoubleImageView extends View {

    //Image 内容
    private Drawable leftDrawable, rightDrawable;
    //Text 内容
    private CharSequence mText;
    //StaticLayout是android中处理文字换行的一个工具类，StaticLayout已经实现了文本绘制换行处理
    private StaticLayout textLayout;

    //TextPaint是Paint的子类，用来绘制文字的
    private TextPaint textPaint;
    private Point textOrigin;
    //logo和text之间的空格
    private int mSpacing;

    public DoubleImageView(Context context) {
        //TODO 这里要自定义view，所以不使用超类的默认实现？？
        //super(context);
        this(context, null);
    }

    public DoubleImageView(Context context, AttributeSet attrs) {
        //super(context, attrs);
        this(context, null, 0);
    }

    public DoubleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);//文字画笔
        textOrigin = new Point(0, 0);//坐标起点

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.DoubleImageView, 0, defStyleAttr);
        //左边的logo
        Drawable d = a.getDrawable(R.styleable.DoubleImageView_android_drawableLeft);
        if(null != d){
            //如果左边的drawable不为空，则设置下来
            setLeftDrawable(d);
        }
        //右边的logo
        d = a.getDrawable(R.styleable.DoubleImageView_android_drawableRight);
        if(null != d){
            setRightDrawable(d);
        }
        //空格
        //getDimensionPixelOffset和getDimension方法功能是一样的，唯一不同就是返回值转换成了integer pixels 该属性值必须为dimension  defValue这里设置为0
        int spacing = a.getDimensionPixelOffset(R.styleable.DoubleImageView_android_spacing, 0);
        setSpacing(spacing);

        //text  color size
        //画笔设置颜色
        int color = a.getColor(R.styleable.DoubleImageView_android_textColor, 0);
        textPaint.setColor(color);
        //画笔设置text大小
        int textSize = a.getDimensionPixelSize(R.styleable.DoubleImageView_android_textSize, 0);
        textPaint.setTextSize(textSize);

        //设置文字
        CharSequence text = a.getText(R.styleable.DoubleImageView_android_text);
        setText(text);
        //一定要释放掉TypedArray
        a.recycle();
    }

    public void setLeftDrawable(Drawable left){

        leftDrawable = left;
        //更新view边界
        updateContentBounds();
        //重新绘制  Invalidate the whole view
        invalidate();
    }

    public void setRightDrawable(Drawable right){
        rightDrawable = right;
        updateContentBounds();
        invalidate();
    }

    public void setSpacing(int spacing){
        mSpacing = spacing;
        updateContentBounds();
        invalidate();
    }

    public void setText(CharSequence textContent){
        if(!TextUtils.equals(textContent, mText)){
            mText = textContent;
            updateContentBounds();
            invalidate();
        }
    }

    public void updateContentBounds(){
        //先计算text的宽度
        if(null ==  mText){
            mText = "";
        }
        //测量 mText文字的宽度
        float  textWidth = textPaint.measureText( mText, 0,  mText.length());
        //采用StaticLayout处理
        textLayout = new StaticLayout( mText, textPaint, (int)textWidth, Layout.Alignment.ALIGN_CENTER, 1f, 0f, false);

        //测量logo
        int left = (getWidth() - getDesiredWidth()) / 2;
        int top = (getHeight() - getDesiredHeight()) / 2;

        if(null != leftDrawable){
            //为drawable对象设置矩形边界，以wrap_content的形式刚好包裹住drawable对象
            //setBounds的方法说明： Specify a bounding rectangle for the Drawable. This is where the drawable will draw when its draw() method is called.
            leftDrawable.setBounds(left, top, left + leftDrawable.getIntrinsicWidth(), top + leftDrawable.getIntrinsicHeight());
            //由于第二张logo叠加在第一张上面，所以笫二张logo的左上角边界坐标需根据第一张的宽高来调整，这一块可以根据需要自由控制重叠的大小
            left += (leftDrawable.getIntrinsicWidth()) * 0.33;
            top += (leftDrawable.getIntrinsicHeight()) * 0.33;
        }

        //绘制第二张logo
        if(null != rightDrawable){
            rightDrawable.setBounds(left, top, left + rightDrawable.getIntrinsicWidth(), top + rightDrawable.getIntrinsicHeight());
            //调整left，显示text
            left = rightDrawable.getBounds().left + mSpacing;
        }

        //绘制text
        if(null != textLayout){
            top = (getHeight() - textLayout.getHeight()) / 2;
            textOrigin.set(left, top);//设置文字绘制起始坐标，每次更新view都需要重新设定text绘制的起始点坐标
        }
    }

    public int getDesiredWidth(){
        //TODO 根据刚才logo重叠绘制可知，两个logo叠加总的宽度是(这里简化两张logo大小完全一样，不同的logo原理是一样的)
        //TODO leftDrawable.getIntrinsicWidth() + rightDrawable.getIntrinsicWidth() * 0.33也即0.67 + 0.33 * 2
        // TODO 所以这里为了尽可能的刚好矩形边界包裹整个自定义控件，设置覆盖logo的宽度为：
        // TODO （leftDrawable.getIntrinsicWidth() + rightDrawable.getIntrinsicWidth()） * 0.67

        int leftWidth;
        if(null == leftDrawable){
            leftWidth = 0;
        }else {
            leftWidth = leftDrawable.getIntrinsicWidth();
        }

        int rightWidth;
        if(null == rightDrawable){
            rightWidth = 0;
        }else {
            rightWidth = rightDrawable.getIntrinsicHeight();
        }

        int textWidth;
        if(null == textLayout){
            textWidth = 0;
        }else {
            textWidth = textLayout.getWidth();
        }

        return (int)(leftWidth * 0.67f + rightWidth * 0.67f + mSpacing + textWidth);
    }

    /**
     * 由于text的高度小于logo，所以只需要考虑logo就可以了
     * @return
     */
    public int getDesiredHeight(){

        int leftHeight;
        if(null == leftDrawable){
            leftHeight = 0;
        }else {
            leftHeight = leftDrawable.getIntrinsicHeight();
        }

        int rightHeight;
        if(null == rightDrawable){
            rightHeight = 0;
        }else {
            rightHeight = rightDrawable.getIntrinsicHeight();
        }

        return (int)(leftHeight * 0.67f + rightHeight * 0.67f);
    }
    /**
     * This is called during layout when the size of this view has changed. If
     * you were just added to the view hierarchy, you're called with the old
     * values of 0.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //super.onSizeChanged(w, h, oldw, oldh);
        if(w != oldw || h != oldh){
            updateContentBounds();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取测量的宽度,高度
        int widthSize = MeasureUtils.getMeasurement(getDesiredWidth(), widthMeasureSpec);
        int heightSize = MeasureUtils.getMeasurement(getDesiredHeight(), heightMeasureSpec);

        //必须要调用下面的方法来设置测量的宽高
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制 对于drawable对象自带draw方法，可以直接利用，对于text则需要额外处理
        if(null != leftDrawable){
            leftDrawable.draw(canvas);
        }
        if(null != textLayout){
            canvas.save();//锁画布，即保存之前的画布状态
            canvas.translate(textOrigin.x, textOrigin.y);//将当前画布原点移动到(textOrigin.x, textOrigin.y)，后面的操作都以这个新的原点为参考点
            textLayout.draw(canvas);//在画布上开始绘制
            canvas.restore();//重新保存画布状态
        }
        if(null != rightDrawable){
            rightDrawable.draw(canvas);
        }
    }
}
