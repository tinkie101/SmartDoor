package za.co.zebrav.smartdoor;

import za.co.zebrav.smartdoor.database.User;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import at.fhhgb.auth.voice.VoiceAuthenticator;

public class VoiceFragment extends Fragment
{
	protected AbstractActivity activity;

	protected VoiceAuthenticator voiceAuthenticator;
	protected ProgressDialog soundLevelDialog;
	protected ProgressDialog processingDialog;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.activity = (AbstractActivity) getActivity();

		this.soundLevelDialog = new ProgressDialog(activity, ProgressDialog.STYLE_HORIZONTAL);
		this.soundLevelDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		this.soundLevelDialog.setTitle("Listening...");
		this.soundLevelDialog.setMessage("Say: \"My voice is my password, and it should log me in\"");
		this.soundLevelDialog.setCancelable(false);

		this.processingDialog = new ProgressDialog(activity, ProgressDialog.STYLE_HORIZONTAL);
		this.processingDialog.setMessage("Processing");
		this.processingDialog.setCancelable(false);

		this.voiceAuthenticator = new VoiceAuthenticator(soundLevelDialog);
	}

	@Override
	public void onPause()
	{
		super.onPause();

		soundLevelDialog.dismiss();
		processingDialog.dismiss();

		// Stop recording
		voiceAuthenticator.cancelRecording();
	}
}
