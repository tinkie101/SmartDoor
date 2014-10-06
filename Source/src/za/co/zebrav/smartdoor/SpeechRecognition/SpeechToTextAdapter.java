package za.co.zebrav.smartdoor.SpeechRecognition;

import java.util.ArrayList;

import za.co.zebrav.smartdoor.AbstractActivity;
import za.co.zebrav.smartdoor.R;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SpeechToTextAdapter
{
	private static final String LOG_TAG = "SpeechToTextAdapter";
	private AbstractActivity context;
	private SpeechRecognizer speechRecogniser;
	private boolean stopListening;

	private String[] possibleCommands;

	private ProgressBar soundLevel;

	public SpeechToTextAdapter(AbstractActivity context)
	{
		this.context = context;
		stopListening = true;

		if (isAvailable())
		{
			speechRecogniser = SpeechRecognizer.createSpeechRecognizer(context);
			speechRecogniser.setRecognitionListener(new SpeechListner());
		}
		else
		{
			speechRecogniser = null;
			Log.d(LOG_TAG, "Speech Recognition not Available on this Device!");
		}
	}
	

	public void destroy()
	{
		speechRecogniser.destroy();		
	}

	public boolean isAvailable()
	{
		return SpeechRecognizer.isRecognitionAvailable(context);
	}

	public void stopListening()
	{
		if (speechRecogniser != null)
		{
			stopListening = true;
			speechRecogniser.stopListening();
			Log.d(LOG_TAG, "Stoped Listening");
		}
	}

	public void listenToSpeech(String[] possibleCommands)
	{
		if (isAvailable())
		{
			soundLevel = (ProgressBar) context.findViewById(R.id.progressBar1);
			this.possibleCommands = possibleCommands;

			// soundLevel.setVisibility(ProgressBar.VISIBLE);

			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

			intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());

			// TODO
			intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1000);// Maybe get rid
																										// of this line,
																										// Android
																										// documentation
																										// recommends to
																										// not change
																										// the length to
																										// a user
																										// specified
																										// value

			Log.d(LOG_TAG, "Start Listening");
			stopListening = false;
			speechRecogniser.startListening(intent);
		}
		else
		{
			Toast.makeText(context, "Speech Recognition not Available on this Device!", Toast.LENGTH_LONG).show();
			Log.d(LOG_TAG, "Speech Recognition not Available on this Device!");
		}
	}
	

	private String getCommand(ArrayList<String> data)
	{
		String resultCommand = null;

		for (int i = 0; i < possibleCommands.length; i++)
		{
			if (data.contains(possibleCommands[i].toLowerCase()))
			{
				resultCommand = possibleCommands[i].toLowerCase();
				break;
			}
		}

		return resultCommand;
	}

	private class SpeechListner implements RecognitionListener
	{

		private static final String LOG_TAG = "SpeechListner";

		/**
		 * The user has started to speak
		 */
		@Override
		public void onBeginningOfSpeech()
		{
			Log.d(LOG_TAG, "onBeginningOfSpeech");
		}

		/**
		 * More sound has been received
		 * It is not guaranteed that this function will be called
		 */
		@Override
		public void onBufferReceived(byte[] buffer)
		{
			Log.d(LOG_TAG, "onBufferReceived");
		}

		/**
		 * Called after the user stops speaking
		 */
		@Override
		public void onEndOfSpeech()
		{
			Log.d(LOG_TAG, "onEndOfSpeech");
			// soundLevel.setVisibility(ProgressBar.GONE);
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
			Log.d(LOG_TAG, "Speech Listner Error: " + error);

			// Let the user know about the error
			switch (error)
			{
				case 1:
					// Toast.makeText(context, "Network operation timed out", Toast.LENGTH_LONG).show();
					Log.d(LOG_TAG, "Network operation timed out");
					// soundLevel.setVisibility(ProgressBar.GONE);
					break;

				case 2:
					// Toast.makeText(context, "Other network related errors", Toast.LENGTH_LONG).show();
					Log.d(LOG_TAG, "Other network related errors");
					// soundLevel.setVisibility(ProgressBar.GONE);
					break;

				case 3:
					// Toast.makeText(context, "Audio recording error", Toast.LENGTH_LONG).show();
					Log.d(LOG_TAG, "Audio recording error");
					// soundLevel.setVisibility(ProgressBar.GONE);
					break;

				case 4:
					// Toast.makeText(context, "Server sends error status", Toast.LENGTH_LONG).show();
					Log.d(LOG_TAG, "Server sends error status");
					// soundLevel.setVisibility(ProgressBar.GONE);
					break;

				case 5:
					// Toast.makeText(context, "Other client side errors", Toast.LENGTH_LONG).show();
					Log.d(LOG_TAG, "Other client side errors");
					// soundLevel.setVisibility(ProgressBar.GONE);
					break;

				case 6:
					// Toast.makeText(context, "Speech Listner Timed Out", Toast.LENGTH_LONG).show();
					Log.d(LOG_TAG, "Speech Listner Timed Out");
					// soundLevel.setVisibility(ProgressBar.GONE);
					break;

				case 7:
					// Toast.makeText(context, "No Match Found", Toast.LENGTH_LONG).show();
					Log.d(LOG_TAG, "No Match Found");
					// soundLevel.setVisibility(ProgressBar.GONE);
					break;

				case 8:
					// Toast.makeText(context, "Recognition service is busy", Toast.LENGTH_LONG).show();
					Log.d(LOG_TAG, "Recognition service is busy");
					break;

				case 9:
					// Toast.makeText(context, "Insufficient permissions", Toast.LENGTH_LONG).show();
					Log.d(LOG_TAG, "Insufficient permissions");
					// soundLevel.setVisibility(ProgressBar.GONE);
					break;

				default:
					// Toast.makeText(context, "I'm also not sure what happened...", Toast.LENGTH_LONG).show();
					Log.d(LOG_TAG, "I'm also not sure what happened...");
					// soundLevel.setVisibility(ProgressBar.GONE);
					break;
			}
			if (!stopListening)
				listenToSpeech(possibleCommands);
		}

		/**
		 * Reserved for adding future events
		 */
		@Override
		public void onEvent(int eventType, Bundle params)
		{
			Log.d(LOG_TAG, "onEvent");
		}

		/**
		 * Partial recognition results are available
		 */
		@Override
		public void onPartialResults(Bundle partialResults)
		{
			Log.d(LOG_TAG, "onPartialResults");
		}

		/**
		 * The user may start to speak
		 */
		@Override
		public void onReadyForSpeech(Bundle params)
		{
			Log.d(LOG_TAG, "onReadyForSpeech");
		}

		/**
		 * recognition results are read
		 */
		@Override
		public void onResults(Bundle results)
		{
			Log.d(LOG_TAG, "onResults");

			ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

			for (int i = 0; i < data.size(); i++)
			{
				data.set(i, data.get(i).toLowerCase());
			}

			String command = getCommand(data);

			if (command != null)
			{
				context.getUserCommands().executeCommand(command);
				Toast.makeText(context, command, Toast.LENGTH_LONG).show();
			}
			else
			{
				Log.d(LOG_TAG, data.get(0));

				if (!stopListening)
					listenToSpeech(possibleCommands);
			}
		}

		/**
		 * Called when the sound level in the audio stream has changed
		 */
		@Override
		public void onRmsChanged(float rmsdB)
		{
			int level = 0;
			
			if(rmsdB < -1)
				level = -10;
			else
				level = (int)rmsdB;
			
			if(soundLevel != null)
			soundLevel.incrementProgressBy(level);
			else
				Log.d(LOG_TAG, "soundLevel is Null");

			Log.d(LOG_TAG, "onRmsChanged " + level);
		}

	}
}
