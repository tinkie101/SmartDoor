package za.co.zebrav.smartdoor;

import za.co.zebrav.smartdoor.database.AddUserActivity;
import za.co.zebrav.smartdoor.database.ViewUserActivity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class LoggedInFragment extends Fragment
{
	private ListView list;
	private ArrayAdapter<String> adapter;
	private String[] commandOptions;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.logged_in, null);
		list = (ListView) view.findViewById(R.id.commandList);
		commandOptions =  getResources().getStringArray(R.array.commandOptions); 
		adapter = new ArrayAdapter<String>(getActivity().getBaseContext(), R.layout.command_list_item, commandOptions);
		list.setAdapter(adapter);
		
		setOnclickListener();
		return view;
	}
	
	public void setOnclickListener()
	{
		list.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{	
				String selectedName = adapter.getItem(position);
				
				if(selectedName.equals("Open door"))
				{
					openDoor();
				}
				else if(selectedName.equals("Add user"))
				{
					addUser();
				}
				else if(selectedName.equals("Remove user"))
				{
					searchUser();
				}
				else if(selectedName.equals("Search user"))
				{
					searchUser();
				}
				else if(selectedName.equals("Twitter setup"))
				{
					twitterSetup();
				}
			}
		});
	}
	//----------------------------------------------------------------------------Execution of commands
	private void openDoor()
	{
		Toast.makeText(getActivity(), "Openning the door", Toast.LENGTH_SHORT).show();
	}
	
	private void addUser()
	{
		Intent intent = new Intent(getActivity(), AddUserActivity.class);
		startActivity(intent);
	}
	
	private void searchUser()
	{
		Intent intent = new Intent(getActivity(),ViewUserActivity.class);
		startActivity(intent);
	}
	
	private void twitterSetup()
	{
		Toast.makeText(getActivity(), "Twitter setup activity must still be created", Toast.LENGTH_SHORT).show();
	}
}
