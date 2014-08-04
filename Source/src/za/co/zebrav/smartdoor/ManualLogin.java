package za.co.zebrav.smartdoor;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class ManualLogin extends Activity
{
	CustomMenu sliderMenu;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.manual_login);
		super.onCreate(savedInstanceState);
		
		// add slider menu
		sliderMenu = new CustomMenu(this, (ListView) findViewById(R.id.drawer_list), (DrawerLayout) findViewById(R.id.drawer_layout), 
				getResources().getStringArray(R.array.manualLoginMenuOptions));
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
