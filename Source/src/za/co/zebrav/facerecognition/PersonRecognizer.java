package za.co.zebrav.facerecognition;

import static org.bytedeco.javacpp.opencv_contrib.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_highgui.imread;

import java.io.File;
import java.nio.IntBuffer;
import java.util.List;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import za.co.zebrav.smartdoor.database.Db4oAdapter;
import za.co.zebrav.smartdoor.database.User;
import android.content.Context;
import android.os.Environment;
import android.util.Log;


public class PersonRecognizer
{
	private static final String TAG = "FacailRecognition::PersonRecognizer";
	FaceRecognizer faceRecognizer;
	private boolean isTrained = false;
	private double certainty = Integer.MIN_VALUE;
	
	public double getCertainty()
	{
		return certainty;
	}

	PersonRecognizer(String fileName)
	{
		faceRecognizer = createLBPHFaceRecognizer();
		faceRecognizer.load(fileName);
	}
	
	PersonRecognizer(Context context)
	{
		//faceRecognizer = createEigenFaceRecognizer();
		//faceRecognizer = createFisherFaceRecognizer();
		faceRecognizer = createLBPHFaceRecognizer();
		faceRecognizer.set("threshold", 150.0);
		//faceRecognizer = createLBPHFaceRecognizer(2, 8, 8, 8, 200);
		
		Db4oAdapter db = new Db4oAdapter(context);
		db.open();
		List<Object> tempList = db.load(new User(null, null, null, null, 0, null));
		if (tempList.size() < 2)
		{
			Log.d(TAG,"List less than 2");
			isTrained = false;
			db.close();
			return;
		}
		Log.d(TAG,"List more than 2");
		Mat labels = new Mat(tempList.size(), 1, CV_32SC1);
		IntBuffer labelsBuf = labels.getIntBuffer();
		MatVector images = new MatVector(tempList.size());
		int i = 0;
		for (Object o : tempList)
		{
			User u = (User)o;
			labelsBuf.put(i, u.getID());
			
			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			File file = new File(path, u.getID() + ".png");
			Log.d(TAG, "File name: " + file.toString());
			if(!file.exists())
			{
				Log.d(TAG, "File does not exist!");
			}
			else
				Log.d(TAG, "File is there..");
			
			Mat m = imread(file.getAbsolutePath(),CV_LOAD_IMAGE_GRAYSCALE);
			Log.d(TAG, "Loaded file");
			//Mat m = li.getGreyImage();
			Log.d(TAG, "Loaded file");
			images.put(i,m);
			i++;
		}
		Log.d(TAG,"Created PersonRecognizer");
		train(images, labels);
		Log.d(TAG,"Trained PersonRecognizer");
		db.close();
		Log.d(TAG,"Database closed");
	}

	private boolean train(MatVector images,Mat labels)
	{
		//Check precondition 1
		if(images == null || images.capacity() == 0) return false;
		//Check precondition 2
		if(labels == null || labels.capacity() == 0) return false;
		//Check precondition 3
		if(labels.cols() != images.capacity()) return false;
		
		//Get start time		
		long startTime = System.currentTimeMillis();
		//Main training
		Log.d(TAG, "Moment of truth");
		faceRecognizer.train(images, labels);
		Log.d(TAG, "After the moment of truth");
		//Get end time
		long endTime = System.currentTimeMillis();
		//calculate and log training time
		long temp = startTime - endTime;
		double timeInSeconds = temp / (double) 1000;
		Log.d(TAG, "Training on " + images.capacity() + "file[s] took " + timeInSeconds + " seconds.");
		//Successfully trained
		isTrained = true;
		return true;
	}

	private boolean canPredict()
	{
		return isTrained;
	}

	public int predict(Mat testImage)
	{
		int n[] = new int[1];
		double p[] = new double[1];
		
		faceRecognizer.predict(testImage, n, p);
		
	    certainty = p[0];
		return n[0];
		
		
//		//Test precondition 1
//		if(testImage == null || testImage.cols() == 0) return Integer.MIN_VALUE;
//		//Test precondition 2
//		if(!canPredict()) return Integer.MIN_VALUE;
//		//Do the prediction
//		return faceRecognizer.predict(testImage);
	}
	
}