package com.example.decodetest.ui.surface;

import android.view.ViewGroup;

/**
 * Created by leip on 2016/5/27.
 */
public interface OnSurfaceViewChangedListener {
    public void onSurfaceViewAdded(ViewGroup viewGroup);

    public void onVideoStartError();
}
