package za.co.zebrav.facerecognition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.nio.IntBuffer;

import static org.bytedeco.javacpp.opencv_contrib.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import android.graphics.Bitmap;
import android.util.Log;

public class PersonRecognizer
{
	private static final String TAG = "FacailRecognition::PersonRecognizer";
	public final static int MAXIMG = 100;
	FaceRecognizer faceRecognizer;
	String mPath;
	int count = 0;
	labels labelsFile;

	static final int WIDTH = 128;
	static final int HEIGHT = 128;;
	private int mProb = 999;

	PersonRecognizer(String path)
	{
		faceRecognizer = createLBPHFaceRecognizer(2, 8, 8, 8, 200);
		mPath = path;
		labelsFile = new labels(mPath);

	}

	void changeRecognizer(int nRec)
	{
		switch (nRec)
		{
			case 0:
				faceRecognizer = createLBPHFaceRecognizer(2, 8, 8, 8, 200);
				break;
			case 1:
				faceRecognizer = createFisherFaceRecognizer();
				break;
			case 2:
				faceRecognizer = createEigenFaceRecognizer();
				break;
		}
		train();
	}

	void add(Mat m, int id)
	{
		Integer i = id;
		String s = i.toString();
		imwrite(s, m);
	}

	public boolean train()
	{

		File root = new File(mPath);

		FilenameFilter imgFilter = new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return name.toLowerCase().endsWith(".jpg");

			};
		};

		File[] imageFiles = root.listFiles(imgFilter);

		MatVector images = new MatVector(imageFiles.length);

		Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
		IntBuffer labelsBuf = labels.getIntBuffer();

		int counter = 0;

		for (File image : imageFiles)
		{
			Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
			int label = Integer.parseInt(image.getName().split("\\-")[0]);

			images.put(counter, img);

			labelsBuf.put(counter, label);

			counter++;
		}
		if (counter > 0)
			if (labelsFile.max() > 1)
				faceRecognizer.train(images, labels);
		labelsFile.Save();
		return true;
	}

	public boolean canPredict()
	{
		if (labelsFile.max() > 1)
			return true;
		else
			return false;

	}

	public int predict(Mat testImage)
	{
		return faceRecognizer.predict(testImage);

	}

	protected void SaveBmp(Bitmap bmp, String path)
	{
		FileOutputStream file;
		try
		{
			file = new FileOutputStream(path, true);

			bmp.compress(Bitmap.CompressFormat.JPEG, 100, file);
			file.close();
		}
		catch (Exception e)
		{
			Log.e("", e.getMessage() + e.getCause());
			e.printStackTrace();
		}
	}

	public void load()
	{
		train();
	}

	public int getProb()
	{
		return mProb;
	}
}