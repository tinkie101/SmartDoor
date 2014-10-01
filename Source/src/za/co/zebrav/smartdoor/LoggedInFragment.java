package za.co.zebrav.smartdoor;

import java.util.List;

import za.co.zebrav.smartdoor.database.AddUserActivity;
import za.co.zebrav.smartdoor.database.Db4oAdapter;
import za.co.zebrav.smartdoor.database.User;
import za.co.zebrav.smartdoor.database.ViewUserActivity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class LoggedInFragment extends Fragment
{
	private ListView list;
	private ArrayAdapter<String> adapter;
	private String[] commandOptions;
	private User user;
	private Db4oAdapter provider;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.logged_in, null);
		list = (ListView) view.findViewById(R.id.commandList);
		commandOptions =  getResources().getStringArray(R.array.commandOptions); 
		adapter = new ArrayAdapter<String>(getActivity().getBaseContext(), R.layout.command_list_item, commandOptions);
		list.setAdapter(adapter);
		
		//get Logged in user
		Bundle bundle = this.getArguments();
		int id = bundle.getInt("id", -6);
		
		if(id != -6)
		{
			if(id > 0)
			{
				provider = new Db4oAdapter(getActivity());
				provider.open();
				List temp = provider.load(new User(null, null, null, null, id, null));
				User t = (User)temp.get(0);
				user = new User(t.getFirstnames(), t.getSurname(), t.getUsername(), t.getPassword(), t.getID(), t.getCodeBook());
				provider.close();
			}
			else if(id == -2)
				user = new User("Admin", "User", null, null, -2, null);
		
			MainActivity m = (MainActivity) getActivity();
			//m.speakOut("Welcome, " + user.getFirstnames() + " " + user.getSurname());
		}
		
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
				else if(selectedName.equals("Settings"))
				{
					settings();
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
		MainActivity m = (MainActivity)getActivity();
		m.switchToTwitterSetup();
	}
	
	private void settings()
	{
		MainActivity m = (MainActivity)getActivity();
		m.switchToSettingsFragment();
	}
}
