package za.co.zebrav.smartdoor;

import java.util.ArrayList;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;
import at.fhhgb.auth.voice.VoiceAuthenticator;

public class IdentifyVoiceFragment extends ListFragment implements OnClickListener
{
	private static final String LOG_TAG = "AuthTest";

	private Button btnIdentify;

	private VoiceAuthenticator voiceAuthenticator;

	private String activeKey;
	private Context context;
	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Bundle bundle = this.getArguments();
		Long id = bundle.getLong("userID", -1);
		activeKey = id.toString();
		return inflater.inflate(R.layout.fragment_identify_voice, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		super.onCreate(savedInstanceState);

		context = getActivity();
		view = getView();

		voiceAuthenticator = new VoiceAuthenticator();

		btnIdentify = (Button) view.findViewById(R.id.btnIdentify);
		btnIdentify.setOnClickListener(this);
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
										new identifyTask().execute();
									}

								}).show();
		}
		else
		{
			Toast.makeText(context, "No Active Key Set!", Toast.LENGTH_LONG).show();
		}
	}
	
	private class identifyTask extends AsyncTask<Void, Void, ArrayList<String>>
	{
		@Override
		protected ArrayList<String> doInBackground(Void... params)
		{
			ArrayList<String> result = voiceAuthenticator.identify();
			return result;
		}
		
		@Override
		protected void onPostExecute(ArrayList<String> result)
		{
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
								result);
			setListAdapter(adapter);
		}
	}
	
	private void identifySpeaker()
	{
		Log.d(LOG_TAG, "Identifying Voice");
		startRecording();
	}
}
