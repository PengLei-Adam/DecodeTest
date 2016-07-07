package com.example.decodetest.player;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.decodetest.Constants;
import com.example.decodetest.R;
import com.example.decodetest.ui.surface.VideoSurfaceView;

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

    private String videoPath = Constants.VIDEO_DIRECTORY;

//    private Handler mhandler;

    private Runnable toastRecordStart = new Runnable() {
        @Override
        public void run() {
            if(surfaceView != null)
                Toast.makeText(surfaceView.getContext(),
                        R.string.recording_start, Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable toastRecordEnd = new Runnable() {
        @Override
        public void run() {
            if(surfaceView != null)
                Toast.makeText(surfaceView.getContext(),
                        R.string.recording_end, Toast.LENGTH_SHORT).show();
        }
    };


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
        setVideoPath(videoPath);

    }
    /*
    * Take photo and save it to file
    * */
    public void takePicture(){
        if(surfaceView.getState() == PlayerState.STATE_PLAY) {

            surfaceView.setState(PlayerState.STATE_SHOOT);

            surfaceView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(surfaceView.getLastSavedPicture() == null){
                        surfaceView.postDelayed(this, 1000);
                    }else {
                        mediaScan(surfaceView.getLastSavedPicture().getPath(), "image/*");
                    }
                }
            }, 1000);
        }
    }
    /*
    * Start recording the video and save to file
    * */

    public void startRecord(){
        String timeStr = new SimpleDateFormat("yyyyMMdd_hhmmss").format(new Date());
        NativeH264Decoder.SetVideoName(videoPath + "/VID_" + timeStr + ".avi");
        if(NativeH264Decoder.GetDecoderState() == PlayerState.STATE_PLAY){
            NativeH264Decoder.SetDecoderState(PlayerState.STATE_RECORD);

            surfaceView.post(toastRecordStart);
        }
    }

    /*
    * End recording the video
    * */
    public void endRecord(){
        if(NativeH264Decoder.GetDecoderState() == PlayerState.STATE_RECORD){
            NativeH264Decoder.SetDecoderState(PlayerState.STATE_END_RECORD);

            surfaceView.post(toastRecordEnd);

            surfaceView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mediaScan(getVideoPath(), "video/x-msvideo");
                }
            }, 1000);
        }
    }
    /*
    * Stop decoding and set state ready
    * */
    public void stopPlay(){
        if(NativeH264Decoder.DeinitDecoder() == 0) {
            Log.d(TAG, "Deinit finish");

            surfaceView.setState(PlayerState.STATE_READY);
            surfaceView.clearImage();
            surfaceView.close();
        }
    }

    @Override
    public void run() {
        surfaceView.setState(PlayerState.STATE_PLAY);
        int ret = NativeH264Decoder.DecodeAndConvert(uri, this.surfaceView);
        if(0 > ret){

            surfaceView.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(surfaceView.getContext(), "连接错误", Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    //Scan the directory of medias to add new files into MediaLibrary
    private void mediaScan(final String filePath, String mineType){

        MediaScannerConnection.MediaScannerConnectionClient mediaScannerClient =
                new MediaScannerConnection.MediaScannerConnectionClient() {

                    private MediaScannerConnection msc = null;
                    {
                        if(surfaceView != null) {
                            msc = new MediaScannerConnection(
                                    surfaceView.getContext(), this);
                            msc.connect();
                        }
                    }
                    @Override
                    public void onMediaScannerConnected() {
                        Log.d(TAG, "File path: " + filePath);
                        msc.scanFile(filePath + "/", mineType);

                    }

                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        msc.disconnect();
                        Log.d(TAG, "File Added at " + uri.toString());

                    }
                };

    }
    private void scanDirAsync(String dir) {
        Intent scanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_DIR");
        scanIntent.setData(Uri.fromFile(new File(dir)));
        surfaceView.getContext().sendBroadcast(scanIntent);
    }
}

