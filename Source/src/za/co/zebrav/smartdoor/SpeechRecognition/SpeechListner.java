package za.co.zebrav.smartdoor.SpeechRecognition;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SpeechListner implements RecognitionListener
{

	private static final String LOG_TAG_SPEECH_LISTNER = "SpeechListner";
	private Context context;
	private ListView list;
	private ProgressBar progressBar;
	private ProgressBar soundLevel;

	/**
	 * 
	 * @param context
	 * @param list
	 * @param progressBar
	 * @param soundLevel
	 */
	public SpeechListner(Context context, ListView list, ProgressBar progressBar, ProgressBar soundLevel)
	{
		this.context = context;
		this.list = list;
		this.progressBar = progressBar;
		this.soundLevel = soundLevel;
	}

	/**
	 * The user has started to speak
	 */
	@Override
	public void onBeginningOfSpeech()
	{
		Log.d(LOG_TAG_SPEECH_LISTNER, "onBeginningOfSpeech");
	}

	/**
	 * More sound has been received
	 */
	@Override
	public void onBufferReceived(byte[] buffer)
	{
		Log.d(LOG_TAG_SPEECH_LISTNER, "onBufferReceived");
	}

	/**
	 * Called after the user stops speaking
	 */
	@Override
	public void onEndOfSpeech()
	{
		Log.d(LOG_TAG_SPEECH_LISTNER, "onEndOfSpeech");
	}

	/**
	 * A recognition error occurred
	 * 
	 * Error codes: 1-Network operation timed out
	 * 2-Other network related errors
	 * 3-Audio recording error
	 * 4-Server sends error status
	 * 5-Other client side errors
	 * 6-No speech input
	 * 7-No recognition result matched
	 * 8-RecognitionService busy
	 * 9-Insufficient permissions
	 */
	@Override
	public void onError(int error)
	{
		Log.d(LOG_TAG_SPEECH_LISTNER, "Speech Listner Error: " + error);

		// Let the user know about the error
		switch (error)
		{
			case 1:
				Toast.makeText(context, "Network operation timed out", Toast.LENGTH_LONG).show();
				progressBar.setVisibility(ProgressBar.GONE);
				soundLevel.setVisibility(ProgressBar.GONE);
				break;

			case 2:
				Toast.makeText(context, "Other network related errors", Toast.LENGTH_LONG).show();
				progressBar.setVisibility(ProgressBar.GONE);
				soundLevel.setVisibility(ProgressBar.GONE);
				break;

			case 3:
				Toast.makeText(context, "Audio recording error", Toast.LENGTH_LONG).show();
				progressBar.setVisibility(ProgressBar.GONE);
				soundLevel.setVisibility(ProgressBar.GONE);
				break;

			case 4:
				Toast.makeText(context, "Server sends error status", Toast.LENGTH_LONG).show();
				progressBar.setVisibility(ProgressBar.GONE);
				soundLevel.setVisibility(ProgressBar.GONE);
				break;

			case 5:
				Toast.makeText(context, "Other client side errors", Toast.LENGTH_LONG).show();
				progressBar.setVisibility(ProgressBar.GONE);
				soundLevel.setVisibility(ProgressBar.GONE);
				break;

			case 6:
				Toast.makeText(context, "Speech Listner Timed Out", Toast.LENGTH_LONG).show();
				progressBar.setVisibility(ProgressBar.GONE);
				soundLevel.setVisibility(ProgressBar.GONE);
				break;

			case 7:
				Toast.makeText(context, "No Match Found", Toast.LENGTH_LONG).show();
				progressBar.setVisibility(ProgressBar.GONE);
				soundLevel.setVisibility(ProgressBar.GONE);
				break;

			case 8:
				Toast.makeText(context, "Recognition service is busy", Toast.LENGTH_LONG).show();
				break;

			case 9:
				Toast.makeText(context, "Insufficient permissions", Toast.LENGTH_LONG).show();
				progressBar.setVisibility(ProgressBar.GONE);
				soundLevel.setVisibility(ProgressBar.GONE);
				break;
				
			default: 
				Toast.makeText(context, "I'm also not sure what happened...", Toast.LENGTH_LONG).show();
				progressBar.setVisibility(ProgressBar.GONE);
				soundLevel.setVisibility(ProgressBar.GONE);
				break;
		}
	}

	/**
	 * Reserved for adding future events
	 */
	@Override
	public void onEvent(int eventType, Bundle params)
	{
		Log.d(LOG_TAG_SPEECH_LISTNER, "onEvent");
	}

	/**
	 * Partial recognition results are available
	 */
	@Override
	public void onPartialResults(Bundle partialResults)
	{
		Log.d(LOG_TAG_SPEECH_LISTNER, "onPartialResults");
	}

	/**
	 * The user may start to speak
	 */
	@Override
	public void onReadyForSpeech(Bundle params)
	{
		Log.d(LOG_TAG_SPEECH_LISTNER, "onReadyForSpeech");
	}

	/**
	 * recognition results are read
	 */
	@Override
	public void onResults(Bundle results)
	{
		Log.d(LOG_TAG_SPEECH_LISTNER, "onResults");

		ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		list.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, data));
		progressBar.setVisibility(ProgressBar.GONE);
		soundLevel.setVisibility(ProgressBar.GONE);
	}

	/**
	 * Called when the sound level in the audio stream has changed
	 */
	@Override
	public void onRmsChanged(float rmsdB)
	{

		double temp = 10 * Math.pow(10, ((double) rmsdB / (double) 10));
		Log.d(LOG_TAG_SPEECH_LISTNER, "onRmsChanged " + rmsdB + ";" + temp);

		soundLevel.setProgress((int) temp);
	}

}
