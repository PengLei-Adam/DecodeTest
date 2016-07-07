package com.example.decodetest;

import android.os.Environment;

/**
 * Created by leip on 2016/6/3.
 */
public final class Constants {
    public static final String VIDEO_DIRECTORY =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/MicroVideo";
    public static final String PHOTO_DIRECTORY =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MicroPhoto";

    public static final String COMPANY_NAME =
            "example";
    public static final String COMPANY_PATH =
            "com." + COMPANY_NAME;
}
