//Authors:
//Eduan Bekker - 12214834
//Zuhnja Riekert - 12040593
//Albert Volschenk - 12054519

//This is the main activity for the Smart Door Application.
package za.co.zebrav.smartdoor;

import java.util.List;

import za.co.zebrav.smartdoor.R.id;
import za.co.zebrav.smartdoor.database.User;
import za.co.zebrav.smartdoor.facerecognition.PersonRecognizer;
import za.co.zebrav.smartdoor.facerecognition.SearchCameraFragment;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AbstractActivity
{
	private static final String TAG = "MainActivity";
	private long back_pressed;
	private CustomMenu sliderMenu;
	private ManualLoginFragment manualFrag;
	private String currentFragment = "advanced";
	private AlertDialog.Builder alert;
	private LoggedInFragment loggedInFragment;
	private boolean loggedIn = false;
	private Fragment twitterFragment;
	private PersonRecognizer personRecognizer;
	private CountDownTimer logoutTimer;
	private float currentBrightness = 0.0f;

	public CountDownTimer getLogoutTimer()
	{
		return logoutTimer;
	}

	private CountDownTimer brightnessTimer;

	public CountDownTimer getBrightnessTimer()
	{
		return brightnessTimer;
	}

	public PersonRecognizer getPersonRecognizer()
	{
		return personRecognizer;
	}

	public void setBrightness(float bright)
	{
		if (currentBrightness == bright)
			return;
		if (bright == 1.0f)
		{
			if(personRecognizer.canPredict())
			{
				speakOut("Hello. Starting to recognise faces.");
			}
			else
			{
				speakOut("Hello. Not enough users in database to recognise on. Please log in as an admin user and add more users.");
			}
		}
		WindowManager.LayoutParams layout = getWindow().getAttributes();
		layout.screenBrightness = bright;
		getWindow().setAttributes(layout);
		currentBrightness = bright;
	}

	public float getCurrentBrightness()
	{
		return currentBrightness;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.activity_main);

		loadDefaultSettings();

		switchToTwitterFragment();

		userCommands = new UserCommands(this);

		// add slider menu
		sliderMenu = new CustomMenu(this, (ListView) findViewById(R.id.drawer_list),
							(DrawerLayout) findViewById(R.id.drawer_layout), getResources().getStringArray(
												R.array.mainMenuOptions));

		// create alert
		alert = new AlertDialog.Builder(this);

		identifyVoiceFragment = new IdentifyVoiceFragment();
		searchCameraFragment = new SearchCameraFragment(-1);
		logoutTimer = new CountDownTimer(60000, 10000)
		{
			public void onFinish()
			{
				speakOut("Logging out due to inactivity.");
				logout();
			}

			@Override
			public void onTick(long millisUntilFinished)
			{
				Log.d(TAG, "Time to logout:" + millisUntilFinished);

			}
		};
		brightnessTimer = new CountDownTimer(30000, 10000)
		{
			public void onFinish()
			{
				setBrightness(0.1f);
			}

			@Override
			public void onTick(long millisUntilFinished)
			{
				Log.d(TAG, "Time to dimm:" + millisUntilFinished);

			}
		}.start();
		switchToCamera();
	}

	@Override
	public void onUserInteraction()
	{
		super.onUserInteraction();
		setBrightness(1.0f);
		brightnessTimer.cancel();
		brightnessTimer.start();
		if (loggedIn)
		{
			logoutTimer.cancel();
			logoutTimer.start();
		}
	}

	@Override
	public void onBackPressed()
	{
		if (back_pressed + 2000 > System.currentTimeMillis())
			super.onBackPressed();
		else
			Toast.makeText(getBaseContext(), "Press again to exit", Toast.LENGTH_SHORT).show();
		back_pressed = System.currentTimeMillis();
	}

	private void loadDefaultSettings()
	{
		String PREFS_NAME = getResources().getString((R.string.settingsFileName));
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

		// Face settings
		String face_TrainPhotoNum = settings.getString("face_TrainPhotoNum", "NOT");
		if (face_TrainPhotoNum.equals("NOT"))// not set, set
		{
			SharedPreferences.Editor editor = settings.edit();

			editor.putString("face_TrainPhotoNum", getResources().getString((R.string.face_TrainPhotoNum)));
			editor.commit();

			editor.putString("face_RecogPhotoNum", getResources().getString((R.string.face_RecogPhotoNum)));
			editor.commit();

			editor.putString("face_recognizerThreshold", getResources().getString((R.string.face_recognizerThreshold)));
			editor.commit();

			editor.putString("face_ImageScale", getResources().getString((R.string.face_ImageScale)));
			editor.commit();

			editor.putString("face_faceRecognizerAlgorithm",
								getResources().getString((R.string.face_faceRecognizerAlgorithm)));
			editor.commit();

			editor.putString("face_detectEyes", getResources().getString((R.string.face_detectEyes)));
			editor.commit();

			editor.putString("face_detectNose", getResources().getString((R.string.face_detectNose)));
			editor.commit();

			editor.putString("face_resolution", getResources().getString((R.string.face_resolution)));
			editor.commit();

			Log.d("missing", "main: " + getResources().getString((R.string.face_GroupRectangleThreshold)));
			editor.putString("face_GroupRectangleThreshold",
								getResources().getString((R.string.face_GroupRectangleThreshold)));
			editor.commit();
		}

		// Twitter settings
		String twitter_Key = settings.getString("twitter_Key", "NOT");
		if (twitter_Key.equals("NOT"))// not set, set
		{
			SharedPreferences.Editor editor = settings.edit();

			editor.putString("twitter_Key", getResources().getString((R.string.twitter_Key)));
			editor.commit();

			editor.putString("twitter_Secret", getResources().getString((R.string.twitter_Secret)));
			editor.commit();

			editor.putString("twitter_TokenKey", getResources().getString((R.string.twitter_TokenKey)));
			editor.commit();

			editor.putString("twitter_TokenSecret", getResources().getString((R.string.twitter_TokenSecret)));
			editor.commit();
		}

		// serverSettings
		String server_IP = settings.getString("server_IP", "NOT");
		if (server_IP.equals("NOT"))// not set, set
		{
			SharedPreferences.Editor editor = settings.edit();

			editor.putString("server_IP", getResources().getString((R.string.server_IP)));
			editor.commit();

			editor.putString("server_Port", getResources().getString((R.string.server_Port)));
			editor.commit();
		}
	}

	protected void onResume()
	{
		super.onResume();
		back_pressed = System.currentTimeMillis();
		String settingsFile = getResources().getString(R.string.settingsFileName);
		int photosPerPerson = Integer.parseInt(getSharedPreferences(settingsFile, 0).getString("face_TrainPhotoNum",
							"5"));
		int algorithm = Integer.parseInt(getSharedPreferences(settingsFile, 0).getString(
							"face_faceRecognizerAlgorithm", "1"));
		int threshold = Integer.parseInt(getSharedPreferences(settingsFile, 0).getString("face_recognizerThreshold",
							"0"));
		personRecognizer = new PersonRecognizer(this, photosPerPerson, algorithm, threshold);
		if (!loggedIn)
		{
			// user = null;
			this.currentFragment = "advanced";
			this.switchToCamera();
		}
		setBrightness(1.0f);
	}

	/**
	 * When the user presses the toggle login button, the fragment part underneath the button must change.
	 * When the current fragment is the advanced part (camera or voice authentication), it switches to a manual login
	 * and vice versa
	 * 
	 * @param v
	 */
	public void switchLogin(View v)
	{
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		if (currentFragment.equals("manual"))
		{
			searchCameraFragment = new SearchCameraFragment(-1);
			fragmentTransaction.replace(R.id.layoutToReplaceFromMain, searchCameraFragment);
			currentFragment = "advanced";
		}
		else if (this.currentFragment.equals("advanced"))
		{
			manualFrag = new ManualLoginFragment();
			fragmentTransaction.replace(R.id.layoutToReplaceFromMain, manualFrag);
			currentFragment = "manual";
		}

		fragmentTransaction.commit();
	}

	public void logout()
	{
		Log.d(TAG, "Logout called here.");
		loggedIn = false;
		logoutTimer.cancel();
		brightnessTimer.start();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		searchCameraFragment = new SearchCameraFragment(-1);
		fragmentTransaction.replace(R.id.layoutToReplaceFromMain, searchCameraFragment);
		currentFragment = "advanced";
		fragmentTransaction.commit();
		Button button = (Button) findViewById(id.switchLoginButton);
		button.setVisibility(View.VISIBLE);
		ProgressBar soundLevel = (ProgressBar) findViewById(R.id.progressBar1);
		soundLevel.setProgress(0);
	}

	public void switchToLoggedInFrag()
	{
		Log.d(TAG, "switchToLoggedIn");
		Button button = (Button) findViewById(R.id.switchLoginButton);
		button.setVisibility(View.GONE);
		loggedInFragment = new LoggedInFragment();

		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.layoutToReplaceFromMain, loggedInFragment);
		fragmentTransaction.commit();
		this.loggedIn = true;
		logoutTimer.start();
		Log.d(TAG, "Started logout timer switchtologgedinfrag");
		brightnessTimer.cancel();
	}

	/**
	 * Alerts the specified message in dialogue box.
	 */
	public void alertMessage(String message)
	{
		alert.setTitle("Alert").setMessage(message).setNeutralButton("OK", null).show();
	}

	IdentifyVoiceFragment identifyVoiceFragment;
	SearchCameraFragment searchCameraFragment;

	public void switchToCamera()
	{
		searchCameraFragment = new SearchCameraFragment(-1);

		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.layoutToReplaceFromMain, searchCameraFragment);
		fragmentTransaction.commit();
		this.loggedIn = false;
	}

	public void switchToVoice(int id)
	{
		List<Object> users = getDatabase().load(new User(null, null, null, null, null, id, null));
		activityUser = (User) users.get(0);

		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.layoutToReplaceFromMain, identifyVoiceFragment);
		fragmentTransaction.commit();
	}

	public void switchToSettingsFragment()
	{
		SettingsFragment t = new SettingsFragment();

		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.layoutToReplaceFromMain, t);
		fragmentTransaction.commit();
	}

	public void switchToTwitterFragment()
	{
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		twitterFragment = new TwitterFragment();
		fragmentTransaction.replace(R.id.twitterFragment, twitterFragment);
		fragmentTransaction.commit();
	}

	public void tryTwitter()
	{
		((TwitterFragment) twitterFragment).tryTwitter();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		brightnessTimer.cancel();
		if (loggedIn)
			logoutTimer.cancel();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		if (loggedIn)
		{
			logoutTimer.start();
			Log.d(TAG, "Started logout timer onStart");
		}
		else
		{
			brightnessTimer.start();
		}
	}

	// -------------------------------------------------------------------------------------Menu
	@Override
	protected void onPostResume()
	{
		super.onPostResume();
		// Sync the toggle state after onRestoreInstanceState has occurred.
		sliderMenu.getDrawerToggle().syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		sliderMenu.getDrawerToggle().onConfigurationChanged(newConfig);
	}

	/**
	 * This is needed to produce the image at the top left position Responsible
	 * for 3 lines image and its movement on opening the menu
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		sliderMenu.getDrawerToggle().syncState();
	}

	/**
	 * Needed for top left button to respond (menu to open) on touch
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (sliderMenu.getDrawerToggle().onOptionsItemSelected(item))
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
