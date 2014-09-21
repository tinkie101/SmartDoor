package za.co.zebrav.facerecognition;

import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_core.cvLoad;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;

import za.co.zebrav.smartdoor.MainActivity;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.util.Log;
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
//------------------------------------------------------------------------------	
	private static final String TAG = "FaceView";
	/**
	 * The factor by which the camera feed needs to be down sampled by.
	 * Higher number = Higher performance.
	 * Higher number = Lower accuracy.
	 * Typically in the range [1,6]
	 */
	protected static final byte SUBSAMPLING_FACTOR_DETECTION = 2;
	/**
	 * Directory where all the XML classifiers are stored.
	 */
	protected static final String directory = "/za/co/zebrav/facerecognition/";
	/**
	 * List of threads to run each classifier.
	 */
	protected Thread[] threads;
	/**
	 * List of Runnable objects used in threads.
	 * List is kept to have access to their local variables.
	 */
	protected concurrentDetector[] runnables;
	/**
	 * Used to draw squares around detected objects.
	 */
	protected Paint paint;
	/**
	 * Grey scale image to detect from.
	 */
	protected IplImage grayImage;
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
		personRecognizer = new PersonRecognizer(activity);
		lastTime = System.currentTimeMillis();
	}
	
	protected void initialiseClassifiers(int classifierCount,int colors[],String[] classifierFiles) throws IOException
	{
		Log.d(TAG, "Classifier count:"+classifierCount);
		storage = CvMemStorage.create();
		threads = new Thread[classifierCount];
		runnables = new concurrentDetector[classifierCount];
		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);
		for (int i = 0; i < classifierCount; i++)
		{
			File file = Loader.extractResource(getClass(), directory + classifierFiles[i], activity.getCacheDir(),
								"classifier", ".xml");
			if (file == null || file.length() <= 0)
			{
				throw new IOException("Could not extract the [" + classifierFiles[i]
									+ "] classifier file from Java resource.");
			}

			CvHaarClassifierCascade classifier = new CvHaarClassifierCascade(cvLoad(file.getAbsolutePath()));
			file.delete();
			if (classifier.isNull())
			{
				throw new IOException("Could not load the [" + classifierFiles[i] + "] classifier file.");
			}
			runnables[i] = new concurrentDetector(classifier, storage);
			threads[i] = new Thread(runnables[i], "" + i);
		}
	}
	/**
	 * Initialises the paint Object
	 */
	private Paint initialisePaint()
	{
		Paint result = new Paint();
		result.setTextSize(20);
		result.setStyle(Paint.Style.STROKE);
		return result;
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
		// First, downsample our image and convert it into a grayscale IplImage
		int f = SUBSAMPLING_FACTOR_DETECTION;
		if (grayImage == null || grayImage.width() != width / f || grayImage.height() != height / f)
		{
			grayImage = IplImage.create(width / f, height / f, IPL_DEPTH_8U, 1);
		}
		int imageWidth = grayImage.width();
		int imageHeight = grayImage.height();
		int dataStride = f * width;
		int imageStride = grayImage.widthStep();
		ByteBuffer imageBuffer = grayImage.getByteBuffer();
		for (int y = 0; y < imageHeight; y++)
		{
			int dataLine = y * dataStride;
			int imageLine = y * imageStride;
			for (int x = 0; x < imageWidth; x++)
			{
				imageBuffer.put(imageLine + x, data[dataLine + f * x]);
			}
		}

		cvClearMemStorage(storage);
		for (int i = 0; i < threads.length; i++)
		{
			threads[i].run();
		}
		for (int i = 0; i < threads.length; i++)
		{
			try
			{
				threads[i].join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		handleDetected(data,width,height);
		postInvalidate();
	}
	
	protected abstract void handleDetected(byte[] data, int width, int height);
	protected int count = 0;

	private String calculateFPS()
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

	@Override
	protected void onDraw(Canvas canvas)
	{
		String FPS = calculateFPS();
		float textWidth = paint.measureText(FPS);
		paint.setStrokeWidth(2);
		paint.setColor(Color.WHITE);
		canvas.drawText(FPS, (getWidth() - textWidth), 15, paint);
		paint.setStrokeWidth(3);
		for (int i = 0; i < getClassifierCount(); i++)
		{
			if (runnables[i].getObjects() != null)
			{
				paint.setColor(getColor(i));
				float scaleX = (float) getWidth() / grayImage.width();
				float scaleY = (float) getHeight() / grayImage.height();
				int total = runnables[i].getObjects().total();
				for (int j = 0; j < total; j++)
				{
					CvRect r = new CvRect(cvGetSeqElem(runnables[i].getObjects(), j));
					int x = r.x(), y = r.y(), w = r.width(), h = r.height();
					canvas.drawRect(getWidth() - ((x + w) * scaleX), y * scaleY, getWidth() - (x * scaleX), (y + h)
										* scaleY, paint);
				}
			}
		}
	}
	protected abstract int getClassifierCount(); 
	
	protected abstract int getColor(int id); 
	/**
	 * Private class to do the object detection.
	 */
	protected class concurrentDetector implements Runnable
	{
		CvHaarClassifierCascade classifier;
		CvSeq objects;

		public CvSeq getObjects()
		{
			return objects;
		}

		CvMemStorage storage;

		public concurrentDetector(CvHaarClassifierCascade classifier, CvMemStorage storage)
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
}
