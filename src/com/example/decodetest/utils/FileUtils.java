package com.example.decodetest.utils;

import android.os.Environment;

import java.io.*;

/**
 * Tools for Saving IutputStream to file
 * Created by leip on 2016/4/14.
 */
public class FileUtils {

    private static String TAG = "FileUtils";

    protected String SDPath;

    /*
    * If wholePath has been set,then use wholePath,
    * or use SDPath in default.
    * */
    protected String wholePath = null;
    protected String fileName = null;
    protected File file = null;

    public FileUtils(){
        SDPath = Environment.getExternalStorageDirectory() + "/";
    }

    /**
    * Create a new file with given file name
    * */
    public File createSDFile(String fileName) throws IOException {
        return new File(fileName);
    }

    /**
     * Create a directory.
     * If dirName is a whole path from root, create dirName.
     * If dirName is a single word, create SDPath + dirName
     *
     * @param dirName
     * */
    public File createSDDir(String dirName) throws IOException{
        if(dirName == null)
            return null;
        File dir;
        if(dirName.startsWith("/"))
            dir = new File(dirName);
        else
            dir = new File(SDPath + dirName);
        if(dir.mkdirs() || dir.isDirectory())
            return dir;
        else{
            throw new FileNotFoundException();

        }


    }

    /**
    * Check whether the file exits
    * */
    public boolean isFileExist(String fileName){
        File file = new File(SDPath + fileName);
        return file.exists();
    }
    public boolean isFileExist(String path, String fileName){
        File file = new File(path + fileName);
        return file.exists();
    }
    /*
    * Get OutputStream from path name and file name
    * */
    public OutputStream getOutputStream(String path, String fileName)throws IOException{

        createSDDir(path);
        File file = createSDFile(path + (path.endsWith("/") ? "" :"/") + fileName);
        return new FileOutputStream(file);

    }
    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setWholePath(String path) {
        if(path == null) {
            if(wholePath == null)
                wholePath = SDPath;
        }else if(path.startsWith("/")){
            wholePath = path;
        }else{
            wholePath = SDPath + path;
        }
    }
    public String getWholePath(){
        return wholePath;
    }

    /**
    * Write bytes from InputStream to file
    *
    * @param path String: if null use wholePath first(if wholePath null use SDPath),
     *            if a single word use SDPath + path,
     *            if a whole path use it directly
    * @param fileName String
    * @param input InputStream
    * */
    public File writeFromInput(String path, String fileName, InputStream input){
        File file = null;
        OutputStream output = null;
        setWholePath(path);
        this.fileName = fileName;
        try{
            createSDDir(wholePath);
            file = createSDFile(wholePath + (wholePath.endsWith("/") ? "" :"/") + this.fileName);
            output = new FileOutputStream(file);
            byte buffer[] = new byte[4 * 1024];
            while((input.read(buffer)) != -1){
                output.write(buffer);
            }
            output.flush();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                output.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return this.file = file;
    }
}
