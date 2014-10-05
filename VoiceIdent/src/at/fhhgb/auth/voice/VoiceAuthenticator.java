package at.fhhgb.auth.voice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Environment;
import android.util.Log;
import at.fhooe.mcm.smc.math.matrix.Matrix;
import at.fhooe.mcm.smc.math.mfcc.FeatureVector;
import at.fhooe.mcm.smc.math.mfcc.MFCC;
import at.fhooe.mcm.smc.math.vq.ClusterUtil;
import at.fhooe.mcm.smc.math.vq.Codebook;
import at.fhooe.mcm.smc.math.vq.KMeans;
import at.fhooe.mcm.smc.wav.WavReader;
import at.fhooe.mcm.smc.wav.WaveRecorder;
import at.fhooe.mcm.sms.Constants;

public class VoiceAuthenticator
{
	private static final int calibrate_time = 5000;

	private static final String TEMP_WAV_FILE = "TempVoiceAuth.wav";

	private static final String LOG_TAG = "VoiceAuthenticator";

	private static final int sampleRate = 44100;

	private WaveRecorder waveRecorder;

	private ArrayList<Codebook> codeBook;

	private File activeFile;

	private boolean isRecording;
	ProgressDialog dialog;

	public ArrayList<Codebook> getCodeBook()
	{
		return new ArrayList<Codebook>(codeBook);
	}

	public VoiceAuthenticator(Dialog dialog)
	{
		this.dialog = (ProgressDialog) dialog;

		waveRecorder = new WaveRecorder(sampleRate, this.dialog);
		codeBook = new ArrayList<Codebook>();
	}
	
	public VoiceAuthenticator()
	{
		this.dialog = null;

		waveRecorder = new WaveRecorder(sampleRate, this.dialog);
		codeBook = new ArrayList<Codebook>();
	}

	public void setCodeBook(ArrayList<Codebook> cb)
	{
		codeBook = new ArrayList<Codebook>(cb);
	}

	public boolean isRecording()
	{
		return isRecording;
	}

	public boolean hasActiveFile()
	{
		if (activeFile != null)
			return true;
		else
			return false;
	}

	public File getActiveFile()
	{
		return activeFile;
	}

	/**
	 * Start recording the wav file
	 * 
	 * @param file
	 */
	public void startRecording()
	{
		Log.d(LOG_TAG, "Started recording.");
		isRecording = true;

		activeFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), TEMP_WAV_FILE);

		// If there already exists a file with this name, delete it
		deleteActiveFile();

		waveRecorder.setOutputFile(activeFile.getAbsolutePath());
		waveRecorder.prepare();
		waveRecorder.start();
		stopRecording();
	}

	public void stopRecording()
	{
		Log.d(LOG_TAG, "Stop Recording");
		if (waveRecorder != null && isRecording)
		{
			// waveRecorder.stop();
			waveRecorder.release();
			waveRecorder.reset();

			isRecording = false;
		}

	}

	public void cancelRecording()
	{
		Log.d(LOG_TAG, "Cancel Recording");
		if (waveRecorder != null && isRecording)
		{
			waveRecorder.stop();
			waveRecorder.release();
			waveRecorder.reset();
			isRecording = false;
		}
	}

	public ArrayList<Double> identifySpeaker(FeatureVector featureVector)
	{
		ArrayList<Double> distortion = new ArrayList<Double>();

		for (int i = 0; i < codeBook.size(); i++)
		{
			double averageDistortion = ClusterUtil.calculateAverageDistortion(featureVector, codeBook.get(i));

			boolean inserted = false;

			// sort from best fit to worst fit
			for (int d = 0; d < distortion.size(); d++)
			{
				if (averageDistortion < distortion.get(d))
				{
					distortion.add(d, averageDistortion);
					inserted = true;
					break;
				}
			}
			if (!inserted)
			{
				distortion.add(averageDistortion);
			}
			Log.i(LOG_TAG, "Calculated avg distortion for user = " + averageDistortion);
		}

		return distortion;
	}

	private FeatureVector createFeatureVector(double[][] mfcc)
	{
		int vectorSize = mfcc[0].length;
		int vectorCount = mfcc.length;
		Log.i(LOG_TAG, "Creating pointlist with dimension=" + vectorSize + ", count=" + vectorCount);
		FeatureVector pl = new FeatureVector(vectorSize, vectorCount);
		for (int i = 0; i < vectorCount; i++)
		{
			pl.add(mfcc[i]);
		}
		Log.d(LOG_TAG, "Added all MFCC vectors to pointlist");
		return pl;
	}

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

	private double[][] calculateMfcc(double[] samples)
	{
		MFCC mfccCalculator = new MFCC(Constants.SAMPLERATE, Constants.WINDOWSIZE, Constants.COEFFICIENTS, false,
							Constants.MINFREQ + 1, Constants.MAXFREQ, Constants.FILTERS);

		int hopSize = Constants.WINDOWSIZE / 2;
		int mfccCount = (samples.length / hopSize) - 1;
		double[][] mfcc = new double[mfccCount][Constants.COEFFICIENTS];
		long start = System.currentTimeMillis();
		for (int i = 0, pos = 0; pos < samples.length - hopSize; i++, pos += hopSize)
		{
			mfcc[i] = mfccCalculator.processWindow(samples, pos);
			if (i % 50 == 0)
			{
				Log.i(LOG_TAG, "Calculating features..." + i + "/" + mfccCount);
			}
		}

		Log.i(LOG_TAG, "Calculated " + mfcc.length + " vectors of MFCCs in " + (System.currentTimeMillis() - start)
							+ "ms");
		return mfcc;
	}

	//TODO read in from buffer, not file
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

				if (i % 1000 == 0)
				{
					Log.i(LOG_TAG, "Reading samples..." + i + "/" + samples.length);
				}
			}
		}
		catch (IOException e)
		{
			Log.e(LOG_TAG, "Exception in reading samples", e);
		}
		return samples;
	}

	private void insertFeature(Codebook cb)
	{
		// Save password in list
		codeBook.add(cb);
	}

	private Codebook createCodebook(KMeans kmeans)
	{
		int numberClusters = kmeans.getNumberClusters();
		Matrix[] centers = new Matrix[numberClusters];
		for (int i = 0; i < numberClusters; i++)
		{
			centers[i] = kmeans.getCluster(i).getCenter();
		}
		Codebook cb = new Codebook();
		cb.setLength(numberClusters);
		cb.setCentroids(centers);
		return cb;
	}

	private KMeans doClustering(FeatureVector pl)
	{
		long start;
		KMeans kmeans = new KMeans(Constants.CLUSTER_COUNT, pl, Constants.CLUSTER_MAX_ITERATIONS);
		Log.i(LOG_TAG, "Prepared k means clustering");
		start = System.currentTimeMillis();

		kmeans.run();
		Log.i(LOG_TAG, "Clustering finished, total time = " + (System.currentTimeMillis() - start) + "ms");
		return kmeans;
	}

	public FeatureVector getCurrentFeatureVector()
	{
		FeatureVector result = null;

		if (activeFile != null && activeFile.exists())
		{
			String filename = activeFile.getAbsolutePath();
			WavReader wavReader = new WavReader(filename);

			Log.i(LOG_TAG, "Starting to read from file " + filename);
			double[] samples = readSamples(wavReader);

			Log.i(LOG_TAG, "Starting to calculate MFCC");
			double[][] mfcc = calculateMfcc(samples);

			Log.i(LOG_TAG, "Creating Feature Vector");
			result = createFeatureVector(mfcc);
		}
		else
		{
			Log.d(LOG_TAG, "Active file not set!");
		}
		return result;
	}

	public ArrayList<Double> identify(FeatureVector featureVector)
	{
		if (featureVector != null)
		{
			Log.i(LOG_TAG, "Identifying Speaker");
			ArrayList<Double> list = identifySpeaker(featureVector);
			return list;
		}
		else
		{
			Log.d(LOG_TAG, "Invalid FeatureVector!");
			return null;
		}
	}

	public boolean train()
	{
		boolean result = false;

		if (activeFile != null && activeFile.exists())
		{
			String filename = activeFile.getAbsolutePath();
			Log.d(LOG_TAG, filename);
			WavReader wavReader = new WavReader(filename);

			Log.i(LOG_TAG, "Starting to read from file " + filename);
			double[] samples = readSamples(wavReader);

			Log.i(LOG_TAG, "Starting to calculate MFCC");
			double[][] mfcc = calculateMfcc(samples);

			Log.i(LOG_TAG, "Creating Feature Vector");
			FeatureVector pl = createFeatureVector(mfcc);

			Log.i(LOG_TAG, "Do Clustering");
			KMeans kmeans = doClustering(pl);

			Log.i(LOG_TAG, "Create CodeBook");
			Codebook cb = createCodebook(kmeans);

			Log.i(LOG_TAG, "Insert Feature");
			insertFeature(cb);

			deleteActiveFile();
			Log.i(LOG_TAG, "Finished Traning.");
			result = true;
		}
		else
		{
			Log.d(LOG_TAG, "Active file not set!");
		}
		return result;
	}

	public void deleteActiveFile()
	{
		if (activeFile != null && activeFile.exists())
			activeFile.delete();
	}
	
	public void setMicThreshold(int threshold)
	{
		if(threshold > 0)
		{		
			waveRecorder.setStartThreshold(threshold);
		}
		else
		{
			threshold = 1;
		}
	}

	public int autoCalibrateActivation()
	{
		Log.i(LOG_TAG, "Calibrating mic...");
		deleteActiveFile();
		activeFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), TEMP_WAV_FILE);
		waveRecorder.setOutputFile(activeFile.getAbsolutePath());
		waveRecorder.prepare();

		int result = waveRecorder.getAverageSoundLevel(calibrate_time);

		waveRecorder.release();
		waveRecorder.reset();
		// increase
		
		if(result < 100)
		{
			result += 50;
		}
		else if(result < 500)
		{
			result += 100;
		}
		else if(result < 1000)
		{
			result += 250;
		}
		else
		{
			result += 500;
		}

		Log.i(LOG_TAG, "Average sound level: " + result);
		waveRecorder.setStartThreshold(result);

		return result;
	}
}
