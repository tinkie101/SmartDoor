package za.co.zebrav.smartdoor.facerecognition;

import java.io.IOException;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

@SuppressLint("ValidFragment") 
public class AddCameraFragment extends CameraFragment
{
	private static final String TAG = "AddCameraFragment";
	public AddCameraFragment(int ID)
	{
		super(ID);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
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
		super.onCreateView(inflater, container, savedInstanceState);
		faceView.setuID(uID);
		return layout;
	}

}