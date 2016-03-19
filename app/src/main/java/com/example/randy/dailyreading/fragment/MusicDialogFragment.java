package com.example.randy.dailyreading.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.randy.dailyreading.R;
import com.example.randy.dailyreading.activity.MusicPlayActivity;

/**
 * Created by randy on 2016/2/21.
 * 定义dialog  采用DialogFragment：当旋转屏幕和按下后退键时可以更好的管理其声明周期
 */
public class MusicDialogFragment extends DialogFragment {

    private TextView msgText;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.music_fragment_dialog, null);
        msgText = (TextView)view.findViewById(R.id.message);
        msgText.setText("亲，你要退出音乐吗？");
        builder.setView(view)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ((MusicPlayActivity)getActivity()).stopMusicPlayService();
                        getActivity().finish();
                    }
                }).setNegativeButton("Cancel", null);

        return builder.create();

    }
}
