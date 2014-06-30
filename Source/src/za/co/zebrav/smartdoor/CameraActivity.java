package za.co.zebrav.smartdoor;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

public class CameraActivity extends Activity
{
	/**
	 * Stores the camera instance for the class
	 */
	private Camera mCamera;
	/**
	 * Preview object to display camera content
	 */
	private CameraPreview mPreview;

	private Context context = null;

	/**
	 * Standard on create method
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_layout);
		// Make the action bar an back button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		context = this;

	}

	/**
	 * Creates the camera each time the activity starts
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart()
	{
		// check that the hardware does indeed have a camera
		checkFrontCamera(context);
		// Create an instance of Camera
		mCamera = getFrontCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
		super.onStart();
	}

	/**
	 * Releases the camera for the OS when the activity closes.
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop()
	{
		mCamera.release();
		super.onStop();
	}

	/**
	 * Check if up button is pressed and finishes the activity
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Check if this device has a camera
	 */
	private boolean checkFrontCamera(Context context)
	{
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA_FRONT))
		{
			// this device has a camera
			return true;
		} else
		{
			Toast t = Toast.makeText(context,
					"No front-facing camera on device", Toast.LENGTH_LONG);
			t.show();
			System.exit(0);
			return false;
		}
	}

	/**
	 * A safe way to get an instance of the Camera object.
	 * */
	public Camera getFrontCameraInstance()
	{
		Camera c = null;
		try
		{
			// attempt to get a Camera instance
			c = Camera.open(CameraInfo.CAMERA_FACING_FRONT); 
			
		} catch (Exception e)
		{
			Toast t = Toast.makeText(context, "Camera in use",
					Toast.LENGTH_LONG);
			t.show();
			System.exit(0);
		}
		return c; // returns null if camera is unavailable
	}
}