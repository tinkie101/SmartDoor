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

public class TwitterArrayAdapter extends ArrayAdapter<Tweet>
{

	private static final String LOG_TAG_TWITTER_ARRAY_ADAPTER = "TwitterArrayAdapter";
	private Context context;
	private List<Tweet> objects;
	private int resource;

	private ArrayList<Drawable> drawableProfileImage;

	public TwitterArrayAdapter(Context context, int resource,
			List<Tweet> objects, ArrayList<Drawable> drawableProfileImage)
	{
		super(context, resource, objects);
		this.context = context;
		this.objects = objects;
		this.resource = resource;

		this.drawableProfileImage = drawableProfileImage;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final Tweet tweet = objects.get(position);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(resource, null);

		ImageView image = (ImageView) view.findViewById(R.id.imageView1);

		try
		{
			if (drawableProfileImage.size() > position
					&& drawableProfileImage.get(position) != null)
				image.setImageDrawable(drawableProfileImage.get(position));
		}
		catch (NullPointerException e)
		{

		}
		catch (IndexOutOfBoundsException e)
		{
			Log.d(LOG_TAG_TWITTER_ARRAY_ADAPTER, e.toString());
		}

		TextView screenName = (TextView) view.findViewById(R.id.textView1);
		screenName.setText(tweet.getUser().getScreenName());

		TextView text = (TextView) view.findViewById(R.id.textView2);
		text.setText(tweet.getText());

		return view;
	}
}
