package za.co.zebrav.facerecognition;

import java.io.IOException;

import org.bytedeco.javacpp.opencv_core.CvSeq;

import android.content.Context;
import android.util.Log;

public class addFaceView extends FaceView
{

	public addFaceView(Context context) throws IOException
	{
		super(context);
	}

	private final String TAG = "addFaceView";

	@Override
	public void processFaces(CvSeq faces)
	{
		Log.d(TAG, "Total Faces: " + faces.total());
	}

}
