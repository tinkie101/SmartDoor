package za.co.zebrav.smartdoor;

import za.co.zebrav.smartdoor.database.AddUserActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AddVoiceFragment extends VoiceFragment implements OnClickListener
{
	private static final String LOG_TAG = "AddVoiceFragment";
	private Button btnDone;
	private Button btnTrain;
	private TextView txtCounter;
	private TrainTask trainTask;
	private int trainCounter;
	private boolean isTraining;

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
		View view = inflater.inflate(R.layout.add_voice, container, false);

		btnTrain = (Button) view.findViewById(R.id.btnTrain);
		btnTrain.setOnClickListener(this);

		btnDone = (Button) view.findViewById(R.id.btnDone);
		btnDone.setOnClickListener(this);

		txtCounter = (TextView) view.findViewById(R.id.txtCounter);

		trainCounter = 0;
		isTraining = false;
		return view;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if (!isTraining)
			trainVoice();
		else
			processingDialog.show();

		txtCounter.setText("Voice Trained Counter: " + trainCounter);
	}

	@Override
	public void onPause()
	{
		super.onPause();
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
		trainTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private class TrainTask extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params)
		{
			boolean wait = activity.isTalking();

			while (wait)
			{
				wait = activity.isTalking();
			}

			Log.i(LOG_TAG, "Training new Voice");
			Log.d(LOG_TAG, "Mic threshold: " + voiceAuthenticator.getMicThreshold());
			voiceAuthenticator.startRecording();
			soundLevelDialog.dismiss();

			if (!voiceAuthenticator.train())
			{
				Log.d(LOG_TAG, "Error with training voice, check if activeFile is set");
				return false;
			}
			else
			{
				trainCounter++;
				return true;
			}
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			processingDialog.dismiss();
			btnDone.setEnabled(true);

			if (result)
			{
				txtCounter.setText("Voice Trained Counter: " + trainCounter);
				Toast.makeText(activity, "Voice Successfully Trained", Toast.LENGTH_LONG).show();
			}
			else
			{
				Toast.makeText(activity, "Error Training voice", Toast.LENGTH_LONG).show();
			}

			isTraining = false;
			Log.i(LOG_TAG, "Done Training Voice");
		}
	}

	private void trainVoice()
	{
		Log.i(LOG_TAG, "Training Voice");
		isTraining = true;
		activity.speakOut("After this voice stoped speaking, clearly reed the phrase out loud.");
		startRecording();
	}

	@Override
	public void onClick(View v)
	{
		if (v.equals(btnTrain))
			trainVoice();
		else if (v.equals(btnDone))
		{
			if (isTraining)
			{
				Toast.makeText(activity, "Aplication is Busy Training", Toast.LENGTH_LONG).show();
			}
			else if (activity.getUser() != null)
			{
				activity.getUser().setCodeBook(voiceAuthenticator.getCodeBook());
				((AddUserActivity) activity).doneStepThreeAddUser();
			}
			else
			{
				Toast.makeText(activity, "user is NULL", Toast.LENGTH_LONG).show();
			}
		}
	}
}
