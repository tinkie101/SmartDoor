package za.co.zebrav.facerecognition;

import java.io.IOException;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;

import org.bytedeco.javacpp.opencv_core.Mat;

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
	private static final int colors[] = { Color.RED, Color.GREEN, Color.BLUE };

	/**
	 * List of Runnable objects used in threads.
	 * List is kept to have access to their local variables.
	 */
	private ClassifierRunnable[] runnables;

	public AddFaceView(Activity activity, Fragment fragment) throws IOException
	{
		super(activity, fragment);
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

		runnables[1] = new EyesClassifierRunnable(storage, activity.getCacheDir());
		threads[1] = new Thread(runnables[1], "" + 1);

		runnables[2] = new NoseClassifierRunnable(storage, activity.getCacheDir());
		threads[2] = new Thread(runnables[2], "" + 2);
	}

	@Override
	protected void handleDetected(byte[] data, int width, int height)
	{
		if (runnables[0].getTotalDetected() == 1 && runnables[1].getTotalDetected() == 2
							&& runnables[2].getTotalDetected() == 1)
		{
			Log.d(TAG, "Conditions met");
			ImageTools.saveImageAsPNG(ImageTools.getGreyMatImage(data, width, height, 1), uID + "-" + count, activity);
			Log.d(TAG, "Saved ID:" + uID + " Number: " + count + " .");
			count++;
			((AddCameraFragment) fragment).setProgress(25 * count);
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

	@Override
	protected ClassifierRunnable[] getRunnables()
	{
		return runnables;
	}

	boolean savedImage = false;

	@Override
	protected void runClassifiers()
	{
		getRunnables()[0].setGrayImage(grayImage);
		threads[0].run();
		try
		{
			threads[0].join();
		}
		catch (InterruptedException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (getRunnables()[0].getTotalDetected() > 0)
		{
			Log.d(TAG, "begin creating result");
			int beginx = (int) (getRunnables()[0].getObjects().x() - ((getRunnables()[0].getObjects().width()) * 0.1));
			int beginy = (int) (getRunnables()[0].getObjects().y() - ((getRunnables()[0].getObjects().height()) * 0.1));
			
			
			int endx = (int) (getRunnables()[0].getObjects().width() + ((getRunnables()[0].getObjects().width()) * 0.1));
			int endy = (int) (getRunnables()[0].getObjects().height() + ((getRunnables()[0].getObjects().height()) * 0.1));
			
			
			Mat result = grayImage.rowRange(beginy, endx);
			result = result.colRange(beginx, endy);
			
			
			 //Mat result = new Mat(getRunnables()[0].getObjects());
			 Log.d(TAG, "done creating result");
//			if (!savedImage)
//			{
//				ImageTools.saveImageAsPNG(result, "test", activity);
//				Log.d(TAG, "done writing image result");
//				savedImage = true;
//			}

			getRunnables()[1].setGrayImage(result);
			threads[1].run();

			getRunnables()[2].setGrayImage(result);
			threads[2].run();

			try
			{
				threads[1].join();
				threads[2].join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}