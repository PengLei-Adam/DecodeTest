package com.example.decodetest.activity;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.example.decodetest.R;
import com.example.decodetest.player.PlayerState;
import com.example.decodetest.player.RtspPlayer;
import com.example.decodetest.ui.button.ButtonObserver;
import com.example.decodetest.ui.fragment.MainButtonsFragment;
import com.example.decodetest.ui.fragment.VideoSurfaceFragment;
import com.example.decodetest.ui.surface.OnSurfaceViewChangedListener;
import com.example.decodetest.ui.surface.VideoSurfaceView;

import edu.tfnrc.rtp.codec.h264.NativeH264Decoder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DecodeMain extends Activity implements View.OnClickListener, OnSurfaceViewChangedListener {

	private static final String TAG = "DecodeMain";

	private static final String LAST_URI = "LAST_URI";

	private static final String PREF_FILE_KEY = "URI_PREF";

	private String uri;

	private ActionBar actionBar;

	private FrameLayout videoContainer;
	private LinearLayout buttonsContainer;

	private VideoSurfaceFragment videoSurfaceFragment;
	private MainButtonsFragment buttonsFragment;

	//test ImageView instead of surfaceView
//	private ImageView imageView = null;

	private VideoSurfaceView surfaceView = null;

	private RtspPlayer player;

	private Handler mHandler = new Handler();

	private Runnable runButtonStart = new Runnable() {
		@Override
		public void run() {
			if (NativeH264Decoder.GetDecoderState() != PlayerState.STATE_PLAY) {
				mHandler.postDelayed(this, 500);
			} else {
				surfaceView.getBackground().setAlpha(0);
				button_started = true;
				notifyButtons();
			}
		}
	};

	private AlertDialog alertDialog = null;

	private SharedPreferences sharedPref;

	private List<ButtonObserver> buttonObserverList = new ArrayList<ButtonObserver>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		//set window full screen
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		Window win = getWindow();
//		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//		win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);

		actionBar = getActionBar();

		//设置响应横屏
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

//		surfaceView = (VideoSurfaceView)findViewById(R.id.videoSurface);
		videoContainer = (FrameLayout)findViewById(R.id.videoSurfaceContainer);
		buttonsContainer = (LinearLayout)findViewById(R.id.buttonsContainer);

		sharedPref = getSharedPreferences(PREF_FILE_KEY, Context.MODE_PRIVATE);
		uri = sharedPref.getString(LAST_URI, getString(R.string.default_uri));


		surfaceView = new VideoSurfaceView(this);
		surfaceView.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		surfaceView.setBackgroundResource(R.drawable.background_logo);

		//test
//		imageView = new ImageView(this);
//		imageView.setLayoutParams(new ViewGroup.LayoutParams(
//				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//		imageView.setImageResource(R.drawable.background_image);

		if(savedInstanceState == null) {
			//add Fragment
			FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			if (videoContainer != null) {
				Log.d(TAG, "new fragment");
				videoSurfaceFragment = new VideoSurfaceFragment();

				fragmentTransaction.add(R.id.videoSurfaceContainer, videoSurfaceFragment, "videoSurface");
			}
			if(buttonsContainer != null){
				buttonsFragment = new MainButtonsFragment();
				fragmentTransaction.add(R.id.buttonsContainer, buttonsFragment, "mainButtons");
			}
			fragmentTransaction.commit();

		}

		surfaceView.setAspectRatio(0.0f);
		preparePlayer(surfaceView);
	}

	public void preparePlayer(VideoSurfaceView videoSurfaceView){
		if(player == null){
			player = new RtspPlayer(videoSurfaceView);
			player.prepare();
		}
	}

	private void setDialog(){

		//Set a AlertDialog.Builder
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Input remote video address");
		//Load layout file
		View layout = getLayoutInflater().inflate(R.layout.dialog_input_uri, null);
		final EditText  uriEdit = (EditText)layout.findViewById(R.id.uriEdit);
		if(uriEdit != null){
			uriEdit.setText(uri);
		}
		builder.setView(layout);
		builder.setPositiveButton(R.string.dialog_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						if (uriEdit == null) {
							Log.d(TAG, "EditText is null");
							return;
						}
						uri = (uriEdit).getText().toString();
						sharedPref.edit().putString(LAST_URI, uri).apply();
						//TODO:check whether the uri correct
						player.setUri(uri);
						new Thread(player).start();


					}
				}
		);
		builder.setNegativeButton(R.string.dialog_cancel, null);
		alertDialog = builder.create();

	}
	private boolean button_started = false;
	private boolean button_recording = false;

	public boolean getButtonPlayState(){
		return button_started;
	}
	public boolean getButtonRecordState(){
		return button_recording;
	}

	public void addButtonObserver(ButtonObserver observer){
		if(observer != null){
			buttonObserverList.add(observer);
		}
	}
	public void deleteButtonObserver(ButtonObserver observer){
		if(observer != null){
			buttonObserverList.remove(observer);
		}
	}

	private void notifyButtons(){
		for(ButtonObserver observer : buttonObserverList){
			observer.update(this);
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){

			case R.id.buttonStartStop:

				if(button_started) {
					player.stopPlay();
					button_started = false;
					notifyButtons();
					surfaceView.getBackground().setAlpha(255);
				}
				else {
					setDialog();
					alertDialog.show();

					mHandler.postDelayed(runButtonStart, 500);

				}
				break;
			case R.id.buttonShoot:
				player.takePicture();
				break;
			case R.id.buttonRecord:
				if(button_recording){
					player.endRecord();
					button_recording = false;
					notifyButtons();
				}else {
					player.startRecord();
					button_recording = true;
					notifyButtons();
				}
				break;

			default:
				break;
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.decode_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
			case R.id.action_settings:
				return true;
			case R.id.menu_open_photos:
				/*Intent intent = new Intent(Intent.ACTION_VIEW);
				Uri uri = Uri.fromFile(new File(player.getPhotoPath() + "/IMG_20160603_043712.jpg"));
				intent.setDataAndType(uri, "image*//*");
//				intent.addCategory(Intent.CATEGORY_OPENABLE);
				try {
					startActivity(intent);
				} catch (android.content.ActivityNotFoundException ex) {
					// Potentially direct the user to the Market with a Dialog
					Toast.makeText(this, "请安装文件管理器", Toast.LENGTH_SHORT)
							.show();
				}*/

				Intent intent = new Intent(this, MediaActivity.class);
				intent.setAction(getResources().getString(MediaActivity.ID_ACTION_MEDIA));
				startActivity(intent);
				return true;
			case R.id.menu_open_videos:

				return true;
		}
		return super.onOptionsItemSelected(item);
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
		buttonObserverList.clear();
	}

	@Override
	protected void onStop() {
		super.onStop();
		player.stopPlay();
	}

	@Override
	public void onSurfaceViewAdded(ViewGroup viewGroup) {
		viewGroup.addView(surfaceView);
	}

	@Override
	public void onVideoStartError() {
		mHandler.removeCallbacks(runButtonStart);
	}

	/**
	 * 设置为全屏显示
	 */
	private void setFullScreen() {
		actionBar.hide();
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

	}

	/**
	 * 退出全屏显示
	 */
	private void cancelFullScreen() {
		actionBar.show();
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){

			setFullScreen();
			buttonsContainer.setVisibility(View.GONE);
		}else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
			cancelFullScreen();
			buttonsContainer.setVisibility(View.VISIBLE);
		}
		super.onConfigurationChanged(newConfig);
	}


}
