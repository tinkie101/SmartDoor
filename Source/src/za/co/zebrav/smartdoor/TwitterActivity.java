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

		drawableProfileImage = new ArrayList<Drawable>();
		userID = new ArrayList<Long>();
		sinceUserTimelineID = -1;
		sinceMentionsTimelineID = -1;

		adapter = new TwitterArrayAdapter(activityContext, R.layout.list_twitter,
					new ArrayList<twitter4j.Status>(), drawableProfileImage, userID);

		setListAdapter(adapter);

		disableRefresh = false;

		getTweets();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.twitter, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				finish();
				break;

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

	private void getTweets()
	{
		ConnectivityManager connectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected())
		{
			Log.d(LOG_TAG_TWITTER_ACTIVITY, "execute");

			// MenuItem item = (MenuItem) findViewById(R.id.action_refresh_tweets);
			// item.setEnabled(false);

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
			// connections, can change this to whatever notification works best
			Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG).show();
			Log.i(LOG_TAG_TWITTER_ACTIVITY, "No network connection available.");
		}
	}

	private class TwitterHandler extends AsyncTask<Void, Void, List<twitter4j.Status>>
	{

		@Override
		protected List<twitter4j.Status> doInBackground(Void... params)
		{
			try
			{
				List<twitter4j.Status> userTimeline;
				List<twitter4j.Status> mentionsTimeline;

				// Only get new Tweets
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

				if (userTimeline.size() > 0)
					sinceUserTimelineID = userTimeline.get(0).getId();

				if (mentionsTimeline.size() > 0)
					sinceMentionsTimelineID = mentionsTimeline.get(0).getId();

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

				// Store a list of already retrieved profile images to reduce the
				// network cost
				for (twitter4j.Status tweet : tweets)
				{

					try
					{
						if (!userID.contains(tweet.getUser().getId()))
						{
							String imageURL = tweet.getUser().getOriginalProfileImageURL();
							// Log.d(LOG_TAG_TWITTER_ACTIVITY, imageURL);

							URL url = new URL(imageURL);
							InputStream content = (InputStream) url.openStream();
							Drawable d = Drawable.createFromStream(content, "src");
							drawableProfileImage.add(d);
							userID.add(tweet.getUser().getId());
						}

					}
					catch (MalformedURLException e)
					{
					}
					catch (IOException e)
					{
					}

				}
				//
				// adapter = new TwitterArrayAdapter(activityContext, R.layout.list_twitter, tweets,
				// drawableProfileImage, userID);

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
			adapter.addTweetsToTop(result);

			disableRefresh = false;
			invalidateOptionsMenu();

			Toast.makeText(activityContext, "Refreshing Complete", Toast.LENGTH_LONG).show();
		}
	}
}
