package za.co.zebrav.smartdoor;

import za.co.zebrav.smartdoor.SpeechRecognition.SpeechToTextAdapter;
import za.co.zebrav.smartdoor.database.Db4oAdapter;
import za.co.zebrav.smartdoor.database.User;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public abstract class AbstractActivity extends Activity
{
	private static final String LOG_TAG = "AbstractActivity";
	protected User activityUser;
	protected Db4oAdapter activityDatabase;
	protected android.app.FragmentManager fragmentManager;
	protected TTS textToSpeech;
	protected SpeechToTextAdapter speechToText;
	protected UserCommands userCommands;
	protected View view;

	public void startListeningForCommands(String[] possibleCommands)
	{
		speechToText.listenToSpeech(possibleCommands);
	}

	public void stopListeningForCommands()
	{
		speechToText.stopListening();
	}

	public UserCommands getUserCommands()
	{
		return userCommands;
	}

	public User getUser()
	{
		return activityUser;
	}

	public Db4oAdapter getDatabase()
	{
		return activityDatabase;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		activityDatabase = new Db4oAdapter(this);
		fragmentManager = getFragmentManager();
		textToSpeech = new TTS(this);
		speechToText = new SpeechToTextAdapter(this);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		activityDatabase.open();
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		activityDatabase.close();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		textToSpeech.destroy();
		speechToText.destroy();
	}

	public void setActiveUser(User user)
	{
		this.activityUser = user;
	}

	/**
	 * @param text
	 *            , The text to be spoken out loud by the device
	 */
	public void speakOut(String text)
	{
		textToSpeech.talk(text);
	}

	public void saveUser()
	{
		if (activityDatabase == null)
			Log.d(LOG_TAG, "activity database is bull");

		if (activityUser == null)
			Log.d(LOG_TAG, "activityUser is bull");

		activityDatabase.save(activityUser);
		activityUser = null;
	}

}
