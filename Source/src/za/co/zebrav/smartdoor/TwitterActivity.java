package za.co.zebrav.smartdoor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import com.google.gson.Gson;

import za.co.zebrav.smartdoor.twitter.Authenticated;
import za.co.zebrav.smartdoor.twitter.Tweet;
import za.co.zebrav.smartdoor.twitter.Twitter;
import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class TwitterActivity extends ListActivity
{
	private static final String LOG_TAG_TWITTER_ACTIVITY = "TwitterActivity";
	
	//Change this to each device's twitter user name
	private String screenName = "tinkie_101";
	private ListActivity activityContext;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twitter);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		activityContext = this;
		downloadTweets();
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
	
	/*
	 * The following code is taken from: https://github.com/Rockncoder/TwitterTutorial
	 * A few minor changes was made from the above repository.
	 */
	
	// download twitter timeline after first checking to see if there is a
	// network connection
	public void downloadTweets()
	{
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected())
		{
			new TwitterHandler().execute(screenName);
		}
		else
		{
			Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG).show();
			Log.i(LOG_TAG_TWITTER_ACTIVITY, "No network connection available.");
		}
	}
	
	private class TwitterHandler extends AsyncTask<String, Void, String>
	{
		private static final String LOG_TAG_TWITTER_HANDLER = "TwitterHandler";

		//Change these keys to be each device's twitter keys. 
		final static String API_KEY = "qcGzp08qWLEZom1x7dxCG5qu0";
		final static String API_SECRET = "ly810vDH1S16Ttw0mpk4ZBYQvLEF9gEO16KSqy9lBqhwRf5XRo";
		
		final static String TwitterTokenURL = "https://api.twitter.com/oauth2/token";
		final static String TwitterStreamURL = "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=";

		@Override
		protected String doInBackground(String... screenNames)
		{
			String result = null;

			if (screenNames.length > 0)
			{
				result = getTwitterStream(screenNames[0]);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result)
		{
			Twitter twits = jsonToTwitter(result);

			// lets write the results to the console as well
			// for (Tweet tweet : twits)
			// {
			// Log.i(LOG_TAG_TWITTER_HANDLER, tweet.getText());
			// }

			// send the tweets to the adapter for rendering
			// Check this context value, maybe it should be TwitterActivity.class?
			ArrayAdapter<Tweet> adapter = new ArrayAdapter<Tweet>(activityContext, android.R.layout.simple_list_item_1, twits);
			setListAdapter(adapter);
		}

		private Twitter jsonToTwitter(String result)
		{
			Twitter twits = null;
			if (result != null && result.length() > 0)
			{
				try
				{
					Gson gson = new Gson();
					twits = gson.fromJson(result, Twitter.class);
				}
				catch (IllegalStateException ex)
				{
					// just eat the exception
				}
			}
			return twits;
		}

		// convert a JSON authentication object into an Authenticated object
		private Authenticated jsonToAuthenticated(String rawAuthorization)
		{
			Authenticated auth = null;
			if (rawAuthorization != null && rawAuthorization.length() > 0)
			{
				try
				{
					Gson gson = new Gson();
					auth = gson.fromJson(rawAuthorization, Authenticated.class);
				}
				catch (IllegalStateException ex)
				{
					// just eat the exception
				}
			}
			return auth;
		}

		private String getResponseBody(HttpRequestBase request)
		{
			StringBuilder sb = new StringBuilder();
			try
			{

				DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
				HttpResponse response = httpClient.execute(request);
				int statusCode = response.getStatusLine().getStatusCode();
				String reason = response.getStatusLine().getReasonPhrase();

				if (statusCode == 200)
				{

					HttpEntity entity = response.getEntity();
					InputStream inputStream = entity.getContent();

					BufferedReader bReader = new BufferedReader(
							new InputStreamReader(inputStream, "UTF-8"), 8);
					String line = null;
					while ((line = bReader.readLine()) != null)
					{
						sb.append(line);
					}
				}
				else
				{
					sb.append(reason);
				}
			}
			catch (UnsupportedEncodingException ex)
			{
			}
			catch (ClientProtocolException ex1)
			{
			}
			catch (IOException ex2)
			{
			}
			return sb.toString();
		}

		private String getTwitterStream(String screenName)
		{
			String results = null;

			// Step 1: Encode consumer key and secret
			try
			{
				// URL encode the consumer key and secret
				String urlApiKey = URLEncoder.encode(API_KEY, "UTF-8");
				String urlApiSecret = URLEncoder.encode(API_SECRET, "UTF-8");

				// Concatenate the encoded consumer key, a colon character, and the
				// encoded consumer secret
				String combined = urlApiKey + ":" + urlApiSecret;

				// Base64 encode the string
				String base64Encoded = Base64.encodeToString(combined.getBytes(),
						Base64.NO_WRAP);

				// Step 2: Obtain a bearer token
				HttpPost httpPost = new HttpPost(TwitterTokenURL);
				httpPost.setHeader("Authorization", "Basic " + base64Encoded);
				httpPost.setHeader("Content-Type",
						"application/x-www-form-urlencoded;charset=UTF-8");
				httpPost.setEntity(new StringEntity("grant_type=client_credentials"));
				String rawAuthorization = getResponseBody(httpPost);
				Authenticated auth = jsonToAuthenticated(rawAuthorization);

				// Applications should verify that the value associated with the
				// token_type key of the returned object is bearer
				if (auth != null && auth.token_type.equals("bearer"))
				{

					// Step 3: Authenticate API requests with bearer token
					HttpGet httpGet = new HttpGet(TwitterStreamURL + screenName);

					// construct a normal HTTPS request and include an Authorisation
					// header with the value of Bearer <>
					httpGet.setHeader("Authorization", "Bearer " + auth.access_token);
					httpGet.setHeader("Content-Type", "application/json");
					// update the results with the body of the response
					results = getResponseBody(httpGet);
				}
			}
			catch (UnsupportedEncodingException ex)
			{
			}
			catch (IllegalStateException ex1)
			{
			}
			return results;
		}
	}
}
