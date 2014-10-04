package za.co.zebrav.smartdoor.facerecognition;

import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;

import java.io.IOException;
import java.text.DecimalFormat;

import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;

import za.co.zebrav.smartdoor.R;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.util.DisplayMetrics;
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

	private static final String TAG = "FaceView";
	/**
	 * The factor by which the camera feed needs to be down sampled by.
	 * Higher number = Higher performance.
	 * Higher number = Lower accuracy.
	 * Typically in the range [1,4]
	 */
	protected final int SUBSAMPLING_FACTOR_DETECTION;

	/**
	 * Used to draw squares around detected objects.
	 */
	protected Paint paint;
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
		String settingsFile = getResources().getString(R.string.settingsFileName);
		SUBSAMPLING_FACTOR_DETECTION = Integer.parseInt(activity.getSharedPreferences(settingsFile, 0).getString(
							"face_ImageScale", "2"));
		Log.d(TAG, "Factor: " + SUBSAMPLING_FACTOR_DETECTION);
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

	protected ClassifierRunnable faceRunnable;
	protected Thread faceThread;

	Mat grayImage;

	protected void processImage(byte[] data, int width, int height)
	{
		grayImage = ImageTools.getGreyMatImage(data, width, height, SUBSAMPLING_FACTOR_DETECTION);

		cvClearMemStorage(storage);
		runClassifiers();

		handleDetected(data, width, height);
		postInvalidate();
	}

	protected void drawFPS(Canvas canvas)
	{
		String FPS = calculateFPS();
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		float textWidth = paint.measureText(FPS);
		paint.setStrokeWidth(2);
		paint.setColor(Color.WHITE);
		canvas.drawText(FPS, (getWidth() - textWidth), getDeviceSize(14), paint);
	}

	protected void drawFace(Canvas canvas)
	{
		Rect faceRectangle = faceRunnable.getObjects().position(0);
		paint.setColor(Integer.parseInt(getResources().getString((R.string.face_FaceColor))));
		float scaleX = (float) getWidth() / grayImage.cols();
		float scaleY = (float) getHeight() / grayImage.rows();
		int x = faceRectangle.x(), y = faceRectangle.y(), w = faceRectangle.width(), h = faceRectangle.height();

		int startx = (int) (getWidth() - ((x + w) * scaleX));
		int starty = (int) (y * scaleY);
		int endx = (int) (getWidth() - (x * scaleX));
		int endy = (int) ((y + h) * scaleY);

		canvas.drawRect(startx, starty, endx, endy, paint);
	}

	protected abstract void runClassifiers();

	protected abstract void handleDetected(byte[] data, int width, int height);

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
}
