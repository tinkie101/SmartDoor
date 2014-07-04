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

public class TextToSpeechActivity extends Activity
{
	private TextToSpeech tts;
	private EditText text;
	private Button btnSpeak;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.texttospeech);
		
		btnSpeak = (Button) findViewById(R.id.SpeakButton);
		text = (EditText) findViewById(R.id.ttsEditText);
		
		tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener()
		{			
			@Override
			public void onInit(int status)
			{
				tts.setLanguage(Locale.UK);
			}
		});
		
		btnSpeak.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				speakOut();
			}
		});
		
	}
	
	@Override
	public void onDestroy() 
	{
		// Don't forget to shutdown tts!
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}

	

	private void speakOut() {

		String textb = text.getText().toString();

		tts.speak(textb, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	

}
