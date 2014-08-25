package za.co.zebrav.smartdoor.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import za.co.zebrav.smartdoor.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ListViewAdapter extends BaseAdapter
{
	Context mContext;
	private List<User> userpopulationlist = null;
	private ArrayList<User> arraylist;
	LayoutInflater inflater;
	
	public ListViewAdapter(Context context, List<User> userpopulationlist)
	{
		mContext = context;
		this.userpopulationlist = userpopulationlist;
		inflater = LayoutInflater.from(mContext);
		this.arraylist = new ArrayList<User>();
		this.arraylist.addAll(userpopulationlist);
	}
	
	public class ViewHolder
	{
		TextView test;
	}
	
	@Override
	public int getCount()
	{
		return userpopulationlist.size();
	}

	@Override
	public User getItem(int position)
	{
		return userpopulationlist.get(position);
	}

	@Override
	public long getItemId(int pos)
	{
		return pos;
	}

	@Override
	public View getView(final int position, View view, ViewGroup parent)
	{
		final ViewHolder holderFirstname;
		if (view == null)
		{
			holderFirstname = new ViewHolder();
			view = inflater.inflate(R.layout.search_user_listview_item, null);
			// Locate the TextViews in listview_item.xml
			holderFirstname.test= (TextView) view.findViewById(R.id.firstnamesTV);
			view.setTag(holderFirstname);
		} else
		{
			holderFirstname = (ViewHolder) view.getTag();
		}
		
		// Set the results into TextViews
		holderFirstname.test.setText(userpopulationlist.get(position).getFirstnames());
		
		view.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				Toast.makeText(mContext, userpopulationlist.get(position).getFirstnames(), Toast.LENGTH_LONG).show();
			}
		});
			
		return view;
	}
	
	// Filter Class
	public void filter(String charText)
	{
		charText = charText.toLowerCase(Locale.getDefault());
		userpopulationlist.clear();
		if (charText.length() == 0)
		{
			userpopulationlist.addAll(arraylist);
		} else
		{
			for (User us : arraylist)
			{
				if (us.getFirstnames().toLowerCase(Locale.getDefault())
						.contains(charText)) 
				{
					userpopulationlist.add(us);
				}
			}
		}
		notifyDataSetChanged();
	}

}
