package za.co.zebrav.smartdoor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import at.fhooe.mcm.smc.wav.WavReader;
import at.fhooe.mcm.smc.wav.WaveRecorder;
import at.fhooe.mcm.sms.Constants;

import com.bitsinharmony.recognito.MatchResult;
import com.bitsinharmony.recognito.Recognito;

public class VoiceIdentificationActivity extends ListActivity
{
	private static final String LOG_TAG = "VoiceIdentificationActivity";

	private Button btnRecognise;

	private Button btnPlay;
	private boolean StartPlaying;

	private Button btnRecord;
	private boolean StartRecording;

	private static final String tempRecordingFile = "TempRecording.wav";
	private static final int sampleRate = 44100;
	private File outputFile;
	private WaveRecorder waveRecorder;

	private MediaPlayer mPlayer = null;
	private Recognito<String> recognito;
	private ArrayList<String> userKeys;
	private boolean isTraining = false;
	private String currentKey;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_voice_identification);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		btnPlay = (Button) findViewById(R.id.btnPlay);
		btnRecord = (Button) findViewById(R.id.btnRecord);
		btnRecognise = (Button) findViewById(R.id.btnRecognise);

		StartPlaying = true;
		StartRecording = true;

		outputFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), tempRecordingFile);
		waveRecorder = new WaveRecorder(sampleRate);
		recognito = new Recognito<String>(44100.0f);
		userKeys = new ArrayList<String>();
		currentKey = "";
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
		stopRecording();
		stopPlaying();
	}

	private void onRecord(File output)
	{
		boolean temp = StartRecording;
		StartRecording = !StartRecording;

		if (temp)
		{
			startRecording(output);
		}
		else
		{
			stopRecording();
		}
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
		btnPlay.setText("Stop Playing");

		mPlayer = new MediaPlayer();
		try
		{
			mPlayer.setDataSource(outputFile.getAbsolutePath());
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
	private void startRecording(File output)
	{
		btnRecord.setText("Stop Recording");

		if (output.exists())
			output.delete();

		waveRecorder.setOutputFile(output.getAbsolutePath());
		waveRecorder.prepare();
		waveRecorder.start();
	}

	/**
	 * Stop the recording of a wav file
	 */
	private void stopRecording()
	{
		btnRecord.setText("Start Recording");

		if (waveRecorder != null)
		{
			waveRecorder.stop();
			waveRecorder.release();
			waveRecorder.reset();
			
			if(isTraining)
			{
				isTraining = !isTraining;
				
				String fileName = currentKey + ".wav";
				//Create voice Prints
				if (!userKeys.contains(currentKey))
				{
					Log.d(LOG_TAG, "Creating voice print for: " + currentKey);
					userKeys.add(currentKey);
					createVoicePrintFromFile(fileName, currentKey);
				}
				else
				{
					Log.d(LOG_TAG, "Merging voice print for: " + currentKey);
					mergeVoicePrintFromFile(fileName, currentKey);
				}
			}
			
		}
	}

	/**
	 * Opens the wav file and try to identify who the speaker is.
	 * Sets the ListView to contain the list of possibilities and their likelihood Ratios
	 * 
	 * @param file
	 *            The wav file that contains the voice to be identified
	 */
	private void identifySpeaker(String file)
	{
		String filename = Environment.getExternalStorageDirectory().getAbsolutePath();
		filename += "/" + file;

		WavReader wavReader = new WavReader(filename);
		double[] samples = readSamples(wavReader);

		List<MatchResult<String>> matches = recognito.identify(samples);

		ArrayList<String> list = new ArrayList<String>();

		// TODO Only get the best 3 results
		for (MatchResult<String> matchResult : matches)
		{
			list.add(matchResult.getKey() + ": " + matchResult.getLikelihoodRatio() + "%");
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		setListAdapter(adapter);
	}

	/**
	 * Read in the wav file and create a new voice print.
	 * 
	 * @param file
	 *            The wav file to read in
	 * @param key
	 *            The key of the voice print to be created
	 */
	private void createVoicePrintFromFile(String file, String key)
	{
		String filename = Environment.getExternalStorageDirectory().getAbsolutePath();
		filename += "/" + file;

		WavReader wavReader = new WavReader(filename);
		double[] samples = readSamples(wavReader);

		recognito.createVoicePrint(key, samples);
	}

	private void mergeVoicePrintFromFile(String file, String key)
	{
		String filename = Environment.getExternalStorageDirectory().getAbsolutePath();
		filename += "/" + file;

		WavReader wavReader = new WavReader(filename);
		double[] samples = readSamples(wavReader);

		recognito.mergeVoiceSample(key, samples);
	}

	/**
	 * TODO(From example code)
	 * 
	 * Reads in a wav file and returns it as a double[]
	 * 
	 * @param wavReader
	 * @return
	 */
	private double[] readSamples(WavReader wavReader)
	{
		int sampleSize = wavReader.getFrameSize();
		int sampleCount = wavReader.getPayloadLength() / sampleSize;
		int windowCount = (int) Math.floor(sampleCount / Constants.WINDOWSIZE);
		byte[] buffer = new byte[sampleSize];
		double[] samples = new double[windowCount * Constants.WINDOWSIZE];

		try
		{
			for (int i = 0; i < samples.length; i++)
			{
				wavReader.read(buffer, 0, sampleSize);
				samples[i] = createSample(buffer);

			}
		}
		catch (IOException e)
		{
			Log.e(LOG_TAG, "Exception in reading samples", e);
		}
		return samples;
	}

	/**
	 * TODO(From example code)
	 * 
	 * Used by the readSamples function
	 * 
	 * @param buffer
	 * @return
	 */
	private short createSample(byte[] buffer)
	{
		short sample = 0;
		// hardcoded two bytes here
		short b1 = buffer[0];
		short b2 = buffer[1];
		b2 <<= 8;
		sample = (short) (b1 | b2);
		return sample;
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
		else if (v == btnRecord)
			onRecord(outputFile);
		else if (v == btnRecognise)
			recognise();
		else if (v == (Button) findViewById(R.id.btnTrain))
			train();
	}

	private void train()
	{
		final EditText input = new EditText(this);

		new AlertDialog.Builder(this).setTitle("Update Status").setMessage("Enter user KEY").setView(input)
							.setPositiveButton("Ok", new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int whichButton)
								{
									String key = input.getText().toString();
									String fileName = key + ".wav";
									File tempFile = new File(Environment.getExternalStorageDirectory()
														.getAbsolutePath(), fileName);
									onRecord(tempFile);
									currentKey = key;
									isTraining = true;
								}
							}).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int whichButton)
								{
									// Do nothing.
								}
							}).show();

	}

	/**
	 * TODO read old voice prints from database
	 * Identify the person speaking in the recorded file
	 * 
	 */
	public void recognise()
	{
		Log.d(LOG_TAG, "Recognising...");
		identifySpeaker(tempRecordingFile);
	}
}
