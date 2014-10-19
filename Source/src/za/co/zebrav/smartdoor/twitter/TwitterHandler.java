package za.co.zebrav.smartdoor.twitter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import za.co.zebrav.smartdoor.R;
import za.co.zebrav.smartdoor.main.AbstractActivity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class TwitterHandler
{
	private static final String LOG_TAG_TWITTER_HANDLER = "TwitterHandler";
	private AbstractActivity fragmentContext;
	private View view;
	public AsynchTwitter asynchTwitter;
	private TwitterArrayAdapter adapter;

	protected Twitter twitter;

	protected ArrayList<Drawable> drawableProfileImage;
	protected ArrayList<Long> userID;
	protected ArrayList<String> userProfileImageURL;
	protected long sinceUserTimelineID;
	protected long sinceMentionsTimelineID;
	
	// TODO
	protected int timelineSize = 20;

	// We get 2 lists with a maximum size of timelineSize each.
	// Worst case is we have timelineSize*2 different images.
	// Add 10 to have a small cache of previous images
	protected int maxImageCount = timelineSize * 2 + 10;

	// The maximum number of Tweets to display
	protected int maxTweetCount = 50;

	public TwitterHandler(AbstractActivity fragmentContext, View view, TwitterArrayAdapter adapter, String key, String secret, String tokenKey, String tokenSecret)
	{
		this.fragmentContext = fragmentContext;
		this.adapter = adapter;
		this.view = view;
		
		String API_KEY = key;
		String API_SECRET = secret;
		String ACCESS_TOKEN = tokenKey;
		String TOKEN_SECRET = tokenSecret;
		

		twitter = constructTwitterAuthority(API_KEY, API_SECRET, ACCESS_TOKEN, TOKEN_SECRET);

		// Initialise the lists and sinceId's
		drawableProfileImage = new ArrayList<Drawable>();
		userID = new ArrayList<Long>();
		userProfileImageURL = new ArrayList<String>();
		sinceUserTimelineID = -1;
		sinceMentionsTimelineID = -1;
		
		adapter.updateProfileArraLists(drawableProfileImage, userID);
	}

	/**
	 * Important! It does no validation of the credentials
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
		cb.setOAuthConsumerKey(API_KEY).setOAuthConsumerSecret(API_SECRET).setOAuthAccessToken(ACCESS_TOKEN)
							.setOAuthAccessTokenSecret(TOKEN_SECRET);
		TwitterFactory tf = new TwitterFactory(cb.build());
		result = tf.getInstance();

		return result;
	}

	/**
	 * Test for network connectivity.
	 * Execute the TwitterHandler to get the tweets.
	 */
	public void getTweets()
	{
		Log.d(LOG_TAG_TWITTER_HANDLER, "Updating tweets");

		ConnectivityManager connectionManager = (ConnectivityManager) fragmentContext
							.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected())
		{
			Toast.makeText(fragmentContext, "Updating Tweets", Toast.LENGTH_LONG).show();
			ProgressBar v = (ProgressBar) view.findViewById(R.id.twitter_refreshBar);
			
			if(v == null)
				Log.d(LOG_TAG_TWITTER_HANDLER, "Null");
			else
				v.setVisibility(ProgressBar.VISIBLE);
			
			asynchTwitter = new AsynchTwitter();
			asynchTwitter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		else
		{
			// Log, and let the user know that there isn't any network
			// connections.
			Toast.makeText(fragmentContext, "No network connection available.", Toast.LENGTH_LONG).show();
			Log.i(LOG_TAG_TWITTER_HANDLER, "No network connection available.");
		}
	}

	public void cancel()
	{
		if (asynchTwitter != null)
			asynchTwitter.cancel(true);
	}
	
	public class AsynchTwitter extends AsyncTask<Void, Void, List<twitter4j.Status>>
	{
		private static final String LOG_TAG_ASYNCH_TWITTER = "AsynchTwitter";

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

			if (verifyCredentials())
			{
				// Get the user and mentions timeline, then merge them into one sorted timeline
				List<twitter4j.Status> tweets = getTimelines();

				doProfileImages(tweets);

				cleanup(tweets);

				return tweets;
			}
			else
			{
				Log.d(LOG_TAG_ASYNCH_TWITTER, "Invalid Twitter Credentials!");
				return null;
			}
		}
		
		/**
		 * Test whether the twitter keys, secrets and tokens are valid before getting timelines
		 * 
		 * @return True if twitter credentials are valid else false
		 */
		public boolean verifyCredentials()
		{
			try
			{
				twitter.verifyCredentials();
				return true;
			}
			catch (TwitterException e)
			{
				return false;
			}
		}

		private void cleanup(List<twitter4j.Status> tweets)
		{
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

		}

		/**
		 * Store a list of already retrieved profile images to reduce the network cost
		 * Add the images to the front of the list. So that the last image in the list is
		 * one that isn't
		 * used a lot.
		 * If the list gets to big, delete the least frequently used images (at the back of
		 * the list)
		 * 
		 * @param tweets
		 */
		private void doProfileImages(List<twitter4j.Status> tweets)
		{

			for (twitter4j.Status tweet : tweets)
			{
				String imageURL = tweet.getUser().getOriginalProfileImageURL();
				Drawable drawable;

				int indexOf = userID.indexOf(tweet.getUser().getId());
				// not in list
				if (indexOf == -1)
				{
					Log.d(LOG_TAG_ASYNCH_TWITTER, "NEW PROFILE IMAGES");

					drawable = getUserProfileImage(imageURL);

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
						drawable = getUserProfileImage(imageURL);

						Log.d(LOG_TAG_ASYNCH_TWITTER, "NEW cache PROFILE IMAGE");
					}
					else
					{
						// Get old image, so we can move it to the front of the list
						drawable = drawableProfileImage.get(indexOf);
						imageURL = userProfileImageURL.get(indexOf);

						Log.d(LOG_TAG_ASYNCH_TWITTER, "move image to front");
					}

					// Move the image to the front of the list.
					if (indexOf != 0)
					{
						drawableProfileImage.remove(indexOf);
						drawableProfileImage.add(0, drawable);

						userProfileImageURL.remove(indexOf);
						userProfileImageURL.add(0, imageURL);

						long tempID = userID.get(indexOf);
						userID.remove(indexOf);
						userID.add(0, tempID);
					}
				}

			}

		}

		private Drawable getUserProfileImage(String imageURL)
		{
			URL url;
			InputStream content;
			Drawable result = null;
			try
			{
				// Get the users profile image
				url = new URL(imageURL);

				content = (InputStream) url.openStream();
				result = Drawable.createFromStream(content, "src");
			}
			catch (IOException e)
			{
				Log.e(LOG_TAG_ASYNCH_TWITTER, "Error retrieving profile image", e);
			}

			return result;
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
				Log.d(LOG_TAG_ASYNCH_TWITTER, "comparing: " + tweet.getId());

				for (int i = 0; i <= result.size(); i++)
				{
					if (i == result.size())
					{
						// List is empty or we reached the end of the list, so add normally
						Log.d(LOG_TAG_ASYNCH_TWITTER, "Added: " + tweet.getId());
						result.add(tweet);
						break;
					}
					else if (result.get(i).getCreatedAt().compareTo(tweet.getCreatedAt()) < 0)
					{
						// Add the tweet in its correct position (sort by date)
						Log.d(LOG_TAG_ASYNCH_TWITTER, "Added: " + tweet.getId() + " to: " + i);
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
					Log.d(LOG_TAG_ASYNCH_TWITTER, "new user timeline");
				}
				else
				// Only get newest tweets
				{
					Paging paging = new Paging(1, timelineSize).sinceId(sinceUserTimelineID);
					result = twitter.getUserTimeline(paging);
					Log.d(LOG_TAG_ASYNCH_TWITTER, "caching user timeline");
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
					Log.d(LOG_TAG_ASYNCH_TWITTER, "new mentions");
				}
				else
				{
					Paging paging = new Paging(1, 20).sinceId(sinceMentionsTimelineID);
					result = twitter.getMentionsTimeline(paging);
					Log.d(LOG_TAG_ASYNCH_TWITTER, "caching mentions");
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
				
				ListView list = (ListView) view.findViewById(R.id.twitter_list);
				list.smoothScrollToPosition(0);
				
				// Let the user know that the update is complete
				Toast.makeText(fragmentContext, "Update Complete", Toast.LENGTH_LONG).show();
			}
			else
			{
				Toast.makeText(fragmentContext, "No new Tweets", Toast.LENGTH_LONG).show();
			}

			Log.d(LOG_TAG_TWITTER_HANDLER, "Update Complete");

			view.findViewById(R.id.twitter_refreshBar).setVisibility(ProgressBar.GONE);
		}
	}
}
