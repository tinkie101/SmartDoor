package za.co.zebrav.smartdoor;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import za.co.zebrav.smartdoor.database.Db4oAdapter;
import za.co.zebrav.smartdoor.database.User;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ManualLogin extends Activity
{
	private CustomMenu sliderMenu;
	private Db4oAdapter provider;
	
	private EditText usernameET;
	private EditText passwordET;
	
	private AlertDialog.Builder alert;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.manual_login);
		super.onCreate(savedInstanceState);
		
		//database provider
		provider = new Db4oAdapter(this);
		
		//create alert and textToSpeech
		alert = new AlertDialog.Builder(this);
		
		//Get needed GUI components
		usernameET = (EditText) findViewById(R.id.ManualLogin_et1);
		passwordET = (EditText) findViewById(R.id.ManualLogin_et2);
		
		// add slider menu
		sliderMenu = new CustomMenu(this, (ListView) findViewById(R.id.drawer_list), (DrawerLayout) findViewById(R.id.drawer_layout), 
				getResources().getStringArray(R.array.mainMenuOptions));
	}
	
	//-------------------------------------------------------------------------Login
	/**
	 * This function is called the moment the "Login" button is clicked
	 * @param v
	 * @throws InterruptedException 
	 */
	public void pressedLoginButton(View v) throws InterruptedException
	{
		User user = getUser();
		
		if(user == null)
		{
			alertMessage("Incorrect username or password.");
		}
		else
		{
			Intent i = new Intent();
			Bundle bundle = new Bundle();
			bundle.putSerializable("user", user);
			i.putExtras(bundle);
			i.setClass(this, OpenDoor.class);
			this.startActivity(i);
			
		}
	}
	
	/**
	 * Alerts the specified message in dialogue box.
	 */
	public void alertMessage(String message)
	{
		alert.setTitle("Alert").setMessage(message).setNeutralButton("OK", null).show();
	}
	
	/**
	 * GetUserInput and return result
	 * @return result of database query
	 */
	private User getUser()
	{
		String uName = usernameET.getText().toString();
		String pass = passwordET.getText().toString();
		provider.open();
		List<Object> users = provider.load(new User(null, null, uName, pass, 0));
	
		if(users.isEmpty())
		{
			provider.close();
			return null;
		}
		
		User temp = (User) users.get(0);
		User user = new User(temp.getFirstnames(), temp.getSurname(), temp.getUsername(), temp.getPassword(), temp.getID());
		
		return user;
	}
	

	//-------------------------------------------------------------------------FOR Menu
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
