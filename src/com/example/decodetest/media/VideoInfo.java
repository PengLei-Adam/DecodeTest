package com.example.decodetest.media;

import com.example.decodetest.Constants;

import java.util.Date;

/**
 * Created by leip on 2016/7/5.
 */
public class VideoInfo extends MediaInfo {
    public VideoInfo(String fileName, String filePath, Date createTime, long size, int width, int height) {
        super(fileName, filePath, createTime, size, width, height);
    }


    public VideoInfo(String fileName, Date createTime, long size, int width, int height) {
        super(fileName, createTime, size, width, height);
        filePath = Constants.VIDEO_DIRECTORY + "/" + fileName;
    }
}
