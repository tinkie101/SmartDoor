package za.co.zebrav.smartdoor.twitter;

import java.util.ArrayList;
import java.util.List;

import za.co.zebrav.smartdoor.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author tinkie101
 * 
 */
public class TwitterArrayAdapter extends ArrayAdapter<twitter4j.Status>
{

	private static final String LOG_TAG_TWITTER_ARRAY_ADAPTER = "TwitterArrayAdapter";
	private Context context;
	private List<twitter4j.Status> objects;
	private int resource;

	private ArrayList<Drawable> drawableProfileImage;
	private ArrayList<Long> userID;

	/**
	 * Constructor for the ArrayAdapter
	 * 
	 * @param context
	 *            The context of the Activity we are working with
	 * @param resource
	 *            The layout used by the ListView
	 * @param objects
	 *            The list of tweets
	 * @param drawableProfileImage
	 *            The list of profile images
	 * @param userID
	 *            The list of user ID's that tweeted a tweet
	 */
	public TwitterArrayAdapter(Context context, int resource, List<twitter4j.Status> objects,
						ArrayList<Drawable> drawableProfileImage, ArrayList<Long> userID)
	{
		super(context, resource, objects);
		this.context = context;
		this.objects = objects;
		this.resource = resource;

		this.drawableProfileImage = drawableProfileImage;
		this.userID = userID;
	}

	/**
	 * Updates the ListView's items.
	 * Returns the view object to display
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		// Get the tweet in the object list based on the position of the view's postion
		twitter4j.Status tweet = objects.get(position);

		// Get the view
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(resource, null);

		// Get the user's profile image, if it doesn't exists then leave it as the defualt image
		int imagePosition = userID.indexOf(tweet.getUser().getId());
		if (imagePosition != -1)
		{
			ImageView image = (ImageView) view.findViewById(R.id.twitterUserImage);
			image.setImageDrawable(drawableProfileImage.get(imagePosition));
		}

		// Set the text to display the user's twitter handle
		TextView screenName = (TextView) view.findViewById(R.id.twitterUserHandle);
		screenName.setText("@" + tweet.getUser().getScreenName());

		// Set the text to display the date on which the tweet was created.
		TextView dateText = (TextView) view.findViewById(R.id.twitterPostDateTime);

		String date = tweet.getCreatedAt().toString();

		// Remove the unwanted parts
		String dateString[] = date.split(" ");
		dateText.setText(dateString[0] + " " + dateString[1] + " " + dateString[2] + " " + dateString[3] + " "
							+ dateString[5]);

		// Display the actual text of the tweet
		TextView text = (TextView) view.findViewById(R.id.twitterUserText);
		text.setText(tweet.getText());

		return view;
	}

	/**
	 * Adds a new tweet to the top of the list, then refreshes the list view to display the new data
	 * 
	 * @param tweet
	 *            The tweet that needs to be added
	 */
	public void addTweetToTop(twitter4j.Status tweet)
	{
		objects.add(0, tweet);
		notifyDataSetChanged();
	}

	/**
	 * Adds a list of new tweets to the top of the list, then refreshes the list view to display the
	 * new data
	 * ListView lv = (ListView) findViewById(android.R.id.list);
	 * lv.smoothScrollToPosition(0);
	 * 
	 * @param newTweets
	 *            The list of new tweets to add
	 */
	public void addTweetsToTop(List<twitter4j.Status> newTweets)
	{
		if (newTweets != null)
		{
			objects.addAll(0, newTweets);
			notifyDataSetChanged();
		}
	}

	/**
	 * Clear all the data.
	 */
	public void clearData()
	{
		Log.d(LOG_TAG_TWITTER_ARRAY_ADAPTER, "Cleared Data");
		objects.clear();
		drawableProfileImage.clear();
		userID.clear();
		notifyDataSetChanged();
	}
}
