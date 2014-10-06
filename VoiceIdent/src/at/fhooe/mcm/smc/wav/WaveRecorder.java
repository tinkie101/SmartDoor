package at.fhooe.mcm.smc.wav;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import android.app.ProgressDialog;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

/**
 * This class lets the user record audio from the built-in microphone as .wav file (as opposed to
 * .3gpp file using the AMR-NB codec when recording with the default {@link MediaRecorder}).
 * 
 * @author Thomas Kaiser, AT
 */
public class WaveRecorder
{
	/** Tag for logging. */
	private static final String LOG_TAG = "WaveRecorder";

	/**
	 * INITIALIZING : recorder is initializing; READY : recorder has been initialized, recorder not
	 * yet started RECORDING : recording ERROR : reconstruction needed STOPPED: reset needed.
	 */
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

	/** Length of WAV header in bytes. */
	private static final int HEADER_LENGTH = 44;

	/** The interval in which the recorded samples are output to the file. */
	private static final int TIMER_INTERVAL = 120;

	private static final String DICTATE_TEMP_PATH = "./temp.wav";

	/** Recorder used for uncompressed recording. */
	private AudioRecord aRecorder = null;

	/** Output file path. */
	private String fPath = null;

	/** Recorder state, see {@link State}. */
	private State state;

	/** File writer. */
	private RandomAccessFile fWriter;

	// Number of channels, sample rate, sample size(size in bits), buffer size,
	// audio source, sample size(see AudioFormat)
	/** Number of channels (1). */
	private short numChannels;
	/** Audio sampling rate. */
	private int sampleRate;
	/** Bits per sample (only 16 possible on HTC Magic). */
	private short bitsPerSample;
	/** Size of audio-in buffer. */
	private int bufferSize;
	/** Audio source, {@link AudioSource}. */
	private int audioSource;
	/** {@link AudioFormat}. */
	private int audioFormat;

	/** Number of frames written to file on each output. */
	private int framePeriod;

	/** How many bytes per block in the .wav file, needed for splitting/reconstructing the file. */
	private int blockAlign;

	/** Buffer for output. */
	private byte[] buffer;

	private boolean recording;
	private final long StopThreshold = 1500; // TODO Setting: time in milliseconds
	private int StartThreshold = 350;// Set default value
	private ProgressDialog dialog;

	private int sampleSize;
	private int payload;
	private byte[] finalBuffer;

	/**
	 * Number of bytes written to file after header(only in uncompressed mode) after stop() is
	 * called, this size is written to the header/data chunk in the wave file.
	 */
	private int payloadSize;

	/**
	 * Gets set in start(), indicates that the user wanted to insert a record into an existing
	 * dictate and a temp file containing the second part exists. This part will have to be appended
	 * to the new dictate file in stop().
	 */
	private boolean mIsInserting = false;

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

	/**
	 * Default constructor. Leaves the recorder in {@link State#INITIALIZING}, except if some kind
	 * of error happens.
	 * 
	 * @param sampleRate
	 *            Audio sampling rate.
	 * @param dialog
	 *            the dialog box to update
	 */
	public WaveRecorder(int sampleRate, ProgressDialog dialog)
	{
		this.dialog = dialog;
		try
		{
			recording = false;

			bitsPerSample = 16;

			numChannels = 1;

			audioSource = AudioSource.MIC;
			this.sampleRate = sampleRate;
			audioFormat = AudioFormat.ENCODING_PCM_16BIT;

			framePeriod = sampleRate * TIMER_INTERVAL / 1000;
			bufferSize = framePeriod * 2 * bitsPerSample * numChannels / 8;
			if (bufferSize < AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
								AudioFormat.ENCODING_PCM_16BIT))
			{
				// increase buffer size if needed
				bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
									AudioFormat.ENCODING_PCM_16BIT);
				// Set frame period and timer interval accordingly
				framePeriod = bufferSize / (2 * bitsPerSample * numChannels / 8);
				Log.w(LOG_TAG, "Increasing buffer size to " + bufferSize);
			}

			aRecorder = new AudioRecord(audioSource, sampleRate, AudioFormat.CHANNEL_IN_MONO,
								AudioFormat.ENCODING_PCM_16BIT, bufferSize);
			if (aRecorder.getState() != AudioRecord.STATE_INITIALIZED)
			{
				throw new Exception("AudioRecord initialization failed");
			}
			aRecorder.setPositionNotificationPeriod(framePeriod);

			fPath = null;
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
			state = State.ERROR;
		}
	}

	/**
	 * Default constructor. Leaves the recorder in {@link State#INITIALIZING}, except if some kind
	 * of error happens.
	 * 
	 * @param sampleRate
	 *            Audio sampling rate.
	 */
	public WaveRecorder(int sampleRate)
	{
		try
		{
			recording = false;
			bitsPerSample = 16;

			numChannels = 1;

			audioSource = AudioSource.MIC;
			this.sampleRate = sampleRate;
			audioFormat = AudioFormat.ENCODING_PCM_16BIT;

			framePeriod = sampleRate * TIMER_INTERVAL / 1000;
			bufferSize = framePeriod * 2 * bitsPerSample * numChannels / 8;
			if (bufferSize < AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
								AudioFormat.ENCODING_PCM_16BIT))
			{
				// increase buffer size if needed
				bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
									AudioFormat.ENCODING_PCM_16BIT);
				// Set frame period and timer interval accordingly
				framePeriod = bufferSize / (2 * bitsPerSample * numChannels / 8);
				Log.w(LOG_TAG, "Increasing buffer size to " + bufferSize);
			}

			aRecorder = new AudioRecord(audioSource, sampleRate, AudioFormat.CHANNEL_IN_MONO,
								AudioFormat.ENCODING_PCM_16BIT, bufferSize);
			if (aRecorder.getState() != AudioRecord.STATE_INITIALIZED)
			{
				throw new Exception("AudioRecord initialization failed");
			}
			aRecorder.setPositionNotificationPeriod(framePeriod);

			fPath = null;
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
			state = State.ERROR;
		}
	}

	/**
	 * Returns the state of the recorder.
	 * 
	 * @return recorder state
	 */
	public State getState()
	{
		return state;
	}

	/**
	 * Sets the output file for the recorder, call in {@link State#INITIALIZING} , right after
	 * constructing.
	 * 
	 * @param path
	 *            Path of the output file.
	 */
	public void setOutputFile(String path)
	{
		if (state == State.INITIALIZING)
		{
			fPath = path;
		}
		else
		{
			Log.e(LOG_TAG, "Output file can only be set in State=INITIALIZING, current state=" + state);
		}
	}

	/**
	 * Prepares the recorder for recording, in case the recorder is not in the INITIALIZING state
	 * and the file path was not set the recorder is set to the ERROR state, which makes a
	 * reconstruction necessary. The header of the wave file is written. The file is DELETED! In
	 * case of an exception, the state is changed to ERROR.
	 */
	public void prepare()
	{
		try
		{
			if (state == State.INITIALIZING)
			{
				if ((aRecorder.getState() == AudioRecord.STATE_INITIALIZED) & (fPath != null))
				{
					// write file header

					fWriter = new RandomAccessFile(fPath, "rw");

					// this will clear out the file
					fWriter.setLength(0);

					// beginn RIFF header block
					// WAVE header keyword
					fWriter.writeBytes("RIFF");
					// no file size known yet
					fWriter.writeInt(0);
					fWriter.writeBytes("WAVE");

					// begin format block
					fWriter.writeBytes("fmt ");
					// format block is 16 bytes long
					fWriter.writeInt(Integer.reverseBytes(16));
					// "1" says we're writing PCM coded data
					fWriter.writeShort(Short.reverseBytes((short) 1));
					// channels
					Log.d(LOG_TAG, "numChannels: " + numChannels);
					fWriter.writeShort(Short.reverseBytes(numChannels));
					// sample rate
					Log.d(LOG_TAG, "sampleRate: " + sampleRate);
					fWriter.writeInt(Integer.reverseBytes(sampleRate));
					// byte rate
					Log.d(LOG_TAG, "byte rate: " + (sampleRate * bitsPerSample * numChannels / 8));
					fWriter.writeInt(Integer.reverseBytes(sampleRate * bitsPerSample * numChannels / 8));
					// block align: how many bytes make up a single
					// frame in the data block
					Log.d(LOG_TAG, "blockAlign: " + blockAlign);
					blockAlign = (numChannels * bitsPerSample / 8);
					sampleSize = blockAlign;
					fWriter.writeShort(Short.reverseBytes((short) blockAlign));
					// bits per sample
					Log.d(LOG_TAG, "bitsPerSample: " + bitsPerSample);
					fWriter.writeShort(Short.reverseBytes(bitsPerSample));

					// begin data block
					fWriter.writeBytes("data");
					// payload size not known yet
					fWriter.writeInt(0);

					buffer = new byte[framePeriod * bitsPerSample / 8 * numChannels];
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

	/**
	 * Releases the resources associated with this class, and removes the unnecessary files, when
	 * necessary.
	 */
	public void release()
	{
		if (state == State.RECORDING)
		{
			stop();
		}
		else
		{
			if ((state == State.READY))
			{
				try
				{
					fWriter.close(); // Remove prepared file
				}
				catch (IOException e)
				{
					Log.e(LOG_TAG, "I/O exception occured while closing output file");
				}
				(new File(fPath)).delete();
			}
		}

		if (aRecorder != null)
		{
			aRecorder.release();
		}
	}

	/**
	 * Resets the recorder to the INITIALIZING state, as if it was just created. In case the class
	 * was in RECORDING state, the recording is stopped. In case of exceptions the class is set to
	 * the ERROR state.
	 */
	public void reset()
	{
		try
		{
			if (state != State.ERROR)
			{
				release();
				fPath = null;
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

	/**
	 * Starts the recording, and sets the state to RECORDING. Call after prepare() for first time
	 * recording or after stop() if you wish to continue recording.
	 */
	public void start()
	{
		start(-1, false);
	}

	/**
	 * See {@link #start()}, defaults to INSERTING a new part at the specified position.
	 * 
	 * @param fromPosition
	 *            Lets you set a position in bytes to insert a new part into an existing record. Use
	 *            -1 to append to end. Find out current record size in bytes with {@link #getRecordSize()}.
	 */
	public void start(int fromPosition)
	{
		start(fromPosition, true);
	}

	/**
	 * See {@link #start()}. Enables inserting a new part by specifying a position to start
	 * recording from in bytes.
	 * 
	 * @param fromPosition
	 *            See {@link #start(int)}.
	 * @param insert
	 *            Set true if you want to insert a new part, set false if the dicate should be
	 *            overwritten from the specified position.
	 */
	public void start(int fromPosition, boolean insert)
	{
		if (fromPosition < -1 || fromPosition > payloadSize + HEADER_LENGTH)
		{
			throw new IllegalArgumentException("fromPosition out of range: was " + fromPosition + ", min/max=-1/"
								+ (payloadSize + HEADER_LENGTH));
		}
		if (state == State.READY)
		{
			aRecorder.startRecording();
			Log.i(LOG_TAG, "Started to record to " + fPath);
			state = State.RECORDING;

			float tempFloatBuffer[] = new float[3];
			int countReads = 0;
			long startTime = System.currentTimeMillis();

			while (state == State.RECORDING)
			{
				// Log.v(TAG, "Update Listener called");
				int numberOfReadBytes = aRecorder.read(buffer, 0, buffer.length); // Fill buffer

				float totalAbsValue = 0.0f;
				short sample = 0;

				// Analyze Sound.
				for (int i = 0; i < buffer.length; i += 2)
				{
					sample = (short) ((buffer[i]) | buffer[i + 1] << 8);
					totalAbsValue += Math.abs(sample) / (numberOfReadBytes / 2);
				}

				// Analyze temp buffer.
				tempFloatBuffer[countReads % 3] = totalAbsValue;
				float temp = 0.0f;
				for (int i = 0; i < 3; ++i)
					temp += tempFloatBuffer[i];

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

				Log.i(LOG_TAG, temp + "");

				if (temp > StartThreshold)
				{
					startTime = System.currentTimeMillis();
					recording = true;
				}

				if (recording)
				{
					try
					{
						Log.i(LOG_TAG, "Recording Voice");
						fWriter.write(buffer); // Write buffer to file
						int oldLength = finalBuffer.length;
						int newLength = oldLength + buffer.length;
						byte[] tempFinalBuffer = new byte[newLength];

						for (int b = 0; b < oldLength; b++)
						{
							tempFinalBuffer[b] = finalBuffer[b];
						}

						int pos = oldLength;
						for (int b = 0; b < buffer.length; b++)
						{
							tempFinalBuffer[pos] = buffer[b];
							pos++;
						}

						finalBuffer = tempFinalBuffer;

						payloadSize += buffer.length;
					}
					catch (IOException e)
					{
						Log.w(LOG_TAG, "IOException occured in updateListener, state=" + state);
					}

					Log.i(LOG_TAG, "time: " + (System.currentTimeMillis() - startTime));

					if (System.currentTimeMillis() - startTime > StopThreshold)
					{
						Log.d(LOG_TAG, "Payload: " + payloadSize);
						stop();
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

	public int getAverageSoundLevel(double numMiliseconds)
	{
		int result = 1;

		if (state == State.READY)
		{
			aRecorder.startRecording();
			Log.i(LOG_TAG, "Started to listen");
			state = State.RECORDING;

			byte[] tempBuffer = buffer.clone();

			float tempFloatBuffer[] = new float[3];
			int countReads = 0;
			long startTime = System.currentTimeMillis();

			while (System.currentTimeMillis() < numMiliseconds + startTime || countReads < 3)
			{
				// Log.v(TAG, "Update Listener called");
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

				// calculate progressbar value, Log10(0) = undefined!
				float tempCalc = temp;
				if (tempCalc < 10)
					tempCalc = 10;
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
				double level = (Math.log10(tempCalc) - 1d) / (2.8d) * 100; // shortened known constants

				// update progressbar
				if (dialog != null)
					dialog.setProgress((int) level);

				result += temp;
				Log.i(LOG_TAG, temp + ";" + result);
				countReads++;
			}

			result = result / countReads;

			aRecorder.stop();
			recording = false;

			if (dialog != null)
			{
				dialog.setProgress(0);
			}
			state = State.STOPPED;
		}
		else
		{
			Log.e(LOG_TAG, "start() called on illegal state");
			state = State.ERROR;
		}

		return result;
	}

	/**
	 * Stops the recording, and sets the state to STOPPED. In case of further usage, a reset is
	 * needed. Also finalizes the wave file in case of uncompressed recording.
	 */
	public void stop()
	{
		if (state == State.RECORDING)
		{
			aRecorder.stop();
			recording = false;

			if (dialog != null)
			{
				dialog.setProgress(0);
			}

			payload = payloadSize;

			try
			{
				fWriter.seek(4); // Write filesize to header
				fWriter.writeInt(Integer.reverseBytes(36 + payloadSize));

				fWriter.seek(40); // Write payload size to header
				fWriter.writeInt(Integer.reverseBytes(payloadSize));
				Log.d(LOG_TAG, "Stopped recording with payloadSize=" + payloadSize + ", was inserting? " + mIsInserting);
				// if we have been inserting, copy back the second part
				// of the file and correct the filesize values
				if (mIsInserting)
				{
					// copy back from second part
					File secondPart = new File(DICTATE_TEMP_PATH);
					File record = new File(fPath);
					appendFile(record, secondPart);
					// we should theoretically get current sizes easily from
					// our file writer
					int totalSize = (int) fWriter.length();
					int fileSize = totalSize - 8;
					int dataSize = totalSize - 44;
					Log.i(LOG_TAG, "Appended file " + secondPart + " to record, totalSize=" + totalSize);
					fWriter.seek(4); // Write filesize to header
					fWriter.writeInt(Integer.reverseBytes(fileSize));

					fWriter.seek(40); // Write payload size to header
					fWriter.writeInt(Integer.reverseBytes(dataSize));

					// also correct the payloadSize member
					payloadSize = dataSize;
				}

				Log.i(LOG_TAG, "Stopped recording, total payloadsize=" + payloadSize);

			}
			catch (IOException e)
			{
				Log.e(LOG_TAG, "I/O exception occured writing to output file in stop()");
				state = State.ERROR;
			}
			state = State.STOPPED;
		}
		else
		{
			Log.e(LOG_TAG, "stop() called on illegal state");
			state = State.ERROR;
		}
	}

	/**
	 * Returns the recorded payload length of the .wav file.
	 * 
	 * @return Size of the recorded audio file (payload part, add 44 bytes to get total filesize) in
	 *         bytes.
	 */
	public int getRecordSize()
	{
		return payloadSize;
	}

	/**
	 * Calculates the bitrate with current settings.
	 * 
	 * @return The bitrate per second.
	 */
	public int getBitsPerSecond()
	{
		return sampleRate * bitsPerSample * numChannels;
	}

	/**
	 * Appends the second file to the first one.
	 * 
	 * @param one
	 *            .
	 * @param two
	 *            .
	 * @throws IOException
	 *             If anything goes wrong writing the file.
	 */
	private void appendFile(File one, File two) throws IOException
	{
		InputStream in = new FileInputStream(two);
		OutputStream out = new FileOutputStream(one, true);
		byte[] buf = new byte[100 * 1024];
		int len;
		while ((len = in.read(buf)) >= 0)
		{
			if (len > 0)
			{
				out.write(buf, 0, len);
			}
		}
		out.flush();
		out.close();
		in.close();
	}

	public int getFrameSize()
	{
		return sampleSize;
	}

	public int getPayloadLength()
	{
		return payload;
	}

	public int readFromBuffer(byte[] buffer2, int currentPos, int sampleSize3)
	{
		int pos = currentPos;
		for (int i = 0; i < sampleSize3; i++)
		{
			buffer2[i] = finalBuffer[i+pos];
		}
		pos += sampleSize3;
		
		return pos;
	}
}
