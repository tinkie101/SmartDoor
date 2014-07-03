package za.co.zebrav.smartdoor;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import za.co.zebrav.smartdoor.twitter.TwitterArrayAdapter;
import android.app.ListActivity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 
 * @author tinkie101
 * 
 */
public class TwitterActivity extends ListActivity
{
	private static final String LOG_TAG_TWITTER_ACTIVITY = "TwitterActivity";
	private ListActivity activityContext;
	Twitter twitter;

	private TwitterArrayAdapter adapter;
	private ArrayList<Drawable> drawableProfileImage;
	private ArrayList<Long> userID;
	private ArrayList<String> userProfileImageURL;

	private long sinceUserTimelineID;
	private long sinceMentionsTimelineID;
	private boolean disableRefresh;
	private Thread updateThread;
	private Handler updateThreadHandler = new Handler();

	private AtomicInteger gettingTweets;

	// TODO
	// We get 2 lists with a maximum size of 20.
	// Worst case is we have 40 different images.
	// Add 10 to have a small cache of previous images
	private int maxImageCount = 50;
	private int maxTweetCount = 50;
	// Auto-update time
	final int updateTime = 61000;

	/**
	 * 
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twitter);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		activityContext = this;
		disableRefresh = false;

		// TODO: Get this from somewhere else
		String API_KEY = "qcGzp08qWLEZom1x7dxCG5qu0";
		String API_SECRET = "ly810vDH1S16Ttw0mpk4ZBYQvLEF9gEO16KSqy9lBqhwRf5XRo";
		String ACCESS_TOKEN = "239453626-73H379K274Qfm9KaPfq8C3hKPhq3jqGk04gQXkIw";
		String TOKEN_SECRET = "7ozxcBnoAA4WEocKeUSrFI9iOO9hVqNYwS2xFOeB0osUl";

		// Set the authority settings for the Twitter API
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(API_KEY).setOAuthConsumerSecret(API_SECRET)
					.setOAuthAccessToken(ACCESS_TOKEN).setOAuthAccessTokenSecret(TOKEN_SECRET);
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();

		// Initialise the lists and sinceId's
		drawableProfileImage = new ArrayList<Drawable>();
		userID = new ArrayList<Long>();
		userProfileImageURL = new ArrayList<String>();
		sinceUserTimelineID = -1;
		sinceMentionsTimelineID = -1;

		// Initialise the ArrayAdapter for the ListView
		adapter = new TwitterArrayAdapter(activityContext, R.layout.list_twitter,
					new ArrayList<twitter4j.Status>(), drawableProfileImage, userID);

		setListAdapter(adapter);

		gettingTweets = new AtomicInteger(1);
		// Get the tweets
		getTweets();

		// Create thread that automatically checks for updates
		updateThread = new Thread()
		{
			public void run()
			{
				// wait for the specified update time
				updateThreadHandler.postDelayed(this, updateTime);

				// Update tweets if we aren't already doing it.
				if (gettingTweets.compareAndSet(0, 1))
				{
					getTweets();
				}
				else
				{
					Log.d(LOG_TAG_TWITTER_ACTIVITY, "Already refreshing tweets!");
				}
			}
		};

	}

	/**
	 * Called when the activity is resumed.
	 */
	@Override
	protected void onResume()
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
	protected void onPause()
	{
		super.onPause();

		// Stop the auto update thread
		updateThreadHandler.removeCallbacks(updateThread);
	}

	/**
	 * 
	 * @param menu
	 * @return
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.twitter, menu);
		return true;
	}

	/**
	 * 
	 * @param item
	 * @return
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				finish();
				break;

			// Clear all the data and get a completely new list of tweets.
			case R.id.action_refresh_tweets:
				if (gettingTweets.compareAndSet(0, 1))
				{
					drawableProfileImage.clear();
					userID.clear();
					userProfileImageURL.clear();
					sinceUserTimelineID = -1;
					sinceMentionsTimelineID = -1;
					adapter.clearData();
					getTweets();
				}
				else
				{
					Log.d(LOG_TAG_TWITTER_ACTIVITY, "Already refreshing tweets!");
				}
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Changes the Refresh button's Enabled state to prevent users from refreshing while we are
	 * already getting new tweets
	 * 
	 * @param menu
	 * @return
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuItem item = menu.findItem(R.id.action_refresh_tweets);

		if (disableRefresh)
			item.setEnabled(false);
		else
			item.setEnabled(true);

		return true;
	}

	/**
	 * Test for network connectivity.
	 * Execute the TwitterHandler to get the tweets.
	 */
	private void getTweets()
	{
		Log.d(LOG_TAG_TWITTER_ACTIVITY, "Updating tweets");

		ConnectivityManager connectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected())
		{
			// Disable the refresh button, we are already refreshing the tweets.
			disableRefresh = true;
			invalidateOptionsMenu();

			Toast.makeText(this, "Updating Tweets", Toast.LENGTH_LONG).show();
			new TwitterHandler().execute();
		}
		else
		{
			// Log, and let the user know that there isn't any network
			// connections.
			// Can change this to whatever notification works best
			Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG).show();
			Log.i(LOG_TAG_TWITTER_ACTIVITY, "No network connection available.");

			if (!gettingTweets.compareAndSet(1, 0))
			{
				Log.e(LOG_TAG_TWITTER_ACTIVITY, "Thread Error!");
			}
		}
	}

	private class TwitterHandler extends AsyncTask<Void, Void, List<twitter4j.Status>>
	{
		/**
		 * Get the Twitter timelines using the twitter4j library.
		 * Add the newest Tweets to the front of the list.
		 * Get each tweet's user profile image and store it. This reduces the network traffic.
		 * Use a least recently used strategy to remove unused profile images if the list gets to
		 * big.
		 * 
		 * @param params
		 * @return
		 */
		@Override
		protected List<twitter4j.Status> doInBackground(Void... params)
		{
			try
			{
				List<twitter4j.Status> userTimeline;
				List<twitter4j.Status> mentionsTimeline;

				// Only get 20 of the newest tweets from each timeline
				if (sinceUserTimelineID == -1)
				{
					userTimeline = twitter.getUserTimeline();
				}
				else
				{
					Paging paging = new Paging(1, 20).sinceId(sinceUserTimelineID);
					userTimeline = twitter.getUserTimeline(paging);
				}
				if (sinceMentionsTimelineID == -1)
				{
					mentionsTimeline = twitter.getMentionsTimeline();
				}
				else
				{
					Paging paging = new Paging(1, 20).sinceId(sinceMentionsTimelineID);
					mentionsTimeline = twitter.getMentionsTimeline(paging);
				}

				// Set the sinceID to the first(newest) Tweet ID, so that in the future we only get
				// newer tweets
				if (userTimeline.size() > 0)
					sinceUserTimelineID = userTimeline.get(0).getId();
				if (mentionsTimeline.size() > 0)
					sinceMentionsTimelineID = mentionsTimeline.get(0).getId();

				Log.d(LOG_TAG_TWITTER_ACTIVITY, sinceMentionsTimelineID + ":" + sinceUserTimelineID);

				// Add both timelines into one.
				// Sort from most recent to least recently posted tweet.
				List<twitter4j.Status> tweets = new ArrayList<twitter4j.Status>(userTimeline);
				for (twitter4j.Status tweet : mentionsTimeline)
				{
					Log.d(LOG_TAG_TWITTER_ACTIVITY, "comparing: " + tweet.getId());
					for (int i = 0; i <= tweets.size(); i++)
					{
						if (i == tweets.size())
						{
							// List is empty or we reached the end of the list, so add normally
							Log.d(LOG_TAG_TWITTER_ACTIVITY, "Added: " + tweet.getId());
							tweets.add(tweet);
							break;
						}
						else if (tweets.get(i).getCreatedAt().compareTo(tweet.getCreatedAt()) < 0)
						{
							Log.d(LOG_TAG_TWITTER_ACTIVITY, "Added: " + tweet.getId() + " to: " + i);
							tweets.add(i, tweet);
							break;
						}
					}
				}

				// Store a list of already retrieved profile images to reduce the network cost
				// Add the images to the front of the list. So that the last image is one that isn't
				// used a lot.
				// If the list gets to big, delete the least frequently used images (at the back of
				// the list)
				for (twitter4j.Status tweet : tweets)
				{
					try
					{
						// Get the profile image
						String imageURL = tweet.getUser().getOriginalProfileImageURL();
						URL url;
						InputStream content;
						Drawable drawable;

						int indexOf = userID.indexOf(tweet.getUser().getId());
						if (indexOf == -1)
						{
							url = new URL(imageURL);
							content = (InputStream) url.openStream();
							drawable = Drawable.createFromStream(content, "src");

							// Add the image to the front of the list.
							drawableProfileImage.add(0, drawable);
							userProfileImageURL.add(0, imageURL);

							// Add user to the front of the list
							userID.add(0, tweet.getUser().getId());
						}
						else
						{
							// Check if there is a new profile image
							if (!userProfileImageURL.contains(imageURL))
							{
								url = new URL(imageURL);
								content = (InputStream) url.openStream();
								drawable = Drawable.createFromStream(content, "src");
							}
							else
							{
								// Move the image to the front of the list.
								drawable = drawableProfileImage.get(indexOf);
								imageURL = userProfileImageURL.get(indexOf);
							}

							drawableProfileImage.remove(indexOf);
							drawableProfileImage.add(0, drawable);

							userProfileImageURL.remove(indexOf);
							userProfileImageURL.add(0, imageURL);

							long tempID = userID.get(indexOf);
							userID.remove(indexOf);
							userID.add(0, tempID);
						}

					}
					catch (MalformedURLException e)
					{
					}
					catch (IOException e)
					{
					}

				}

				// Remove the least recently used profile images if the List gets to big
				while (userID.size() > maxImageCount)
				{
					int i = userID.size() - 1;
					userID.remove(i);
					drawableProfileImage.remove(i);
					userProfileImageURL.remove(i);
				}

				// Save some space
				drawableProfileImage.trimToSize();
				userID.trimToSize();
				userProfileImageURL.trimToSize();

				// Only save the newest tweets (don't let the tweet list get to long)
				while (tweets.size() > maxTweetCount)
				{
					int i = tweets.size() - 1;
					tweets.remove(i);
				}

				return tweets;
			}
			catch (TwitterException e)
			{
				Log.d(LOG_TAG_TWITTER_ACTIVITY, "Twitter Error: " + e.toString());
			}
			return null;
		}

		/**
		 * Add the new tweets to the top of the list and re-enable the refresh button.
		 * 
		 * @param result
		 */
		@Override
		protected void onPostExecute(List<twitter4j.Status> result)
		{
			if (result != null && result.size() > 0)
			{
				adapter.addTweetsToTop(result);

				ListView lv = (ListView) findViewById(android.R.id.list);
				lv.smoothScrollToPosition(0);

				Toast.makeText(activityContext, "Update Complete", Toast.LENGTH_LONG).show();
			}
			else
			{
				Toast.makeText(activityContext, "No new Tweets", Toast.LENGTH_LONG).show();
			}

			disableRefresh = false;
			invalidateOptionsMenu();

			Log.d(LOG_TAG_TWITTER_ACTIVITY, "Update Complete");

			if (!gettingTweets.compareAndSet(1, 0))
			{
				Log.e(LOG_TAG_TWITTER_ACTIVITY, "Thread Error!");
			}
		}
	}
}
