package za.co.zebrav.smartdoor.facerecognition;

import java.io.IOException;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_objdetect;

import za.co.zebrav.smartdoor.MainActivity;
import za.co.zebrav.smartdoor.R;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

class SearchFaceView extends FaceView
{
	private static final String TAG = "FaceView";
	private static final int classifierCount = 1;
	private static final int colors[] = { Color.RED };

	public SearchFaceView(Activity activity, Fragment fragment) throws IOException
	{
		super(activity, fragment);
		String settingsFile = getResources().getString(R.string.settingsFileName);
		int photosPerPerson = Integer.parseInt(activity.getSharedPreferences(settingsFile, 0).getString("face_TrainPhotoNum", "5"));
		int algorithm = Integer.parseInt(activity.getSharedPreferences(settingsFile, 0).getString("face_faceRecognizerAlgorithm", "1"));
		int threshold = Integer.parseInt(activity.getSharedPreferences(settingsFile, 0).getString("face_recognizerThreshold", "0"));
		personRecognizer = new PersonRecognizer(activity, photosPerPerson,algorithm, threshold);
		String settinsFile = getResources().getString(R.string.settingsFileName);
		recognisePhotos = Integer.parseInt(activity.getSharedPreferences(settinsFile, 0).getString("face_RecogPhotoNum", "5"));

		initialiseClassifiers();
	}
	
	private int recognisePhotos;
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
				((SearchCameraFragment) fragment).setProgress((100/recognisePhotos) * count);
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
	protected void runClassifiers()
	{
		for (int i = 0; i < threads.length; i++)
		{
			getRunnables()[i].setGrayImage(grayImage);
			threads[i].run();
		}
		for (int i = 0; i < threads.length; i++)
		{
			try
			{
				threads[i].join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		String FPS = calculateFPS();
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		float textWidth = paint.measureText(FPS);
		paint.setStrokeWidth(2);
		paint.setColor(Color.WHITE);
		
		canvas.drawText(FPS, (getWidth() - textWidth), getDeviceSize(14), paint);
		if (grayImage == null)
			return;
		
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3);

		int total = getRunnables()[0].getTotalDetected();
		Rect rect = getRunnables()[0].getObjects();
		paint.setColor(getColor(0));
		float scaleX = (float) getWidth() / grayImage.cols();
		float scaleY = (float) getHeight() / grayImage.rows();

		for (int j = 0; j < total; j++)
		{
			Rect r = rect.position(j);
			int x = r.x(), y = r.y(), w = r.width(), h = r.height();
			canvas.drawRect(getWidth() - ((x + w) * scaleX), y * scaleY, getWidth() - (x * scaleX), (y + h) * scaleY,
								paint);
		}

	}
}