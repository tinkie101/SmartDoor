package za.co.zebrav.facerecognition;

import java.io.IOException;

import org.bytedeco.javacpp.opencv_core.CvSeq;

import android.content.Context;
import android.util.Log;

public class SearchFaceView extends FaceView
{
	public SearchFaceView(Context context) throws IOException
	{
		super(context);
		// TODO Auto-generated constructor stub
	}

	private final String TAG = "SearchFaceView";

	@Override
	public void processFaces(CvSeq faces)
	{
		Log.d(TAG, "Total Faces: " + faces.total());
	}

}
