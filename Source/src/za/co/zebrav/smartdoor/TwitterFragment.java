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
import android.view.Gravity;
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
	public String PREFS_NAME;
	private AlertDialog.Builder alert;
	private View view;
	private TextView warning;

	// Auto-update time delay
	final int updateTime = 60000;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		view = inflater.inflate(R.layout.fragment_twitter, container, false);
		fragmentContext = (FragmentActivity) getActivity();
		
		PREFS_NAME = getResources().getString((R.string.settingsFileName));
		this.alert  = new AlertDialog.Builder(fragmentContext);
		
		tryTwitter();
		
		return view;
	}
	
	public void tryTwitter()
	{
		settings = fragmentContext.getSharedPreferences(PREFS_NAME, 0);
		String key = settings.getString("twitter_Key", "Not");
		String secret = settings.getString("twitter_Secret", "Not");
		String tokenKey = settings.getString("twitter_TokenKey", "Not");
		String tokenSecret = settings.getString("twitter_TokenSecret", "Not");
		
		if(!key.equals("Not") && !secret.equals("Not") && !tokenKey.equals("Not") && !tokenSecret.equals("Not"))
		{
			if(warning != null)
				warning.setVisibility(View.INVISIBLE);
			startTwitterFeed(key, secret, tokenKey, tokenSecret);
		}
		else//no settings found
		{
			setupGUI(view);
		}
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
		warning = new TextView(fragmentContext);
		warning.setText("Twitter keys not setup");
		warning.setGravity(Gravity.CENTER);
		layout.addView(warning);
		
		
	}
	
	
	/**
	 * Here the 
	 * @param key
	 * @param secret
	 * @param tokenKey
	 * @param tokenSecret
	 */
	public void startTwitterFeed(String key, String secret, String tokenKey, String tokenSecret)
	{
		feedSet = true;
		// Initialise the ArrayAdapter for the ListView
		adapter = new TwitterArrayAdapter(fragmentContext, R.layout.list_twitter, new ArrayList<twitter4j.Status>(), new ArrayList<Drawable>(), new ArrayList<Long>());
		setListAdapter(adapter);
		
		twitterHandler = new TwitterHandler(fragmentContext, view, adapter,key, secret, tokenKey, tokenSecret);
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