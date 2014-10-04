package za.co.zebrav.smartdoor.facerecognition;

import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_highgui.imwrite;

import java.io.File;
import java.nio.ByteBuffer;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;

import android.content.Context;
import android.util.Log;

public class ImageTools
{
	private static final String TAG = "ImageTools";

	public static Mat getGreyMatImage(byte[] data, int width, int height, int samplingFactor)
	{

		return new Mat(getGreyIplImage(data,width,height,samplingFactor));
	}
	
	public static IplImage getGreyIplImage(byte[] data, int width, int height, int samplingFactor)
	{
		IplImage grayImage = IplImage.create(width / samplingFactor, height / samplingFactor, IPL_DEPTH_8U, 1);
		grayImage = IplImage.create(width / samplingFactor, height / samplingFactor, IPL_DEPTH_8U, 1);

		int imageWidth = grayImage.width();
		int imageHeight = grayImage.height();
		int dataStride = samplingFactor * width;
		int imageStride = grayImage.widthStep();
		ByteBuffer imageBuffer = grayImage.getByteBuffer();
		for (int y = 0; y < imageHeight; y++)
		{
			int dataLine = y * dataStride;
			int imageLine = y * imageStride;
			for (int x = 0; x < imageWidth; x++)
			{
				imageBuffer.put(imageLine + x, data[dataLine + samplingFactor * x]);
			}
		}
		return grayImage;
	}

	public static boolean saveImageAsPNG(Mat mat, String fileName, Context context)
	{
		fileName = fileName + ".png";
		File path = context.getDir("data", 0);
		File file = new File(path + "/photos/");

		if (!file.exists())
		{
			Log.d(TAG, "Creating dir");
			if (!file.mkdir())
			{
				Log.e(TAG, "Failed Creating dirs");
				return false;
			}
		}

		file = new File(path + "/photos/", fileName);

		if (!imwrite(file.toString(), mat))
		{
			Log.e(TAG, "Fail writing image to external storage");
			return false;
		}
		return true;
	}

}
