package za.co.zebrav.facerecognition;

import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.cvLoad;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;

import java.io.File;
import java.io.IOException;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;

import android.util.Log;

/**
 * Private class to do the object detection.
 */
public abstract class ClassifierRunnable implements Runnable
{
	private static final String TAG = "ClassifierRunnable";
	CvHaarClassifierCascade classifier;
	CvSeq objects;
	IplImage grayImage;
	/**
	 * Directory where all the XML classifiers are stored.
	 */
	protected static final String directory = "/za/co/zebrav/facerecognition/";

	protected abstract String getClassifierfile();

	public IplImage getGrayImage()
	{
		return grayImage;
	}

	public void setGrayImage(IplImage grayImage)
	{
		this.grayImage = grayImage;
	}

	public CvSeq getObjects()
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
			classifier = new CvHaarClassifierCascade(cvLoad(file.getAbsolutePath()));
			file.delete();
			if (classifier.isNull())
			{
				String message = "Could not load the [" + getClassifierfile() + "] classifier file.";
				Log.e(TAG, message);
				throw new IOException(message);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		this.storage = storage;
	}

	@Override
	public void run()
	{
		objects = cvHaarDetectObjects(grayImage, classifier, this.storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
	}

}