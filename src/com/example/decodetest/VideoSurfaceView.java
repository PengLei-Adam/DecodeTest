package com.example.decodetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by leip on 2016/3/21.
 */
public class VideoSurfaceView extends SurfaceView {
    //debug
    private static final String TAG = "VideoSurfaceView";
    /**
     * No aspect ratio ×Ýºá±È
     */
    public static float NO_RATIO = 0.0f;

    /**
     * Display area aspect ratio
     */
    private float aspectRatio = NO_RATIO;

    /**
     * Surface has been created state
     */
    private boolean surfaceCreated = false;

    /**
     * Surface holder
     */
    private SurfaceHolder holder;

    /*
    * Bitmap
    * */
    private Bitmap rgbFrame = null;

    private int frameWidth = 1280;
    private int frameHeight = 720;

    private int[] frameData = null;

    /**
     * Constructor
     *
     * @param context Context
     */
    public VideoSurfaceView(Context context) {
        super(context);

        init();
    }
    /**
     * Constructor
     *
     * @param context Context
     * @param attrs Attributes
     */
    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    /**
     * Constructor
     *
     * @param context Context
     * @param attrs Attributes
     * @param defStyle Style
     */
    public VideoSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    /**
     * Set aspect ration according to desired width and height
     *
     * @param width Width
     * @param height Height
     */
    public void setAspectRatio(int width, int height) {
        setAspectRatio((float)width / (float)height);
    }

    public void setAspectRatio(float ratio){
        if(aspectRatio != ratio) {
            aspectRatio = ratio;
            requestLayout();
            invalidate();
        }
    }

    /**
     * Ensure aspect ratio
     *
     * @param widthMeasureSpec Width
     * @param heightMeasureSpec Heigh
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        Log.d(TAG, "on Measure");
        if(aspectRatio != NO_RATIO){
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            int width = widthSpecSize;
            int height = heightSpecSize;

            if(width > 0 && height > 0){
                float defaultRatio = (((float)width) /((float)height));
                if(defaultRatio < aspectRatio){
                    //Need to reduce height
                    height = (int)(width / aspectRatio);
                } else if(defaultRatio > aspectRatio){
                    width = (int)(height * aspectRatio);
                }
                width = Math.min(width, widthSpecSize);
                height = Math.min(height, heightSpecSize);
                setMeasuredDimension(width, height);

                Log.i(TAG, "set ratio:" + aspectRatio);
                return;
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Set image from a bitmap
     *
     * @param bmp Bitmap
     */
    public void setImage(Bitmap bmp){

        if(surfaceCreated){
            Canvas canvas = null;
            try{
                synchronized(holder){
                    canvas = holder.lockCanvas();
                }
            }finally{
                if(canvas != null){
                    Log.d(TAG, "canvas draw");
                    //Clear screen first
                    //TODO: is neccesary???
                    canvas.drawARGB(255, 0, 0, 0);
                    //Then draw bmp
                    canvas.drawBitmap(bmp, null, canvas.getClipBounds(), null);
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    public void clearImage(){
        if(surfaceCreated){
            Canvas canvas = null;
            try{
                synchronized(holder){
                    canvas = holder.lockCanvas();
                }
            } finally{
                if(canvas != null){
                    canvas.drawARGB(255, 0, 0, 0);
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    /**
     * Init the view
     */
    private void init() {
        Log.d(TAG, "init");
        // Get a surface holder
        holder = this.getHolder();
        holder.addCallback(surfaceCallback);
        //init frameData
        frameData = new int[frameWidth * frameHeight];

    }

    /**
     * Surface holder callback
     */
    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            surfaceCreated = true;
            Log.d(TAG, "surface created");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.i(TAG, "Width: " + width + " Height: " + height);

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            surfaceCreated = false;
        }
    };

    private void createFrame(int width, int height){
        frameWidth = width;
        frameHeight = height;
        try {
            rgbFrame = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }catch (Exception e) {
            Log.d(TAG, "rgbFrame is null " + (rgbFrame == null),e);

        }
    }

    public void drawPicture(int width, int height) {

        if (rgbFrame == null) {
            createFrame(width, height);
        }
        /*else if(frameWidth != width || frameHeight != height){
            rgbFrame.recycle();
            rgbFrame = createFrame(width, height);
            frameData = new int[width * height];
            return;
        }*/
        rgbFrame.setPixels(frameData, 0, width, 0, 0, width, height);

        setImage(rgbFrame);
    }
}
