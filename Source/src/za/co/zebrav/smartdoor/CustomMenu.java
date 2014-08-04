package za.co.zebrav.smartdoor;

import za.co.zebrav.smartdoor.users.AddUserActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * This class allows one to add a slider menu to an activity
 */
public class CustomMenu
{
	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ActionBarDrawerToggle drawerToggle;
	private String title = "";
	private Activity activity;
	private String[] options;
	
	private ArrayAdapter<String> adapter;
	
	//menu content related variable declarations:
	private String[] twitterOptions = {"Goto main menu", "Twitter key", "Twitter secret", "Twitter token key", "Twitter token secret", "update rate", "Exit"};
	private String[] themeOptions = {"Goto main menu","Background", "ActionBar", "Exit"};
	private String[] userOptions = {"Goto main menu", "Add user", "Search User", "Delete User", "Exit"};
	private AlertDialog.Builder alert;
	
	/**
	 * @param activity - the activity using the sliderMenu
	 * @param drawerList - specified drawerList to be used
	 * @param drawerLayout - specified drawerLayout to be used
	 */
	public CustomMenu(Activity activity, ListView drawerList, DrawerLayout drawerLayout, String[] options)
	{
		this.options = options;
		this.alert  = new AlertDialog.Builder(activity);
		
		setActivity(activity);
		setTitle((String)activity.getTitle());
		setDrawerList(drawerList);
		setDrawerLayout(drawerLayout);
		setDrawerToggle();
		
		setupSettings();
		setDrawerListOnclickListener();
	}
	
	/**
	 * Setup settings is used to:
	 * 	- set the toggle to the drawerLayout
	 *  - initialize a new adapter and set it to the drawerList
	 * @param baseContext - base context of activity using this custom slider menu
	 * @param stringArray - content to be displayed on the menu as menu options
	 */
	public void setupSettings()
	{
		drawerLayout.setDrawerListener(drawerToggle);
		adapter = new ArrayAdapter<String>(activity.getBaseContext(), R.layout.drawer_list_item, options);
		drawerList.setAdapter(adapter);
	}
	
	/**
	 * this is used to change the options available in menu
	 * @param stringArray - menu options displayed in slide menu
	 */
	public void editMenuOptions(String[] stringArray, String title)
	{
		adapter = new ArrayAdapter<String>(activity.getBaseContext(), R.layout.drawer_list_item, stringArray);
		activity.getActionBar().setTitle(title);
		drawerList.setAdapter(adapter);
	}
	
	/**
	 * @param fragmentManager - fragmentManager of the activity using this custom slider menu
	 */
	public void setDrawerListOnclickListener()
	{
		// Setting item click listener for the listview mDrawerList
		drawerList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			/**
			 * Get selected item data and safe in fragment
			 * Then commit fragment transaction using fragment manager
			 */
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{	
				String selectedName = adapter.getItem(position);
				
				//------------------------------------------------------------------Main Menu Options:
				if(selectedName.equals("Switch Login"))// selected login
				{
					//If current activity is main activity then go to classic login activity
					getDrawerlayout().closeDrawer(getDrawerList());
					MainActivity mainActivity = new MainActivity();
					if(mainActivity.getClass().equals(getActivity().getClass()))
					{
						Intent intent = new Intent(getActivity(), ManualLogin.class);
						activity.startActivity(intent);
					}
					//if current activity is not main activity finish current activity to switch back to main login
					else
						activity.finish();
				}
				else if(selectedName.equals("User options"))
				{
					editMenuOptions(userOptions, "Menu");
				}
				else if(selectedName.equals("Twitter prefrences"))
				{
					//menu.setDrawerListOnclickListener();
					editMenuOptions(twitterOptions, "Menu");
				}
				else if(selectedName.equals("Theme prefrences"))
				{
					//menu.setDrawerListOnclickListener();
					editMenuOptions(themeOptions, "Menu");
				}
				else if(selectedName.equals("About"))
				{
					Toast.makeText(getActivity().getApplicationContext(),selectedName, Toast.LENGTH_LONG).show();
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
					editMenuOptions(options, "Menu");
				}
				//----------------------------------------------------------------Twitter specific Options:
				else if(selectedName.equals("Twitter key"))
				{
					alert.setTitle("Change Twitter key");
					
					final EditText input = new EditText(activity.getApplicationContext());
					alert.setView(input);
					
					alert.setNegativeButton("Cancel",null);
					alert.setPositiveButton("Save", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							String value = input.getText().toString();
							//do something with value
							Toast.makeText(getActivity().getApplicationContext(),"Entered " + value, Toast.LENGTH_LONG).show();
						}
					});
					
					alert.show();
				}
				else if(selectedName.equals("Twitter secret"))
				{
					alert.setTitle("Change Twitter secret");
					
					final EditText input = new EditText(activity.getApplicationContext());
					alert.setView(input);
					
					alert.setNegativeButton("Cancel",null);
					alert.setPositiveButton("Save", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							String value = input.getText().toString();
							//do something with value
							Toast.makeText(getActivity().getApplicationContext(),"Entered " + value, Toast.LENGTH_LONG).show();
						}
					});
					
					alert.show();
				}
				else if(selectedName.equals("Twitter token key"))
				{
					alert.setTitle("Change Twitter Token key");
					
					final EditText input = new EditText(activity.getApplicationContext());
					alert.setView(input);
					
					alert.setNegativeButton("Cancel",null);
					alert.setPositiveButton("Save", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							String value = input.getText().toString();
							//do something with value
							Toast.makeText(getActivity().getApplicationContext(),"Entered " + value, Toast.LENGTH_LONG).show();
						}
					});
					
					alert.show();
				}
				else if(selectedName.equals("Twitter token secret"))
				{
					alert.setTitle("Change Twitter Token secret");
					
					final EditText input = new EditText(activity.getApplicationContext());
					alert.setView(input);
					
					alert.setNegativeButton("Cancel",null);
					alert.setPositiveButton("Save", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							String value = input.getText().toString();
							//do something with value
							Toast.makeText(getActivity().getApplicationContext(),"Entered " + value, Toast.LENGTH_LONG).show();
						}
					});
					
					alert.show();
				}
				else if(selectedName.equals("update rate"))
				{
					alert.setTitle("Change Twitter update rate");
					
					final EditText input = new EditText(activity.getApplicationContext());
					alert.setView(input);
					
					alert.setNegativeButton("Cancel",null);
					alert.setPositiveButton("Save", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							String value = input.getText().toString();
							//do something with value
							Toast.makeText(getActivity().getApplicationContext(),"Entered " + value, Toast.LENGTH_LONG).show();
						}
					});
					
					alert.show();
				}
				//----------------------------------------------------------------Theme specific Options:
				else if(selectedName.equals("Background"))
				{
					
				}
				else if(selectedName.equals("ActionBar"))
				{
					
				}
				//---------------------------------------------------------------User Options:
				else if(selectedName.equals("Add user"))
				{
					editMenuOptions(options, "Menu");
					getDrawerlayout().closeDrawer(getDrawerList());
					Intent intent = new Intent(getActivity(), AddUserActivity.class);
					activity.startActivity(intent);
				}
				else if(selectedName.equals("Search user"))
				{
					
				}
				else if(selectedName.equals("Delete user"))
				{
					
				}
			}
			
		});
	}
	
	/**
	 * @return the drawerLayout used by menu
	 */
	public DrawerLayout getDrawerlayout()
	{
		return drawerLayout;
	}
	
	/**
	 * @param the drawerLayout to be used by menu
	 */
	public void setDrawerLayout(DrawerLayout drawerLayout)
	{
		this.drawerLayout = drawerLayout;
	}
	
	/**
	 * @return the drawerList used by menu
	 */
	public ListView getDrawerList()
	{
		return drawerList;
	}
	
	/**
	 * Setting reference to the ActionBarDrawerToggle
	 * @param drawerList to be by menu
	 */
	public void setDrawerList(ListView drawerList)
	{
		this.drawerList = drawerList;
	}
	
	/**
	 * @return drawerToggle used by menu
	 */
	public ActionBarDrawerToggle getDrawerToggle()
	{
		return drawerToggle;
	}
	
	/**
	 * Also handles title change as menu opens and closes
	 * @param drawerToggle to be used by menu
	 */
	public void setDrawerToggle()
	{
		this.drawerToggle = new ActionBarDrawerToggle(activity,getDrawerlayout(), R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
		{
			/** 
			 * Called when a drawer is opened 
			 * Changes title to menu
			 */
			public void onDrawerOpened(View drawerView)
			{
				activity.getActionBar().setTitle("Menu");
				activity.invalidateOptionsMenu();
			}
			/** 
			 * Called when drawer is closed 
			 * The title is set back to original title: Smart Door
			 */
			public void onDrawerClosed(View view)
			{
				activity.getActionBar().setTitle(getTitle());
				activity.invalidateOptionsMenu();
			}
		};
		
		activateTopLeftButton();
	}
	
	/**
	 * Needed for menu button top left to respond
	 */
	public void activateTopLeftButton()
	{
		activity.getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	/**
	 * @return title in action bar
	 */
	public String getTitle()
	{
		return title;
	}
	
	/**
	 * @param title in action bar
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	/**
	 * @param activity using this menu
	 */
	public void setActivity(Activity activity)
	{
		this.activity = activity;
	}
	
	/**
	 * @return activity using this custom slider menu
	 */
	public Activity getActivity()
	{
		return activity;
	}	
	
}
