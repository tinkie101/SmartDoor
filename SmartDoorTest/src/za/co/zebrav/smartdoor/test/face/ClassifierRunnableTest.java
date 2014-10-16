package za.co.zebrav.smartdoor.test.face;

import static org.bytedeco.javacpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;

import org.bytedeco.javacpp.opencv_core.Rect;

import static org.bytedeco.javacpp.opencv_highgui.imread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.junit.Test;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import za.co.zebrav.smartdoor.TestFragmentActivity;
import za.co.zebrav.smartdoor.facerecognition.ClassifierRunnable;
import za.co.zebrav.smartdoor.facerecognition.EyesClassifierRunnable;
import za.co.zebrav.smartdoor.facerecognition.FaceClassifierRunnable;
import za.co.zebrav.smartdoor.facerecognition.NoseClassifierRunnable;
import junit.framework.TestCase;

public class ClassifierRunnableTest extends ActivityInstrumentationTestCase2<TestFragmentActivity>
{
	private static final String LOG_TAG = "ClassifierRunnableTest";
	CvMemStorage storage;
	String directory;
	File imageToDetectOn;
	double groupRectangleThreshold = 0.2;
	Mat grayImage;
	File cacheDir;

	public ClassifierRunnableTest()
	{
		super(TestFragmentActivity.class);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		storage = new CvMemStorage();
		directory = "/za/co/zebrav/smartdoor/facerecognition/";

		File f = new File(getActivity().getCacheDir() + "/subject01.jpg");
		if (!f.exists())
			try
			{
				InputStream is = getActivity().getAssets().open("pictures/subject01.jpg");
				int size = is.available();
				byte[] buffer = new byte[size];
				is.read(buffer);
				is.close();

				FileOutputStream fos = new FileOutputStream(f);
				fos.write(buffer);
				fos.close();
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}

		imageToDetectOn = f;

		cacheDir = getActivity().getCacheDir();

		assertTrue("image to detect on should exist",imageToDetectOn.exists());

		assertTrue("cache dir should exist",cacheDir.exists());

		grayImage = imread(imageToDetectOn.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
		
	}

	@Test
	public void testFace()
	{
		ClassifierRunnable face = new FaceClassifierRunnable(storage, cacheDir, groupRectangleThreshold);
		face.setGrayImage(grayImage);
		face.classify();
		assertSame("Should detect 1 face.",1, face.getTotalDetected());
	}

	@Test
	public void testEyes()
	{
		ClassifierRunnable eyes = new EyesClassifierRunnable(storage, cacheDir, groupRectangleThreshold);
		eyes.setGrayImage(grayImage);
		eyes.classify();
		assertSame("Should detect 2 eyes.",2, eyes.getTotalDetected());
	}

	@Test
	public void testNose()
	{
		ClassifierRunnable nose = new NoseClassifierRunnable(storage, cacheDir, groupRectangleThreshold);
		nose.setGrayImage(grayImage);
		nose.classify();
		assertSame("Should detect 1 nose.",1, nose.getTotalDetected());
	}

	@Test
	public void testRectangle()
	{
		ClassifierRunnable face = new FaceClassifierRunnable(storage, cacheDir, groupRectangleThreshold);
		Rect test = new Rect(1);
		test.position(0).x(0).y(0).width(100).height(100);
		face.setObjects(test);
		assertSame("Should be same as inithilised value", 1, face.getTotalDetected());
		face.rectangleGroup();
		assertSame("Only one rectangle so one should return", 1, face.getTotalDetected());

		test = new Rect(2);
		test.position(0).x(0).y(0).width(100).height(100);
		test.position(1).x(0).y(0).width(100).height(100);
		face.setObjects(test);
		assertSame("Should be same as inithilised value", 2, face.getTotalDetected());
		face.rectangleGroup();
		assertSame("2 identical rectangles should be grouped to 1", 1, face.getTotalDetected());

		test = new Rect(10);
		test.position(0).x(0).y(0).width(100).height(100);
		test.position(2).x(0).y(0).width(100).height(100);
		test.position(3).x(0).y(0).width(100).height(100);
		test.position(4).x(0).y(0).width(100).height(100);
		test.position(5).x(0).y(0).width(100).height(100);
		test.position(6).x(0).y(0).width(100).height(100);
		test.position(7).x(0).y(0).width(100).height(100);
		test.position(8).x(0).y(0).width(100).height(100);
		test.position(9).x(0).y(0).width(100).height(100);
		face.setObjects(test);
		assertSame("Should be same as inithilised value", 10, face.getTotalDetected());
		face.rectangleGroup();
		assertSame("10 identical rectangles should be grouped to 1", 1, face.getTotalDetected());

		test = new Rect(2);
		test.position(0).x(0).y(0).width(100).height(100);
		test.position(1).x(0).y(0).width(101).height(100);
		face.setObjects(test);
		assertSame("Should be same as inithilised value", 2, face.getTotalDetected());
		face.rectangleGroup();
		assertSame("2 very simular rectangles should group to 1", 1, face.getTotalDetected());

		test = new Rect(2);
		test.position(0).x(0).y(0).width(100).height(100);
		test.position(1).x(10).y(10).width(110).height(110);
		face.setObjects(test);
		assertSame("Should be same as inithilised value", 2, face.getTotalDetected());
		face.rectangleGroup();
		assertSame("2 fairly simular rectangles should group to 1", 1, face.getTotalDetected());

		test = new Rect(2);
		test.position(0).x(0).y(0).width(100).height(100);
		test.position(1).x(100).y(100).width(200).height(200);
		face.setObjects(test);
		assertSame("Should be same as inithilised value", 2, face.getTotalDetected());
		face.rectangleGroup();
		assertSame("2 completely different rectangles should not be grouped", 2, face.getTotalDetected());

		face = new FaceClassifierRunnable(storage, cacheDir, Double.MAX_VALUE);
		test = new Rect(2);
		test.position(0).x(0).y(0).width(100).height(100);
		test.position(1).x(100).y(100).width(200).height(200);
		face.setObjects(test);
		assertSame("Should be same as inithilised value", 2, face.getTotalDetected());
		face.rectangleGroup();
		assertSame("When threshold tends to inf it should group all rectangles", 1, face.getTotalDetected());
	}
}
