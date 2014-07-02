package za.co.zebrav.smartdoor;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
	private long sinceUserTimelineID;
	private long sinceMentionsTimelineID;
	private boolean disableRefresh;

	// TODO
	// We get 2 lists with a maximum size of 20.
	// Worst case is we have 40 different images.
	// Add 10 to have a small cache of previous images
	private int maxImageCount = 50;

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
		sinceUserTimelineID = -1;
		sinceMentionsTimelineID = -1;

		// Initialise the ArrayAdapter for the ListView
		adapter = new TwitterArrayAdapter(activityContext, R.layout.list_twitter,
					new ArrayList<twitter4j.Status>(), drawableProfileImage, userID);
		setListAdapter(adapter);

		// Get the tweets
		getTweets();
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
				drawableProfileImage.clear();
				userID.clear();
				sinceUserTimelineID = -1;
				sinceMentionsTimelineID = -1;
				adapter.clearData();

				getTweets();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Changes the Refresh buttons Enabled state to prevent users from refreshing while we are
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
		ConnectivityManager connectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected())
		{
			Log.d(LOG_TAG_TWITTER_ACTIVITY, "execute");

			// Disable the refresh button, we are already getting the tweets.
			disableRefresh = true;
			invalidateOptionsMenu();

			Toast.makeText(this, "Refreshing Tweets", Toast.LENGTH_LONG).show();
			new TwitterHandler().execute();

			// while(true)
			// {
			// while(!(twitterHandler.getStatus() == AsyncTask.Status.FINISHED))
			// {
			// }
			// twitterHandler.execute();
			// }
		}
		else
		{
			// Log, and let the user know that there isn't any network
			// connections.
			// Can change this to whatever notification works best
			Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG).show();
			Log.i(LOG_TAG_TWITTER_ACTIVITY, "No network connection available.");
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

				// Add both timelines into one.
				// Sort from most recent to least recently posted tweet.
				List<twitter4j.Status> tweets = new ArrayList<twitter4j.Status>(userTimeline);
				for (twitter4j.Status tweet : mentionsTimeline)
				{
					for (int i = 0; i < tweets.size(); i++)
					{
						if (tweets.get(i).getCreatedAt().compareTo(tweet.getCreatedAt()) < 0)
						{
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
						int indexOf = userID.indexOf(tweet.getUser().getId());
						if (indexOf == -1)
						{
							// Get the profile image
							String imageURL = tweet.getUser().getOriginalProfileImageURL();
							Log.d(LOG_TAG_TWITTER_ACTIVITY, imageURL);
							URL url = new URL(imageURL);
							InputStream content = (InputStream) url.openStream();
							Drawable d = Drawable.createFromStream(content, "src");

							// Add the image to the front of the list.
							drawableProfileImage.add(0, d);
							userID.add(0, tweet.getUser().getId());
						}
						else
						{
							// Move the image to the front of the list.
							Drawable tempDrawable = drawableProfileImage.get(indexOf);
							drawableProfileImage.remove(indexOf);
							drawableProfileImage.add(0, tempDrawable);

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
				}

				Log.d(LOG_TAG_TWITTER_ACTIVITY, userID.toString());

				// Save some space
				drawableProfileImage.trimToSize();
				userID.trimToSize();

				return tweets;
			}
			catch (TwitterException e)
			{
				Toast.makeText(activityContext,
							"The Twitter Birdy Died! (To many requests in a short time?)",
							Toast.LENGTH_LONG).show();
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
			adapter.addTweetsToTop(result);

			disableRefresh = false;
			invalidateOptionsMenu();

			Toast.makeText(activityContext, "Refreshing Complete", Toast.LENGTH_LONG).show();
		}
	}
}
