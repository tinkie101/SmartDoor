package za.co.zebrav.smartdoor;

import za.co.zebrav.smartdoor.SpeechRecognition.SpeechListner;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SpeechToText 
{
	private static final String LOG_TAG_SPEECH_TO_TEXT_ACTIVITY = "SpeechToTextActivity";
	private SpeechRecognizer speechRecogniser;
	private boolean enableRecognition;

	private ProgressBar progressBar;
	private ProgressBar soundLevel;
	private ListView list;
	private Context context;
	
	public SpeechToText(Context context)
	{
		this.context = context;
		if (SpeechRecognizer.isRecognitionAvailable(context))
		{
			enableRecognition();
		}
		else
		{
			disableRecognition();
		}
	}
	
	private void enableRecognition()
	{
		enableRecognition = true;
		list = new ListView(context);

		speechRecogniser = SpeechRecognizer.createSpeechRecognizer(context);
		speechRecogniser.setRecognitionListener(new SpeechListner(context, list, progressBar, soundLevel));
	}
	
	private void disableRecognition()
	{
		enableRecognition = false;

		Log.d(LOG_TAG_SPEECH_TO_TEXT_ACTIVITY, "Speech Recognition not Available on this Device!");
	}
	
	public boolean getEnableRecognition()
	{
		return enableRecognition;
	}
	
	public SpeechRecognizer getspeechRecogniser()
	{
		return speechRecogniser;
	}
	
	public void listenToSpeech(View v)
	{
		if (enableRecognition)
		{
			list.setAdapter(null);
			progressBar.setVisibility(ProgressBar.VISIBLE);
			soundLevel.setVisibility(ProgressBar.VISIBLE);

			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

			intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
			
			//TODO
			intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1000);//Maybe get rid of this line, Android documentation recommends to not change the length to a user specified value
			
			Log.d(LOG_TAG_SPEECH_TO_TEXT_ACTIVITY, "Start Listening");
			speechRecogniser.startListening(intent);
		}
		else
		{
			Toast.makeText(context, "Speech Recognition not Available on this Device!", Toast.LENGTH_LONG).show();
		}
	}
	
	public void destroy()
	{
		speechRecogniser.destroy();
	}

}
