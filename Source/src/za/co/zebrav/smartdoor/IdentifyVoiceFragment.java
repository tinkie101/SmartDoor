package za.co.zebrav.smartdoor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import za.co.zebrav.smartdoor.database.Db4oAdapter;
import za.co.zebrav.smartdoor.database.User;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
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
	private final int maxNumAttempts = 3;
	private final int NUM_COMPARISONS = 5;
	private TextView txtTempUser;

	// private ListView listView;

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
		// TODO
		// listView = new ListView(activity);
		soundLevelDialog.setMessage("Say: \"The quick brown fox jumps over the lazy dog\"");
		soundLevelDialog.setCancelable(true);		
		soundLevelDialog.setOnCancelListener(new OnCancelListener()
		{
			
			@Override
			public void onCancel(DialogInterface dialog)
			{
				activity.speakOut("Voice Identification Cancelled");
				
				activity.setActiveUser(null);
				((MainActivity) activity).switchToCamera();
			}
		});

		return view;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		loopCounter = maxNumAttempts;
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

		User compareUser = activity.getUser();

		if (tempList.size() < 1 || compareUser == null)
		{
			Log.d(LOG_TAG, "Invalid users to compare");
			return null;
		}
		else
		{
			ArrayList<String> result = new ArrayList<String>();
			ArrayList<Float> resultDist = new ArrayList<Float>();

			ArrayList<User> users = new ArrayList<User>();
			ArrayList<Integer> selectedUsers = new ArrayList<Integer>();

			int size = tempList.size();

			// Only compare some users in the db to save process time
			if (size > NUM_COMPARISONS)
			{
				Random randomizer = new Random();

				for (int i = 0; i < NUM_COMPARISONS; i++)
				{
					int random = randomizer.nextInt(size);

					boolean selected = false;

					User tempUser = (User) tempList.get(random);
					int tempID = tempUser.getID();

					while (!selected)
					{
						if (!selectedUsers.contains(tempID) && tempID != compareUser.getID())
						{
							selected = true;
							break;
						}
						else
						{

							random = randomizer.nextInt(size);
							tempUser = (User) tempList.get(random);
							tempID = tempUser.getID();
						}
					}

					users.add(tempUser);
					selectedUsers.add(tempUser.getID());
				}
				users.add(compareUser);
			}
			else
			{
				for (Object userObject : tempList)
				{
					users.add((User) userObject);
				}
			}

			for (User tempUser : users)
			{

				ArrayList<Codebook> cb = tempUser.getCodeBook();

				float tempAvgDist = 0.0f;
				if (cb != null)
				{
					voiceAuthenticator.setCodeBook(cb);

					tempAvgDist = voiceAuthenticator.identifySpeaker(featureVector);

					if (tempAvgDist == -1f)
					{
						Log.d(LOG_TAG, "Error with calculating feature vector distance for user!");
						continue;
					}
					Log.i(LOG_TAG, "user average distance = " + tempAvgDist);

					// Insert new user into sorted list
					boolean inserted = false;
					for (int l = 0; l < resultDist.size(); l++)
					{
						if (resultDist.get(l) > tempAvgDist)
						{
							resultDist.add(l, tempAvgDist);
							Integer id = tempUser.getID();
							result.add(l, id.toString());
							inserted = true;
							break;
						}
					}

					if (!inserted)
					{
						resultDist.add(tempAvgDist);
						Integer id = tempUser.getID();
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
			boolean wait = activity.isTalking();

			while (wait)
			{
				wait = activity.isTalking();
			}

			voiceAuthenticator.startRecording();
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

			// ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1,
			// result);
			// listView.setAdapter(adapter);

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
		((MainActivity)activity).getBrightnessTimer().cancel();
		((MainActivity)activity).getBrightnessTimer().start();
		Log.d(LOG_TAG, "Identifying Voice");
		if (loopCounter == maxNumAttempts)
			activity.speakOut("After this voice stoped speaking, clearly reed the phrase out loud.");
		else if (loopCounter > 0)
		{
			activity.speakOut("Could not recognize voice, please try again.");
			soundLevelDialog.setMessage("Say: \"My voice is my password, and it should log me in\"");
		}
		else
		{
			activity.speakOut("Could not recognize voice.");
		}
		startRecording();
	}
}
