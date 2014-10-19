package za.co.zebrav.smartdoor.main;

import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

public class TTS implements OnInitListener
{
	private static final String LOG_TAG = "TTS";
	private TextToSpeech tts;
	private Activity activity;
	private boolean isTalking;
	private HashMap<String, String> map;
	
	public TTS(Activity act)
	{
		this.activity = act;
		this.isTalking = false;
		tts = new TextToSpeech(act, this);

		map = new HashMap<String, String>();
		map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID");
		
		tts.setOnUtteranceProgressListener(new UtteranceProgressListener()
		{
			
			@Override
			public void onStart(String utteranceId)
			{
				Log.d(LOG_TAG, "Started to speak");
				isTalking = true;				
			}
			
			@Override
			public void onError(String utteranceId)
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDone(String utteranceId)
			{
				Log.d(LOG_TAG, "Done speaking");
				isTalking = false;
			}
		});
	}
	
	@Override
	public void onInit(int status) 
	{
		if (status == TextToSpeech.SUCCESS)
		{
			int result = tts.setLanguage(Locale.UK);

			if (result == TextToSpeech.LANG_MISSING_DATA|| result == TextToSpeech.LANG_NOT_SUPPORTED)
			{
				Toast.makeText(activity, "Language not supported",Toast.LENGTH_LONG).show();
			}
		}
	}

	public void destroy()
	{
		if (tts != null)
		{
			tts.stop();
			tts.shutdown();
		}
	}
	
	public void talk(String text)
	{
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
	}
	
	public boolean isTalking()
	{
		return isTalking;
	}
}
