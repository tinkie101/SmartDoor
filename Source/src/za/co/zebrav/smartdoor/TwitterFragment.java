package za.co.zebrav.smartdoor;

import java.util.ArrayList;

import za.co.zebrav.smartdoor.twitter.TwitterArrayAdapter;
import za.co.zebrav.smartdoor.twitter.TwitterHandler;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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

	// Auto-update time delay
	final int updateTime = 60000;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_twitter, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		fragmentContext = (FragmentActivity) getActivity();

		// Initialise the ArrayAdapter for the ListView
		adapter = new TwitterArrayAdapter(fragmentContext, R.layout.list_twitter, new ArrayList<twitter4j.Status>(), new ArrayList<Drawable>(), new ArrayList<Long>());
		setListAdapter(adapter);
		
		twitterHandler = new TwitterHandler(fragmentContext, adapter);

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

		// Restart the auto update
		updateThreadHandler.removeCallbacks(updateThread);
		updateThreadHandler.postDelayed(updateThread, updateTime);
	}

	/**
	 * Called when the activity is paused
	 */
	@Override
	public void onPause()
	{
		super.onPause();

		// Stop the auto update thread
		updateThreadHandler.removeCallbacks(updateThread);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();

		twitterHandler.cancel();
	}
}