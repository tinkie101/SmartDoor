package za.co.zebrav.smartdoor;

import java.util.Locale;
import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TextToSpeechActivity extends Activity implements OnInitListener
{
	//Layout objects
	private TextToSpeech tts;
	private Button speakButton;
	private EditText editText;

	@Override
	/**
	 * Find layout Objects and set onClick listeners
	 */
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.texttospeech);
		
		//Get Layout Objects
		tts = new TextToSpeech(this, this);
		speakButton = (Button) findViewById(R.id.btnSpeak);
		editText = (EditText) findViewById(R.id.editTextTextTTS);

		//Set OnclickListener for "speak" button
		speakButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View arg0)
			{
				speakOut(editText.getText().toString());
			}
		});
	}

	@Override
	/**
	 * ShutDown TextToSpeech when activity is destroyed
	 */
	public void onDestroy()
	{
		
		if (tts != null)
		{
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}

	/**
	 * Sets voice pronunciation 
	 */
	public void onInit(int status)
	{
		if (status == TextToSpeech.SUCCESS)
		{
			int result = tts.setLanguage(Locale.UK);

			if (result == TextToSpeech.LANG_MISSING_DATA|| result == TextToSpeech.LANG_NOT_SUPPORTED)
			{
				Toast.makeText(this, "Language not supported",Toast.LENGTH_LONG).show();
			} 
			else
			{
				speakButton.setEnabled(true);
			}
		}
	}

	/**
	 * @param text, The text to be spoken out loud by the device
	 */
	public void speakOut(String text)
	{
		if (text.length() == 0)
		{
			tts.speak("You haven't typed text", TextToSpeech.QUEUE_FLUSH, null);
		} else
		{
			tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}
	}
}
