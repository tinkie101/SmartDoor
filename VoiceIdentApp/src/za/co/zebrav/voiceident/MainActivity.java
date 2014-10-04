package za.co.zebrav.voiceident;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import at.fhhgb.auth.voice.VoiceAuthenticator;
import at.fhooe.mcm.smc.math.mfcc.FeatureVector;

public class MainActivity extends ListActivity
{
	private static final String LOG_TAG = "AuthTest";

	protected static final int RECORDER_SAMPLERATE = 44100;
	protected static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	protected static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	protected static final byte RECORDER_BPP = 16;

	private Button btnRecognise;
	private Button btnTrain;
	private Button btnPlay;

	private boolean StartPlaying;
	private MediaPlayer mPlayer = null;

	private VoiceAuthenticator voiceAuthenticator;

	private Context context;
	ProgressDialog soundLevelDialog;
	ProgressDialog processingDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		context = this;

		soundLevelDialog = new ProgressDialog(context, ProgressDialog.STYLE_HORIZONTAL);
		soundLevelDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		soundLevelDialog.setCancelable(false);
		soundLevelDialog.setMessage("Listening...");
		
		processingDialog = new ProgressDialog(context, ProgressDialog.STYLE_HORIZONTAL);
		processingDialog.setCancelable(false);
		processingDialog.setMessage("Processing");
		
		voiceAuthenticator = new VoiceAuthenticator(soundLevelDialog);

		btnRecognise = (Button) findViewById(R.id.btnRecognise);
		btnTrain = (Button) findViewById(R.id.btnTrain);
		btnPlay = (Button) findViewById(R.id.btnPlay);

		StartPlaying = true;
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
		File activeFile = voiceAuthenticator.getActiveFile();

		if (activeFile != null && activeFile.exists())
		{
			btnPlay.setText("Stop Playing");

			mPlayer = new MediaPlayer();
			try
			{
				Log.i(LOG_TAG, "Playing Active File");
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
			Toast.makeText(context, "Invalid ActiveFile!", Toast.LENGTH_LONG).show();
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
			Log.i(LOG_TAG, "Stoped audio playback");
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
		Log.i(LOG_TAG, "Recording to File");		
		
		processingDialog.show();		
		soundLevelDialog.show();
		
		voiceAuthenticator.autoCalibrateActivation();
		
		if (isTraining)
		{
			new trainTask().execute();
		}
		else
		{
			new identifyTask().execute();
		}
	}

	private void doRecording()
	{
		// Get the minimum buffer size required for the successful creation of an
		// AudioRecord object.
		int bufferSizeInBytes = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS,
							RECORDER_AUDIO_ENCODING);
		System.out.println(bufferSizeInBytes);
		// Initialize Audio Recorder.
		AudioRecord audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE,
							RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSizeInBytes);
		// Start Recording.
		audioRecorder.startRecording();

		int numberOfReadBytes = 0;
		byte audioBuffer[] = new byte[bufferSizeInBytes];
		boolean recording = false;
		float tempFloatBuffer[] = new float[3];
		int tempIndex = 0;
		int totalReadBytes = 0;
		byte totalByteBuffer[] = new byte[60 * 44100 * 2];

		long startTime = System.currentTimeMillis();
		long StopThreshold = 1500; // time in milliseconds
		int StartThreshold = 350;
		// While data come from microphone.
		while (true)
		{
			float totalAbsValue = 0.0f;
			short sample = 0;

			numberOfReadBytes = audioRecorder.read(audioBuffer, 0, bufferSizeInBytes);

			// Analyze Sound.
			for (int i = 0; i < bufferSizeInBytes; i += 2)
			{
				sample = (short) ((audioBuffer[i]) | audioBuffer[i + 1] << 8);
				totalAbsValue += Math.abs(sample) / (numberOfReadBytes / 2);
			}

			// Analyze temp buffer.
			tempFloatBuffer[tempIndex % 3] = totalAbsValue;
			float temp = 0.0f;
			for (int i = 0; i < 3; ++i)
				temp += tempFloatBuffer[i];

			Log.i("TAG", temp + "");
			// calculate progressbar value, Log10(0) = undefined!
			float tempCalc = temp;
			if (tempCalc < 15)
				tempCalc = 15;
			/*
			 * Scale the values from between 1 and 3.9 to 0 and 100 to fit into progressbar
			 * ts = scaled value
			 * tu = unscaled value
			 * tumin = minimum that tu can be
			 * tumax = maximum that tu can be
			 * tsmax = the maximum that ts should be
			 * tsmin = the mimimum that ts should be
			 * ts = (tu - tumin)/(tumax - tumin)*(tsmax - tsmin)-(tsmin);
			 */
			double level = (Math.log10(tempCalc) - 1d) / (2.9d) * 100; // shortened known constants
//			soundLevel.setProgress((int) level);

			if ((temp >= 0 && temp <= StartThreshold) && recording == false)
			{
				tempIndex++;
				continue;
			}

			if (temp > StartThreshold)
			{
				startTime = System.currentTimeMillis();
				recording = true;
			}

			if ((temp >= 0 && temp <= StartThreshold) && recording == true)
			{
				long timeElapsed = System.currentTimeMillis() - startTime;
				if (timeElapsed > StopThreshold)
				{
					Log.i("TAG", "Save audio to file.");

					// Save audio to file.
					String filepath = Environment.getExternalStorageDirectory().getPath();
					File file = new File(filepath, "AudioRecorder");
					if (!file.exists())
						file.mkdirs();

					String fn = file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".wav";

					long totalAudioLen = 0;
					long totalDataLen = totalAudioLen + 36;
					long longSampleRate = RECORDER_SAMPLERATE;
					int channels = 1;
					long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;
					totalAudioLen = totalReadBytes;
					totalDataLen = totalAudioLen + 36;
					byte finalBuffer[] = new byte[totalReadBytes + 44];

					finalBuffer[0] = 'R'; // RIFF/WAVE header
					finalBuffer[1] = 'I';
					finalBuffer[2] = 'F';
					finalBuffer[3] = 'F';
					finalBuffer[4] = (byte) (totalDataLen & 0xff);
					finalBuffer[5] = (byte) ((totalDataLen >> 8) & 0xff);
					finalBuffer[6] = (byte) ((totalDataLen >> 16) & 0xff);
					finalBuffer[7] = (byte) ((totalDataLen >> 24) & 0xff);
					finalBuffer[8] = 'W';
					finalBuffer[9] = 'A';
					finalBuffer[10] = 'V';
					finalBuffer[11] = 'E';
					finalBuffer[12] = 'f'; // 'fmt ' chunk
					finalBuffer[13] = 'm';
					finalBuffer[14] = 't';
					finalBuffer[15] = ' ';
					finalBuffer[16] = 16; // 4 bytes: size of 'fmt ' chunk
					finalBuffer[17] = 0;
					finalBuffer[18] = 0;
					finalBuffer[19] = 0;
					finalBuffer[20] = 1; // format = 1
					finalBuffer[21] = 0;
					finalBuffer[22] = (byte) channels;
					finalBuffer[23] = 0;
					finalBuffer[24] = (byte) (longSampleRate & 0xff);
					finalBuffer[25] = (byte) ((longSampleRate >> 8) & 0xff);
					finalBuffer[26] = (byte) ((longSampleRate >> 16) & 0xff);
					finalBuffer[27] = (byte) ((longSampleRate >> 24) & 0xff);
					finalBuffer[28] = (byte) (byteRate & 0xff);
					finalBuffer[29] = (byte) ((byteRate >> 8) & 0xff);
					finalBuffer[30] = (byte) ((byteRate >> 16) & 0xff);
					finalBuffer[31] = (byte) ((byteRate >> 24) & 0xff);
					finalBuffer[32] = (byte) (2 * 16 / 8); // block align
					finalBuffer[33] = 0;
					finalBuffer[34] = RECORDER_BPP; // bits per sample
					finalBuffer[35] = 0;
					finalBuffer[36] = 'd';
					finalBuffer[37] = 'a';
					finalBuffer[38] = 't';
					finalBuffer[39] = 'a';
					finalBuffer[40] = (byte) (totalAudioLen & 0xff);
					finalBuffer[41] = (byte) ((totalAudioLen >> 8) & 0xff);
					finalBuffer[42] = (byte) ((totalAudioLen >> 16) & 0xff);
					finalBuffer[43] = (byte) ((totalAudioLen >> 24) & 0xff);

					for (int i = 0; i < totalReadBytes; ++i)
						finalBuffer[44 + i] = totalByteBuffer[i];

					FileOutputStream out;
					try
					{
						out = new FileOutputStream(fn);
						try
						{
							out.write(finalBuffer);
							out.close();
						}
						catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					catch (FileNotFoundException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					// */
					tempIndex++;
					break;
				}
				else
				{
					Log.i(LOG_TAG, "waiting for voice: " + timeElapsed);
				}
			}

			// -> Recording sound here.
			Log.i("TAG", "Recording Sound.");
			for (int i = 0; i < numberOfReadBytes; i++)
				totalByteBuffer[totalReadBytes + i] = audioBuffer[i];
			totalReadBytes += numberOfReadBytes;
			// */

			tempIndex++;

		}
		audioRecorder.release();
	}

	private class record extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			doRecording();
			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{

		}
	}

	private class identifyTask extends AsyncTask<Void, Void, ArrayList<String>>
	{
		@Override
		protected ArrayList<String> doInBackground(Void... params)
		{

			voiceAuthenticator.startRecording();
			soundLevelDialog.dismiss();

			FeatureVector featureVector = voiceAuthenticator.getCurrentFeatureVector();
			
			ArrayList<Double> result = voiceAuthenticator.identify(featureVector);

			Double average = 0.0;

			for (Double double1 : result)
			{
				average += double1;
			}

			average = average / (double) result.size();

			ArrayList<String> Finalresult = new ArrayList<String>();

			Finalresult.add("Average Distance: " + average);

			return Finalresult;
		}

		@Override
		protected void onPostExecute(ArrayList<String> result)
		{
			processingDialog.dismiss();
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
								result);
			setListAdapter(adapter);
		}
	}

	private class trainTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			voiceAuthenticator.startRecording();
			soundLevelDialog.dismiss();
			voiceAuthenticator.train();
			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			processingDialog.dismiss();
		}
	}

	private void trainVoice()
	{
		Log.i(LOG_TAG, "Training Voice");
		startRecording(true);
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
		if (v == btnRecognise)
			identifySpeaker();
		else if (v == btnTrain)
			trainVoice();
		else if (v == btnPlay)
			onPlay();
	}
}
