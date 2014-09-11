package za.co.zebrav.smartdoor;

import za.co.zebrav.smartdoor.database.AddUserActivity;
import za.co.zebrav.smartdoor.database.Db4oAdapter;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import at.fhhgb.auth.voice.VoiceAuthenticator;

public class AddVoiceFragment extends ListFragment implements OnClickListener
{
	private static final String LOG_TAG = "AuthTest";

	private Button btnTrain;
	private Button btnDone;

	private VoiceAuthenticator voiceAuthenticator;

	private String activeKey;
	private Context context;
	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Bundle bundle = this.getArguments();
		Integer id = bundle.getInt("userID", -1);
		activeKey = id.toString();
		return inflater.inflate(R.layout.fragment_add_voice, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		super.onCreate(savedInstanceState);

		context = getActivity();
		view = getView();

		voiceAuthenticator = new VoiceAuthenticator();

		btnTrain = (Button) view.findViewById(R.id.btnTrain);
		btnTrain.setOnClickListener(this);
		
		btnDone = (Button) view.findViewById(R.id.btnDone);
		btnDone.setEnabled(false);
		btnDone.setOnClickListener(this);
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
		if (v == btnTrain)
			trainVoice();
		else if(v == btnDone)
		{
			saveToDataBase();
			
			AddUserActivity activity = (AddUserActivity) context;
			activity.doneStepThreeAddUser(btnDone);
		}
	}

	/**
	 * Delete old recording and record a new file as wav
	 * 
	 * @param output
	 */
	private void startRecording()
	{
		if (activeKey != null && activeKey.length() > 0)
		{
			Log.i(LOG_TAG, "Recording to File");
			voiceAuthenticator.startRecording(activeKey);

			// Alert user of recording and Stop button
			new AlertDialog.Builder(context).setTitle("Recording...").setMessage("Stop Recording")
								.setNeutralButton("Ok", new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int whichButton)
									{
										voiceAuthenticator.stopRecording();
										btnDone.setEnabled(false);
										new trainTask().execute();
									}

								}).show();
		}
		else
		{
			Toast.makeText(context, "No Active Key Set!", Toast.LENGTH_LONG).show();
		}
	}

	private class trainTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			voiceAuthenticator.train(activeKey);
			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			btnDone.setEnabled(true);
		}
	}
	
	private void saveToDataBase()
	{
		Db4oAdapter database = new Db4oAdapter(context);
		database.open();
		database.save(voiceAuthenticator.getCodeBook());
		database.close();
	}

	private void trainVoice()
	{
		Log.i(LOG_TAG, "Training Voice");

		new AlertDialog.Builder(context).setTitle("Train new Voice").setMessage("Press Ok to Start Recording.")
							.setPositiveButton("Ok", new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int whichButton)
								{
									startRecording();
								}
							}).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int whichButton)
								{
									// Do nothing.
								}
							}).show();
	}
}
