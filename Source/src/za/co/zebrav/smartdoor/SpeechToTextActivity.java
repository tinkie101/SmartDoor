package za.co.zebrav.smartdoor;

import java.util.List;

import za.co.zebrav.smartdoor.SpeechRecognition.SpeechListner;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

public class SpeechToTextActivity extends Activity
{

	private static final String LOG_TAG_SPEECH_TO_TEXT_ACTIVITY = "SpeechToTextActivity";
	private SpeechRecognizer speechRecogniser;
	private boolean enableRecognition;

	private ProgressBar progressBar;
	private ProgressBar soundLevel;
	private ListView list;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_speech_to_text);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		list = (ListView) findViewById(R.id.speechToTextList);
		progressBar = (ProgressBar) findViewById(R.id.speech_loadingBar);
		soundLevel = (ProgressBar) findViewById(R.id.speech_soundLevel);

		if (SpeechRecognizer.isRecognitionAvailable(this))
		{
			enableRecognition();
		}
		else
		{
			disableRecognition();
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
	
	@Override
	protected void onStop()
	{
		super.onStop();
		
		//Destroy any active speechRecognisers
		speechRecogniser.destroy();
	}

	private void enableRecognition()
	{
		enableRecognition = true;
		ListView list = (ListView) findViewById(R.id.speechToTextList);

		speechRecogniser = SpeechRecognizer.createSpeechRecognizer(this);
		speechRecogniser.setRecognitionListener(new SpeechListner(this, list, progressBar, soundLevel));
	}

	private void disableRecognition()
	{
		enableRecognition = false;

		Button button = (Button) findViewById(R.id.listenSpeech);
		button.setEnabled(false);

		Log.d(LOG_TAG_SPEECH_TO_TEXT_ACTIVITY, "Speech Recognition not Available on this Device!");
	}

	/*
	 * Getter
	 */
	public boolean getEnableRecognition()
	{
		return enableRecognition;
	}

	/*
	 * Getter
	 */
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

			speechRecogniser.startListening(intent);
		}
		else
		{
			Toast.makeText(this, "Speech Recognition not Available on this Device!", Toast.LENGTH_LONG).show();
		}
	}
}
