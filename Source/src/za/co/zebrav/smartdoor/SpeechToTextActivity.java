package za.co.zebrav.smartdoor;

import za.co.zebrav.smartdoor.SpeechRecognition.SpeechListner;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

public class SpeechToTextActivity extends Activity
{
	
	private static final String LOG_TAG_SPEECH_TO_TEXT_ACTIVITY = "SpeechToTextActivity";
	SpeechRecognizer speechRecogniser;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_speech_to_text);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		if(SpeechRecognizer.isRecognitionAvailable(this))
		{

			ListView list = (ListView) findViewById(R.id.speechToTextList);
			
			speechRecogniser = SpeechRecognizer.createSpeechRecognizer(this);
			
			
			speechRecogniser.setRecognitionListener(new SpeechListner(this, list));
		}
		else
		{
			Toast.makeText(this, "Speech Recognition not Available on this Device!", Toast.LENGTH_LONG).show();
			
			Button button = (Button) findViewById(R.id.listenSpeech);
			button.setEnabled(false);
			
			Log.d(LOG_TAG_SPEECH_TO_TEXT_ACTIVITY, "Speech Recognition not Available on this Device!");
		}
	}

	/**
	 * 
	 * @param item
	 * @return
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void listenToSpeech(View v)
	{
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

			intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
			
			speechRecogniser.startListening(intent);
	}
}
