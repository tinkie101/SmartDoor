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
	 * @param resource
	 * @param objects
	 * @param drawableProfileImage
	 * @param userID
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
	 * 
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		twitter4j.Status tweet = objects.get(position);

		LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(resource, null);

		ImageView image = (ImageView) view.findViewById(R.id.twitterUserImage);

		// Get the user's profile image, if it doesn't exists then leave it as the defualt image
		int imagePosition = userID.indexOf(tweet.getUser().getId());
		if (imagePosition != -1)
		{
			image.setImageDrawable(drawableProfileImage.get(imagePosition));
		}

		// Set the text to display the user's twitter handle
		TextView screenName = (TextView) view.findViewById(R.id.twitterUserHandle);
		screenName.setText("@" + tweet.getUser().getScreenName());

		// Set the text to display the date on which the tweet was created.
		TextView dateText = (TextView) view.findViewById(R.id.twitterPostDateTime);

		// TODO remove the depricated toGMTString()
		String date = tweet.getCreatedAt().toGMTString();

		if (date.contains("+"))
		{
			// Use \\+ to escape the special character (working with regular
			// expression?)
			String dateString[] = date.split("\\+");

			dateString[1] = dateString[1].substring(dateString[1].indexOf(' ') + 1);
			dateText.setText(dateString[0] + dateString[1]);
		}
		else
		{
			dateText.setText(date);
		}

		// Display the actual text of the tweet
		TextView text = (TextView) view.findViewById(R.id.twitterUserText);
		text.setText(tweet.getText());

		return view;
	}

	/**
	 * Adds a new tweet to the top of the list, then refreshes the list view to display the new data
	 * 
	 * @param tweet
	 */
	public void addTweetToTop(twitter4j.Status tweet)
	{
		objects.add(0, tweet);
		notifyDataSetChanged();
	}

	/**
	 * Adds a list of new tweets to the top of the list, then refreshes the list view to display the
	 * new data
	 * 
	 * @param newTweets
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
