package za.co.zebrav.facerecognition;

import static org.bytedeco.javacpp.opencv_contrib.*;
import static org.bytedeco.javacpp.opencv_core.*;
import android.util.Log;


public class PersonRecognizer
{
	private static final String TAG = "FacailRecognition::PersonRecognizer";
	FaceRecognizer faceRecognizer;
	private boolean isTrained = false;

	PersonRecognizer()
	{
		isTrained = false;
		//faceRecognizer = createEigenFaceRecognizer();
		//faceRecognizer = createFisherFaceRecognizer();
		faceRecognizer = createLBPHFaceRecognizer();
		//faceRecognizer = createLBPHFaceRecognizer(2, 8, 8, 8, 200);
	}

	public boolean train(MatVector images,Mat labels)
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
		//Test precondition 1
		if(testImage == null || testImage.cols() == 0) return Integer.MIN_VALUE;
		//Test precondition 2
		if(!canPredict()) return Integer.MIN_VALUE;
		//Do the prediction
		return faceRecognizer.predict(testImage);
	}
	
//	public boolean train()
//	{
//
//		File root = new File("Temp path");
//
//		FilenameFilter imgFilter = new FilenameFilter()
//		{
//			public boolean accept(File dir, String name)
//			{
//				return name.toLowerCase().endsWith(".jpg");
//
//			};
//		};
//
//		File[] imageFiles = root.listFiles(imgFilter);
//
//		MatVector images = new MatVector(imageFiles.length);
//
//		Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
//		IntBuffer labelsBuf = labels.getIntBuffer();
//
//		int counter = 0;
//
//		for (File image : imageFiles)
//		{
//			Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
//			int label = Integer.parseInt(image.getName().split("\\-")[0]);
//
//			images.put(counter, img);
//
//			labelsBuf.put(counter, label);
//
//			counter++;
//		}
//		if (counter > 0)
//			if (labelsFile.max() > 1)
//				faceRecognizer.train(images, labels);
//		labelsFile.Save();
//		return true;
//	}
}