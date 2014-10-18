package za.co.zebrav.smartdoor.facerecognition;

import java.io.IOException;

import za.co.zebrav.smartdoor.GlobalApplication;
import za.co.zebrav.smartdoor.MainActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

@SuppressLint("ValidFragment") 
public class SearchCameraFragment extends CameraFragment
{
	public SearchCameraFragment(int ID)
	{
		super(ID);
	}
	private static final String TAG = "SearchCameraFragment";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		try
		{
			this.faceView = new SearchFaceView(activity, this);
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
	
	@Override
	public void onResume()
	{
		super.onResume();
		GlobalApplication application = (GlobalApplication)activity.getApplication();
		faceView.setPersonRecognizer(application.personRecognizer);
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