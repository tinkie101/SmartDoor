package za.co.zebrav.facerecognition;

import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;

import java.io.IOException;
import java.text.DecimalFormat;

import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.Mat;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Paint;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.view.View;

public abstract class FaceView extends View implements Camera.PreviewCallback
{
	protected Activity activity;
	protected Fragment fragment;
	protected int uID;

	public void setuID(int uID)
	{
		this.uID = uID;
	}

	// ------------------------------------------------------------------------------
	private static final String TAG = "FaceView";
	/**
	 * The factor by which the camera feed needs to be down sampled by.
	 * Higher number = Higher performance.
	 * Higher number = Lower accuracy.
	 * Typically in the range [1,4]
	 */
	protected static final byte SUBSAMPLING_FACTOR_DETECTION = 2;

	/**
	 * List of threads to run each classifier.
	 */
	protected Thread[] threads;

	/**
	 * Used to draw squares around detected objects.
	 */
	protected Paint paint;
	/**
	 * Grey scale image to detect from.
	 */
	protected Mat grayImage;
	/**
	 * Storage to store temporary image.
	 */
	protected CvMemStorage storage;
	/**
	 * Last time a frame was drawn.
	 * Used to calculate FPS.
	 */
	protected long lastTime;
	/**
	 * Used to recognize faces.
	 */
	protected PersonRecognizer personRecognizer;

	public FaceView(Activity activity, Fragment fragment) throws IOException
	{
		super(activity);
		this.activity = activity;
		this.fragment = fragment;
		paint = initialisePaint();
		lastTime = System.currentTimeMillis();
	}

	/**
	 * Initialises the paint Object
	 */
	private Paint initialisePaint()
	{
		Paint result = new Paint();
		result.setTextSize(getDeviceSize(14));
		return result;
	}
	
	protected int getDeviceSize(int size)
	{
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return (int) (size * dm.scaledDensity);
	}
	
	public void onPreviewFrame(final byte[] data, final Camera camera)
	{
		try
		{
			Camera.Size size = camera.getParameters().getPreviewSize();
			processImage(data, size.width, size.height);
			camera.addCallbackBuffer(data);
		}
		catch (RuntimeException e)
		{
			// The camera has probably just been released, ignore.
		}
	}

	protected void processImage(byte[] data, int width, int height)
	{
		grayImage = ImageTools.getGreyMatImage(data, width, height, SUBSAMPLING_FACTOR_DETECTION);

		cvClearMemStorage(storage);
		runClassifiers();

		handleDetected(data, width, height);
		postInvalidate();
	}

	protected abstract void runClassifiers();
	protected abstract void handleDetected(byte[] data, int width, int height);

	protected abstract ClassifierRunnable[] getRunnables();

	protected int count = 0;

	protected String calculateFPS()
	{
		long newTime = System.currentTimeMillis();
		String result = "FPS: ";
		if (lastTime != newTime)
		{
			long temp = newTime - lastTime;
			double temp2 = temp / (double) 1000;
			double fps = (double) 1 / temp2;
			DecimalFormat df = new DecimalFormat("#.00");
			if (fps < 1)
				result = result + "0" + df.format(fps);
			else
				result = result + df.format(fps);
		}
		else
		{
			result = result + "max";
		}
		lastTime = newTime;
		return result;
	}

	

	protected abstract int getClassifierCount();

	protected abstract int getColor(int id);
}
