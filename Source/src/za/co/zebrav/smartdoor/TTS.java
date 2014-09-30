package za.co.zebrav.smartdoor;

import java.util.Locale;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.Toast;

public class TTS implements OnInitListener
{
	private TextToSpeech tts;
	private Activity activity;
	
	public TTS(Activity act)
	{
		this.activity = act;
		tts = new TextToSpeech(act, this);
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
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
}
