package com.example.decodetest.ui.button;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;
import com.example.decodetest.activity.DecodeMain;
import com.example.decodetest.R;

/**
 * Created by leip on 2016/5/30.
 */
public class RecordStateButton extends ImageButton implements ButtonObserver {
    public RecordStateButton(Context context){
        super(context);
    }

    public RecordStateButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordStateButton(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void update(Activity activity) {
        if(((DecodeMain)activity).getButtonRecordState()){
            this.setImageResource(R.drawable.ic_record_red);
        }else {
            this.setImageResource(R.drawable.ic_record_purple);
        }
    }
}
