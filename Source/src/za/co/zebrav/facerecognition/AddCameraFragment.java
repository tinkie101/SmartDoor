package za.co.zebrav.facerecognition;

import java.io.IOException;

import za.co.zebrav.smartdoor.database.AddUserActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AddCameraFragment extends CameraFragment
{
	private int uID = -1;
	private static final String TAG = "AddCameraFragment";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		try
		{
			this.faceView = new AddFaceView(activity, this);
			mPreview = new Preview(activity, faceView);
			layout.addView(mPreview);
			layout.addView(faceView);
			layout.addView(bar);
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
		Bundle bundle = this.getArguments();
		uID = bundle.getInt("userID", -1);
		faceView.setuID(uID);
		return layout;
	}

}