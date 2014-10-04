package za.co.zebrav.facerecognition;

import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;

import java.io.File;
import java.io.IOException;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;

import android.util.Log;

/**
 * Private class to do the object detection.
 */
public abstract class ClassifierRunnable implements Runnable
{
	private static final String TAG = "ClassifierRunnable";
	CvHaarClassifierCascade classifier;
	// CvSeq objects;
	Rect objects = new Rect();
	Mat grayImage;
	/**
	 * Directory where all the XML classifiers are stored.
	 */
	protected static final String directory = "/za/co/zebrav/facerecognition/";

	protected abstract String getClassifierfile();

	public Mat getGrayImage()
	{
		return grayImage;
	}

	public void setGrayImage(Mat grayImage)
	{
		this.grayImage = grayImage;
	}

	public Rect getObjects()
	{
		return objects;
	}

	CvMemStorage storage;

	public ClassifierRunnable(CvMemStorage storage, File cacheDir)
	{
		Loader.load(opencv_objdetect.class);
		try
		{
			File file;
			file = Loader.extractResource(getClass(), directory + getClassifierfile(), cacheDir, "classifier", ".xml");
			if (file == null || file.length() <= 0)
			{
				String message = "Could not extract the [" + getClassifierfile()
									+ "] classifier file from Java resource.";
				Log.e(TAG, message);
				throw new IOException(message);
			}
			cascade = new CascadeClassifier(file.getAbsolutePath());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		this.storage = storage;
	}

	CascadeClassifier cascade;

	int totalDetected = 0;

	public int getTotalDetected()
	{
		return totalDetected;
	}
	
	public void setTotalDeteced(int total)
	{
		totalDetected = total;
	}

	@Override
	public void run()
	{
		objects.deallocate();
		objects = new Rect();
		cascade.detectMultiScale(grayImage, objects, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING, new Size(),
							new Size(grayImage.cols(), grayImage.rows()));
		if (objects.width() == 0)
			totalDetected = 0;
		else
			totalDetected = objects.capacity();
//		int temp = totalDetected;
//		if (totalDetected > 0)
//		{
//
//			//IntPointer rweights = new IntPointer(1);
//			//groupRectangles(objects, 100);
//			groupRectangles(objects, 1, Double.MAX_VALUE);
//		}
//		if (objects.width() == 0)
//			totalDetected = 0;
//		else
//			totalDetected = objects.capacity();
//		Log.d(TAG, "change:" + (temp - totalDetected));

	}

}