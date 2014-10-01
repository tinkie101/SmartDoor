//Authors:
//Eduan Bekker - 12214834
//Zuhnja Riekert - 12040593
//Albert Volschenk - 12054519

//This is the main activity for the Smart Door Application.
package za.co.zebrav.smartdoor;


import java.util.Locale;

import za.co.zebrav.facerecognition.SearchCameraFragment;
import za.co.zebrav.smartdoor.R.id;
import za.co.zebrav.smartdoor.database.User;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity 
{
	private static final String TAG = "MainActivity";
	private CustomMenu sliderMenu;
	private android.app.FragmentManager fm;
	private android.app.FragmentTransaction ft;
	private ManualLoginFragment manualFrag;
	private String currentFragment = "advanced";
	private AlertDialog.Builder alert;
	private TTS tts;
	//private User user = null;
	private boolean loggedIn = false;
	private TwitterFragment twitterFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.activity_main);

		switchToTwitterFragment();
		
		tts = new TTS(this);
		
		// add slider menu
		sliderMenu = new CustomMenu(this, (ListView) findViewById(R.id.drawer_list),
							(DrawerLayout) findViewById(R.id.drawer_layout), getResources().getStringArray(
												R.array.mainMenuOptions));
		
		//create alert and textToSpeech
		alert = new AlertDialog.Builder(this);
		
		identifyVoiceFragment = new IdentifyVoiceFragment();
		searchCameraFragment = new SearchCameraFragment();
		switchToCamera();
	}
	
	protected void onResume()
	{
		super.onResume();
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
		fm = getFragmentManager();
		ft = fm.beginTransaction();
		
		if(button.getText().equals("Logout") || currentFragment.equals("manual"))
		{
			button.setText("Switch Login");
			searchCameraFragment = new SearchCameraFragment();
			ft.replace(R.id.layoutToReplaceFromMain , searchCameraFragment);
			currentFragment = "advanced";
		}
		else if(this.currentFragment.equals("advanced"))
		{
			manualFrag = new ManualLoginFragment();
			ft.replace(R.id.layoutToReplaceFromMain , manualFrag);
			currentFragment = "manual";
		}
		
		ft.commit();
	}
	
	/**
	 * Function called when Login button from the manual login fragment is pressed.
	 * @param v
	 * @throws InterruptedException
	 */
	public void pressedLoginButton(View v)
	{
		User user = null;
		if(!currentFragment.equals("advanced"))
			user = manualFrag.getUser();
		
		if(user == null)
		{
			alertMessage("Incorrect username or password.");
		}
		else
		{	
			switchToLoggedInFrag(user.getID());
			changeOnlyButtonText("Logout");
		}
	}
	
	private void changeOnlyButtonText(String text)
	{
		Button button = (Button) findViewById(id.switchLoginButton);
		button.setText(text);
	}
	
	public void switchToLoggedInFrag(int id)
	{
		LoggedInFragment t = new LoggedInFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("id", id);
		t.setArguments(bundle);
		fm = getFragmentManager();
		ft = fm.beginTransaction();
		ft.replace(R.id.layoutToReplaceFromMain , t);
		ft.commit();
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
		fm = getFragmentManager();
		ft = fm.beginTransaction();
		ft.replace(R.id.layoutToReplaceFromMain , searchCameraFragment);
		ft.commit();
		this.loggedIn = false;
	}
	
	public void switchToVoice(int id)
	{
		Bundle bundle = new Bundle();
		bundle.putInt("userID", id);
		identifyVoiceFragment.setArguments(bundle);
		fm = getFragmentManager();
		ft = fm.beginTransaction();
		ft.replace(R.id.layoutToReplaceFromMain , identifyVoiceFragment);
		ft.commit();
	}
	
	public void switchToTwitterSetup()
	{
		TwitterSetupFragment t = new TwitterSetupFragment();
		fm = getFragmentManager();
		ft = fm.beginTransaction();
		ft.replace(R.id.layoutToReplaceFromMain , t);
		ft.commit();
	}
	
	public void switchToSettingsFragment()
	{
		SettingsFragment t = new SettingsFragment();
		fm = getFragmentManager();
		ft = fm.beginTransaction();
		ft.replace(R.id.layoutToReplaceFromMain , t);
		ft.commit();
	}
	
	public void switchToTwitterFragment()
	{
		android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
	    android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	    twitterFragment = new TwitterFragment();
	    fragmentTransaction.replace(R.id.twitterFragment, twitterFragment);
	    fragmentTransaction.commit();
	}
	
	public void tryTwitter()
	{
		twitterFragment.tryTwitter();
	}
	
	/*
	 * On click button handler.
	 * Go to the SpeechToText.
	 */
	public void gotoSpeechToText(View v)
	{
		Intent intent = new Intent(this, SpeechToTextActivity.class);
		startActivity(intent);
	}
	//-------------------------------------------------------------------------------------Speech to text
	
	//-------------------------------------------------------------------------------------Text to speech
	@Override
	/**
	 * ShutDown TextToSpeech when activity is destroyed
	 */
	public void onDestroy()
	{	
		tts.destroy();
		super.onDestroy();
	}
	
	/**
	 * @param text, The text to be spoken out loud by the device
	 */
	public void speakOut(String text)
	{
		tts.talk(text);
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
