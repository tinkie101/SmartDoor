package za.co.zebrav.smartdoor.facerecognition;

import java.io.IOException;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_objdetect;

import za.co.zebrav.smartdoor.R;
import za.co.zebrav.smartdoor.database.AddUserActivity;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

class AddFaceView extends FaceView
{
	private ClassifierRunnable noseRunnable;
	private Thread noseThread;

	private ClassifierRunnable eyesRunnable;
	private Thread eyesThread;

	private static final String TAG = "AddFaceView";
	private int count = 0;

	public AddFaceView(Activity activity, Fragment fragment) throws IOException
	{
		super(activity, fragment);
		initialiseClassifiers();
		String settingsFile = getResources().getString(R.string.settingsFileName);
		trainNumPhotos = Integer.parseInt(activity.getSharedPreferences(settingsFile, 0).getString(
							"face_TrainPhotoNum", "0"));
	}

	protected void initialiseClassifiers() throws IOException
	{
		String settingsFile = getResources().getString(R.string.settingsFileName);
		storage = CvMemStorage.create();
		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);

		faceRunnable = new FaceClassifierRunnable(storage, activity.getCacheDir());
		faceThread = new Thread(faceRunnable, "" + 0);

		if ((activity.getSharedPreferences(settingsFile, 0).getString("face_detectNose", "0")).equals("true"))
		{
			noseRunnable = new NoseClassifierRunnable(storage, activity.getCacheDir());
			noseThread = new Thread(noseRunnable, "" + 0);
		}
		if ((activity.getSharedPreferences(settingsFile, 0).getString("face_detectEyes", "0")).equals("true"))
		{
			eyesRunnable = new EyesClassifierRunnable(storage, activity.getCacheDir());
			eyesThread = new Thread(eyesRunnable, "" + 0);
		}
	}

	private boolean checkConditions()
	{
		if (noseRunnable != null && eyesRunnable != null)
		{
			if (faceRunnable.getTotalDetected() == 1 && eyesRunnable.getTotalDetected() == 2
								&& noseRunnable.getTotalDetected() == 1)
			{
				return true;
			}
		}
		else if (noseRunnable != null)
		{
			if (faceRunnable.getTotalDetected() == 1 && noseRunnable.getTotalDetected() == 1)
			{
				return true;
			}
		}
		else if (eyesRunnable != null)
		{
			if (faceRunnable.getTotalDetected() == 1 && eyesRunnable.getTotalDetected() == 2)
			{
				return true;
			}
		}
		else if (faceRunnable.getTotalDetected() == 1)
		{
			return true;
		}
		return false;
	}

	@Override
	protected void handleDetected(byte[] data, int width, int height)
	{
		if (checkConditions())
		{
			Log.d(TAG, "Conditions met");
			ImageTools.saveImageAsPNG(ImageTools.getGreyMatImage(data, width, height, 1), uID + "-" + count, activity);
			// ImageTools.saveImageAsPNG(face, uID + "-" + count, activity);
			Log.d(TAG, "Saved ID:" + uID + " Number: " + count + " .");
			count++;
			((AddCameraFragment) fragment).setProgress((100 / trainNumPhotos) * count);
			if (count == trainNumPhotos)
			{
				((AddUserActivity) activity).switchFragToStep3();
			}
		}
	}

	private int trainNumPhotos;

	private void runFace(Mat grayImage)
	{
		faceRunnable.setGrayImage(grayImage);
		faceThread.run();
		try
		{
			faceThread.join();
		}
		catch (InterruptedException e1)
		{
			e1.printStackTrace();
		}
	}

	private void runNoseAndEyes(Mat grayImage)
	{
		try
		{
			if (noseRunnable != null)
			{
				noseRunnable.setGrayImage(grayImage);
				noseThread.run();
			}
			if (eyesRunnable != null)
			{
				eyesRunnable.setGrayImage(grayImage);
				eyesThread.run();
			}
			if (noseRunnable != null)
				noseThread.join();
			if (eyesRunnable != null)
				eyesThread.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void runClassifiers()
	{
		runFace(grayImage);
		if (faceRunnable.getTotalDetected() > 0)
		{
			int beginx = (int) (faceRunnable.getObjects().x());
			int beginy = (int) (faceRunnable.getObjects().y());

			int endx = (int) (faceRunnable.getObjects().width());
			int endy = (int) (faceRunnable.getObjects().height());

			Mat face = grayImage.rowRange(beginy, beginy + endy);
			face = face.colRange(beginx, beginx + endx);

			runNoseAndEyes(face);
		}
		else
		{
			eyesRunnable.setTotalDeteced(0);
			noseRunnable.setTotalDeteced(0);
		}
	}

	protected void drawClassifier(Canvas canvas, ClassifierRunnable runnable, Rect faceRectangle)
	{
		float scaleX = (float) getWidth() / grayImage.cols();
		float scaleY = (float) getHeight() / grayImage.rows();
		int total = runnable.getTotalDetected();
		for (int j = 0; j < total; j++)
		{
			Rect rect = runnable.getObjects().position(j);
			int x = rect.x();
			int y = rect.y();
			int w = rect.width();
			int h = rect.height();

			int startx = (int) (getWidth() - (faceRectangle.x() + x + w) * scaleX);
			int endx = (int) (getWidth() - (faceRectangle.x() + x) * scaleX);

			int starty = (int) ((y + faceRectangle.y()) * scaleY);
			int endy = (int) ((y + faceRectangle.y() + h) * scaleY);
			canvas.drawRect(startx, starty, endx, endy, paint);
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

		// Draw Eyes
		if (eyesRunnable != null)
		{
			paint.setColor(Integer.parseInt(getResources().getString((R.string.face_EyesColor))));
			drawClassifier(canvas, eyesRunnable, faceRunnable.getObjects());
		}
		// Draw Nose
		if (noseRunnable != null)
		{
			paint.setColor(Integer.parseInt(getResources().getString((R.string.face_NoseColor))));
			drawClassifier(canvas, noseRunnable, faceRunnable.getObjects());
		}
	}
}