package com.example.randy.dailyreading.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.widgets.LoadingDialog;

/**
 * Created by ${randy} on 2015/10/8.
 */
public class UIUtil {

    private static LoadingDialog loadingDialog;

    /**
     * 显示通用加载框
     */
    public static void showLoadingDialog(Context context){

        showLoadingDialog(context, true);
    }

    /**
     * 显示加载框
     */
    public static void showLoadingDialog(Context context, boolean cancleable){

        //比较好的处理方式是在显示加载对话框之前判断loadingDialog是否为空，如果不是先要取消掉
        try{
            if(null != loadingDialog){
                loadingDialog.dismiss();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            loadingDialog = null;
        }

        try{
            loadingDialog = new LoadingDialog(context, R.style.LoadingDialogStyle);
            loadingDialog.show();
            loadingDialog.setCanceledOnTouchOutside(false);//点击窗口外禁止取消
            loadingDialog.setCancelable(cancleable);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 取消动画框显示
     */
    public static void cancleLoadingDialog(){
        try{
            if(null != loadingDialog && loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
            loadingDialog = null;//防止内存泄露
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 动画加载失败，即泡米花扑街的情况
     */
    public static void showLoadingFailedLayout(Activity activity, final View.OnClickListener listener){

        final View loading_failed_layout = activity.findViewById(R.id.loading_fail_layout);
        loading_failed_layout.setVisibility(View.VISIBLE);
        loading_failed_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading_failed_layout.setVisibility(View.GONE);
                listener.onClick(view);//重写该点击方法，可以处理点击时重新加载数据
            }
        });
    }



}
