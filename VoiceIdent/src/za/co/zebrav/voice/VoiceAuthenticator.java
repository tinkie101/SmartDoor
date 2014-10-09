package za.co.zebrav.voice;

import java.util.ArrayList;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.util.Log;
import at.fhooe.mcm.smc.math.matrix.Matrix;
import at.fhooe.mcm.smc.math.mfcc.FeatureVector;
import at.fhooe.mcm.smc.math.mfcc.MFCC;
import at.fhooe.mcm.smc.math.vq.ClusterUtil;
import at.fhooe.mcm.smc.math.vq.Codebook;
import at.fhooe.mcm.smc.math.vq.KMeans;
import at.fhooe.mcm.sms.Constants;

public class VoiceAuthenticator
{
	private static final String LOG_TAG = "VoiceAuthenticator";

	private static final int calibrate_time = 5000;
	private static final int sampleRate = 44100;

	private VoiceRecorder recorder;
	private ArrayList<Codebook> codeBook;

	ProgressDialog dialog;

	public VoiceAuthenticator(Dialog dialog)
	{
		this.dialog = (ProgressDialog) dialog;

		recorder = new VoiceRecorder(sampleRate, this.dialog);
		codeBook = new ArrayList<Codebook>();
	}

	public VoiceAuthenticator()
	{
		this.dialog = null;

		recorder = new VoiceRecorder(sampleRate, this.dialog);
		codeBook = new ArrayList<Codebook>();
	}

	public ArrayList<Codebook> getCodeBook()
	{
		return new ArrayList<Codebook>(codeBook);
	}

	public void setCodeBook(ArrayList<Codebook> cb)
	{
		codeBook = new ArrayList<Codebook>(cb);
	}

	public boolean isRecording()
	{
		return recorder.isRecording();
	}


	public void doRecording()
	{
		Log.d(LOG_TAG, "Started recording.");
		recorder.startRecorder();
	}
	
	public void cancelRecording()
	{
		recorder.cancelRecorder();
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

	private double[] readSamplesFromBuffer()
	{
		// TODO check if recorder is in correct state
		int sampleSize = recorder.getFrameSize();
		Log.d(LOG_TAG, "sampleBufferSize: " + sampleSize);

		int sampleCount = recorder.getPayloadSize() / sampleSize;
		Log.d(LOG_TAG, "sampleBufferCount: " + sampleCount);

		int windowCount = (int) Math.floor(sampleCount / Constants.WINDOWSIZE);

		double[] samples = new double[windowCount * Constants.WINDOWSIZE];
		byte[] buffer = new byte[sampleSize];
		int currentPos = 0;

		for (int i = 0; i < samples.length; i++)
		{
			currentPos = recorder.readFromBuffer(buffer, currentPos, sampleSize);
			samples[i] = createSample(buffer);
		}

		Log.i(LOG_TAG, samples.length + " samples loaded into memory");
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
		// TODO check if vector exists

		double[] samples = readSamplesFromBuffer();

		Log.i(LOG_TAG, "Starting to calculate MFCC");
		double[][] mfcc = calculateMfcc(samples);

		Log.i(LOG_TAG, "Creating Feature Vector");
		result = createFeatureVector(mfcc);

		return result;
	}

	public float getAverageFeatureDistance(FeatureVector featureVector)
	{
		float result = -1f;
		if (featureVector != null)
		{
			result = 0;
			Log.i(LOG_TAG, "Calculating average feature vector distance");
			for (int i = 0; i < codeBook.size(); i++)
			{
				double averageDistortion = ClusterUtil.calculateAverageDistortion(featureVector, codeBook.get(i));
				
				result += averageDistortion;
			}

			result = result / (float)codeBook.size();
		}
		else
		{
			Log.i(LOG_TAG, "Invalid FeatureVector!");
		}

		return result;
	}

	public boolean train()
	{
		// TODO check valid recording

		double[] samples = readSamplesFromBuffer();

		if (samples.length < 1)
		{
			Log.d(LOG_TAG, "Nothing Recored");
			return false;
		}

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

		Log.i(LOG_TAG, "Finished Traning.");

		return true;
	}

	public void setMicThreshold(int threshold)
	{
		if (recorder != null)
		{
			if (threshold > 0)
			{
				recorder.setStartThreshold(threshold);
			}
			else
			{
				threshold = 1;
			}
		}
		else
		{
			Log.i(LOG_TAG, "Invalid recorder for auto mic threshold");
		}
	}

	public int getMicThreshold()
	{
		if (recorder != null)
		{
			return recorder.getStartThreshold();
		}
		else
		{
			Log.i(LOG_TAG, "Invalid recorder for auto mic threshold");
			return -1;
		}
	}

	public int autoCalibrateActivation()
	{
		Log.i(LOG_TAG, "Calibrating mic...");

		int result = recorder.getAverageSoundLevel(calibrate_time);

		// increase for voice
		if (result < 100)
		{
			result += 50;
		}
		else if (result < 500)
		{
			result += 100;
		}
		else if (result < 1000)
		{
			result += 250;
		}
		else
		{
			result += 500;
		}

		Log.i(LOG_TAG, "Average sound level: " + result);
		recorder.setStartThreshold(result);

		return result;
	}
}
