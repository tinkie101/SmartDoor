package za.co.zebrav.smartdoor;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class CameraFragment extends Fragment
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

		context = getActivity().getBaseContext();
		// check that the hardware does indeed have a camera
		checkFrontCamera(context);
		// Create an instance of Camera
		mCamera = getFrontCameraInstance();
		mCamera.setFaceDetectionListener(new FDL());
		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(context, mCamera);

	}

	/**
	 * Sets the whole preview to a CameraPreview
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		return mPreview;
	}

	/**
	 * Releases the camera for the OS when the activity closes.
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause()
	{
		super.onPause();
		releaseCamera();
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
			// c = Camera.open(CameraInfo.CAMERA_FACING_BACK);

		} catch (Exception e)
		{
			Toast t = Toast.makeText(context, "Camera in use",
					Toast.LENGTH_LONG);
			t.show();
			System.exit(0);
		}
		return c; // returns null if camera is unavailable
	}

	/**
	 * helper function to release the camera
	 */
	private void releaseCamera()
	{
		if (mCamera != null)
		{
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

	private class FDL implements Camera.FaceDetectionListener
	{

		@Override
		public void onFaceDetection(Face[] faces, Camera camera)
		{
			if (faces.length > 0)
			{
				// Face has been detected.
				// Draw a rectangle around the face

				// Paint myPaint = new Paint();
				// myPaint.setColor(Color.rgb(0, 0, 0));
				// myPaint.setStrokeWidth(10);
				// c.drawRect(100, 100, 200, 200, myPaint);

				Log.d("FaceDetection", "face detected: " + faces.length
						+ " Face 1 Location X: " + faces[0].rect.centerX()
						+ "Y: " + faces[0].rect.centerY());
			}
		}
	}
}
