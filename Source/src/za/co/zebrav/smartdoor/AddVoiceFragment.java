package za.co.zebrav.smartdoor;

import za.co.zebrav.smartdoor.database.AddUserActivity;
import za.co.zebrav.smartdoor.database.User;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
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

	private int activeID;
	private Context context;
	private View view;
	private User user;

	private ProgressDialog soundLevelDialog;
	private ProgressDialog processingDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Bundle bundle = getArguments();
		user = (User) bundle.getSerializable("user");
		activeID = user.getID();
		return inflater.inflate(R.layout.fragment_add_voice, container, false);
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
		else if (v == btnDone)
		{
			saveToDataBase();

			AddUserActivity activity = (AddUserActivity) context;
			activity.doneStepThreeAddUser(btnDone);
		}
	}

	public User getUser()
	{
		return user;
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

		new trainTask().execute();
	}

	private class trainTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			Log.i(LOG_TAG, "Training new Voice");
			voiceAuthenticator.startRecording();
			soundLevelDialog.dismiss();

			if (!voiceAuthenticator.train())
			{
				Toast.makeText(context, "Error with Training voice", Toast.LENGTH_LONG).show();
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

	private void saveToDataBase()
	{
		user.setCodeBook(voiceAuthenticator.getCodeBook());

	}

	private void trainVoice()
	{
		Log.i(LOG_TAG, "Training Voice");
		startRecording();

	}
}
