package com.example.decodetest.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.example.decodetest.activity.DecodeMain;
import com.example.decodetest.ui.surface.OnSurfaceViewChangedListener;
import com.example.decodetest.R;
import com.example.decodetest.ui.button.PlayStateButton;
import com.example.decodetest.ui.button.RecordStateButton;

/**
 * 视频显示的界面Fragment
 * Created by leip on 2016/5/27.
 */
public class VideoSurfaceFragment extends Fragment {

    private static final String TAG = "VideoSurfaceFragment";

//    VideoSurfaceView surfaceView = null;

    OnSurfaceViewChangedListener surfaceViewAddedListener;

    Handler mHandler = new Handler();

    RelativeLayout buttonsBand;

    PlayStateButton buttonStartStop;
    RecordStateButton buttonRecord;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        surfaceViewAddedListener = (OnSurfaceViewChangedListener) activity;

    }

    //whether the screen is landscape
    private boolean isLandscape = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.video_surface, container,false);

        RelativeLayout videoBox = (RelativeLayout)view.findViewById(R.id.videoBox);

        buttonsBand = (RelativeLayout)view.findViewById(R.id.buttonsBand);

        DecodeMain mainActivity = (DecodeMain)getActivity();

        buttonStartStop = (PlayStateButton)view.findViewById(R.id.buttonStartStop);
        buttonStartStop.setOnClickListener(mainActivity);
        mainActivity.addButtonObserver(buttonStartStop);

        ((ImageButton) view.findViewById(R.id.buttonShoot)).setOnClickListener(mainActivity);

        buttonRecord = (RecordStateButton) view.findViewById(R.id.buttonRecord);
        buttonRecord.setOnClickListener(mainActivity);
        mainActivity.addButtonObserver(buttonRecord);


        videoBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLandscape) {
                    if (buttonsBand.getVisibility() != View.VISIBLE) {
                        //点击显示按钮，过3秒后自动消失
                        buttonsBand.bringToFront();
                        buttonsBand.setVisibility(View.VISIBLE);
                        mHandler.postDelayed(autoHideButtons, 3000);
                    }else{
                        //按钮可见时，点击屏幕，按钮消失
                        mHandler.removeCallbacks(autoHideButtons);
                        buttonsBand.setVisibility(View.GONE);
                    }
                }
            }
        });
        surfaceViewAddedListener.onSurfaceViewAdded(videoBox);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true;
            buttonsBand.bringToFront();
        } else {
            isLandscape = false;
            buttonsBand.setVisibility(View.GONE);
        }
        super.onConfigurationChanged(newConfig);
    }

    private Runnable autoHideButtons = new Runnable() {
        @Override
        public void run() {
            buttonsBand.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DecodeMain mainActivity = (DecodeMain)getActivity();
        mainActivity.deleteButtonObserver(buttonStartStop);
        mainActivity.deleteButtonObserver(buttonRecord);
    }
}
