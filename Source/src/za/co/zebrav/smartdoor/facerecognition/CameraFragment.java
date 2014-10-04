package za.co.zebrav.smartdoor.facerecognition;

import za.co.zebrav.smartdoor.R;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

public abstract class CameraFragment extends Fragment
{

	protected FrameLayout layout;
	protected FaceView faceView;
	/**
	 * Standard on create method
	 * 
	 * @see android.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.activity = getActivity();
		// check that the hardware does indeed have a camera
		checkFrontCamera(activity);
		// Create the view with nothing to show
		// This is so that onCreateView has a view to return
		// Then onResume we add the camera to the preview
		layout = new FrameLayout(activity);
		bar = new ProgressBar(activity,null, android.R.attr.progressBarStyleHorizontal);
		bar.setProgress(0);
		bar.setScrollBarStyle(R.style.soundBarStyle);
		bar.setIndeterminate(false);
	}
	/**
	 * Preview object to display camera content
	 */
	protected Preview mPreview;
	/**
	 * Stores the camera instance for the class
	 */
	protected Camera mCamera;
	/**
	 * Check if this device has a camera
	 */
	
	protected Activity activity;
	protected boolean checkFrontCamera(Context context)
	{
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT))
		{
			// this device has a camera
			return true;
		}
		else
		{
			Toast t = Toast.makeText(context, "No front-facing camera on device", Toast.LENGTH_LONG);
			t.show();
			System.exit(0);
			return false;
		}
	}

	/**
	 * Releases the camera to the OS
	 */
	@Override
	public void onPause()
	{
		super.onPause();
		releaseCamera();
	}

	/**
	 * Acquires the camera from the OS
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		// Create an instance of Camera
		mCamera = getFrontCameraInstance();
		mPreview.setCamera(mCamera);
		// TODO: add loadPersonRecognizer call once db is fixed.
		// faceView.loadPersonRecognizer();
	}

	protected void releaseCamera()
	{
		if (mCamera != null)
		{
			Log.d("Camera", "Releasing Camera");
			mCamera.release(); // release the camera for other applications
			mCamera = null;
			mPreview.setCamera(null);
		}
	}

	/**
	 * A safe way to get an instance of the Camera object.
	 * */
	protected Camera getFrontCameraInstance()
	{
		Log.d("Camera", "Aquiring Camera");
		Camera c = null;
		try
		{
			// attempt to get a Camera instance
			c = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
		}
		catch (Exception e)
		{
			Toast t = Toast.makeText(activity, "Camera in use", Toast.LENGTH_LONG);
			t.show();
			System.exit(0);
		}
		return c; // returns null if camera is unavailable
	}
	ProgressBar bar;
	public void setProgress(int p)
	{
		bar.setProgress(p);
	}
}
