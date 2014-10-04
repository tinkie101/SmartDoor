package za.co.zebrav.smartdoor.facerecognition;
import java.io.File;

import org.bytedeco.javacpp.opencv_core.CvMemStorage;

public class NoseClassifierRunnable extends ClassifierRunnable
{
	private static final String classifierFile = "haarcascade_nose.xml";
	@Override
	protected String getClassifierfile()
	{
		return classifierFile;
	}
	public NoseClassifierRunnable(CvMemStorage storage, File cacheDir)
	{
		super(storage,cacheDir);
	}

}
