package za.co.zebrav.smartdoor.facerecognition;

import java.io.IOException;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_objdetect;

import za.co.zebrav.smartdoor.MainActivity;
import za.co.zebrav.smartdoor.R;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

class SearchFaceView extends FaceView
{
	private static final String TAG = "FaceView";

	public SearchFaceView(Activity activity, Fragment fragment) throws IOException
	{
		super(activity, fragment);
		
		String settinsFile = getResources().getString(R.string.settingsFileName);
		recognisePhotos = Integer.parseInt(activity.getSharedPreferences(settinsFile, 0).getString(
							"face_RecogPhotoNum", "5"));

		initialiseClassifiers();
	}

	private int recognisePhotos;

	protected void initialiseClassifiers() throws IOException
	{
		storage = CvMemStorage.create();
		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);

		faceRunnable = new FaceClassifierRunnable(storage, activity.getCacheDir());
		faceThread = new Thread(faceRunnable, "" + 0);
	}

	private int tempdetected = -1;
		
	@Override
	protected void handleDetected(byte[] data, int width, int height)
	{
		if (faceRunnable.getTotalDetected() == 1 && personRecognizer.canPredict())
		{
			Mat fullImage = ImageTools.getGreyMatImage(data, width, height, 1);
			Log.d(TAG, "m width:" + fullImage.cols() + "; m height:"+fullImage.rows());
			int detectedId = personRecognizer.predict(fullImage);
			Log.d(TAG, "Face detected:" + detectedId);
			Log.d(TAG, "Certainty:" + personRecognizer.getCertainty());
			if (detectedId != -1)
			{
				if (tempdetected != detectedId)
					count = 0;
				else
					count++;
				((SearchCameraFragment) fragment).setProgress((100 / recognisePhotos) * count);
				tempdetected = detectedId;
				Log.d(TAG, "Count in a row:" + count);
				if (count == recognisePhotos)
					((MainActivity) activity).switchToVoice(detectedId);
			}
			else
				tempdetected = 0;
		}
	}
	
	@Override
	protected void runClassifiers()
	{
		faceRunnable.setGrayImage(grayImage);
		faceThread.run();
		try
		{
			faceThread.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	

	@Override
	protected void onDraw(Canvas canvas)
	{
		// Draw FPS
		drawFPS(canvas);
		
		if (grayImage == null)
			return;
		// Change for squares
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3);
		
		// Draw faces
		drawFace(canvas);
	}

}