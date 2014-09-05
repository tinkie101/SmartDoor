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
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;

abstract class FaceView extends View implements Camera.PreviewCallback
{
	private static final String TAG = "FaceView";
	/**
	 * The factor by which the camera feed needs to be down sampled by.
	 * Higher number = Higher performance.
	 * Higher number = Lower accuracy.
	 * Typically in the range [1,6]
	 */
	public static final byte SUBSAMPLING_FACTOR = 2;
	/**
	 * List of all the XML files used for classifiers
	 */
	private static final String[] classifierFiles = {"haarcascade_frontalface_alt.xml","haarcascade_eye.xml","haarcascade_nose.xml"};
	/**
	 * Colors which will be used to draw squares around classified objects.
	 * Order will be the same as for classifiers.
	 */
	private static final int Colors[] = {Color.RED,Color.GREEN,Color.BLUE};
	/**
	 * Directory where all the XML classifiers are stored.
	 */
	private static final String directory = "/za/co/zebrav/facerecognition/";
	/**
	 * List of threads to run each classifier.
	 */
	private Thread[] threads;
	/**
	 * List of Runnable objects used in threads.
	 * List is kept to have access to their local variables.
	 */
	private concurrentDetector[] runnables;
	/**
	 * Used to draw squares around detected objects.
	 */
	private Paint paint;
	/**
	 * Grey scale image to detect from. 
	 */
	private IplImage grayImage;
	/**
	 * Storage to store temporary image.
	 */
	private CvMemStorage storage;
	/**
	 * Last time a frame was drawn.
	 * Used to calculate FPS.	
	 */
	private long lastTime;
	public FaceView(Context context) throws IOException
	{
		super(context);
		paint = new Paint();
		// Load the classifier file from Java resources.
		storage = CvMemStorage.create();
		threads = new Thread[classifierFiles.length];
		runnables = new concurrentDetector[classifierFiles.length];
		for(int i = 0; i < classifierFiles.length;i++)
		{
			File file = Loader.extractResource(getClass(),
					directory + classifierFiles[i], context.getCacheDir(),
					"classifier", ".xml");
			if (file == null || file.length() <= 0)
			{
				throw new IOException("Could not extract the ["+classifierFiles[i]+"] classifier file from Java resource.");
			}
			// Preload the opencv_objdetect module to work around a known bug.
			Loader.load(opencv_objdetect.class);
			CvHaarClassifierCascade classifier = new CvHaarClassifierCascade(cvLoad(file.getAbsolutePath()));
			file.delete();
			if (classifier.isNull())
			{
				throw new IOException("Could not load the ["+classifierFiles[i]+"] classifier file.");
			}
			runnables[i] = new concurrentDetector(classifier, storage);
			threads[i] = new  Thread(runnables[i], "" + i);
		}
		lastTime = System.currentTimeMillis();
	}
	
	public abstract void processFaces(CvSeq faces);

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
		int f = SUBSAMPLING_FACTOR;
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
		for(int i = 0; i < threads.length;i++)
		{
			threads[i].run();
		}
		for(int i = 0; i < threads.length;i++)
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
		postInvalidate();
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		
		paint.setTextSize(20);
		long newTime = System.currentTimeMillis();
		String s = "FPS: ";
		if(lastTime != newTime)
		{
			long temp = newTime-lastTime;
			double temp2 = temp/(double)1000;
			Log.d(TAG, "" +temp2);
			double fps = (double)1/temp2;
			DecimalFormat df = new DecimalFormat("#.00");
			if(fps < 1) s = s + "0" +   df.format(fps);
			else s = s +  df.format(fps);
		}
		else
		{
			s = s + "max";
		}
		 float textWidth = paint.measureText(s);
		 canvas.drawText(s, (getWidth() - textWidth), 15, paint);
		lastTime = newTime;
		paint.setStrokeWidth(3);
		paint.setStyle(Paint.Style.STROKE);

		for(int i = 0; i < runnables.length; i++)
		{
			if (runnables[i].getObjects() != null)
			{
				paint.setColor(Colors[i]);
				float scaleX = (float) getWidth() / grayImage.width();
				float scaleY = (float) getHeight() / grayImage.height();
				int total = runnables[i].getObjects().total();
				for (int j = 0; j < total; j++)
				{
					CvRect r = new CvRect(cvGetSeqElem(runnables[i].getObjects(), j));
					int x = r.x(), y = r.y(), w = r.width(), h = r.height();
					// x = (int) (getWidth() - (x * scaleX));
					canvas.drawRect(getWidth() - ((x + w) * scaleX), y * scaleY, getWidth() - (x * scaleX), (y + h)
										* scaleY, paint);
				}
			}
		}
	}
	private class concurrentDetector implements Runnable
	{
		CvHaarClassifierCascade classifier;
		CvSeq objects;
		public CvSeq getObjects() {
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