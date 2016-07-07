package com.example.decodetest.media;

import com.example.decodetest.Constants;

import java.util.Date;

/**
 * Class to store info of photos or videos
 * Created by leip on 2016/7/5.
 */
public class MediaInfo {

    //Media info
    private String fileName;
    protected String filePath;
    private Date createTime;
    private long size;
    private int width;
    private int height;

    public MediaInfo(String fileName, String filePath, Date createTime, long size, int width, int height) {
        this.height = height;
        this.fileName = fileName;
        this.filePath = filePath;
        this.createTime = createTime;
        this.size = size;
        this.width = width;
    }

    public MediaInfo(String fileName, Date createTime, long size, int width, int height) {
        this.fileName = fileName;
        this.createTime = createTime;
        this.size = size;
        this.width = width;
        this.height = height;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public long getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getHeight() {
        return height;
    }

    public String getFilePath(){
        return filePath;
    }

    public int getWidth() {
        return width;
    }

    public void setAspect(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
