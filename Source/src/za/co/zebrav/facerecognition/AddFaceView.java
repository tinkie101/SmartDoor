package za.co.zebrav.facerecognition;

import java.io.IOException;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.Rect;

import org.bytedeco.javacpp.opencv_core.Mat;

import za.co.zebrav.smartdoor.R;
import za.co.zebrav.smartdoor.database.AddUserActivity;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
		String settinsFile = getResources().getString(R.string.settingsFileName);
		trainNumPhotos = Integer.parseInt(activity.getSharedPreferences(settinsFile, 0).getString("face_TrainPhotoNum", "0"));
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
			//ImageTools.saveImageAsPNG(ImageTools.getGreyMatImage(data, width, height, 1), uID + "-" + count, activity);
			ImageTools.saveImageAsPNG(face, uID + "-" + count, activity);
			Log.d(TAG, "Saved ID:" + uID + " Number: " + count + " .");
			count++;
			((AddCameraFragment) fragment).setProgress((100/trainNumPhotos) * count);
			if (count == trainNumPhotos)
			{
				((AddUserActivity) activity).switchFragToStep3();
			}
		}
	}
	
	private int trainNumPhotos;
	
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
	
	Mat face;
	
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
			e1.printStackTrace();
		}

		if (getRunnables()[0].getTotalDetected() > 0)
		{
			int beginx = (int) (getRunnables()[0].getObjects().x());
			int beginy = (int) (getRunnables()[0].getObjects().y());

			int endx = (int) (getRunnables()[0].getObjects().width());
			int endy = (int) (getRunnables()[0].getObjects().height());

			face = grayImage.rowRange(beginy,beginy + endy);
			face = face.colRange(beginx, beginx + endx);

			getRunnables()[1].setGrayImage(face);
			threads[1].run();

			getRunnables()[2].setGrayImage(face);
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
		else
		{
			runnables[1].setTotalDeteced(0);
			runnables[2].setTotalDeteced(0);
		}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		// Draw FPS
		String FPS = calculateFPS();
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		float textWidth = paint.measureText(FPS);
		paint.setStrokeWidth(2);
		paint.setColor(Color.WHITE);
		canvas.drawText(FPS, (getWidth() - textWidth), getDeviceSize(14), paint);
		if (grayImage == null)
			return;
		// Change for eyes and nose
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3);
		// Draw faces
		int total = getRunnables()[0].getTotalDetected();
		Rect faceRectangle = getRunnables()[0].getObjects().position(0);
		paint.setColor(getColor(0));
		float scaleX = (float) getWidth() / grayImage.cols();
		float scaleY = (float) getHeight() / grayImage.rows();
		int x = faceRectangle.x(), y = faceRectangle.y(), w = faceRectangle.width(), h = faceRectangle.height();
		
		
		int startx = (int) (getWidth() - ((x + w) * scaleX));
		int starty = (int) (y * scaleY);
		int endx = (int) (getWidth() - (x * scaleX));
		int endy = (int) ((y + h) * scaleY);
		
		
		canvas.drawRect(startx, starty,endx , endy, paint);
		
		//scaleX = (float) grayImage.cols() / faceRectangle.width();
		//scaleY = (float) grayImage.rows() / faceRectangle.height();
		// Draw eyes and nose
		for (int i = 1; i < getClassifierCount(); i++)
		{
			if (getRunnables()[i].getObjects() != null)
			{
				paint.setColor(getColor(i));
				total = getRunnables()[i].getTotalDetected();
				for (int j = 0; j < total; j++)
				{
					Rect rect = getRunnables()[i].getObjects().position(j);
					x = rect.x();
					y = rect.y();
					w = rect.width();
					h = rect.height();
					
					startx = (int) (getWidth() - (faceRectangle.x() + x + w) * scaleX);
					endx = (int) (getWidth() - (faceRectangle.x() + x) * scaleX);
					//startx = (int) (((faceRectangle.x() + faceRectangle.width()) - (x + w)) * scaleX);
					//endx = (int) (((faceRectangle.x() + faceRectangle.width()) - x) * scaleX);
					
					starty = (int)( (y + faceRectangle.y()) * scaleY);
					endy = (int) ((y + faceRectangle.y() + h) * scaleY);
					canvas.drawRect(startx, starty,endx, endy, paint);
				}
			}
		}
	}
}