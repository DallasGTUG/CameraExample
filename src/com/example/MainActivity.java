package com.example;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends Activity implements SurfaceHolder.Callback 
{
	static final String TAG="GTUG-CAMERA";
	
	private Camera camera;
	private boolean isPreviewing=false;
	private SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyMMddHHssSS");
	
	private SurfaceView surface;
	private SurfaceHolder surfaceHolder;
	private Uri targetUri=Images.Media.EXTERNAL_CONTENT_URI;
	
	Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() 
	{	
		@Override
		public void onShutter() 
		{
			//it could be fun to give the camera some "personality" based on shutter events
		}
	};
	
	Camera.PictureCallback pictureCallbackRaw = new Camera.PictureCallback() 
	{	
		@Override
		public void onPictureTaken(byte[] data, Camera camera) 
		{
			//magic will happen here. data is the picture bytes and camera is the Camera instance that created 'em
			MainActivity.this.camera.startPreview();
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        
        setContentView(R.layout.main);
        surface = (SurfaceView)findViewById(R.id.surface);
        //get a reference to the working area
        surfaceHolder = surface.getHolder();
        //tell the working area that this activity wants to participate
        surfaceHolder.addCallback(this);
        //tell the holder that it should make its data available
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		MenuItem item = menu.add(0,0,0, "View photos");
		item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() 
		{		
			@Override
			public boolean onMenuItemClick(MenuItem item) 
			{
				//might be interesting to define the intent externally, maybe using a list of intents
				//other than (or in addition to) using the intent querying mechanism in android. 
				//then switch out the intent to call based on some other variable which could be 
				//affected by a part of the ui, user info/preferences, voice command, time of day, location
				//or whatever
				Intent intent = new Intent(Intent.ACTION_VIEW,MainActivity.this.targetUri);
				startActivity(intent);
				
				//since we handled the click, return true
				return false;
			}
		});
		return true;
	}

	private static int pictureCount = 1;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		ImageCaptureCallback callback = null;
		if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
		{
			try
			{
				String filename = this.timeStampFormat.format(new Date());
				ContentValues values = new ContentValues();
				values.put(MediaColumns.TITLE, filename);
				values.put(Images.ImageColumns.DESCRIPTION,String.format("GTUG Pic %s",pictureCount));
				//values.put(MediaColumns.DATA, filename+".jpg");
				Uri uri = getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
				callback = new ImageCaptureCallback(getContentResolver().openOutputStream(uri));
				
				this.camera.takePicture(shutterCallback, pictureCallbackRaw, callback);
				pictureCount++;
				return true;
			}
			catch(Exception e)
			{
				Log.e(TAG,e.getMessage());
			}			
		}
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			return super.onKeyDown(keyCode, event);
		}
		
		return false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
	{
		if(this.isPreviewing)
			this.camera.stopPreview();
		
		Camera.Parameters parameters = this.camera.getParameters();
		parameters.setPreviewSize(width, height);
		this.camera.setParameters(parameters);
		try 
		{
			//could have an io issue with the camera.
			this.camera.setPreviewDisplay(holder);
			this.camera.startPreview();
			this.isPreviewing=true;
		} 
		catch (IOException e) 
		{
			Log.e(TAG, e.getMessage());
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) 
	{
		//camera doesn't have a public constructor. calling the static "open" method
		this.camera = Camera.open();
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		this.camera.stopPreview();
		this.isPreviewing=false;
		this.camera.release();
	}
}