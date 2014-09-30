package za.co.zebrav.facerecognition;

import java.io.File;

import org.bytedeco.javacpp.opencv_core.CvMemStorage;

public class FaceClassifierRunnable extends ClassifierRunnable
{
	private static final String classifierFile = "haarcascade_frontalface_alt.xml";
	@Override
	protected String getClassifierfile()
	{
		return classifierFile;
	}
	public FaceClassifierRunnable(CvMemStorage storage, File cacheDir)
	{
		super(storage,cacheDir);
		
	}

}
