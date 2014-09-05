package za.co.zebrav.smartdoor;

import java.io.File;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import at.fhhgb.auth.voice.VoiceAuthenticator;

public class VoiceAuth extends ListActivity
{
	private static final String LOG_TAG = "AuthTest";

	private Button btnRecognise;
	private Button btnPlay;
	private Button btnTrain;

	private boolean StartPlaying;

	private File activeFile;
	private String activeKey;
	private MediaPlayer mPlayer = null;

	private VoiceAuthenticator voiceAuthenticator;

	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_voice_auth);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		context = this;
		
		voiceAuthenticator = new VoiceAuthenticator(this);

		btnPlay = (Button) findViewById(R.id.btnPlay);
		btnRecognise = (Button) findViewById(R.id.btnRecognise);
		btnTrain = (Button) findViewById(R.id.btnTrain);

		StartPlaying = true;
	}

	/**
	 * 
	 * @param item
	 * @return
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPause()
	{
		super.onPause();

		// Stop play/record
		voiceAuthenticator.cancelRecording();
		stopPlaying();
	}

	private void onPlay()
	{
		boolean temp = StartPlaying;
		StartPlaying = !StartPlaying;

		if (temp)
		{
			startPlaying();
		}
		else
		{
			stopPlaying();
		}
	}

	/**
	 * Start to play the file that was recorded
	 */
	private void startPlaying()
	{
		if (activeFile != null)
		{
			btnPlay.setText("Stop Playing");

			mPlayer = new MediaPlayer();
			try
			{
				mPlayer.setDataSource(activeFile.getAbsolutePath());
				mPlayer.prepare();
				mPlayer.start();

				// Stop playing when done
				mPlayer.setOnCompletionListener(new OnCompletionListener()
				{

					@Override
					public void onCompletion(MediaPlayer mp)
					{
						// will handle the current play state
						onPlay();
					}
				});
			}
			catch (IOException e)
			{
				Log.e(LOG_TAG, "prepare() failed");
			}
		}
		else
		{
			Toast.makeText(context, "No active file set!", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Stop playing the file
	 */
	private void stopPlaying()
	{
		btnPlay.setText("Start Playing");

		if (mPlayer != null)
		{
			mPlayer.release();
			mPlayer = null;
		}
	}

	/**
	 * Delete old recording and record a new file as wav
	 * 
	 * @param output
	 */
	private void startRecording(final boolean isTraining)
	{
		if(activeFile != null)
		{
		voiceAuthenticator.startRecording(activeFile);

		// Alert user of recording and Stop button
		new AlertDialog.Builder(context).setTitle("Recording...").setMessage("Stop Recording").setPositiveButton("Ok", new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int whichButton)
								{
									voiceAuthenticator.stopRecording(activeKey, isTraining);
								}
							}).show();
		}
		else
		{
			Toast.makeText(context, "No Active File Set", Toast.LENGTH_LONG).show();
		}
	}

	private void trainVoice()
	{
		Log.d(LOG_TAG, "Training Voice");
		final EditText input = new EditText(this);

		new AlertDialog.Builder(this).setTitle("Update Status").setMessage("Enter user KEY").setView(input)
							.setPositiveButton("Ok", new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int whichButton)
								{
									activeKey = input.getText().toString();
									String fileName = activeKey + ".wav";
									activeFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
														fileName);
									startRecording(true);
								}
							}).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int whichButton)
								{
									// Do nothing.
								}
							}).show();
	}

	private void identifySpeaker()
	{
		Log.d(LOG_TAG, "Identifying Voice");
		startRecording(false);
	}

	/**
	 * Handle the button pressed
	 * 
	 * @param v
	 */
	public void buttonClicked(View v)
	{
		if (v == btnPlay)
			onPlay();
		else if (v == btnRecognise)
			identifySpeaker();
		else if (v == btnTrain)
			trainVoice();
	}
}
