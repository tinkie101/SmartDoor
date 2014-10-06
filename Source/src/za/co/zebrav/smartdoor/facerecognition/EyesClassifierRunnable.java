package za.co.zebrav.smartdoor.facerecognition;

import java.io.File;

import org.bytedeco.javacpp.opencv_core.CvMemStorage;

public class EyesClassifierRunnable extends ClassifierRunnable
{
	private static final String TAG = "EyesClassifierRunnable";
	private static final String classifierFile = "haarcascade_eye.xml";
	@Override
	protected String getClassifierfile()
	{
		return classifierFile;
	}
	public EyesClassifierRunnable(CvMemStorage storage, File cacheDir,double groupRectangleThreshold)
	{
		super(storage,cacheDir,groupRectangleThreshold);
		
	}
	@Override
	public void run()
	{
		super.run();
		if (totalDetected > 2)
		{
			rectangleGroup();
		}
	}

}
