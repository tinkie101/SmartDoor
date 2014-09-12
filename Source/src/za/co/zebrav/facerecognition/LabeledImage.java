package za.co.zebrav.facerecognition;

import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;

import java.nio.ByteBuffer;

import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;

public class LabeledImage
{
	byte[] data;
	int label;
	int width;
	int height;
	final static int SUBSAMPLING_FACTOR = 1;
	public LabeledImage()
	{
		//blank for db search
	}
	public Mat getGreyImage()
	{
		CvMemStorage storage = CvMemStorage.create();
		int f = SUBSAMPLING_FACTOR;
		
		IplImage grayImage = IplImage.create(width / f, height / f, IPL_DEPTH_8U, 1);
		grayImage = IplImage.create(width / f, height / f, IPL_DEPTH_8U, 1);
		
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
		return new Mat(grayImage);
	}
	public LabeledImage(byte[] data, int label, int width, int height)
	{
		super();
		this.data = data;
		this.label = label;
		this.width = width;
		this.height = height;
	}

	public byte[] getData()
	{
		return data;
	}
	public void setData(byte[] data)
	{
		this.data = data;
	}
	public int getLabel()
	{
		return label;
	}
	public void setLabel(int label)
	{
		this.label = label;
	}
	public int getWidth()
	{
		return width;
	}
	public void setWidth(int width)
	{
		this.width = width;
	}
	public int getHeight()
	{
		return height;
	}
	public void setHeight(int height)
	{
		this.height = height;
	}

}
