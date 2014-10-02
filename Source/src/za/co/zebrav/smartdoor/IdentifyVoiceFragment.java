package za.co.zebrav.smartdoor;

import java.util.ArrayList;
import java.util.List;

import za.co.zebrav.smartdoor.database.AddUserActivity;
import za.co.zebrav.smartdoor.database.Db4oAdapter;
import za.co.zebrav.smartdoor.database.User;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import at.fhhgb.auth.voice.VoiceAuthenticator;
import at.fhooe.mcm.smc.math.mfcc.FeatureVector;
import at.fhooe.mcm.smc.math.vq.Codebook;

public class IdentifyVoiceFragment extends Fragment implements OnClickListener
{
	private static final String LOG_TAG = "AuthTest";

	private Button btnIdentify;
	private Button btnDone;
	private ListView listView;

	private VoiceAuthenticator voiceAuthenticator;

	private int activeID;
	private Context context;
	private View view;

	private ProgressDialog soundLevelDialog;
	private ProgressDialog processingDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Bundle bundle = this.getArguments();
		activeID = bundle.getInt("userID", -1);
		return inflater.inflate(R.layout.fragment_identify_voice, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		super.onCreate(savedInstanceState);

		context = getActivity();
		view = getView();

		soundLevelDialog = new ProgressDialog(context, ProgressDialog.STYLE_HORIZONTAL);
		soundLevelDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		soundLevelDialog.setTitle("Listening...");
		soundLevelDialog.setCancelable(false);

		processingDialog = new ProgressDialog(context, ProgressDialog.STYLE_HORIZONTAL);
		processingDialog.setCancelable(false);

		voiceAuthenticator = new VoiceAuthenticator(soundLevelDialog);

		btnIdentify = (Button) view.findViewById(R.id.btnIdentify);
		btnIdentify.setOnClickListener(this);

		btnDone = (Button) view.findViewById(R.id.btnDone);
		btnDone.setOnClickListener(this);

		listView = (ListView) view.findViewById(R.id.identify_list);
	}

	@Override
	public void onPause()
	{
		super.onPause();

		// Stop play/record
		voiceAuthenticator.cancelRecording();
	}

	/**
	 * Handle the button pressed
	 * 
	 * @param v
	 */
	@Override
	public void onClick(View v)
	{
		if (v == btnIdentify)
			identifySpeaker();
		else if (v == btnDone)
		{
			// TODO Goto next fragment
		}
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

		new identifyTask().execute();
	}

	private ArrayList<String> calculateDistances(FeatureVector featureVector)
	{
		Db4oAdapter db = new Db4oAdapter(context);
		db.open();
		List<Object> tempList = db.load(new User(null, null, null, null, null, 0, null));

		if (tempList.size() < 1)
		{
			Log.d(LOG_TAG, "No valid users in database");
			db.close();
			return null;
		}
		else
		{
			ArrayList<String> result = new ArrayList<String>();
			ArrayList<Double> resultDist = new ArrayList<Double>();

			for (Object o : tempList)
			{
				Double tempAvgDist = 0.0;

				User user = (User) o;
				ArrayList<Codebook> cb = user.getCodeBook();

				if (cb != null)
				{
					voiceAuthenticator.setCodeBook(cb);

					ArrayList<Double> tempResult = voiceAuthenticator.identify(featureVector);

					if (tempResult == null)
					{
						Log.d(LOG_TAG, "Error with identify! Check if ActiveFile is set");
						continue;
					}

					// caluclate user's average
					for (int l = 0; l < tempResult.size(); l++)
					{
						Log.i(LOG_TAG, l + "= " + tempResult.get(l));
						tempAvgDist += tempResult.get(l);
					}

					tempAvgDist = tempAvgDist / (double) tempResult.size();
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
			db.close();

			return result;
		}
	}

	private class identifyTask extends AsyncTask<Void, Void, ArrayList<String>>
	{
		@Override
		protected ArrayList<String> doInBackground(Void... params)
		{
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

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
								result);
			listView.setAdapter(adapter);

			//TODO remove this line
			voiceAuthenticator.deleteActiveFile();
			processingDialog.dismiss();

			Log.d(LOG_TAG, "Adapter Set to Results");
			
			//Check if the user identified corresponds to the facial recognition
			String bestResult = result.get(0);
			try
			{
				Integer bestMatch = Integer.parseInt(bestResult.substring(0, bestResult.indexOf(":")));
				
				if(bestMatch.equals(activeID))
					{
						MainActivity activity = (MainActivity) context;
						activity.switchToLoggedInFrag(activeID);
					}
			}
			catch(NumberFormatException e)
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
