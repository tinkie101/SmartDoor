package za.co.zebrav.facerecognition;

import java.io.IOException;

import org.bytedeco.javacpp.opencv_core.Mat;

import za.co.zebrav.smartdoor.MainActivity;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;

class SearchFaceView extends FaceView
{
	private static final String TAG = "FaceView";
	private static final int classifierCount = 1;
	private static final int colors[] = {Color.RED};
	private static final String[] classifierFiles = {"haarcascade_frontalface_alt.xml"};

	public SearchFaceView(Activity activity) throws IOException
	{
		super(activity);
		initialiseClassifiers(classifierCount, colors, classifierFiles);
	}
	
	private static final int DETECTED_IN_A_ROW = 5;
	private int tempdetected = -1;
	
	@Override
	protected void handleDetected(byte[] data, int width, int height)
	{
		if (runnables[0].getObjects().total() == 1 && personRecognizer.canPredict())
		{
			int detectedId =  personRecognizer.predict(new Mat(grayImage));
			Log.d(TAG, "Face detected:" + detectedId);
			Log.d(TAG, "Certainty:" + personRecognizer.getCertainty());
			if(detectedId != -1)
			{
				if(tempdetected != detectedId)
					count = 0;
				else count++;
				tempdetected = detectedId;
				Log.d(TAG, "Count in a row:" + count);
				if(count == DETECTED_IN_A_ROW)
					((MainActivity)activity).switchToVoice(detectedId);
			}
			else
				tempdetected = 0;				
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