package za.co.zebrav.facerecognition;

import org.bytedeco.javacpp.opencv_core.Mat;

public class labeledMat
{
	long label;
	Mat mat;
	public labeledMat(long uID, Mat mat)
	{
		super();
		this.label = uID;
		this.mat = mat;
	}
	public long getLabel()
	{
		return label;
	}
	public void setLabel(long label)
	{
		this.label = label;
	}
	public Mat getMat()
	{
		return mat;
	}
	public void setMat(Mat mat)
	{
		this.mat = mat;
	}


}
