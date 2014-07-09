package za.co.zebrav.smartdoor.SpeechRecognition;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SpeechListner implements RecognitionListener
{

	private static final String LOG_TAG_SPEECH_LISTNER = "SpeechListner";
	private Context context;
	private ListView list;
	
	public SpeechListner(Context context, ListView list)
	{
		this.context = context;
		this.list = list;
	}

	@Override
	public void onBeginningOfSpeech()
	{
		Log.d(LOG_TAG_SPEECH_LISTNER, "onBeginningOfSpeech");
	}

	@Override
	public void onBufferReceived(byte[] buffer)
	{
		Log.d(LOG_TAG_SPEECH_LISTNER, "onBufferReceived");
	}

	@Override
	public void onEndOfSpeech()
	{
		Log.d(LOG_TAG_SPEECH_LISTNER, "onEndOfSpeech");
	}

	@Override
	public void onError(int error)
	{
		Log.d(LOG_TAG_SPEECH_LISTNER, "Speech Listner Error: " + error);
	}

	@Override
	public void onEvent(int eventType, Bundle params)
	{
		Log.d(LOG_TAG_SPEECH_LISTNER, "onEvent");
	}

	@Override
	public void onPartialResults(Bundle partialResults)
	{
		Log.d(LOG_TAG_SPEECH_LISTNER, "onPartialResults");
	}

	@Override
	public void onReadyForSpeech(Bundle params)
	{
		Log.d(LOG_TAG_SPEECH_LISTNER, "onReadyForSpeech");
	}

	@Override
	public void onResults(Bundle results)
	{
		Log.d(LOG_TAG_SPEECH_LISTNER, "onResults");
		
		ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		
		list.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1 ,data));
	}

	@Override
	public void onRmsChanged(float rmsdB)
	{
		Log.d(LOG_TAG_SPEECH_LISTNER, "onRmsChanged");
	}

}
