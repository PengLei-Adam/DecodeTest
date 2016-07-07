package com.example.decodetest.ui.fragment;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.decodetest.Constants;
import com.example.decodetest.media.PhotoInfo;
import com.example.decodetest.provider.MediaProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Scan and show the photos in Constants.PHOTO_DIRECTORY.
 * Created by leip on 2016/7/4.
 */
public class PhotoLibraryFragment extends MediaLibraryFragment{
    private static final String TAG = "PhotoLibraryFragment";

    private static final String REGEX_IMAGE = "^.+\\.(?i)(jpe?g|bmp|png)$";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


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

        int count = cr.delete(MediaProvider.CONTENT_URI_PHOTOS, null, null);

        Log.d(TAG, "delete numbers: " + count);
        getLoaderManager().restartLoader(0, null, PhotoLibraryFragment.this);
        scanMedia();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {


        CursorLoader loader = new CursorLoader(getActivity(), MediaProvider.CONTENT_URI_PHOTOS,
                null, null, null, null);

        return loader;
    }

    @Override
    public void scanMedia(){

        //scan the directory of photos and save the files' information
        File PhotoDir = new File(Constants.PHOTO_DIRECTORY);
        File[] files = PhotoDir.listFiles();
        Log.i(TAG, "files count: " + files.length);


        for(File f : files){
            if(!f.isDirectory() && f.getName().matches(REGEX_IMAGE)){
                String fileName = f.getName();

                Log.i(TAG, fileName + " is scanned");
                Date createTime = new Date(f.lastModified());
                long size = f.length();

                //TODO: read the width and height
                PhotoInfo photoInfo = new PhotoInfo(fileName,
                        createTime, size, 1280, 720);
                addNewItem(MediaProvider.CONTENT_URI_PHOTOS, photoInfo);


            }
        }


    }



}
