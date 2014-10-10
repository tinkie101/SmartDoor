package za.co.zebrav.voice;

import android.app.ProgressDialog;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;
import at.fhooe.mcm.smc.wav.WaveRecorder;
import at.fhooe.mcm.sms.Constants;

public class VoiceRecorder
{
	/** Tag for logging. */
	private static final String LOG_TAG = "VoiceRecorder";

	public enum State
	{
		/** . */
		INITIALIZING,
		/** . */
		READY,
		/** . */
		RECORDING,
		/** . */
		ERROR,
		/** . */
		STOPPED
	};

	/** Recorder used for uncompressed recording. */
	private AudioRecord aRecorder = null;

	/** The interval in which the recorded samples are output to the file. */
	private static final int TIMER_INTERVAL = 120;

	// Number of channels, sample rate, sample size(size in bits), buffer size,
	// audio source, sample size(see AudioFormat)

	/** Number of frames written to file on each output. */
	private int framePeriod;

	private boolean recording;
	private final long StopThreshold = 1500; // TODO Setting: time in milliseconds
	private int StartThreshold = 300;// Set default value
	private int maxRecordTime = 20000; // TODO Setting: time in milliseconds
	private ProgressDialog dialog;

	private int sampleSize;
	private byte[] finalBuffer;
	private int payloadSize;
	private int sampleRate;
	private int bufferSize;
	private byte[] readBuffer;

	private State state;

	private final int bitsPerSample = 16;
	private final int micChannel = AudioFormat.CHANNEL_IN_MONO;
	private final short numChannels = 1;
	private final int audioSource = AudioSource.MIC;
	private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

	public void setStartThreshold(int threshold)
	{
		if (threshold < 1)
			StartThreshold = 1;
		else
			StartThreshold = threshold;
	}

	public int getStartThreshold()
	{
		return StartThreshold;
	}

	public boolean isRecording()
	{
		return recording;
	}

	public VoiceRecorder(ProgressDialog dialog)
	{
		this.dialog = dialog;
		try
		{
			recording = false;

			sampleRate = Constants.SAMPLERATE;

			framePeriod = sampleRate * TIMER_INTERVAL / 1000;
			bufferSize = framePeriod * 2 * bitsPerSample * numChannels / 8;

			sampleSize = (numChannels * bitsPerSample / 8);

			if (bufferSize < AudioRecord.getMinBufferSize(sampleRate, micChannel, audioFormat))
			{
				// increase buffer size if needed
				bufferSize = AudioRecord.getMinBufferSize(sampleRate, micChannel, audioFormat);

				// Set frame period and timer interval accordingly
				framePeriod = bufferSize / (2 * bitsPerSample * numChannels / 8);
				Log.w(LOG_TAG, "Increasing buffer size to " + bufferSize);
			}

			aRecorder = new AudioRecord(audioSource, sampleRate, micChannel, audioFormat, bufferSize);

			if (aRecorder.getState() != AudioRecord.STATE_INITIALIZED)
			{
				throw new Exception("AudioRecord initialization failed");
			}

			aRecorder.setPositionNotificationPeriod(framePeriod);

			state = State.INITIALIZING;
		}
		catch (Exception e)
		{
			if (e.getMessage() != null)
			{
				Log.e(LOG_TAG, e.getMessage());
			}
			else
			{
				Log.e(LOG_TAG, "Unknown error occured while initializing recording");
			}
		}
	}

	public void prepare()
	{
		try
		{
			if (state == State.INITIALIZING)
			{
				if (aRecorder.getState() == AudioRecord.STATE_INITIALIZED)
				{
					int blockAlign = (numChannels * bitsPerSample / 8);
					sampleSize = blockAlign;

					readBuffer = new byte[framePeriod * bitsPerSample / 8 * numChannels];
					finalBuffer = new byte[0];
					state = State.READY;

					payloadSize = 0;
				}
				else
				{
					Log.e(LOG_TAG, "prepare() method called on uninitialized recorder");
					state = State.ERROR;
				}
			}
			else
			{
				Log.e(LOG_TAG, "prepare() method called on illegal state");
				release();
				state = State.ERROR;
			}
		}
		catch (Exception e)
		{
			if (e.getMessage() != null)
			{
				Log.e(LOG_TAG, e.getMessage(), e);
			}
			else
			{
				Log.e(LOG_TAG, "Unknown error occured in prepare()");
			}
			state = State.ERROR;
		}
	}

	public void release()
	{
		if (state == State.RECORDING)
		{
			stopRecorder();
		}

		if (aRecorder != null)
		{
			aRecorder.release();
		}
	}

	public void reset()
	{
		try
		{
			if (state != State.ERROR)
			{
				release();
				aRecorder = new AudioRecord(audioSource, sampleRate, numChannels + 1, audioFormat, bufferSize);
				aRecorder.setPositionNotificationPeriod(framePeriod);
				state = State.INITIALIZING;
			}
		}
		catch (Exception e)
		{
			Log.e(WaveRecorder.class.getName(), e.getMessage());
			state = State.ERROR;
		}
	}

	public void startRecorder()
	{
		if (state == State.READY)
		{
			aRecorder.startRecording();
			Log.i(LOG_TAG, "Listening for sound to record");
			state = State.RECORDING;

			float tempFloatBuffer[] = new float[3];
			int countReads = 0;
			long startTime = -1;
			long lastTime = -1;

			while (state == State.RECORDING)
			{
				int numberOfReadBytes = aRecorder.read(readBuffer, 0, readBuffer.length); // Fill buffer

				float totalAbsValue = 0.0f;
				short sample = 0;

				// Analyze Sound.
				for (int i = 0; i < readBuffer.length; i += 2)
				{
					sample = (short) ((readBuffer[i]) | readBuffer[i + 1] << 8);
					totalAbsValue += Math.abs(sample) / (numberOfReadBytes / 2);
				}

				// Analyze temp buffer.
				tempFloatBuffer[countReads % 3] = totalAbsValue;
				float temp = 0.0f;
				for (int i = 0; i < 3; ++i)
					temp += tempFloatBuffer[i];

				updateProgressDialog(temp);

				Log.d(LOG_TAG, temp + "");

				if (temp > StartThreshold)
				{
					lastTime = System.currentTimeMillis();
					
					if(!recording)
					{
						startTime = lastTime;
						recording = true;
					}
				}

				if (recording)
				{
					Log.i(LOG_TAG, "Recording Voice");
					int oldLength = finalBuffer.length;
					int newLength = oldLength + readBuffer.length;
					byte[] tempFinalBuffer = new byte[newLength];

					for (int b = 0; b < oldLength; b++)
					{
						tempFinalBuffer[b] = finalBuffer[b];
					}

					int pos = oldLength;
					for (int b = 0; b < readBuffer.length; b++)
					{
						tempFinalBuffer[pos] = readBuffer[b];
						pos++;
					}

					finalBuffer = tempFinalBuffer;
					payloadSize += readBuffer.length;

					long currentTime = System.currentTimeMillis();

					if (currentTime - lastTime > StopThreshold || currentTime - startTime > maxRecordTime)
					{
						stopRecorder();
					}
				}
				countReads++;
			}
		}
		else
		{
			Log.e(LOG_TAG, "start() called on illegal state");
			state = State.ERROR;
		}
	}

	/**
	 * Stops the recording, and sets the state to STOPPED. In case of further usage, a reset is
	 * needed. Also finalizes the wave file in case of uncompressed recording.
	 */
	public void stopRecorder()
	{
		if (state == State.RECORDING)
		{
			aRecorder.stop();
			recording = false;

			if (dialog != null)
			{
				dialog.setProgress(0);
			}

			Log.i(LOG_TAG, "Stopped recording with payloadSize=" + payloadSize);
			state = State.STOPPED;
		}
		else
		{
			Log.e(LOG_TAG, "stop() called on illegal state");
			state = State.ERROR;
		}

	}

	public int getAverageSoundLevel(double numMiliseconds)
	{
		int result = 1;

		byte[] tempBuffer = new byte[framePeriod * bitsPerSample / 8 * numChannels];
		float tempFloatBuffer[] = new float[3];
		int countReads = 0;
		long startTime = System.currentTimeMillis();

		aRecorder.startRecording();
		Log.i(LOG_TAG, "Started to listen");

		while (System.currentTimeMillis() < numMiliseconds + startTime || countReads < 3)
		{
			int numberOfReadBytes = aRecorder.read(tempBuffer, 0, tempBuffer.length); // Fill buffer

			float totalAbsValue = 0.0f;
			short sample = 0;

			// Analyze Sound.
			for (int i = 0; i < tempBuffer.length; i += 2)
			{
				sample = (short) ((tempBuffer[i]) | tempBuffer[i + 1] << 8);
				totalAbsValue += Math.abs(sample) / (numberOfReadBytes / 2);
			}

			// Analyze temp buffer.
			tempFloatBuffer[countReads % 3] = totalAbsValue;
			float temp = 0.0f;
			for (int i = 0; i < 3; ++i)
				temp += tempFloatBuffer[i];

			updateProgressDialog(temp);

			result += temp;
			countReads++;
		}

		result = result / countReads;

		aRecorder.stop();

		if (dialog != null)
		{
			dialog.setProgress(0);
		}

		Log.i(LOG_TAG, "Mic threshold calculated as: " + result);

		return result;
	}

	private void updateProgressDialog(float temp)
	{
		if (dialog != null)
		{
			// calculate progressbar value, Log10(0) = undefined!
			float tempCalc = temp;

			// Scale tempCalc because Log(10) = 1 and Log(<10) < 1. We want from 1 to 3.9
			if (tempCalc < StartThreshold)
				tempCalc = 11;
			else
			{
				tempCalc = tempCalc - StartThreshold;

				// Just to show that we are indeed recording (this will happen when the voice is only slightly
				// above the startThreshold
				if (tempCalc < 11)
					tempCalc = 12;
			}

			// calculate maxVal that log10() can MAY be (8000 is just a very high value that the mic may return
			// when it records a very load noise)
			float tuMax = (float) Math.log10(8000d - StartThreshold);
			float tuMin = 1f;

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
			double level = (Math.log10(tempCalc) - tuMin) / (tuMax - tuMin) * 100; // shortened known constants

			int oldValue = dialog.getProgress();

			// scale to make the changes look more smooth
			int newValue;
			if (oldValue > level)
			{
				int dist = (int) (oldValue - level);
				newValue = oldValue - (int) (dist / 2);
			}
			else
				newValue = (int) level;

			dialog.setProgress((int) newValue);
		}
	}

	public int getFrameSize()
	{
		return sampleSize;
	}

	public int getPayloadSize()
	{
		return payloadSize;
	}

	public int readFromBuffer(byte[] buffer2, int currentPos, int sampleSize3)
	{
		int pos = currentPos;
		for (int i = 0; i < sampleSize3; i++)
		{
			buffer2[i] = finalBuffer[i + pos];
		}
		pos += sampleSize3;

		return pos;
	}
}
