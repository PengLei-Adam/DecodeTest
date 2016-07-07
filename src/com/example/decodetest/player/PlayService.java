package com.example.decodetest.player;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by leip on 2016/7/1.
 */
public class PlayService extends Service {

    private static final String TAG = "PlayService";

    private RtspPlayer player;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
