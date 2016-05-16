package com.example.decodetest;

import android.os.Environment;
import android.util.Log;
import edu.tfnrc.rtp.codec.h264.NativeH264Decoder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by leip on 2016/3/22.
 */
public class RtspPlayer implements Runnable{
    private static String TAG = "RtspPlayer";

    private VideoSurfaceView surfaceView = null;

    private String uri;

    private String videoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/";


    public RtspPlayer(String uri, VideoSurfaceView surfaceView){
        this.uri = uri;
        this.surfaceView = surfaceView;
    }
    public RtspPlayer(VideoSurfaceView surfaceView){
        this.uri = null;
        this.surfaceView = surfaceView;
    }

    public String getUri(){
        return this.uri;
    }

    public void setUri(String uri){
        this.uri = uri;
    }

    public String getPhotoPath() {
        return surfaceView.getPhotoPath();
    }

    public void setPhotoPath(String photoPath) {
        surfaceView.setPhotoPath(photoPath);
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        if(videoPath != null) this.videoPath = videoPath;
        File videoDir = new File(videoPath);
        videoDir.mkdirs();

    }

    /*
            * Init the player and decoder
            * Called once when created
            * */
    public void prepare(){
        if(NativeH264Decoder.InitDecoder() < 0){
            Log.d(TAG,"Failed to init decoder");
        }else
            surfaceView.setState(PlayerState.STATE_READY);
    }
    /*
    * Take photo and save it to file
    * */
    public void takePicture(){
        if(surfaceView.getState() == PlayerState.STATE_PLAY) {

            surfaceView.setState(PlayerState.STATE_SHOOT);
        }
    }
    /*
    * Start recording the video and save to file
    * */

    public void startRecord(){
        String timeStr = new SimpleDateFormat("yyyyMMdd_hhmmss").format(new Date());
        NativeH264Decoder.SetVideoName(videoPath + "VID_" + timeStr + ".avi");
        if(NativeH264Decoder.GetDecoderState() == PlayerState.STATE_PLAY){
            NativeH264Decoder.SetDecoderState(PlayerState.STATE_RECORD);
        }
    }

    /*
    * End recording the video
    * */
    public void endRecord(){
        if(NativeH264Decoder.GetDecoderState() == PlayerState.STATE_RECORD){
            NativeH264Decoder.SetDecoderState(PlayerState.STATE_END_RECORD);
        }
    }
    /*
    * Stop decoding and set state ready
    * */
    public void stopPlay(){
        if(NativeH264Decoder.DeinitDecoder() == 0)
            Log.d(TAG, "Deinit finish");

        surfaceView.setState(PlayerState.STATE_READY);
        surfaceView.clearImage();
        surfaceView.close();
    }

    @Override
    public void run() {
        surfaceView.setState(PlayerState.STATE_PLAY);
        NativeH264Decoder.DecodeAndConvert(uri, this.surfaceView);
    }


}

