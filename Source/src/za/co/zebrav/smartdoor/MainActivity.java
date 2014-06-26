//Authors:
//Eduan Bekker - 12214834
//Zuhnja Riekert - 12040593
//Albert Volschenk - 12054519

//This is the main activity for the Smart Door Application

package za.co.zebrav.smartdoor;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if(!checkCameraHardware(this))
		{
			Toast msg = Toast.makeText(this, "Camera required", Toast.LENGTH_LONG);
			msg.show();
			finish();
		}
		else
		{
			Toast msg = Toast.makeText(this, "Camera found", Toast.LENGTH_LONG);
			msg.show();
			Camera camera = getCameraInstance();
		}
	}

	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context)
	{
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA))
		{
			// this device has a camera
			return true;
		} else
		{
			// no camera on this device
			return false;
		}
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance()
	{
		Camera c = null;
		try
		{
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e)
		{
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}
}
