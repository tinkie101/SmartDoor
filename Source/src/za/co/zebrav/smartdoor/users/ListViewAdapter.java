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
	private List<User> useList = null;
	private ArrayList<User> arraylist;
	LayoutInflater inflater;
	
	public ListViewAdapter(Context context, List<User> userList)
	{
		mContext = context;
		this.useList = userList;
		inflater = LayoutInflater.from(mContext);
		this.arraylist = new ArrayList<User>();
		this.arraylist.addAll(userList);
	}
	
	public class ViewHolder
	{
		TextView firstName;
		TextView surname;
		TextView username;
	}
	
	@Override
	public int getCount()
	{
		return useList.size();
	}

	@Override
	public User getItem(int position)
	{
		return useList.get(position);
	}

	@Override
	public long getItemId(int pos)
	{
		return pos;
	}

	@Override
	public View getView(final int position, View view, ViewGroup parent)
	{
		final ViewHolder holder;
		if (view == null)
		{
			holder= new ViewHolder();
			view = inflater.inflate(R.layout.search_user_listview_item, null);
			holder.firstName = (TextView) view.findViewById(R.id.firstnamesTV);
			holder.surname = (TextView) view.findViewById(R.id.surnamesTV);
			holder.username = (TextView) view.findViewById(R.id.usernameTV);
			view.setTag(holder);
		} else
		{
			holder = (ViewHolder) view.getTag();
		}
		
		// Set the results into TextViews
		holder.firstName.setText(useList.get(position).getFirstnames());
		holder.surname.setText(useList.get(position).getSurname());
		holder.username.setText(useList.get(position).getUsername());
		
		view.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				Toast.makeText(mContext, useList.get(position).getFirstnames(), Toast.LENGTH_LONG).show();
			}
		});
			
		return view;
	}
	
	//--------------------------------------------------------------------------Search functionality
	/**
	 * Filter the listView via first names, surnames or even usernames
	 * @param charText
	 */
	public void filter(String charText)
	{
		charText = charText.toLowerCase(Locale.getDefault());
		useList.clear();
		if (charText.length() == 0)
		{
			useList.addAll(arraylist);
		} else
		{
			for (User us : arraylist)
			{
				if (us.getFirstnames().toLowerCase(Locale.getDefault()).contains(charText)
						|| us.getSurname().toLowerCase(Locale.getDefault()).contains(charText) 
						|| us.getUsername().toLowerCase(Locale.getDefault()).contains(charText))
				{
					useList.add(us);
				}
			}
		}
		notifyDataSetChanged();
	}
}
