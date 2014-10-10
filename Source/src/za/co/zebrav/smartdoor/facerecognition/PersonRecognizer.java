package za.co.zebrav.smartdoor.facerecognition;

import static org.bytedeco.javacpp.opencv_contrib.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_contrib.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_contrib.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_highgui.imread;

import java.io.File;
import java.nio.IntBuffer;
import java.util.List;

import org.bytedeco.javacpp.opencv_contrib.FaceRecognizer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import za.co.zebrav.smartdoor.AbstractActivity;
import za.co.zebrav.smartdoor.database.Db4oAdapter;
import za.co.zebrav.smartdoor.database.User;
import android.content.Context;
import android.util.Log;

public class PersonRecognizer
{
	public static final int  LBPHFaceRecognizer = 1;
	public static final int  FisherFaceRecognizer = 2;
	public static final int  EigenFaceRecognizer = 3;
	private static final String TAG = "FacailRecognition::PersonRecognizer";
	/**
	 * JavaCV FaceRegocniser object being wrapped.
	 */
	FaceRecognizer faceRecognizer;
	/**
	 * Indicate whether training has been completed.
	 */
	private boolean isTrained = false;
	/**
	 * Certainty of the last training.
	 */
	private double certainty = -1;

	private int photosPerPerson = 0;

	/**
	 * Default constructor. Will attempt to train from the database.
	 * 
	 * @param context
	 *            Context for this PersonRecogniser. Needed for File IO.
	 * @param photosPerPerson
	 *            The amount of photos per person stored in database
	 * @param algorithm
	 *            Type of algorithm to use. Use PersonRecognizer.LBPHFaceRecognizer or PersonRecognizer.FisherFaceRecognizer or PersonRecognizer.EigenFaceRecognizer
	 */
	public PersonRecognizer(Context context, int photosPerPerson, int algorithm, int threshold)
	{
//		this.photosPerPerson = photosPerPerson;
//		switch (algorithm)
//		{
//			case 1:
//				faceRecognizer = createLBPHFaceRecognizer();
//				Log.d(TAG, "Created LBPHFaceRecognizer");
//				break;
//			case 2:
//				faceRecognizer = createFisherFaceRecognizer();
//				Log.d(TAG, "Created FisherFaceRecognizer");
//				break;
//			case 3:
//				faceRecognizer = createEigenFaceRecognizer();
//				Log.d(TAG, "Created EigenFaceRecognizer");
//				break;
//			default:
//				faceRecognizer = createLBPHFaceRecognizer();
//				Log.d(TAG, "Created LBPHFaceRecognizer");
//				break;
//		}
//		Log.d(TAG,"Threshold:" + threshold);
//		faceRecognizer.set("threshold", threshold);
//		isTrained = initialiseRecogniserFromDatabase(context);
	}

	/**
	 * Function that gets a list of users from the database and then loads all the training images for that user.
	 * 
	 * @precondition: Must be at least 2 users in the database.
	 * @precondition: All files for a user must exist.
	 * @postcondition: Person recognizer will be trained.
	 * @param context
	 *            The context needed for the database.
	 * @return True when preconditions were met, False otherwise.
	 */
	private boolean initialiseRecogniserFromDatabase(Context context)
	{
		// Create Database connection and find all users
		Db4oAdapter db = ((AbstractActivity)context).getDatabase();
		List<Object> tempList = db.load(new User(null, null, null, null, null, 0, null));
		Log.d(TAG, "Size:" + tempList.size());
		// Check Precondition 1
		if (tempList.size() < 2)
		{
			Log.e(TAG, "List less than 2");
			return false;
		}
		// Create labels and images
		Mat labels = new Mat(tempList.size() * photosPerPerson, 1, CV_32SC1);
		IntBuffer labelsBuf = labels.getIntBuffer();
		MatVector images = new MatVector(tempList.size() * photosPerPerson);
		// Path where files are located
		File path = context.getDir("data", 0);
		// loop through each user in database
		int i = 0; // used to keep count of amount of images and labels added
		for (Object o : tempList)
		{
			// get current user
			User u = (User) o;
			// loop through each photo of the specific user
			for (int j = 0; j < photosPerPerson; j++)
			{
				// add the user's id to the back
				labelsBuf.put(i, u.getID());

				// Get file
				File file = new File(path + "/photos/", u.getID() + "-" + j + ".png");
				// Check precondition 2
				if (!file.exists())
				{
					Log.e(TAG, file.toString() + " does not exist!");
					return false;
				}
				// load image and add to back
				Mat m = imread(file.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
				images.put(i, m);
				Log.d(TAG, "m width:" + m.cols() + "; m height:"+m.rows());
				// increment counter for image count
				i++;
			}
		}
		// Train on all the loaded images
		boolean result = train(images, labels);
		return result;
	}

	/**
	 * The main training function.
	 * 
	 * @precondition: Images must be a valid list.
	 * @precondition: Labels must be a valid list.
	 * @precondition: Labels must be the same length as images.
	 * @precondition: Requires a minimum of 2 images to train on.
	 * @postcondition: Person recognizer will be trained.
	 * @param images
	 *            List of images to train on.
	 * @param labels
	 *            List of labels to train on.
	 * @return True when preconditions were met, False otherwise.
	 */
	private boolean train(MatVector images, Mat labels)
	{
		// Check precondition 1
		if (images == null || images.capacity() == 0)
		{
			Log.e(TAG, "Images must be a valid list.");
			return false;
		}
		// Check precondition 2
		if (labels == null || labels.capacity() == 0)
		{
			Log.e(TAG, "Labels must be a valid list.");
			return false;
		}
		// Check precondition 3
		if (labels.rows() != images.size())
		{
			Log.e(TAG, "Labels must be the same length as images.");
			return false;
		}
		// Check precondition 4
		if (images.size() < 2)
		{
			Log.e(TAG, "Requires a minimum of 2 images to train on.");
			return false;
		}
		// Get start time
		long startTime = System.currentTimeMillis();
		faceRecognizer.train(images, labels);
		// Get end time
		long endTime = System.currentTimeMillis();
		// calculate and log training time
		long temp = endTime - startTime;
		double timeInSeconds = temp / (double) 1000;
		Log.d(TAG, "Training on " + images.size() + " files took " + timeInSeconds + " seconds.");
		// Successfully trained
		return true;
	}

	/**
	 * Checks whether the Recognizer has been trained.
	 * 
	 * @return True if it has been trained and it is able to predict, false otherwise.
	 */
	public boolean canPredict()
	{
		return isTrained;
	}

	/**
	 * Does the prediction.
	 * 
	 * @precondition: Image must not be empty.
	 * @precondition: Recognizer must be trained.
	 * @postcondition: Predicted label.
	 * @postcondition: Predicted certainty.
	 * @param testImage
	 *            The image to do the prediction on.
	 * @return ID of the person that was recognized. If none was recognized -1 will be returned.
	 */
	public int predict(Mat testImage)
	{
		// Test precondition 1
		if (testImage == null || testImage.cols() == 0)
		{
			certainty = -1;
			return -1;
		}
		// Test precondition 2
		if (!canPredict())
		{
			certainty = -1;
			return -1;
		}
		// Do the prediction

		int predictedID[] = new int[1];
		double predictedCertainty[] = new double[1];
		long startTime = System.currentTimeMillis();
		faceRecognizer.predict(testImage, predictedID, predictedCertainty);
		long endTime = System.currentTimeMillis();
		long temp = endTime - startTime;
		double timeInSeconds = temp / (double) 1000;
		Log.d(TAG, "Prediction took " + timeInSeconds + " seconds.");
		certainty = predictedCertainty[0];
		return predictedID[0];
	}

	/**
	 * Getter for certainty.
	 * 
	 * @return Certainty of last prediction. If none was predicted, or prediction is not available -1 will be returned.
	 */
	public double getCertainty()
	{
		return certainty;
	}
}