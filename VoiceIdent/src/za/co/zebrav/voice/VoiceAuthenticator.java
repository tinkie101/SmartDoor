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

	private VoiceRecorder voiceRecorder;

	private ArrayList<Codebook> codeBook;

	private boolean isRecording;
	ProgressDialog dialog;

	public ArrayList<Codebook> getCodeBook()
	{
		return new ArrayList<Codebook>(codeBook);
	}

	public VoiceAuthenticator(Dialog dialog)
	{
		this.dialog = (ProgressDialog) dialog;
		voiceRecorder = new VoiceRecorder(this.dialog);
		codeBook = new ArrayList<Codebook>();
	}

	public VoiceAuthenticator()
	{
		this.dialog = null;

		voiceRecorder = new VoiceRecorder(this.dialog);
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

	public void startRecording()
	{
		Log.d(LOG_TAG, "Started recording.");
		isRecording = true;

		voiceRecorder.prepare();
		voiceRecorder.startRecorder();
		stopRecording();
	}

	public void stopRecording()
	{
		Log.d(LOG_TAG, "Stop Recording");
		if (voiceRecorder != null && isRecording)
		{
			voiceRecorder.release();
			voiceRecorder.reset();

			isRecording = false;
		}
	}

	public void cancelRecording()
	{
		Log.d(LOG_TAG, "Cancel Recording");
		if (voiceRecorder != null && isRecording)
		{
			voiceRecorder.stopRecorder();
			voiceRecorder.release();
			voiceRecorder.reset();
			isRecording = false;
		}
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

	private FeatureVector mergeFeatureVector(FeatureVector featureVector, double[][] mfcc)
	{
		int vectorCount = mfcc.length;
		for (int i = 0; i < vectorCount; i++)
		{
			featureVector.add(mfcc[i]);
		}
		Log.d(LOG_TAG, "Added all MFCC vectors to pointlist");
		return featureVector;
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
		int sampleSize = voiceRecorder.getFrameSize();
		Log.d(LOG_TAG, "sampleBufferSize: " + sampleSize);

		int sampleCount = voiceRecorder.getPayloadSize() / sampleSize;
		Log.d(LOG_TAG, "sampleBufferCount: " + sampleCount);

		int windowCount = (int) Math.floor(sampleCount / Constants.WINDOWSIZE);

		double[] samples = new double[windowCount * Constants.WINDOWSIZE];
		byte[] buffer = new byte[sampleSize];
		int currentPos = 0;

		for (int i = 0; i < samples.length; i++)
		{
			currentPos = voiceRecorder.readFromBuffer(buffer, currentPos, sampleSize);
			samples[i] = createSample(buffer);
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
		Log.i(LOG_TAG, "Starting to read samples from buffer");
		double[] samples = readSamplesFromBuffer();

		Log.i(LOG_TAG, "Starting to calculate MFCC");
		double[][] mfcc = calculateMfcc(samples);

		Log.i(LOG_TAG, "Creating Feature Vector");
		FeatureVector result = createFeatureVector(mfcc);

		return result;
	}

	public float identifySpeaker(FeatureVector featureVector)
	{
		float result = -1f;
		if (featureVector != null && codeBook.size() > 0)
		{
			Log.i(LOG_TAG, "Identifying Speaker");
			result = 0f;

			for (int i = 0; i < codeBook.size(); i++)
			{
				double averageDistortion = ClusterUtil.calculateAverageDistortion(featureVector, codeBook.get(i));

				result += averageDistortion;

				Log.i(LOG_TAG, "Calculated avg distortion for user = " + averageDistortion);
			}

			result = result / codeBook.size();
		}
		else
		{
			Log.d(LOG_TAG, "Invalid FeatureVector!");
		}
		return result;
	}

	public FeatureVector train(FeatureVector featureVector)
	{
		Log.i(LOG_TAG, "Starting to samples from buffer");
		double[] samples = readSamplesFromBuffer();

		if (samples.length < 1)
		{
			Log.d(LOG_TAG, "Nothing Recored");
			return null;
		}

		Log.i(LOG_TAG, "Starting to calculate MFCC");
		double[][] mfcc = calculateMfcc(samples);

		Log.i(LOG_TAG, "Creating Feature Vector");

		if (featureVector == null)
		{
			featureVector = createFeatureVector(mfcc);
		}
		else
		{
			featureVector = mergeFeatureVector(featureVector, mfcc);
		}

		return featureVector;
	}

	public boolean finishTraining(FeatureVector featureVector)
	{
		if (featureVector != null)
		{
			Log.i(LOG_TAG, "Do Clustering");
			KMeans kmeans = doClustering(featureVector);

			Log.i(LOG_TAG, "Create CodeBook");
			Codebook cb = createCodebook(kmeans);

			Log.i(LOG_TAG, "Insert Feature");
			insertFeature(cb);

			Log.i(LOG_TAG, "Finished Traning.");
			return true;
		}
		else
		{
			return false;
		}
	}

	public void setMicThreshold(int threshold)
	{
		if (threshold > 0)
		{
			voiceRecorder.setStartThreshold(threshold);
		}
		else
		{
			threshold = 1;
		}
	}

	public int getMicThreshold()
	{
		return voiceRecorder.getStartThreshold();
	}

	public int autoCalibrateActivation()
	{
		Log.i(LOG_TAG, "Calibrating mic...");
		voiceRecorder.prepare();

		int result = voiceRecorder.getAverageSoundLevel(calibrate_time);

		voiceRecorder.release();
		voiceRecorder.reset();

		// increase
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
		voiceRecorder.setStartThreshold(result);

		return result;
	}
}
