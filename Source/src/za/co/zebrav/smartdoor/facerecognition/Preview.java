package za.co.zebrav.smartdoor.facerecognition;

import java.io.IOException;
import java.util.List;

import za.co.zebrav.smartdoor.AbstractActivity;
import za.co.zebrav.smartdoor.R;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class Preview extends SurfaceView implements SurfaceHolder.Callback
{
	SurfaceHolder mHolder;
	Camera mCamera;
	Camera.PreviewCallback previewCallback;
	AbstractActivity context;
	private static final String TAG = "Preview";

	public void setCamera(Camera camera)
	{
		mCamera = camera;
	}

	Preview(Context context, Camera.PreviewCallback previewCallback)
	{
		super(context);
		this.context = (AbstractActivity) context;
		this.previewCallback = previewCallback;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
	}

	public void surfaceCreated(SurfaceHolder holder)
	{
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		//mCamera = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
		try
		{
			mCamera.setPreviewDisplay(holder);
		}
		catch (IOException exception)
		{
			mCamera.release();
			mCamera = null;
			// TODO: add more exception handling logic here
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder)
	{
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
//		mCamera.stopPreview();
//		mCamera.release();
//		mCamera = null;
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h)
	{
		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes)
		{
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff)
			{
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null)
		{
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes)
			{
				if (Math.abs(size.height - targetHeight) < minDiff)
				{
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
	{
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();

		List<Size> sizes = parameters.getSupportedPreviewSizes();
		//Size optimalSize = getOptimalPreviewSize(sizes, w, h);
		String settingsFile = getResources().getString(R.string.settingsFileName);
		int pos = Integer.parseInt(context.getSharedPreferences(settingsFile, 0).getString(
							"face_resolution", "1"));
		Size optimalSize = sizes.get(pos);
		Log.d(TAG,"width:" + optimalSize.width + " height: " + optimalSize.height);
		parameters.setPreviewSize(optimalSize.width, optimalSize.height);

		mCamera.setParameters(parameters);
		if (previewCallback != null)
		{
			mCamera.setPreviewCallbackWithBuffer(previewCallback);
			Camera.Size size = parameters.getPreviewSize();
			byte[] data = new byte[size.width * size.height
								* ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8];
			mCamera.addCallbackBuffer(data);
		}
		mCamera.startPreview();
	}
}