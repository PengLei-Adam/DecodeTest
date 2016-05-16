package com.example.decodetest;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;

/**
 * Save Bitmap to file
 * Created by leip on 2016/4/14.
 */
public class BmpSaver extends FileUtils implements Callable{

    private static String TAG = "BmpSaver";


    private Bitmap bitmap = null;


    private void initPicturePath(){
        SDPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/";
    }
    public BmpSaver(){
        initPicturePath();
    }
    public BmpSaver(String path, String fileName, Bitmap bmp){
        initPicturePath();
        this.wholePath = path.startsWith("/") ? path : SDPath + path;
        this.fileName = fileName;
        this.bitmap = bmp;
    }

    public File getFile() {
        return file;
    }

    /**
     * Write from Bitmap to file
     *
     * @param path String: if null use wholePath first(if wholePath null use SDPath),
     *             if a single word use SDPath + path,
     *             if
     * @param filename String: if null use current time
     * @param bmp Bitmap
     * */
    public File writeFromBmp(String path, String filename, Bitmap bmp){
        File file = null;
        OutputStream output = null;
        setWholePath(path);
        this.fileName = filename == null ? ("IMG_" + getTimeStr("yyyyMMdd_hhmmss") +".jpg") : filename;
        this.bitmap = bmp;
        try {
            createSDDir(wholePath);
            file = createSDFile(wholePath + (wholePath.endsWith("/") ? "" : "/") + this.fileName);
            output = new FileOutputStream(file);

            bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);
        }catch (Exception e){
            Log.e(TAG, "failed output", e);
        }
        finally {
            try{
                output.close();
            }catch (Exception e){
                Log.e(TAG, "failed to close output stream", e);
            }
        }
        Log.i(TAG, "finish saving picture");
        return this.file = file;
    }

    private String getTimeStr(String dateFormat){
        SimpleDateFormat date = new SimpleDateFormat(dateFormat);
        return date.format(new java.util.Date());
    }

    public void setBitmap(Bitmap bmp){
        this.bitmap = bmp;
    }


    @Override
    public Object call() throws Exception {
        return writeFromBmp(this.wholePath, this.fileName, this.bitmap);
    }
}
