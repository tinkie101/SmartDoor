package za.co.zebrav.smartdoor;

import java.util.LinkedList;
import java.util.List;

import za.co.zebrav.smartdoor.database.Db4oAdapter;
import za.co.zebrav.smartdoor.database.User;
import za.co.zebrav.smartdoor.facerecognition.PersonRecognizer;
import android.app.Application;

public class GlobalApplication extends Application 
{
	public PersonRecognizer personRecognizer;
	private LinkedList<Integer> getIDList(Db4oAdapter db)
	{
		List<Object> tempList = db.load(new User(null, null, null, null, null, 0, null));
		LinkedList<Integer> result = new LinkedList<Integer>();
		for(Object o : tempList)
		{
			User u = (User)o;
			int i = u.getID();
			result.add(i);
		}
		return result;
	}
	
	public void trainPersonRecogniser(Db4oAdapter db)
	{
		String settingsFile = getResources().getString(R.string.settingsFileName);
		int photosPerPerson = Integer.parseInt(getSharedPreferences(settingsFile, 0).getString("face_TrainPhotoNum",
							"5"));
		int algorithm = Integer.parseInt(getSharedPreferences(settingsFile, 0).getString(
							"face_faceRecognizerAlgorithm", "1"));
		int threshold = Integer.parseInt(getSharedPreferences(settingsFile, 0).getString("face_recognizerThreshold",
							"0"));
		
		//preload for workaround of known bug
//		try
//		{
//			Loader.load(opencv_nonfree.class);
//		}
//		catch(Exception e)
//		{
//			Log.d(TAG, e.toString());
//		}
		//processingDialog.show();
		LinkedList<Integer> idList = getIDList(db);
		personRecognizer = new PersonRecognizer(photosPerPerson, algorithm, threshold,idList,getDir("data", 0));
		//processingDialog.dismiss();
	}
}
