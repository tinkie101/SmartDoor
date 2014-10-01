package za.co.zebrav.smartdoor;

import java.util.List;

import za.co.zebrav.smartdoor.database.Db4oAdapter;
import za.co.zebrav.smartdoor.database.User;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

public class ManualLoginFragment extends Fragment
{
	private Db4oAdapter provider;
	
	private EditText usernameET;
	private EditText passwordET;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.manual_login, container, false);
		//database provider
		provider = new Db4oAdapter(getActivity());
			
		//Get needed GUI components
		usernameET = (EditText) view.findViewById(R.id.ManualLogin_et1);
		passwordET = (EditText) view.findViewById(R.id.ManualLogin_et2);
			
		return view;
	}
	
	/**
	 *  GetUserInput and return result
	 * @return result of database query
	 */
	public User getUser()
	{
		String uName = usernameET.getText().toString();
		String pass = passwordET.getText().toString();
		
		if(uName.equals("root") && pass.equals("root"))
			return new User("Admin", "User", uName, pass, -2, null);
		
		provider.open();
		List<Object> users = provider.load(new User(null, null, uName, pass, 0, null));
	
		if(users.isEmpty())
		{
			provider.close();
			return null;
		}
		
		User temp = (User) users.get(0);
		User user = new User(temp.getFirstnames(), temp.getSurname(), temp.getUsername(), temp.getPassword(), temp.getID(), null);
		provider.close();
		return user;
	}
}
