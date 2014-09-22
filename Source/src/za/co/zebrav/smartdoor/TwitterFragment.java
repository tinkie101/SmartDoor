package za.co.zebrav.smartdoor;

import java.util.ArrayList;

import za.co.zebrav.smartdoor.twitter.TwitterArrayAdapter;
import za.co.zebrav.smartdoor.twitter.TwitterHandler;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author tinkie101
 * 
 */
public class TwitterFragment extends ListFragment
{
	private static final String LOG_TAG_TWITTER_FRAGMENT = "TwitterFragment";
	private FragmentActivity fragmentContext;

	private TwitterArrayAdapter adapter;

	private TwitterHandler twitterHandler;

	private Thread updateThread;
	private Handler updateThreadHandler = new Handler();
	private boolean feedSet = false;
	private SharedPreferences settings = null;
	public static final String PREFS_NAME = "MyPrefsFile";
	private AlertDialog.Builder alert;

	// Auto-update time delay
	final int updateTime = 60000;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_twitter, container, false);
		fragmentContext = (FragmentActivity) getActivity();
		settings = fragmentContext.getSharedPreferences(PREFS_NAME, 0);
		this.alert  = new AlertDialog.Builder(fragmentContext);
		
		String key = settings.getString("key", "Not");
		String secret = settings.getString("secret", "Not");
		String tokenKey = settings.getString("tokenKey", "Not");
		String tokenSecret = settings.getString("tokenSecret", "Not");
		
		if(!key.equals("Not") && !secret.equals("Not") && !tokenKey.equals("Not") && !tokenSecret.equals("Not"))
		{
			startTwitterFeed(key, secret, tokenKey, tokenSecret);
		}
		else//no settings found
		{
			setupGUI(view);
		}
		
		return view;
	}
	
	/**
	 * This sets up the initial layout of the Fragment IF no Twitter settings are saved.
	 * @param view
	 */
	private void setupGUI(View view)
	{
		//Get needed layout to add GUI items to
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.twitterFragment);
		
		//Create header displaying that no Twitter settings found
		TextView header = new TextView(fragmentContext);
		header.setText("Twitter keys not setup");
		layout.addView(header);
		
		//Default button added
		/*Button defaultB = new Button(fragmentContext);
		defaultB.setText("Use defualts");
		layout.addView(defaultB);
		defaultB.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	useDefaults();
            }
        });
		
		//Set Twitter Settings
		Button setB = new Button(fragmentContext);
		setB.setText("Set settings");
		layout.addView(setB);
		setB.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	setSettings();
            }
        });*/
	}
	
	private void setSettings()
	{
		saveNewTwitter("Set Twitter key", "key");
		saveNewTwitter("Set Twitter secret", "secret");
		saveNewTwitter("Set Twitter Token Key", "tokenKey");
		saveNewTwitter("Set Twitter Token Secret", "tokenSecret");
		
		String key = settings.getString("key", "Not");
		String secret = settings.getString("secret", "Not");
		String tokenKey = settings.getString("tokenKey", "Not");
		String tokenSecret = settings.getString("tokenSecret", "Not");
		
		if(!key.equals("Not") && !secret.equals("Not") && !tokenKey.equals("Not") && !tokenSecret.equals("Not"))
		{
			startTwitterFeed(key, secret, tokenKey, tokenSecret);
		}
	}
	
	private void saveNewTwitter(String heading, final String key)
	{
		alert.setTitle(heading);
		
		final EditText input = new EditText(this.fragmentContext.getApplicationContext());
		alert.setView(input);
		
		alert.setNegativeButton("Cancel",null);
		alert.setPositiveButton("Save", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String value = input.getText().toString();
				SharedPreferences.Editor editor = settings.edit();
			    editor.putString(key, value);
			    editor.commit();
			}
		});
		
		alert.show();
	}
	
	/**
	 * If the option for choosing defaults are chosen then the string tokens in string files must be used
	 */
	private void useDefaults()
	{
		String key = getResources().getString(R.string.twitterKey);
		String secret = getResources().getString(R.string.twitterSecret);
		String tokenKey = getResources().getString(R.string.twitterToken);
		String tokenSecret = getResources().getString(R.string.twitterTokenKey);
		
		startTwitterFeed(key, secret, tokenKey, tokenSecret);
	}
	
	/**
	 * Here the 
	 * @param key
	 * @param secret
	 * @param tokenKey
	 * @param tokenSecret
	 */
	private void startTwitterFeed(String key, String secret, String tokenKey, String tokenSecret)
	{
		feedSet = true;
		// Initialise the ArrayAdapter for the ListView
		adapter = new TwitterArrayAdapter(fragmentContext, R.layout.list_twitter, new ArrayList<twitter4j.Status>(), new ArrayList<Drawable>(), new ArrayList<Long>());
		setListAdapter(adapter);
		
		twitterHandler = new TwitterHandler(fragmentContext, adapter,key, secret, tokenKey, tokenSecret);
		// Get the tweets
		twitterHandler.getTweets();

		// Create a thread that automatically checks for updates
		updateThread = new Thread()
		{
			public void run()
			{
				// wait for the specified update time
				updateThreadHandler.postDelayed(this, updateTime);

				// Get the tweets
				twitterHandler.getTweets();
				
			}
		};
	
	}

	/**
	 * Called when the activity is resumed.
	 */
	@Override
	public void onResume()
	{
		super.onResume();

		if(feedSet)
		{
			// Restart the auto update
			updateThreadHandler.removeCallbacks(updateThread);
			updateThreadHandler.postDelayed(updateThread, updateTime);
		}
	}

	/**
	 * Called when the activity is paused
	 */
	@Override
	public void onPause()
	{
		super.onPause();

		if(feedSet)
		{
			// Stop the auto update thread
			updateThreadHandler.removeCallbacks(updateThread);
		}
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();

		if(feedSet)
		{
			twitterHandler.cancel();
		}
		
	}
}