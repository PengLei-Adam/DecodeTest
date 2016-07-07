package com.example.decodetest.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by leip on 2016/1/17.
 */
public class NalReader {
    private static final String TAG = "NalReader";

    private String filename;

    FileInputStream inputStream;

    int nalLength = 0;
    //最大缓存
    byte[] bigBuffer = new byte[1400 * 70 + 1];
    //返回的nal缓存
    byte[] nalBuffer;

    public NalReader(String filename){

        this.filename = filename;
    }

    public int getNalLength() {
        return nalLength;
    }

    public void open(){
        try {
            inputStream = new FileInputStream(filename);
        } catch (IOException e){
            Log.e(TAG, "failed to open input stream");
        }
    }

    private boolean isBlank = false;
    private boolean findHeader = true;
    public int readNal(){
        nalLength = 0;
        int readByte;
        byte[] readBuffer = new byte[3];
        try {
            while ((readByte = inputStream.read()) >= 0) {
                if (!isBlank) {
                    if (readByte != 0) {
                        bigBuffer[nalLength++] = (byte) readByte;
                    } else {
                        inputStream.read(readBuffer, 0, 3);
                        if (readBuffer[0] == 0 && readBuffer[1] == 0 && readBuffer[2] == 0) {
                            isBlank = true;
                            if(findHeader){
                                findHeader = false;
                                return nalLength;
                            }else
                                continue;
                        }else {
                            bigBuffer[nalLength++] = (byte)readByte;
                            bigBuffer[nalLength++] = readBuffer[0];
                            bigBuffer[nalLength++] = readBuffer[1];
                            bigBuffer[nalLength++] = readBuffer[2];

                        }
                    }
                }
                else if(readByte == 1){
                    bigBuffer[nalLength++] = 0;
                    bigBuffer[nalLength++] = 0;
                    bigBuffer[nalLength++] = 0;
                    bigBuffer[nalLength++] = 1;
                    isBlank = false;
                    findHeader = true;

                }
            }
            Log.d(TAG, "Close input stream");
            inputStream.close();
        } catch (IOException e){
            Log.e(TAG, "read bytes error");
        }
        return 0;
    }

    public byte[] getNalBuffer() {
        if(nalLength > 0){
            nalBuffer = new byte[nalLength];
            System.arraycopy(bigBuffer, 0, nalBuffer, 0, nalLength);
            return nalBuffer;
        }else
            return null;
    }
}
