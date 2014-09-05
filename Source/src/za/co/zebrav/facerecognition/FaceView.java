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
import android.view.View;

abstract class FaceView extends View implements Camera.PreviewCallback
{
	public static final int SUBSAMPLING_FACTOR = 2;

	private IplImage grayImage;
	private CvHaarClassifierCascade faceClassifier;
	private CvHaarClassifierCascade eyeClassifier;
	private CvHaarClassifierCascade noseClassifier;
	private CvMemStorage storage;
	private CvSeq faces;
	private CvSeq eyes;
	private CvSeq noses;
	private Paint paint;

	public FaceView(Context context) throws IOException
	{
		super(context);
		paint = new Paint();
		// Load the classifier file from Java resources.
		File faceClassifierFile = Loader.extractResource(getClass(),
							"/za/co/zebrav/facerecognition/haarcascade_frontalface_alt.xml", context.getCacheDir(),
							"classifier", ".xml");
		File eyeClassifierFile = Loader.extractResource(getClass(),
							"/za/co/zebrav/facerecognition/haarcascade_eye.xml", context.getCacheDir(),
							"classifier", ".xml");
		File noseClassifierFile = Loader.extractResource(getClass(),
							"/za/co/zebrav/facerecognition/haarcascade_nose.xml", context.getCacheDir(),
							"classifier", ".xml");
		if (faceClassifierFile == null || faceClassifierFile.length() <= 0)
		{
			throw new IOException("Could not extract the face classifier file from Java resource.");
		}
		if (eyeClassifierFile == null || eyeClassifierFile.length() <= 0)
		{
			throw new IOException("Could not extract the classifier eye file from Java resource.");
		}
		if (noseClassifierFile == null || noseClassifierFile.length() <= 0)
		{
			throw new IOException("Could not extract the nose classifier file from Java resource.");
		}
		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);
		faceClassifier = new CvHaarClassifierCascade(cvLoad(faceClassifierFile.getAbsolutePath()));
		faceClassifierFile.delete();
		if (faceClassifier.isNull())
		{
			throw new IOException("Could not load the face classifier file.");
		}

		Loader.load(opencv_objdetect.class);
		eyeClassifier = new CvHaarClassifierCascade(cvLoad(eyeClassifierFile.getAbsolutePath()));
		eyeClassifierFile.delete();
		if (eyeClassifier.isNull())
		{
			throw new IOException("Could not load the eye classifier file.");
		}
		
		Loader.load(opencv_objdetect.class);
		noseClassifier = new CvHaarClassifierCascade(cvLoad(noseClassifierFile.getAbsolutePath()));
		noseClassifierFile.delete();
		if (noseClassifier.isNull())
		{
			throw new IOException("Could not load the nose classifier file.");
		}
		storage = CvMemStorage.create();
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
		faces = cvHaarDetectObjects(grayImage, faceClassifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
		eyes = cvHaarDetectObjects(grayImage, eyeClassifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
		noses = cvHaarDetectObjects(grayImage, noseClassifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
		if (faces.total() > 0)
			processFaces(faces);
		postInvalidate();
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		
		paint.setTextSize(20);

		// String s = "FacePreview - This side up.";
		// float textWidth = paint.measureText(s);
		// canvas.drawText(s, (getWidth() - textWidth) / 2, 20, paint);

		if (faces != null)
		{
			paint.setColor(Color.RED);
			paint.setStrokeWidth(2);
			paint.setStyle(Paint.Style.STROKE);
			float scaleX = (float) getWidth() / grayImage.width();
			float scaleY = (float) getHeight() / grayImage.height();
			int total = faces.total();
			for (int i = 0; i < total; i++)
			{
				CvRect r = new CvRect(cvGetSeqElem(faces, i));
				int x = r.x(), y = r.y(), w = r.width(), h = r.height();
				// x = (int) (getWidth() - (x * scaleX));
				canvas.drawRect(getWidth() - ((x + w) * scaleX), y * scaleY, getWidth() - (x * scaleX), (y + h)
									* scaleY, paint);
			}
		}
		if (eyes != null)
		{
			paint.setColor(Color.GREEN);
			paint.setStrokeWidth(2);
			paint.setStyle(Paint.Style.STROKE);
			float scaleX = (float) getWidth() / grayImage.width();
			float scaleY = (float) getHeight() / grayImage.height();
			int total = eyes.total();
			for (int i = 0; i < total; i++)
			{
				CvRect r = new CvRect(cvGetSeqElem(eyes, i));
				int x = r.x(), y = r.y(), w = r.width(), h = r.height();
				// x = (int) (getWidth() - (x * scaleX));
				canvas.drawRect(getWidth() - ((x + w) * scaleX), y * scaleY, getWidth() - (x * scaleX), (y + h)
									* scaleY, paint);
			}
		}
		if (noses != null)
		{
			paint.setColor(Color.BLUE);
			paint.setStrokeWidth(2);
			paint.setStyle(Paint.Style.STROKE);
			float scaleX = (float) getWidth() / grayImage.width();
			float scaleY = (float) getHeight() / grayImage.height();
			int total = noses.total();
			for (int i = 0; i < total; i++)
			{
				CvRect r = new CvRect(cvGetSeqElem(noses, i));
				int x = r.x(), y = r.y(), w = r.width(), h = r.height();
				// x = (int) (getWidth() - (x * scaleX));
				canvas.drawRect(getWidth() - ((x + w) * scaleX), y * scaleY, getWidth() - (x * scaleX), (y + h)
									* scaleY, paint);
			}
		}
	}
}