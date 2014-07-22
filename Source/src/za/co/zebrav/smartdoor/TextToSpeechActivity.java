package za.co.zebrav.smartdoor;

import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TextToSpeechActivity extends Activity implements OnInitListener
{
	private TextToSpeech tts;
	private Button speakButton;
	private EditText editText;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.texttospeech);
		tts = new TextToSpeech(this, this);
		speakButton = (Button) findViewById(R.id.btnSpeak);
		editText = (EditText) findViewById(R.id.txtText);

		speakButton.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View arg0)
			{
				speakOut();
			}
		});
	}

	@Override
	public void onDestroy()
	{
		// Don't forget to shutdown!
		if (tts != null)
		{
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}

	public void onInit(int status)
	{
		// TODO Auto-generated method stub

		if (status == TextToSpeech.SUCCESS)
		{

			int result = tts.setLanguage(Locale.US);

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED)
			{
				Toast.makeText(this, "Language not supported",
						Toast.LENGTH_LONG).show();
				Log.e("TTS", "Language is not supported");
			} else
			{
				speakButton.setEnabled(true);

			}

		} else
		{
			Log.e("TTS", "Initilization Failed");
		}

	}

	private void speakOut()
	{
		String text = editText.getText().toString();
		if (text.length() == 0)
		{
			tts.speak("You haven't typed text", TextToSpeech.QUEUE_FLUSH, null);
		} else
		{
			tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}

	}
}
