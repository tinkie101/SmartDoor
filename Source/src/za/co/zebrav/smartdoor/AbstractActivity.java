package za.co.zebrav.smartdoor;

import za.co.zebrav.smartdoor.SpeechRecognition.SpeechToTextAdapter;
import za.co.zebrav.smartdoor.database.Db4oAdapter;
import za.co.zebrav.smartdoor.database.User;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

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
	protected ProgressDialog processingDialog;
	protected MainActivity mainActivity;
	
	public MainActivity getMainActivity() 
	{
		return mainActivity;
	}

	public void setMainActivity(MainActivity mainActivity) 
	{
		this.mainActivity = mainActivity;
	}

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
		activityDatabase = Db4oAdapter.getInstance(this);
		fragmentManager = getFragmentManager();
		textToSpeech = new TTS(this);
		speechToText = new SpeechToTextAdapter(this);
		this.processingDialog = new ProgressDialog(this, ProgressDialog.STYLE_HORIZONTAL);
		this.processingDialog.setMessage("Training faces.");
		this.processingDialog.setCancelable(false);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		if (!activityDatabase.isOpen())
			activityDatabase.open();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		if (activityDatabase.isOpen())
			activityDatabase.close();

		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if (!activityDatabase.isOpen())
			activityDatabase.open();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		textToSpeech.destroy();
		speechToText.destroy();
		Log.d(LOG_TAG, "destroy)");
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

	public boolean isTalking()
	{
		return textToSpeech.isTalking();
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
