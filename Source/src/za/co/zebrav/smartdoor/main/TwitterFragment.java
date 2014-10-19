package za.co.zebrav.smartdoor.main;

import java.util.ArrayList;

import za.co.zebrav.smartdoor.R;
import za.co.zebrav.smartdoor.twitter.TwitterArrayAdapter;
import za.co.zebrav.smartdoor.twitter.TwitterHandler;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 
 * @author tinkie101
 * 
 */
public class TwitterFragment extends Fragment
{
	private static final String LOG_TAG = "TwitterFragment";
	private AbstractActivity fragmentContext;

	private TwitterArrayAdapter adapter;

	private TwitterHandler twitterHandler;

	private Thread updateThread;
	private Handler updateThreadHandler = new Handler();
	private boolean feedSet = false;
	private SharedPreferences settings = null;
	public String PREFS_NAME;
	private View view;
	private TextView warning;
	private ListView list;

	// Auto-update time delay
	final int updateTime = 60000;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		view = inflater.inflate(R.layout.fragment_twitter, container, false);
		fragmentContext = (AbstractActivity) getActivity();
		
		PREFS_NAME = getResources().getString((R.string.settingsFileName));
		
		list = (ListView) view.findViewById(R.id.twitter_list);
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
	
	public void startTwitterFeed(String key, String secret, String tokenKey, String tokenSecret)
	{
		feedSet = true;
		// Initialise the ArrayAdapter for the ListView
		adapter = new TwitterArrayAdapter(fragmentContext, R.layout.list_twitter, new ArrayList<twitter4j.Status>(), new ArrayList<Drawable>(), new ArrayList<Long>());
		list.setAdapter(adapter);
		
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