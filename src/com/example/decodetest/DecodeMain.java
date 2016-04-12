package com.example.decodetest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class DecodeMain extends Activity implements View.OnClickListener{

	private static final String TAG = "DecodeMain";

	private String uri = "rtsp://192.168.2.1:6880/test.264";

	private Button buttonClose;
	private Button buttonStart;

	private VideoSurfaceView surfaceView = null;

	private RtspPlayer player;

	private AlertDialog alertDialog = null;
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

		/*buttonInit = (Button)findViewById(R.id.buttonInit);
		buttonInit.setOnClickListener(this);*/

		buttonClose = (Button)findViewById(R.id.buttonClose);
		buttonClose.setOnClickListener(this);

		buttonStart = (Button)findViewById(R.id.buttonStart);
		buttonStart.setOnClickListener(this);

		Log.d(TAG, "new player");
//		nalReader = new NalReader(/*Environment.getExternalStorageDirectory()*/
//				"/sdcard/Pictures/frames7.264");
		player = new RtspPlayer(uri, surfaceView);
		player.prepare();

	}



	private void setDialog(){

		//Set a AlertDialog.Builder
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if(builder == null){
			Log.d(TAG, "builder is null");
			return;
		}
		builder.setTitle("Input remote video address");
		//Load layout file
		builder.setView((LinearLayout)getLayoutInflater().inflate(R.layout.dialog_input_uri, null));
		builder.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						EditText textUri = (EditText)alertDialog.findViewById(R.id.uriEdit);
						if(textUri == null){
							Log.d(TAG, "EditText is null");
							return;
						}
						String uriInput = (textUri).getText().toString();
						//TODO:check whether the uri is correct
						player.setUri(uriInput);
						player.start();
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
				player.closePlayer();

				break;
			case R.id.buttonStart:
				setDialog();
				alertDialog.show();
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
	

}
