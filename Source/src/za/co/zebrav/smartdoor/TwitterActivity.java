package za.co.zebrav.smartdoor;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
import android.view.MenuItem;
import android.widget.Toast;

public class TwitterActivity extends ListActivity
{
	private static final String LOG_TAG_TWITTER_ACTIVITY = "TwitterActivity";
	private ListActivity activityContext;
	Twitter twitter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twitter);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		activityContext = this;

		// TODO: Get this from somewhere else
		String API_KEY = "qcGzp08qWLEZom1x7dxCG5qu0";
		String API_SECRET = "ly810vDH1S16Ttw0mpk4ZBYQvLEF9gEO16KSqy9lBqhwRf5XRo";
		String ACCESS_TOKEN = "239453626-73H379K274Qfm9KaPfq8C3hKPhq3jqGk04gQXkIw";
		String TOKEN_SECRET = "7ozxcBnoAA4WEocKeUSrFI9iOO9hVqNYwS2xFOeB0osUl";

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(API_KEY).setOAuthConsumerSecret(API_SECRET)
					.setOAuthAccessToken(ACCESS_TOKEN).setOAuthAccessTokenSecret(TOKEN_SECRET);
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();
		getTweets();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void getTweets()
	{
		ConnectivityManager connectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected())
		{
			// execute the doInBackground() function
			new TwitterHandler().execute();
		}
		else
		{
			// Log, and let the user know that there isn't any network
			// connections, can change this to whatever notification works best
			Toast.makeText(this, "No network connection available.",
					Toast.LENGTH_LONG).show();
			Log.i(LOG_TAG_TWITTER_ACTIVITY, "No network connection available.");
		}
	}
	
	private class TwitterHandler extends AsyncTask<Void, Void, List<twitter4j.Status>>
	{
		private TwitterArrayAdapter adapter;

		@Override
		protected List<twitter4j.Status> doInBackground(Void... params)
		{
			try
			{
				List<twitter4j.Status> tweets = twitter.getMentionsTimeline();
				// List<twitter4j.Status> tweets = twitter.getUserTimeline();

				// Store a list of already retrieved profile images to reduce the
				// network cost
				ArrayList<Drawable> drawableProfileImage = new ArrayList<Drawable>();
				ArrayList<String> userID = new ArrayList<String>();
				for (twitter4j.Status tweet : tweets)
				{
					try
					{
						if (!userID.contains(tweet.getUser().getScreenName()))
						{
							String imageURL = tweet.getUser().getOriginalProfileImageURL();
							// Log.d(LOG_TAG_TWITTER_ACTIVITY, imageURL);

							URL url = new URL(imageURL);
							InputStream content = (InputStream) url.openStream();
							Drawable d = Drawable.createFromStream(content, "src");
							drawableProfileImage.add(d);
							userID.add(tweet.getUser().getScreenName());
						}
						else
						{
							drawableProfileImage.add(drawableProfileImage.get(userID.indexOf(tweet
										.getUser().getScreenName())));
						}

					}
					catch (MalformedURLException e)
					{
					}
					catch (IOException e)
					{
					}

				}
				adapter = new TwitterArrayAdapter(activityContext, R.layout.list_twitter, tweets,
							drawableProfileImage);

				return tweets;
			}
			catch (TwitterException e)
			{
				Log.e(LOG_TAG_TWITTER_ACTIVITY, "Twitter Error", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<twitter4j.Status> result)
		{
			setListAdapter(adapter);
		}
	}
}
