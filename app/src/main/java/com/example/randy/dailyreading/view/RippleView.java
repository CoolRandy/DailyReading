package com.example.randy.dailyreading.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import com.example.randy.dailyreading.R;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;


/**
 * Created by ${randy} on 2015/10/5.
 * 自定义view，用于点击显示波纹效果
 */
public class RippleView extends FrameLayout {

    //手指按下坐标
    private float mDownX;
    private float mDownY;
    private int mRippleColor;
    private float mAlphaFactor;
    private boolean mHover = true;
    //屏幕密度
    private float mDestiny;
    //半径
    private float mRadius;
    //绘图的画笔
    private Paint mPaint;
    //波纹圆的最大半径
    private float mMaxRadius;


    //属性动画
    private ObjectAnimator objectAnimator;
    private ObjectAnimator mRadiusAnimator;
    //动画正在执行标志位
    private boolean mIsAimating = false;
    //环形渲染
    private RadialGradient mRadialGrandient;

    public RippleView(Context context) {
        this(context, null);
    }

    public RippleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RippleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.RippleView);
        mRippleColor = a.getColor(R.styleable.RippleView_rippleColor1,
                mRippleColor);
        mAlphaFactor = a.getFloat(R.styleable.RippleView_alphaFactor,
                mAlphaFactor);
        mHover = a.getBoolean(R.styleable.RippleView_hover, mHover);
        a.recycle();
    }
    //初始化一些参数
    public void init() {
        mDestiny = getContext().getResources().getDisplayMetrics().density;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAlpha(100);
        setRippleColor(getResources().getColor(R.color.light_blue), 0.2f);

    }

    public void setRippleColor(int rippleColor, float alphaFactor){
        mRippleColor = rippleColor;
        mAlphaFactor = alphaFactor;
    }

    public void setHover(boolean enable){
        mHover = enable;//用于控制动画使能的
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMaxRadius = (float) Math.sqrt(w * w + h * h);
    }
    //动画取消标志位
    private boolean mAnimationIsCancle;
    private Rect mRect;

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        Log.d("TouchEvent", String.valueOf(event.getActionMasked()));
        Log.d("mIsAnimating", String.valueOf(mIsAimating));
        Log.d("mAnimationIsCancel", String.valueOf(mAnimationIsCancle));
        boolean superResult = super.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                && this.isEnabled() && mHover) {
            mRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
            mAnimationIsCancle = false;
            mDownX = event.getX();
            mDownY = event.getY();

            mRadiusAnimator = ObjectAnimator.ofFloat(this, "radius", 0, dp(50))
                    .setDuration(300);
            mRadiusAnimator
                    .setInterpolator(new AccelerateDecelerateInterpolator());
            mRadiusAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mIsAimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    setRadius(0);
                    ViewHelper.setAlpha(RippleView.this, 1);
                    mIsAimating = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            mRadiusAnimator.start();
            if (!superResult) {
                return true;
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE
                && this.isEnabled() && mHover) {
            mDownX = event.getX();
            mDownY = event.getY();

            // Cancel the ripple animation when moved outside
            //判断如果手指移动超出了该view，则取消动画
            //判断原则即：首先可以拿到view相对屏幕的左上角的坐标(getLeft(), getTop()),即view的坐标原点
            //然后可以拿到手指点击位置的坐标即(event.getX(), event.getY())
            //这样如果坐标点(getLeft() + event.getX(), getTop() + event.getY())不在Rect内部，即超出了view，就取消动画
            if (mAnimationIsCancle = !mRect.contains(
                    getLeft() + (int) event.getX(),
                    getTop() + (int) event.getY())) {
                setRadius(0);
            } else {
                setRadius(dp(50));
            }
            if (!superResult) {//如果超类返回的是false，则处理为return true，即消费掉该事件，不再向下传递
                return true;
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP
                && !mAnimationIsCancle && this.isEnabled()) {
            mDownX = event.getX();
            mDownY = event.getY();

            final float tempRadius = (float) Math.sqrt(mDownX * mDownX + mDownY
                    * mDownY);
            float targetRadius = Math.max(tempRadius, mMaxRadius);

            if (mIsAimating) {
                mRadiusAnimator.cancel();
            }
            mRadiusAnimator = ObjectAnimator.ofFloat(this, "radius", dp(50),
                    targetRadius);
            mRadiusAnimator.setDuration(300);
            mRadiusAnimator
                    .setInterpolator(new AccelerateDecelerateInterpolator());
            mRadiusAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mIsAimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    setRadius(0);
                    ViewHelper.setAlpha(RippleView.this, 1);
                    mIsAimating = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            mRadiusAnimator.start();
            if (!superResult) {
                return true;
            }
        }
        return superResult;
    }

    private int dp(int dp) {
        return (int) (dp * mDestiny + 0.5f);
    }

    //设置半径  TODO 参数出错   应该是float而不是int  细节问题，还需要多想想
    public void setRadius(final float radius){
        //mRadius = (float)radius;  //传入整形强制转换为浮点型，没有效果，不可行？？
        mRadius = radius;
        if(mRadius > 0){
            //调用环形渲染波纹效果
            mRadialGrandient = new RadialGradient(mDownX, mDownY,
                    mRadius, adjustAlpha(mRippleColor, mAlphaFactor), mRippleColor, Shader.TileMode.MIRROR);
            //用画笔绘制环形渐变
            mPaint.setShader(mRadialGrandient);
        }
        //重绘
        invalidate();
    }
    //调整颜色、透明度
    public int adjustAlpha(int color, float factor){
        int alpha = Math.round(Color.alpha(color) * factor);
        //获取3元色
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    private Path path = new Path();
    //重写onDraw
    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        if(isInEditMode()){
            return;
        }
        canvas.save(Canvas.CLIP_SAVE_FLAG);
        path.reset();
        path.addCircle(mDownX, mDownY, mRadius, Path.Direction.CW);
        canvas.clipPath(path);
        canvas.restore();
        //NPE
        canvas.drawCircle(mDownX, mDownY, mRadius, mPaint);
    }
}
