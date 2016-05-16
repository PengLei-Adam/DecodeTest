package com.example.decodetest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import edu.tfnrc.rtp.codec.h264.NativeH264Decoder;

public class DecodeMain extends Activity implements View.OnClickListener{

	private static final String TAG = "DecodeMain";

	private static final String LAST_URI = "LAST_URI";

	private static final String PREF_FILE_KEY = "URI_PREF";

	private String uri;

	private Button buttonClose;
	private Button buttonStart;
	private Button buttonShoot;
	private Button buttonRecord;
	private Button buttonEndRecord;

	private VideoSurfaceView surfaceView = null;

	private RtspPlayer player;

	private AlertDialog alertDialog = null;

	private SharedPreferences sharedPref;
	SharedPreferences.Editor editor;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		//set window full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_decode_main);

		surfaceView = (VideoSurfaceView)findViewById(R.id.videoSurface);

		buttonRecord = (Button)findViewById(R.id.buttonRecord);
		buttonRecord.setOnClickListener(this);

		buttonClose = (Button)findViewById(R.id.buttonClose);
		buttonClose.setOnClickListener(this);

		buttonStart = (Button)findViewById(R.id.buttonStart);
		buttonStart.setOnClickListener(this);

		buttonShoot = (Button)findViewById(R.id.buttonShoot);
		buttonShoot.setOnClickListener(this);

		buttonEndRecord =(Button)findViewById(R.id.buttonEndRecord);
		buttonEndRecord.setOnClickListener(this);

//		nalReader = new NalReader(/*Environment.getExternalStorageDirectory()*/
//				"/sdcard/Pictures/frames7.264");
		sharedPref = getSharedPreferences(PREF_FILE_KEY, Context.MODE_PRIVATE);
		uri = sharedPref.getString(LAST_URI, getString(R.string.default_uri));

	}
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "new player");

		player = new RtspPlayer(surfaceView);
		player.prepare();
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
		builder.setPositiveButton("确定",
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
		builder.setNegativeButton("取消", null);
		alertDialog = builder.create();

	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.buttonClose:
				player.stopPlay();

				break;
			case R.id.buttonStart:
				setDialog();
				alertDialog.show();
				break;
			case R.id.buttonShoot:
				player.takePicture();
				break;
			case R.id.buttonRecord:
				player.startRecord();
				break;
			case R.id.buttonEndRecord:
				player.endRecord();
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
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

//	Runnable read264 = new Runnable() {
//		@Override
//		public void run() {
//			while(nalReader.readNal() > 0){
//				byte[] nal = nalReader.getNalBuffer();
//				Log.i(TAG, "nalLength: " + nalReader.getNalLength());
//				NativeH264Decoder.DecodeAndConvert("rtsp://192.168.2.1:6880/test.264");
//			}
//		}
//	};


	@Override
	protected void onDestroy() {
		super.onDestroy();
		player.stopPlay();
	}
}
