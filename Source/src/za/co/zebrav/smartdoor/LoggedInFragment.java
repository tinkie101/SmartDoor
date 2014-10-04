package za.co.zebrav.smartdoor;

import java.util.List;

import za.co.zebrav.smartdoor.database.Db4oAdapter;
import za.co.zebrav.smartdoor.database.User;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LoggedInFragment extends Fragment
{
	private static final String LOG_TAG = "LoggedInFragment";
	private ListView list;
	private ArrayAdapter<String> adapter;
	private String[] commandOptions;
	private static User user;
	private Db4oAdapter provider;
	private MainActivity mainActivity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.logged_in,container, false);

		mainActivity = (MainActivity) getActivity();
		
		//get Logged in user
		Bundle bundle = this.getArguments();
		int id = bundle.getInt("id", -6);
		
		if(id != -6)
		{
			if(id > 0)
			{
				provider = new Db4oAdapter(getActivity());
				provider.open();
				List<Object> temp = provider.load(new User(null, null, null, null,null, id, null));
				User t = (User)temp.get(0);
				user = new User(t.getFirstnames(), t.getSurname(), t.getUsername(), t.getPassword(),t.getAdminRights() ,t.getID(), t.getCodeBook());
				provider.close();
			}
			else if(id == -2)
				user = new User("Admin", "User", null, null, true, -2, null);
			mainActivity.speakOut("Welcome, " + user.getFirstnames() + " " + user.getSurname());
		}
		if(user == null)
			Log.d("User", "user null");
		else
			Log.d("User", "user NOT null");
		if(user.getAdminRights())
			commandOptions =  getResources().getStringArray(R.array.commandOptions);
		else
			commandOptions = getResources().getStringArray(R.array.basicCommandOptions);
		list = (ListView) view.findViewById(R.id.commandList);
		adapter = new ArrayAdapter<String>(getActivity().getBaseContext(), R.layout.command_list_item, commandOptions);
		list.setAdapter(adapter);
		
		setOnclickListener();
		return view;
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		mainActivity.startListeningForCommands(commandOptions);
		Log.d(LOG_TAG, "onResume");
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		mainActivity.stopListeningForCommands();
		Log.d(LOG_TAG, "onStop");
	}
	
	public void setOnclickListener()
	{
		list.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{	
				String selectedName = adapter.getItem(position);
				
				mainActivity.userCommands.executeCommand(selectedName.toLowerCase());
			}
		});
	}
	
}
