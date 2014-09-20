package za.co.zebrav.facerecognition;

import java.io.IOException;

import za.co.zebrav.smartdoor.MainActivity;
import za.co.zebrav.smartdoor.database.AddUserActivity;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

public class SearchCameraFragment extends CameraFragment
{
	private static final String TAG = "SearchCameraFragment";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		try
		{
			this.faceView = new SearchFaceView(activity);
			mPreview = new Preview(activity, faceView);
			layout.addView(mPreview);
			layout.addView(faceView);
			faceView.setActivity(activity);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Sets the whole preview to a CameraPreview
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreateView");
		return layout;
	}

}