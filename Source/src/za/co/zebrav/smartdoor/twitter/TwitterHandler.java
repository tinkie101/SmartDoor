package za.co.zebrav.smartdoor.twitter;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import za.co.zebrav.smartdoor.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class TwitterHandler
{
	private static final String LOG_TAG_TWITTER_HANDLER = "TwitterHandler";
	private FragmentActivity fragmentContext;
	private AsynchTwitter asynchTwitter;
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

	public TwitterHandler(FragmentActivity fragmentContext, TwitterArrayAdapter adapter)
	{
		this.fragmentContext = fragmentContext;
		this.adapter = adapter;

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
			fragmentContext.findViewById(R.id.twitter_refreshBar).setVisibility(ProgressBar.VISIBLE);
			asynchTwitter = new AsynchTwitter(this);
			asynchTwitter.execute();
		}
		else
		{
			// Log, and let the user know that there isn't any network
			// connections.
			// Can change this to whatever notification works best
			Toast.makeText(fragmentContext, "No network connection available.", Toast.LENGTH_LONG).show();
			Log.i(LOG_TAG_TWITTER_HANDLER, "No network connection available.");
		}
	}

	public void cancel()
	{
		if (asynchTwitter != null)
			asynchTwitter.cancel(true);
	}

	public void processFinish(List<Status> result)
	{
		// If there is new tweets to add
		if (result != null && result.size() > 0)
		{
			// Add to the top of the list, and scroll the list view to the top position
			adapter.addTweetsToTop(result);

			// Let the user know that the update is complete
			Toast.makeText(fragmentContext, "Update Complete", Toast.LENGTH_LONG).show();
		}
		else
		{
			Toast.makeText(fragmentContext, "No new Tweets", Toast.LENGTH_LONG).show();
		}

		Log.d(LOG_TAG_TWITTER_HANDLER, "Update Complete");

		fragmentContext.findViewById(R.id.twitter_refreshBar).setVisibility(ProgressBar.GONE);
	}

}
