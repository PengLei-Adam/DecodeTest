package edu.tfnrc.rtp.codec.h264;

import com.example.decodetest.VideoSurfaceView;

/**
 * Created by leip on 2016/1/16.
 */
public class NativeH264Decoder {
    //加载顺序需要注意
    static {
        System.loadLibrary("avutil-55");
        System.loadLibrary("postproc-54");

        System.loadLibrary("swresample-2");
        System.loadLibrary("avcodec-57");
//        System.loadLibrary("avdevice-57");
        System.loadLibrary("avformat-57");
        System.loadLibrary("swscale-4");


        System.loadLibrary("decoder");
    }

    public static native int InitDecoder();

    public static native int DeinitDecoder();

    public static native int DecodeAndConvert(String jurl, VideoSurfaceView surfaceView);

    public static native int SetDecoderStatus(int statusId);
}
