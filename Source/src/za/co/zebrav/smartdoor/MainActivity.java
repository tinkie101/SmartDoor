//Authors:
//Eduan Bekker - 12214834
//Zuhnja Riekert - 12040593
//Albert Volschenk - 12054519

//This is the main activity for the Smart Door Application.
package za.co.zebrav.smartdoor;


import java.util.List;

import za.co.zebrav.smartdoor.R.id;
import za.co.zebrav.smartdoor.database.Db4oAdapter;
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
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends AbstractActivity 
{
	private static final String TAG = "MainActivity";
	private CustomMenu sliderMenu;
	private ManualLoginFragment manualFrag;
	private String currentFragment = "advanced";
	private AlertDialog.Builder alert;
	private LoggedInFragment loggedInFragment;
	//private User user = null;
	private boolean loggedIn = false;
	private Fragment twitterFragment;
	private PersonRecognizer personRecognizer;
	public PersonRecognizer getPersonRecognizer()
	{
		return personRecognizer;
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
		
		//create alert
		alert = new AlertDialog.Builder(this);
		
		identifyVoiceFragment = new IdentifyVoiceFragment();
		searchCameraFragment = new SearchCameraFragment();
		switchToCamera();
	}
	
	private void loadDefaultSettings()
	{
		String PREFS_NAME = getResources().getString((R.string.settingsFileName));
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		
		//Training settings
		String face_TrainPhotoNum = settings.getString("face_TrainPhotoNum", "NOT");
		if(face_TrainPhotoNum.equals("NOT"))//not set, set
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
		    
		    editor.putString("face_faceRecognizerAlgorithm", getResources().getString((R.string.face_faceRecognizerAlgorithm)));
		    editor.commit();
		    
		    editor.putString("face_detectEyes", getResources().getString((R.string.face_detectEyes)));
		    editor.commit();
		    
		    editor.putString("face_detectNose", getResources().getString((R.string.face_detectNose)));
		    editor.commit();
		}
		
		//Twitter settings
		String twitter_Key = settings.getString("twitter_Key", "NOT");
		if(twitter_Key.equals("NOT"))//not set, set
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
		
		//serverSettings
		String server_IP = settings.getString("server_IP", "NOT");
		if(server_IP.equals("NOT"))//not set, set
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
		String settingsFile = getResources().getString(R.string.settingsFileName);
		int photosPerPerson = Integer.parseInt(getSharedPreferences(settingsFile, 0).getString(
							"face_TrainPhotoNum", "5"));
		int algorithm = Integer.parseInt(getSharedPreferences(settingsFile, 0).getString(
							"face_faceRecognizerAlgorithm", "1"));
		int threshold = Integer.parseInt(getSharedPreferences(settingsFile, 0).getString(
							"face_recognizerThreshold", "0"));
		personRecognizer = new PersonRecognizer(this, photosPerPerson, algorithm, threshold);
		if(!loggedIn)
		{
			//user = null;
			this.currentFragment = "advanced";
			this.switchToCamera();
		}
	}
	
	/**
	 * When the user presses the toggle login button, the fragment part underneath the button must change.
	 * When the current fragment is the advanced part (camera or voice authentication), it switches to a manual login
	 * and vice versa
	 * @param v
	 */
	public void switchLogin(View v)
	{
		Button button = (Button) findViewById(id.switchLoginButton);
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		
		if(button.getText().equals("Logout") || currentFragment.equals("manual"))
		{
			button.setText("Switch Login");
			searchCameraFragment = new SearchCameraFragment();
			fragmentTransaction.replace(R.id.layoutToReplaceFromMain , searchCameraFragment);
			currentFragment = "advanced";
		}
		else if(this.currentFragment.equals("advanced"))
		{
			manualFrag = new ManualLoginFragment();
			fragmentTransaction.replace(R.id.layoutToReplaceFromMain , manualFrag);
			currentFragment = "manual";
		}
		
		fragmentTransaction.commit();
	}
	
	public void logout()
	{
		if(loggedInFragment != null)
			loggedInFragment.logout();
		Button button = (Button) findViewById(id.switchLoginButton);
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		button.setText("Switch Login");
		searchCameraFragment = new SearchCameraFragment();
		fragmentTransaction.replace(R.id.layoutToReplaceFromMain , searchCameraFragment);
		currentFragment = "advanced";
		fragmentTransaction.commit();
	}
	
	private void changeOnlyButtonText(String text)
	{
		Button button = (Button) findViewById(id.switchLoginButton);
		button.setText(text);
	}
	
	public void switchToLoggedInFrag()
	{
		changeOnlyButtonText("Logout");
		loggedInFragment = new LoggedInFragment();

		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.layoutToReplaceFromMain , loggedInFragment);
		fragmentTransaction.commit();
		this.loggedIn = true;
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
		searchCameraFragment = new SearchCameraFragment();

		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.layoutToReplaceFromMain , searchCameraFragment);
		fragmentTransaction.commit();
		this.loggedIn = false;
	}
	
	public void switchToVoice(int id)
	{
		List<Object> users = getDatabase().load(new User(null, null, null, null, null, id, null));
		activityUser = (User) users.get(0);
		
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.layoutToReplaceFromMain , identifyVoiceFragment);
		fragmentTransaction.commit();
	}
	
	public void switchToTwitterSetup()
	{
		TwitterSetupFragment t = new TwitterSetupFragment();

		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.layoutToReplaceFromMain , t);
		fragmentTransaction.commit();
	}
	
	public void switchToSettingsFragment()
	{
		SettingsFragment t = new SettingsFragment();

		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.layoutToReplaceFromMain , t);
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

	//-------------------------------------------------------------------------------------Menu
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
