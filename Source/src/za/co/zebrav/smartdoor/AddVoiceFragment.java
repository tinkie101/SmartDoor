package za.co.zebrav.smartdoor;

import za.co.zebrav.smartdoor.database.AddUserActivity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class AddVoiceFragment extends VoiceFragment
{
	private static final String LOG_TAG = "AddVoiceFragment";
	private Button btnDone;
	private Button btnTrain;
	private TrainTask trainTask;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d(LOG_TAG, "onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.d(LOG_TAG, "onCreateView");
		LinearLayout layout = new LinearLayout(activity);
		
		btnTrain = new Button(activity);
		btnTrain.setText("Train");
		
		btnTrain.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				trainVoice();			
			}
		});
		
		
		btnDone = new Button(activity);
		btnDone.setText("Done");
		
		btnDone.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (activity.getUser() != null)
				{
					activity.getUser().setCodeBook(voiceAuthenticator.getCodeBook());
				}
				else
				{
					Toast.makeText(activity, "user is NULL", Toast.LENGTH_LONG).show();
				}				
				
				((AddUserActivity) activity).doneStepThreeAddUser();			
			}
		});
		
		layout.addView(btnTrain);
		layout.addView(btnDone);
		
		return layout;
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		trainVoice();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
		if(trainTask != null)
			trainTask.cancel(true);
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

		trainTask = new TrainTask();
		trainTask.execute();
	}

	private class TrainTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			Log.i(LOG_TAG, "Training new Voice");
			voiceAuthenticator.startRecording();
			soundLevelDialog.dismiss();

			if (!voiceAuthenticator.train())
			{
				Toast.makeText(activity, "Error Training voice", Toast.LENGTH_LONG).show();
				Log.d(LOG_TAG, "Error with training voice, check if activeFile is set");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			processingDialog.dismiss();
			btnDone.setEnabled(true);
			Log.i(LOG_TAG, "Done Training Voice");
		}
	}

	private void trainVoice()
	{
		Log.i(LOG_TAG, "Training Voice");
		startRecording();
	}
}
