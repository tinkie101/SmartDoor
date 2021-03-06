package za.co.zebrav.smartdoor.main;

import za.co.zebrav.smartdoor.R;
import za.co.zebrav.voice.VoiceAuthenticator;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class VoiceFragment extends Fragment
{
	private static final String LOG_TAG = "VoiceFragment";

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

		this.processingDialog = new ProgressDialog(activity, ProgressDialog.STYLE_HORIZONTAL);
		this.processingDialog.setMessage("Processing");
		this.processingDialog.setCancelable(false);

		this.voiceAuthenticator = new VoiceAuthenticator(soundLevelDialog);
		String settingsFile = getResources().getString(R.string.settingsFileName);
		try
		{
			Integer threshold = Integer.parseInt(activity.getSharedPreferences(settingsFile, 0).getString(
								"voice_Calibration", "350"));

			this.voiceAuthenticator.setMicThreshold(threshold);
		}
		catch (NumberFormatException e)
		{
			Log.d(LOG_TAG, "Error, Mic threshold not set!");
		}
	}
	
	public ProgressDialog getSoundLevelDialog()
	{
		return soundLevelDialog;
	}
	
	public ProgressDialog getProcessinglDialog()
	{
		return processingDialog;
	}
	
	public VoiceAuthenticator getVoiceAuthenticator()
	{
		return voiceAuthenticator;
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
