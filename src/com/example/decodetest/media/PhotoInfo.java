package com.example.decodetest.media;

import com.example.decodetest.Constants;

import java.util.Date;

/**
 * Created by leip on 2016/7/5.
 */
public class PhotoInfo extends MediaInfo {

    public PhotoInfo(String fileName, String filePath, Date createTime, long size, int width, int height) {
        super(fileName, filePath, createTime, size, width, height);
    }

    public PhotoInfo(String fileName, Date createTime, long size, int width, int height) {
        super(fileName, createTime, size, width, height);
        filePath = Constants.PHOTO_DIRECTORY + "/" + fileName;

    }

}
