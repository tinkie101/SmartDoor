package za.co.zebrav.smartdoor.test;

import static org.bytedeco.javacpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_highgui.imread;

import java.io.File;

import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.junit.Test;

import za.co.zebrav.smartdoor.facerecognition.ClassifierRunnable;
import za.co.zebrav.smartdoor.facerecognition.EyesClassifierRunnable;
import za.co.zebrav.smartdoor.facerecognition.FaceClassifierRunnable;
import za.co.zebrav.smartdoor.facerecognition.NoseClassifierRunnable;
import junit.framework.TestCase;

public class ClassifierRunnableTest extends TestCase
{
	CvMemStorage storage;
	String directory;
	File imageToDetectOn;
	double groupRectangleThreshold = 0.2;
	Mat grayImage;
	File cacheDir;
//	@Override
//	protected void setUp() throws Exception
//	{
//		super.setUp();
//		storage = new CvMemStorage();
//		directory = "/za/co/zebrav/smartdoor/facerecognition/";
//		
//		imageToDetectOn = new File("subject01.gif");
//		
//		cacheDir = new File("/data/data/za.co.zebrav.smartdoor.test/cache");
//		
//		System.out.println("File Exists("+imageToDetectOn.getAbsolutePath()+"):" + imageToDetectOn.exists());
//		assertTrue(imageToDetectOn.exists());
//		
//		System.out.println("File Exists:" + cacheDir.exists());
//		assertTrue(cacheDir.exists());
//		
//		grayImage = imread(imageToDetectOn.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
//	}
//	
//	@Test
//	public void testFace()
//	{
//		ClassifierRunnable face = new FaceClassifierRunnable(storage,cacheDir,groupRectangleThreshold);
//		face.setGrayImage(grayImage);
//		face.classify();
//		assertNotSame(1, face.getTotalDetected());
//	}
//	
//	@Test
//	public void testEyes()
//	{
//		ClassifierRunnable eyes = new EyesClassifierRunnable(storage,cacheDir,groupRectangleThreshold);
//		eyes.setGrayImage(grayImage);
//		eyes.classify();
//		assertNotSame(2, eyes.getTotalDetected());
//	}
//	
//	@Test
//	public void testNose()
//	{
//		ClassifierRunnable nose = new NoseClassifierRunnable(storage,cacheDir,groupRectangleThreshold);
//		nose.setGrayImage(grayImage);
//		nose.classify();
//		assertNotSame(1, nose.getTotalDetected());
//	}
	
	@Test
	public void testRectangle()
	{
		ClassifierRunnable face = new FaceClassifierRunnable(storage,cacheDir,groupRectangleThreshold);
		Rect test = new Rect(1);
		test.position(0).x(0).y(0).width(100).height(100); 
		face.setObjects(test);
		assertSame("Should be same as inithilised value",1, face.getTotalDetected());
		face.rectangleGroup();
		assertSame("After run should have same amount of rectangles",1, face.getTotalDetected());
	}
}
