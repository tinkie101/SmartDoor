package za.co.zebrav.facerecognition;

import java.io.IOException;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.Mat;

import za.co.zebrav.smartdoor.MainActivity;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.util.Log;

class SearchFaceView extends FaceView
{
	private static final String TAG = "FaceView";
	private static final int classifierCount = 1;
	private static final int colors[] = { Color.RED };

	public SearchFaceView(Activity activity, Fragment fragment) throws IOException
	{
		super(activity, fragment);
		personRecognizer = new PersonRecognizer(activity);
		initialiseClassifiers();
	}

	protected void initialiseClassifiers() throws IOException
	{
		Log.d(TAG, "Classifier count:" + classifierCount);
		storage = CvMemStorage.create();
		threads = new Thread[classifierCount];
		runnables = new ClassifierRunnable[classifierCount];
		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);

		runnables[0] = new FaceClassifierRunnable(storage, activity.getCacheDir());
		threads[0] = new Thread(runnables[0], "" + 0);
	}

	/**
	 * List of Runnable objects used in threads.
	 * List is kept to have access to their local variables.
	 */
	private ClassifierRunnable[] runnables;
	
	protected ClassifierRunnable[] getRunnables()
	{
		return runnables;
	}
	private static final int DETECTED_IN_A_ROW = 5;
	private int tempdetected = -1;

	@Override
	protected void handleDetected(byte[] data, int width, int height)
	{
		if (runnables[0].getTotalDetected() == 1 && personRecognizer.canPredict())
		{
			int detectedId = personRecognizer.predict(new Mat(grayImage));
			Log.d(TAG, "Face detected:" + detectedId);
			Log.d(TAG, "Certainty:" + personRecognizer.getCertainty());
			if (detectedId != -1)
			{
				if (tempdetected != detectedId)
					count = 0;
				else
					count++;
				((SearchCameraFragment) fragment).setProgress(25 * count);
				tempdetected = detectedId;
				Log.d(TAG, "Count in a row:" + count);
				if (count == DETECTED_IN_A_ROW)
					((MainActivity) activity).switchToVoice(detectedId);
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