package com.example.decodetest.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.decodetest.activity.DecodeMain;
import com.example.decodetest.R;
import com.example.decodetest.ui.button.PlayStateButton;
import com.example.decodetest.ui.button.RecordStateButton;

/**
 * Created by leip on 2016/5/27.
 */
public class MainButtonsFragment extends Fragment {

    RecordStateButton buttonRecord;
    PlayStateButton buttonStartStop;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.buttons_fragment, container, false);

        DecodeMain mainActivity = (DecodeMain)getActivity();

        buttonRecord = (RecordStateButton) view.findViewById(R.id.buttonRecord);
        buttonRecord.setOnClickListener(mainActivity);
        mainActivity.addButtonObserver(buttonRecord);

        buttonStartStop = (PlayStateButton)view.findViewById(R.id.buttonStartStop);
        buttonStartStop.setOnClickListener(mainActivity);
        mainActivity.addButtonObserver(buttonStartStop);

        ImageButton buttonShoot = (ImageButton)view.findViewById(R.id.buttonShoot);
        buttonShoot.setOnClickListener(mainActivity);

        return view;
    }
}
