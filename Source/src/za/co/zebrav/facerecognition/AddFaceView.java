package za.co.zebrav.facerecognition;

import java.io.IOException;

import za.co.zebrav.smartdoor.database.AddUserActivity;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.util.Log;

class AddFaceView extends FaceView
{
	private static final String TAG = "AddFaceView";
	private int count = 0;
	private static final int classifierCount = 3;
	private static final String[] classifierFiles = { "haarcascade_frontalface_alt.xml", "haarcascade_eye.xml",
						"haarcascade_nose.xml" };
	private static final int colors[] = { Color.RED, Color.GREEN, Color.BLUE };

	public AddFaceView(Activity activity, Fragment fragment) throws IOException
	{
		super(activity, fragment);
		initialiseClassifiers(classifierCount, colors, classifierFiles);
	}

	@Override
	protected void handleDetected(byte[] data, int width, int height)
	{
		if (runnables[0].getObjects().total() == 1 && runnables[1].getObjects().total() == 2
							&& runnables[2].getObjects().total() == 1)
		{
			ImageTools.saveImageAsPNG(ImageTools.getGreyMatImage(data, width, height, 1), uID + "-" + count, activity);
			Log.d(TAG, "Saved ID:" + uID + " Number: " + count + " .");
			count++;
			((AddCameraFragment)fragment).setProgress(25*count);
			if (count == 5)
			{
				((AddUserActivity) activity).switchFragToStep3();
			}
		}
	}

	@Override
	protected int getColor(int id)
	{
		return colors[id];
	}

	@Override
	protected int getClassifierCount()
	{
		return classifierCount;
	}
}