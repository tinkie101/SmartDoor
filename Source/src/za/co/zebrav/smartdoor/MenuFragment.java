package za.co.zebrav.smartdoor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;

@SuppressLint("ValidFragment")
public class MenuFragment extends Fragment
{
	private String selectedName;
	private CustomMenu menu;
	private Activity activity;
	
	private String[] twitterOptions = {"Goto main menu", "Twitter key", "Twitter secret", "Twitter token key", "Twitter token secret", "update rate", "Exit"};
	private String[] themeOptions = {"Goto main menu","Background", "ActionBar", "Exit"};
	
	public void setup(String s, CustomMenu menu, Activity activity)
	{
		this.selectedName = s;
		this.menu = menu;
		this.activity = activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		//------------------------------------------------------------------Main Menu Options:
		if(selectedName.equals("Switch Login"))// selected login
		{
			menu.getDrawerlayout().closeDrawer(menu.getDrawerList());
			MainActivity mainActivity = new MainActivity();
			if(mainActivity.getClass().equals(getActivity().getClass()))
			{
				Intent intent = new Intent(getActivity(), ManualLogin.class);
				startActivity(intent);
			}
			
		}
		else if(selectedName.equals("Twitter prefrences"))
		{
			//menu.setDrawerListOnclickListener();
			menu.editMenuOptions(twitterOptions, "Twitter menu");
		}
		else if(selectedName.equals("Theme prefrences"))
		{
			//menu.setDrawerListOnclickListener();
			menu.editMenuOptions(themeOptions, "Theme menu");
		}
		//----------------------------------------------------------------All
		/**
		 * Exit leaves closing this app up to the operating system (when device is switched off)
		 * When one uses "Exit" the device's home button is issued. 
		 */
		else if(selectedName.equals("Exit"))
		{
			System.exit(0);
		}
		//----------------------------------------------------------------All except Main Menu Options
		else if(selectedName.equals("Goto main menu"))
		{
			String[] mainOptions = activity.getResources().getStringArray(R.array.mainMenuOptions);
			menu.editMenuOptions(mainOptions, "Menu");
		}
		//----------------------------------------------------------------Twitter specific Options:
		//----------------------------------------------------------------Theme specific Options:
		else
		{
			Toast.makeText(getActivity().getApplicationContext(),selectedName, Toast.LENGTH_LONG).show();
		}
		return null;
	}
}