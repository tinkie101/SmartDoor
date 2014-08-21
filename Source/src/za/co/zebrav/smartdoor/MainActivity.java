//Authors:
//Eduan Bekker - 12214834
//Zuhnja Riekert - 12040593
//Albert Volschenk - 12054519

//This is the main activity for the Smart Door Application.
package za.co.zebrav.smartdoor;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class MainActivity extends FragmentActivity
{
	CustomMenu sliderMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// add slider menu
		sliderMenu = new CustomMenu(this, (ListView) findViewById(R.id.drawer_list), (DrawerLayout) findViewById(R.id.drawer_layout), 
				getResources().getStringArray(R.array.mainMenuOptions));
	}

	/*
	 * On click button handler.
	 * Go to the TwitterActivity.
	 */
	public void gotoTextToSpeech(View v)
	{
		Intent intent = new Intent(this, TextToSpeechActivity.class);
		startActivity(intent);
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
	
	public void gotoVoiceIdentification(View v)
	{
		Intent intent = new Intent(this, VoiceIdentificationActivity.class);
		startActivity(intent);
	}
	public void gotoFdActivity(View v)
	{
		Intent intent = new Intent(this, za.co.zebrav.facerecognition.FdActivity.class);
		startActivity(intent);
	}
	

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
