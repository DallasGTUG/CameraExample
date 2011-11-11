package com.example;

import java.io.OutputStream;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;

public class ImageCaptureCallback implements PictureCallback 
{
	private OutputStream output;
	
	public ImageCaptureCallback(OutputStream value)
	{
		this.output=value;
	}
	
	@Override
	public void onPictureTaken(byte[] data, Camera camera) 
	{
		try
		{
			output.write(data);
			output.flush();
		}
		catch(Exception e)
		{
			Log.e(MainActivity.TAG, e.getMessage());
		}
		finally
		{
			try{output.close();}
			catch(Exception e){}
		}
	}

}
