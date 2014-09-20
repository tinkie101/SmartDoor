package za.co.zebrav.facerecognition;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.view.View;

public abstract class FaceView extends View implements Camera.PreviewCallback
{
	Activity activity;
	protected int uID;
	public FaceView(Context context)
	{
		super(context);
	}
	public void setActivity(Activity activity)
	{
		this.activity = activity;
	}
	public void setuID(int uID)
	{
		this.uID = uID;
	}
}
