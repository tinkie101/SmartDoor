package za.co.zebrav.facerecognition;

import java.nio.ByteBuffer;

import org.bytedeco.javacpp.opencv_core.CvMat;
import org.bytedeco.javacpp.opencv_core.Mat;

import android.graphics.Bitmap;

public class labeledMat
{
	long label;
	byte[] mat;
	int width;
	public int getWidth()
	{
		return width;
	}
	public void setWidth(int width)
	{
		this.width = width;
	}
	int height;
	public int getHeight()
	{
		return height;
	}
	public void setHeight(int height)
	{
		this.height = height;
	}
	public labeledMat(long uID, byte[] mat, int width, int height)
	{
		super();
		this.label = uID;
		this.mat = mat;
		this.width = width;
		this.height = height;
	}
	public long getLabel()
	{
		return label;
	}
	public void setLabel(long label)
	{
		this.label = label;
	}
	public byte[] getMat()
	{
		return mat;
	}
	public void setMat(byte[] mat)
	{
		this.mat = mat;
	}

}
