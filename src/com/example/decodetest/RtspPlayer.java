package com.example.decodetest;

import android.util.Log;
import edu.tfnrc.rtp.codec.h264.NativeH264Decoder;

/**
 * Created by leip on 2016/3/22.
 */
public class RtspPlayer extends Thread{
    private static String TAG = "RtspPlayer";

    private VideoSurfaceView surfaceView = null;

    private String uri;

    //whether interrupt the decoding circling
    private int interrupt = 0;

    public RtspPlayer(String uri, VideoSurfaceView surfaceView){
        this.uri = uri;
        this.surfaceView = surfaceView;
    }

    public String getUri(){
        return this.uri;
    }

    public void setUri(String uri){
        this.uri = uri;
    }
    public void preparePlayer(){
    }
    public void stopPlaying(){

    }

    public void closePlayer(){
        NativeH264Decoder.SetDecoderStatus(3);
        if(NativeH264Decoder.DeinitDecoder() == 0)
            Log.d(TAG, "Deinit finish");
        surfaceView.clearImage();
    }

    @Override
    public void run() {
        if(NativeH264Decoder.InitDecoder() < 0){
            Log.d(TAG,"Failed to init decoder");
        }
        NativeH264Decoder.DecodeAndConvert(uri, this.surfaceView);
    }
}
