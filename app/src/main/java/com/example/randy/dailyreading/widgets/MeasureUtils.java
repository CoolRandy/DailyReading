package com.example.randy.dailyreading.widgets;

import android.view.View;

/**
 * Created by randy on 2015/10/29.
 */
public class MeasureUtils {

    /**
     * Utility to return a view's standard measurement. Uses the
     * supplied size when constraints are given. Attempts to
     * hold to the desired size unless it conflicts with provided
     * constraints.
     * 该方法用来返回view的标准测量值。试图在不与父类view冲突的情况下，保持该view为希望的尺寸
     * 该方法参照View源码中的超类onMeasure中的getDefaultSize(int size, int measureSpec)方法来编写
     * @param measureSpec Constraints imposed by the parent  父view限制的规格
     * @param contentSize Desired size for the view  需要的尺寸
     * @return The size the view should be.
     */

    public static int getMeasurement(int contentSize, int measureSpec){
        int resultSize = 0;
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);
        switch (specMode){
            case View.MeasureSpec.UNSPECIFIED://父元素不对子元素任何限制
                //Big as we want to be
                resultSize = contentSize;
                break;
            case View.MeasureSpec.AT_MOST://子view最多达到指定大小的值，所以这里只能取两者中较小的
                //Big as we want to be, up to the spec
                resultSize = Math.min(contentSize, specSize);
                break;
            case View.MeasureSpec.EXACTLY://父view决定子view的规格大小
                //Must be the spec
                resultSize = specSize;
                break;
        }
        return resultSize;
    }
}
