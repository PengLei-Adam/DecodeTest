package com.example.decodetest.ui.surface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.example.decodetest.Constants;
import com.example.decodetest.R;
import com.example.decodetest.player.PlayerState;
import com.example.decodetest.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by leip on 2016/3/21.
 */
public class VideoSurfaceView extends SurfaceView {
    //debug
    private static final String TAG = "VideoSurfaceView";

    private static final int DEFAULT_OFFSET = 4;
    /**
     * No aspect ratio �ݺ��
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
    private Bitmap photoFrame = null;

    private int frameWidth = 1280;
    private int frameHeight = 720;
    /*
    * offset = 0 or 4 to make frameData 16-byte aligned
    * the address should be 0x...0,
    * not 0x...8 which will arise a SIGBUS signal
    * */
    private int offset = 0;

    private int[] frameData = null;

    //Player state
    private PlayerState playerState = null;

    //Thread pool
    private ThreadPoolExecutor pool = null;
    private Future future = null;
    private BmpSaver bmpSaver = null;
    private File lastSavedPicture = null;

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
                    Log.d(TAG, "canvas draw at " + System.currentTimeMillis());
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

    public void setState(int state){
        playerState.setState(state);
    }
    public int getState(){
        return playerState.getState();
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
//        frameData = new int[frameWidth * frameHeight + DEFAULT_OFFSET];
        //init player state
        playerState = new PlayerState();
        //create thread pool
        pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
        //create bmpSaver for saving photos
        bmpSaver = new BmpSaver(getContext());
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

    public int createFrame(int width, int height){
        frameWidth = width;
        frameHeight = height;
        try {
            frameData = new int[width * height + DEFAULT_OFFSET];
            rgbFrame = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }catch (Exception e) {
            Log.d(TAG, "rgbFrame is null " + (rgbFrame == null),e);

            return -1;
        }
        return 0;
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
//        Log.d(TAG, "offset = " + offset);
        rgbFrame.setPixels(frameData, offset, width, 0, 0, width, height);

        if(playerState.getState() == PlayerState.STATE_SHOOT){
            savePicture(rgbFrame);
        }
        setImage(rgbFrame);
    }

    /**
     * Draw picture and save the bitmap
     * Called by C code at STATE_SHOOT
     * */
    private void savePicture(Bitmap picture){

        playerState.setState(PlayerState.STATE_PLAY);
        //copy rgbFrame to photoFrame and save to file
        picture = rgbFrame.copy(Bitmap.Config.ARGB_8888, false);
        if(picture == null){
            //TODO:notify the failure of copying bitmap
        }
        bmpSaver.setBitmap(picture);
        //use default file name
        bmpSaver.setFileName(null);
        future = pool.submit(bmpSaver);

    }

    public void setPhotoPath(String path){
        bmpSaver.setWholePath(path);
    }
    public String getPhotoPath(){
        return bmpSaver.getWholePath();
    }

    public File getLastSavedPicture() {
        try {
            lastSavedPicture = (File) future.get();
        } catch (Exception e){
            Log.e(TAG, "failed to get saved picture", e);
        }
        return lastSavedPicture;
    }

    /*
    * Deinit the SurfaceView
    * */
    public void close(){
        pool.shutdown();
    }


    static class  BmpSaver extends FileUtils implements Callable {

        private String TAG = "BmpSaver";

        private Context context = null;

        private Bitmap bitmap = null;

        private Handler mhandler = null;
        private Runnable toastFail = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, R.string.save_photo_fail, Toast.LENGTH_LONG).show();
            }
        };
        private Runnable toastSucceed = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, R.string.save_photo_succeed, Toast.LENGTH_SHORT).show();
            }
        };


        private void initPicturePath(){
            SDPath = Constants.PHOTO_DIRECTORY;
        }
        public BmpSaver(){
            initPicturePath();
        }
        public BmpSaver(Context context){
            this.context = context;
            initPicturePath();
            this.wholePath = SDPath;
            this.fileName = null;
        }
        public BmpSaver(Context context, String path, String fileName, Bitmap bmp){
            initPicturePath();
            this.context = context;
            this.wholePath = (path == null) ? SDPath :
                    (path.startsWith("/") ? path : SDPath + path);
            this.fileName = fileName;
            this.bitmap = bmp;
            //init handler
            mhandler = new Handler(Looper.getMainLooper());
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
                }catch (Exception e) {
                    Log.e(TAG, "failed to close output stream", e);
                }

                if(file == null){
                    mhandler.post(toastFail);
                }else {
                    mhandler.post(toastSucceed);
                }
            }
//            Log.i(TAG, "finish saving picture");
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
}
