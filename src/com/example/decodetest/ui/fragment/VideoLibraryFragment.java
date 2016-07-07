package com.example.decodetest.ui.fragment;

import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.decodetest.Constants;
import com.example.decodetest.media.VideoInfo;
import com.example.decodetest.provider.MediaProvider;

import java.io.File;
import java.util.Date;

/**
 * Scan and show the videos in Constants.VIDEO_DIRECTORY
 * Created by leip on 2016/7/6.
 */
public class VideoLibraryFragment extends MediaLibraryFragment {

    private static final String TAG = "VideoLibraryFragment";

    private static final String REGEX_VIDEO = "^.+\\.(?i)(avi|mp4|flv)$";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //init loader
        getLoaderManager().initLoader(0, null, this);

        Log.d(TAG, "loader init");

        refreshList();
    }

    @Override
    protected void refreshList() {
        super.refreshList();

        int count = cr.delete(MediaProvider.CONTENT_URI_VIDEOS, null, null);

        Log.d(TAG, "delete numbers: " + count);
        getLoaderManager().restartLoader(0, null, VideoLibraryFragment.this);
        scanMedia();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

        CursorLoader loader = new CursorLoader(getActivity(), MediaProvider.CONTENT_URI_VIDEOS,
                null, null, null, null);

        return loader;
    }

    @Override
    protected void scanMedia() {

        //scan the directory of videos and save the files' information
        File videoDir = new File(Constants.VIDEO_DIRECTORY);
        File[] files = videoDir.listFiles();
        Log.i(TAG, "files count: " + files.length);

        for(File f : files){
            if(!f.isDirectory() && f.getName().matches(REGEX_VIDEO)){
                String fileName = f.getName();

                Log.i(TAG, fileName + " is scanned");
                Date createTime = new Date(f.lastModified());
                long size = f.length();

                //TODO: read the width and height
                VideoInfo videoInfo = new VideoInfo(fileName,
                        createTime, size, 1280, 720);
                addNewItem(MediaProvider.CONTENT_URI_VIDEOS, videoInfo);


            }
        }
    }
}
