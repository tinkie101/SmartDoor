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
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
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
	private Twitter twitter;

	private TwitterArrayAdapter adapter;
	private ArrayList<Drawable> drawableProfileImage;
	private ArrayList<Long> userID;
	private ArrayList<String> userProfileImageURL;
	private long sinceUserTimelineID;
	private long sinceMentionsTimelineID;

	private TwitterHandler twitterHandler;

	private Thread updateThread;
	private Handler updateThreadHandler = new Handler();
	private AtomicInteger gettingTweets;

	// TODO
	private int timelineSize = 20;

	// We get 2 lists with a maximum size of timelineSize each.
	// Worst case is we have timelineSize*2 different images.
	// Add 10 to have a small cache of previous images
	private int maxImageCount = timelineSize * 2 + 10;

	// The maximum number of Tweets to display
	private int maxTweetCount = 50;

	// Auto-update time delay
	final int updateTime = 60000;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_twitter, container, false);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		if (twitterHandler != null)
			twitterHandler.cancel(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		fragmentContext = (FragmentActivity) getActivity();

		// TODO: Get this from somewhere else
		String API_KEY = "qcGzp08qWLEZom1x7dxCG5qu0";
		String API_SECRET = "ly810vDH1S16Ttw0mpk4ZBYQvLEF9gEO16KSqy9lBqhwRf5XRo";
		String ACCESS_TOKEN = "239453626-73H379K274Qfm9KaPfq8C3hKPhq3jqGk04gQXkIw";
		String TOKEN_SECRET = "7ozxcBnoAA4WEocKeUSrFI9iOO9hVqNYwS2xFOeB0osUl";

		twitter = constructTwitterAuthority(API_KEY, API_SECRET, ACCESS_TOKEN, TOKEN_SECRET);

		// Initialise the lists and sinceId's
		drawableProfileImage = new ArrayList<Drawable>();
		userID = new ArrayList<Long>();
		userProfileImageURL = new ArrayList<String>();
		sinceUserTimelineID = -1;
		sinceMentionsTimelineID = -1;

		// Initialise the ArrayAdapter for the ListView
		adapter = new TwitterArrayAdapter(fragmentContext, R.layout.list_twitter, new ArrayList<twitter4j.Status>(),
							drawableProfileImage, userID);

		setListAdapter(adapter);

		// Set the atomic integer to handle threads
		gettingTweets = new AtomicInteger(1);

		// Get the tweets
		getTweets();

		// Create a thread that automatically checks for updates
		updateThread = new Thread()
		{
			public void run()
			{
				// wait for the specified update time
				updateThreadHandler.postDelayed(this, updateTime);

				// Update tweets if we aren't already doing it.
				// If gettingTweets is 0 then set to 1 and get tweets, else do nothing
				if (gettingTweets.compareAndSet(0, 1))
				{
					// Get the tweets
					getTweets();
				}
				else
				{
					Log.d(LOG_TAG_TWITTER_FRAGMENT, "Already refreshing tweets!");
				}
			}
		};

	}

	/**
	 * 
	 * @param API_KEY
	 * @param API_SECRET
	 * @param ACCESS_TOKEN
	 * @param TOKEN_SECRET
	 * @return
	 */
	private Twitter constructTwitterAuthority(String API_KEY, String API_SECRET, String ACCESS_TOKEN,
						String TOKEN_SECRET)
	{
		Twitter result = null;

		// Set the authority settings for the Twitter API
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(API_KEY).setOAuthConsumerSecret(API_SECRET)
							.setOAuthAccessToken(ACCESS_TOKEN).setOAuthAccessTokenSecret(TOKEN_SECRET);
		TwitterFactory tf = new TwitterFactory(cb.build());
		result = tf.getInstance();

		return result;
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

	/**
	 * Test for network connectivity.
	 * Execute the TwitterHandler to get the tweets.
	 */
	private void getTweets()
	{
		Log.d(LOG_TAG_TWITTER_FRAGMENT, "Updating tweets");

		ConnectivityManager connectionManager = (ConnectivityManager) fragmentContext
							.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected())
		{
			Toast.makeText(fragmentContext, "Updating Tweets", Toast.LENGTH_LONG).show();
			fragmentContext.findViewById(R.id.twitter_refreshBar).setVisibility(ProgressBar.VISIBLE);
			twitterHandler = new TwitterHandler();
			twitterHandler.execute();
		}
		else
		{
			// Log, and let the user know that there isn't any network
			// connections.
			// Can change this to whatever notification works best
			Toast.makeText(fragmentContext, "No network connection available.", Toast.LENGTH_LONG).show();
			Log.i(LOG_TAG_TWITTER_FRAGMENT, "No network connection available.");

			if (!gettingTweets.compareAndSet(1, 0))
			{
				// This should never happen, if we got here it means the gettingTweets should be 1
				// If it isn't 1 then something went terribly wrong
				Log.e(LOG_TAG_TWITTER_FRAGMENT, "Thread Error!");
			}
		}
	}

	/**
	 * 
	 * @author tinkie101
	 * 
	 */
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
		 *            We don't have any input values, thus Void
		 * @return the list of tweets received.
		 */
		@Override
		protected List<twitter4j.Status> doInBackground(Void... params)
		{
			// Get the user and mentions timeline, then merge them into one sorted timeline
			List<twitter4j.Status> tweets = getTimelines();

			// Store a list of already retrieved profile images to reduce the network cost
			// Add the images to the front of the list. So that the last image in the list is
			// one that isn't
			// used a lot.
			// If the list gets to big, delete the least frequently used images (at the back of
			// the list)
			for (twitter4j.Status tweet : tweets)
			{
				try
				{
					String imageURL = tweet.getUser().getOriginalProfileImageURL();
					URL url;
					InputStream content;
					Drawable drawable;

					int indexOf = userID.indexOf(tweet.getUser().getId());
					// Already in list
					if (indexOf == -1)
					{
						// Get the users profile image
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
							// Get new profile image
							url = new URL(imageURL);
							content = (InputStream) url.openStream();
							drawable = Drawable.createFromStream(content, "src");
						}
						else
						{
							// Get old image, so we can move it to the front of the list
							drawable = drawableProfileImage.get(indexOf);
							imageURL = userProfileImageURL.get(indexOf);
						}

						// Move the image to the front of the list.
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

			// Only save the newest tweets (don't let the tweet list get to long)
			while (tweets.size() > maxTweetCount)
			{
				int i = tweets.size() - 1;
				tweets.remove(i);
			}

			// Save some space
			drawableProfileImage.trimToSize();
			userID.trimToSize();
			userProfileImageURL.trimToSize();

			return tweets;
		}

		private List<twitter4j.Status> getTimelines()
		{
			List<twitter4j.Status> result = null;

			List<twitter4j.Status> userTimeline = getUserTimeline();
			List<twitter4j.Status> mentionsTimeline = getMentionsTimeline();

			result = mergeAndSort(userTimeline, mentionsTimeline);

			return result;
		}

		private List<twitter4j.Status> mergeAndSort(List<twitter4j.Status> userTimeline,
							List<twitter4j.Status> mentionsTimeline)
		{
			// Add both timelines into one.
			// Sort from most recent to least recently posted tweet.
			List<twitter4j.Status> result = new ArrayList<twitter4j.Status>(userTimeline);

			for (twitter4j.Status tweet : mentionsTimeline)
			{
				Log.d(LOG_TAG_TWITTER_FRAGMENT, "comparing: " + tweet.getId());

				for (int i = 0; i <= result.size(); i++)
				{
					if (i == result.size())
					{
						// List is empty or we reached the end of the list, so add normally
						Log.d(LOG_TAG_TWITTER_FRAGMENT, "Added: " + tweet.getId());
						result.add(tweet);
						break;
					}
					else if (result.get(i).getCreatedAt().compareTo(tweet.getCreatedAt()) < 0)
					{
						// Add the tweet in its correct position (sort by date)
						Log.d(LOG_TAG_TWITTER_FRAGMENT, "Added: " + tweet.getId() + " to: " + i);
						result.add(i, tweet);
						break;
					}
				}
			}
			return result;
		}

		/**
		 * 
		 * @return
		 */
		private List<twitter4j.Status> getUserTimeline()
		{
			List<twitter4j.Status> result = null;

			try
			{
				// Get default amount of tweets for beginning
				if (sinceUserTimelineID == -1)
				{
					result = twitter.getUserTimeline();
				}
				else
				// Only get newest tweets
				{
					Paging paging = new Paging(1, timelineSize).sinceId(sinceUserTimelineID);
					result = twitter.getUserTimeline(paging);
				}

				// Set the sinceID to the first(newest) Tweet ID, so that in the future we only get
				// newer tweets
				if (result.size() > 0)
					sinceUserTimelineID = result.get(0).getId();
			}
			catch (TwitterException e)
			{
				result = null;
			}

			return result;
		}

		/**
		 * 
		 * @return
		 */
		private List<twitter4j.Status> getMentionsTimeline()
		{
			List<twitter4j.Status> result = null;

			try
			{
				if (sinceMentionsTimelineID == -1)
				{
					result = twitter.getMentionsTimeline();
				}
				else
				{
					Paging paging = new Paging(1, 20).sinceId(sinceMentionsTimelineID);
					result = twitter.getMentionsTimeline(paging);
				}

				// Set the sinceID to the first(newest) Tweet ID, so that in the future we only get
				// newer tweets
				if (result.size() > 0)
					sinceMentionsTimelineID = result.get(0).getId();

			}
			catch (TwitterException e)
			{
				result = null;
			}

			return result;
		}

		/**
		 * Add the new tweets to the top of the list and re-enable the refresh button.
		 * 
		 * @param result
		 *            The list of tweets received from the doInBackground() function
		 */
		@Override
		protected void onPostExecute(List<twitter4j.Status> result)
		{
			// If there is new tweets to add
			if (result != null && result.size() > 0)
			{
				// Add to the top of the list, and scroll the list view to the top position
				adapter.addTweetsToTop(result);

				ListView lv = (ListView) getView().findViewById(android.R.id.list);
				lv.smoothScrollToPosition(0);

				// Let the user know that the update is complete
				Toast.makeText(fragmentContext, "Update Complete", Toast.LENGTH_LONG).show();
			}
			else
			{
				Toast.makeText(fragmentContext, "No new Tweets", Toast.LENGTH_LONG).show();
			}

			Log.d(LOG_TAG_TWITTER_FRAGMENT, "Update Complete");

			fragmentContext.findViewById(R.id.twitter_refreshBar).setVisibility(ProgressBar.GONE);

			if (!gettingTweets.compareAndSet(1, 0))
			{
				// This should never happen, if we got here then there was a terrible mistake
				Log.e(LOG_TAG_TWITTER_FRAGMENT, "Thread Error!");
			}
		}
	}
}