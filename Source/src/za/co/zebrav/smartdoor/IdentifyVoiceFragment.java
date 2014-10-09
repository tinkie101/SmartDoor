package za.co.zebrav.smartdoor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import za.co.zebrav.smartdoor.database.Db4oAdapter;
import za.co.zebrav.smartdoor.database.User;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import at.fhooe.mcm.smc.math.mfcc.FeatureVector;
import at.fhooe.mcm.smc.math.vq.Codebook;

public class IdentifyVoiceFragment extends VoiceFragment
{
	private static final String LOG_TAG = "AuthTest";

	private IdentifyTask identifyTask;
	private int loopCounter;

	private TextView txtTempUser;
	//private ListView listView;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d(LOG_TAG, "onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{

		View view = inflater.inflate(R.layout.identify_voice, container, false);

		User user = activity.getUser();

		txtTempUser = (TextView) view.findViewById(R.id.txtTempUser);
		txtTempUser.setText(user.getUsername());

		// get user image
		File path = activity.getDir("data", 0);

		Bitmap image = BitmapFactory.decodeFile(path + "/photos/" + user.getID() + "-0.png");

		ImageView imgUser = (ImageView) view.findViewById(R.id.imgTempUser);
		imgUser.setImageBitmap(image);
		//TODO
		//listView = new ListView(activity);
		soundLevelDialog.setMessage("Say: \"The quick brown fox jumps over the lazy dog\"");

		return view;
	}

	@Override
	public void onStart()
	{
		super.onStart();
		loopCounter = 3;
		identifySpeaker();
	}

	@Override
	public void onPause()
	{
		super.onPause();

		if (identifyTask != null)
			identifyTask.cancel(true);
	}

	/**
	 * Delete old recording and record a new file as wav
	 * 
	 * @param output
	 */
	private void startRecording()
	{
		processingDialog.show();
		soundLevelDialog.show();

		identifyTask = new IdentifyTask();
		identifyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private ArrayList<String> calculateDistances(FeatureVector featureVector)
	{
		Db4oAdapter db = activity.getDatabase();
		List<Object> tempList = db.load(new User(null, null, null, null, null, 0, null));

		if (tempList.size() < 1)
		{
			Log.d(LOG_TAG, "No valid users in database");
			return null;
		}
		else
		{
			ArrayList<String> result = new ArrayList<String>();
			ArrayList<Float> resultDist = new ArrayList<Float>();

			for (Object o : tempList)
			{
				float tempAvgDist = 0.0f;

				User user = (User) o;
				ArrayList<Codebook> cb = user.getCodeBook();

				if (cb != null)
				{
					voiceAuthenticator.setCodeBook(cb);

					tempAvgDist = voiceAuthenticator.getAverageFeatureDistance(featureVector);

					if (tempAvgDist == -1f)
					{
						Log.d(LOG_TAG, "Error with calculating feature vector distance for user!");
						continue;
					}
					
					Log.d(LOG_TAG, "user average distance = " + tempAvgDist);

					// Insert new user into sorted list
					boolean inserted = false;
					for (int l = 0; l < resultDist.size(); l++)
					{
						if (resultDist.get(l) > tempAvgDist)
						{
							resultDist.add(l, tempAvgDist);
							Integer id = user.getID();
							result.add(l, id.toString());
							inserted = true;
							break;
						}
					}

					if (!inserted)
					{
						resultDist.add(tempAvgDist);
						Integer id = user.getID();
						result.add(id.toString());
					}
				}
				else
				{
					Log.d(LOG_TAG, "User's CodeBook is Null");
				}
			}

			// Add distance values to result
			for (int l = 0; l < result.size(); l++)
			{
				result.set(l, result.get(l) + ": " + resultDist.get(l));
			}

			return result;
		}
	}

	private class IdentifyTask extends AsyncTask<Void, Void, ArrayList<String>>
	{
		@Override
		protected ArrayList<String> doInBackground(Void... params)
		{
			voiceAuthenticator.doRecording();
			soundLevelDialog.dismiss();
			return calculateDistances(voiceAuthenticator.getCurrentFeatureVector());
		}

		@Override
		protected void onPostExecute(ArrayList<String> result)
		{
			for (String string : result)
			{
				Log.i(LOG_TAG, string);
			}

			//ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, result);
			//listView.setAdapter(adapter);

			processingDialog.dismiss();

			Log.d(LOG_TAG, "Adapter Set to Results");

			// Check if the user identified corresponds to the facial recognition
			String bestResult = result.get(0);
			try
			{
				Integer bestMatch = Integer.parseInt(bestResult.substring(0, bestResult.indexOf(":")));

				int activeID = activity.getUser().getID();

				if (bestMatch.equals(activeID))
				{
					((MainActivity) activity).switchToLoggedInFrag();
				}
				else
				{
					loopCounter--;

					if (loopCounter > 0)
						identifySpeaker();
					else
					{
						activity.setActiveUser(null);
						((MainActivity) activity).switchToCamera();
					}
				}
			}
			catch (NumberFormatException e)
			{
				Log.d(LOG_TAG, "Incorrect conversion of userID");
			}

		}
	}

	private void identifySpeaker()
	{
		Log.d(LOG_TAG, "Identifying Voice");
		startRecording();
	}
}
