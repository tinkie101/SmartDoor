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
import java.util.List;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;

import com.db4o.ObjectSet;

import za.co.zebrav.smartdoor.database.Db4oAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;

class AddFaceView extends View implements Camera.PreviewCallback
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
	private static final String[] classifierFiles = { "haarcascade_frontalface_alt.xml", "haarcascade_eye.xml",
						"haarcascade_nose.xml" };
	/**
	 * Colors which will be used to draw squares around classified objects.
	 * Order will be the same as for classifiers.
	 */
	private static final int Colors[] = { Color.RED, Color.GREEN, Color.BLUE };
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
	/**
	 * Used to recognize faces.
	 */
	protected PersonRecognizer personRecognizer;

	/**
	 * Database to load the personRecognizer from.
	 */
	private Db4oAdapter database;

	/**
	 * Method implemented by child classes.
	 * This processes the detected faces to either add to DB or compare to DB.
	 * 
	 * @param faces
	 */
	private void processFaces(CvSeq faces)
	{
		
	}
	
	public Mat getFace()
	{
		return new Mat(runnables[0].getObjects().first());
	}
	private Context context;

	public AddFaceView(Context context) throws IOException
	{
		super(context);
		this.context = context;
		paint = initialisePaint();
		// Load the classifier file from Java resources.
		storage = CvMemStorage.create();
		threads = new Thread[classifierFiles.length];
		runnables = new concurrentDetector[classifierFiles.length];
		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);
		for (int i = 0; i < classifierFiles.length; i++)
		{
			File file = Loader.extractResource(getClass(), directory + classifierFiles[i], context.getCacheDir(),
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
		// TODO: add loadPersonRecognizer call once db is fixed.
		// loadPersonRecognizer();
		lastTime = System.currentTimeMillis();
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

	/**
	 * Loads PersonRecognizer from the database.
	 * If there is no PersonRecognizer in database it will create and save a new Recognizer.
	 * Called from constructor. Is save to call multiple times.
	 * Must be called onResume of Fragment/Activity.
	 * 
	 * @return True if success, false otherwise.
	 */
//	public boolean loadPersonRecognizer()
//	{
//		// If the personRecognizer has a value then it has already been loaded and we can emmidiatly return.
//		if (personRecognizer != null)
//			return true;
//		// For safety make sure that we do not have 2 databases
//		if (database == null)
//			database = new Db4oAdapter(this.context);
//		// For safety make sure that we do not open the database twice
//		if (!database.isOpen())
//			database.open();
//		// Get all PersonRecognizers from database
//		List<Object> tempList = database.load(new PersonRecognizer(null));
//		// If tempList has an entry we can just load from database
//		if (tempList != null && tempList.size() != 0)
//		{
//			personRecognizer = (PersonRecognizer) tempList.get(0);
//		}
//		// Otherwise we have to create the recogniser
//		else
//		{
//			// TODO: change constructor once PersonRecognizer is updated.
//			personRecognizer = new PersonRecognizer("dummy");
//			database.save(personRecognizer);
//		}
//		return true;
//	}

	/**
	 * Saves the personRecognizer in the database.
	 * Closes connection to database.
	 * Important to call when Fragment/Activity is paused.
	 * 
	 * @return True if success, false otherwise.
	 */
//	public boolean savePersonRecognizer()
//	{
//		// If the database is null we cannot save to it.
//		if (database == null)
//			return false;
//		// If the database is closed we cannot write to it.
//		if (!database.isOpen())
//			return false;
//		database.save(personRecognizer);
//		database.close();
//		return true;
//	}

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
		postInvalidate();
	}

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
		for (int i = 0; i < runnables.length; i++)
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
					//Mat test = new Mat(cvGetSeqElem(runnables[i].getObjects(), j));
					int x = r.x(), y = r.y(), w = r.width(), h = r.height();
					canvas.drawRect(getWidth() - ((x + w) * scaleX), y * scaleY, getWidth() - (x * scaleX), (y + h)
										* scaleY, paint);
				}
			}
		}
	}

	/**
	 * Private class to do the object detection.
	 */
	private class concurrentDetector implements Runnable
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