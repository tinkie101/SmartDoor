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

public class TwitterArrayAdapter extends ArrayAdapter<twitter4j.Status>
{

	private static final String LOG_TAG_TWITTER_ARRAY_ADAPTER = "TwitterArrayAdapter";
	private Context context;
	private List<twitter4j.Status> objects;
	private int resource;

	private ArrayList<Drawable> drawableProfileImage;
	private ArrayList<Long> userID;

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

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		twitter4j.Status tweet = objects.get(position);

		LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(resource, null);

		ImageView image = (ImageView) view.findViewById(R.id.twitterUserImage);

		// Test if image isn't out of bounds or null in the array list
		try
		{
			Log.d(LOG_TAG_TWITTER_ARRAY_ADAPTER, Long.toString(tweet.getId()));
			int imagePosition = userID.indexOf(tweet.getUser().getId());
			if (imagePosition != -1)
			{
				image.setImageDrawable(drawableProfileImage.get(imagePosition));
			}
		}
		catch (NullPointerException e)
		{
			// Log.d(LOG_TAG_TWITTER_ARRAY_ADAPTER, e.toString());
		}
		catch (IndexOutOfBoundsException e)
		{
			// Log.d(LOG_TAG_TWITTER_ARRAY_ADAPTER, e.toString());
		}

		TextView screenName = (TextView) view.findViewById(R.id.twitterUserHandle);
		screenName.setText("@" + tweet.getUser().getScreenName());

		TextView dateText = (TextView) view.findViewById(R.id.twitterPostDateTime);

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
			dateText.setText(date);

		TextView text = (TextView) view.findViewById(R.id.twitterUserText);
		text.setText(tweet.getText());

		return view;
	}
	
	public void addTweetToTop()
	{
		objects.add(0, objects.get(0));
		notifyDataSetChanged();
	}
	
	public void addTweetsToTop(List<twitter4j.Status> newTweets)
	{
		objects.addAll(0, newTweets);
		notifyDataSetChanged();
	}
}
