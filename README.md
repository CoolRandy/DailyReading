# DailyReading
app主要是仿照知乎日报实现的 资讯类应用
----------------
项目代码暂时放在了develop分支上
----------------------------
关于自定义圆角矩形的实现说明：
bitmap绘制：
1.首先调用createBitmap创建一个bitmap对象：设置新建bitmap的宽高 Bitmap target = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
其中mWidth和mHeight分别是自定义view的宽和高 mHeight = getHeight();  mWidth = getWidth();
2.根据bitmap对象创建画布Canvas Canvas canvas = new Canvas(target);这里target就是canvas原有的图片即背景，也就是dst
3.以圆角矩形为例，采用矩形绘制，根据mWidth和mHeight创建一个矩形 RectF rect = new RectF(0, 0, mWidth, mHeight);
4.直接调用画布的绘制圆角矩形的方法，采用指定画笔绘制矩形 canvas.drawRoundRect(rect, 20, 20, paint);  到这里仍然是dst的图片绘制，即背景图为一个圆角矩形
5.设置画笔的Xfermode，这里采用Src-in的方式  paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
6.开始在画布上绘制源bitmap，这里为了使得原始图片大小充满整个圆角矩形，采用矩阵的方式对图片进行缩放
 ```Java
 canvas.drawBitmap(resize(source), 0, 0, paint);
 private Bitmap resize(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        //注意：参数必须是float类型
        matrix.postScale((float)mWidth/(float)bitmap.getWidth(), (float)mHeight/(float)bitmap.getHeight()); //长和宽放大缩小的比例
        //java.lang.IllegalArgumentException: x + width must be <= bitmap.width()
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        return resizeBmp;
    }
 ``` 
这里调用矩阵对象的postScale方法，传入的参数分别为：自定义view的宽与原始图片的宽的比值，以及自定义view的高与原始图片的高的比值，根据比值的大小进行缩放，从而避免图片不能充满view或者view不够显示的情况
