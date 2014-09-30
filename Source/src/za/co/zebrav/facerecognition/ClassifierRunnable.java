package za.co.zebrav.facerecognition;

import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;

import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;

/**
 * Private class to do the object detection.
 */
public class ClassifierRunnable implements Runnable
{
	CvHaarClassifierCascade classifier;
	CvSeq objects;
	IplImage grayImage;

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

	public ClassifierRunnable(CvHaarClassifierCascade classifier, CvMemStorage storage)
	{
		this.classifier = classifier;
		this.storage = storage;
	}

	@Override
	public void run()
	{
		objects = cvHaarDetectObjects(grayImage, classifier, this.storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
	}

}