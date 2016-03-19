package com.example.randy.dailyreading.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.util.Utils;

/**
 * Created by randy on 2015/12/19.
 */
public class RoundImageView extends ImageView {

    //圆角默认大小
    private static final int BORDER_RADIUS_DEFAULT = 10;
    //圆角的大小
    private int mBorderRadius;
    //Paint
    private Paint paint;
    //圆角的半径
    private int radius;

    private RectF rectF;

    /**
     * 3 X 3矩阵  用于放大缩小
     * @param
     */
    private Matrix matrix;

    /**
     * BitmapShader 渲染图像
     * @param
     */
    private BitmapShader mBitmapShader;
    /**
     * View 的宽度和高度
     * @param
     */
    private int mWidth;
    private int mHeight;
    private RectF mRoundRect;
    private Context context;

    public RoundImageView(Context context) {
        super(context);
        this.context = context;
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.context = context;
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        matrix = new Matrix();
        paint = new Paint();
        //获取View的宽高，这个宽高可以直接在xml章直接设置
        int width = getWidth();
        int height = getHeight();
        //根据view的大小绘制矩形
        rectF = new RectF(0, 0, width, height);
        paint.setAntiAlias(true);
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);
        mBorderRadius = a.getDimensionPixelSize(R.styleable.RoundImageView_borderRadius,
                (int) TypedValue
                        .applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                BORDER_RADIUS_DEFAULT, getResources()
                                        .getDisplayMetrics()));// default 10dp
        a.recycle();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        /*super.onDraw(canvas);
        Drawable drawable = getDrawable();
        if(null == drawable){
            return;
        }
        Bitmap bitmap = drawableToBitmap(drawable);
        Bitmap resizedBitmap = resizedBitmap(bitmap);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawRoundRect(rectF, mBorderRadius, mBorderRadius, paint);
        *//*final Rect rectSrc = new Rect(0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());
        final Rect rectDest = new Rect(0,0,getWidth(),getHeight());
        canvas.drawBitmap(resizedBitmap, rectSrc, rectDest, paint);*//*
        canvas.drawBitmap(resizedBitmap, 0, 0, paint);*/

        Drawable drawable = getDrawable();
        if (null != drawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Bitmap b = getRoundBitmap(bitmap, 20);
            final Rect rectSrc = new Rect(0, 0, b.getWidth(), b.getHeight());
            final Rect rectDest = new Rect(0,0,getWidth(),getHeight());
            paint.reset();
            canvas.drawBitmap(b, rectSrc, rectDest, paint);

        } else {
            super.onDraw(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //根据view的大小绘制一个矩形
//        mRoundRect = new RectF(0, 0, getWidth(), getHeight());
    }

    /**
     * 初始化BitmapShader
     */
    public void setupShader(Canvas canvas){

        Drawable drawable = getDrawable();
        if(null == drawable){
            return;
        }

        Bitmap bitmap = drawableToBitmap(drawable);
        //根据bitmap大小在创建一个同样大小的bitmap对象
//        Bitmap bitmap1 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap1);
        //获取View的宽高，这个宽高可以直接在xml章直接设置
        int width = getWidth();
        int height = getHeight();
        //根据view的大小绘制矩形
        RectF rectF = new RectF(0, 0, width, height);

        //缩放矩阵
//        Matrix matrix = new Matrix();
        matrix.postScale((float)width / bitmap.getWidth(), (float)height / bitmap.getHeight());

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//        mBitmapShader.setLocalMatrix(matrix);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, mBorderRadius, mBorderRadius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(resizedBitmap, matrix, paint);
    }

    public Bitmap resizedBitmap(Bitmap bitmap){
        //获取View的宽高，这个宽高可以直接在xml章直接设置
        int width = getWidth();
        int height = getHeight();
        //缩放矩阵
        Matrix matrix = new Matrix();
        matrix.postScale((float)width / bitmap.getWidth(), (float)height / bitmap.getHeight());
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return newBitmap;
    }

    /**
     * drawable 转换成 bitmap
     * @param drawable
     * @return
     */
    public Bitmap drawableToBitmap(Drawable drawable){
        if(drawable instanceof BitmapDrawable){
            BitmapDrawable bd = (BitmapDrawable)drawable;
            return bd.getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        //根据drawable对象的宽高新建一个bitmap对象
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //根据bitmap对象新建画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 添加一个方法  设置圆角的半径
     */
    public void setmBorderRadius(int borderRadius){

        int px = Utils.dp2px(borderRadius, context);
        if(this.mBorderRadius != px){
            this.mBorderRadius = px;
            invalidate();
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    private void drawRoundAngle(Canvas canvas)
    {
        Paint maskPaint = new Paint();
        maskPaint.setAntiAlias(true);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        Path maskPath = new Path();
        maskPath.addRoundRect(new RectF(0.0F, 0.0F, getWidth(), getHeight()), mBorderRadius, mBorderRadius, Path.Direction.CW);

        //这是设置了填充模式，非常关键
        maskPath.setFillType(Path.FillType.INVERSE_WINDING);
        canvas.drawPath(maskPath, maskPaint);
    }

    /**
     * 获取圆角矩形图片方法
     * @param bitmap
     * @param roundPx,一般设置成14
     * @return Bitmap
     * @author caizhiming
     */
    private Bitmap getRoundBitmap(Bitmap bitmap, int roundPx) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;

        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        int x = bitmap.getWidth();

        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;


    }
}


