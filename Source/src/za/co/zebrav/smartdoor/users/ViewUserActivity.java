package za.co.zebrav.smartdoor.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import za.co.zebrav.smartdoor.R;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.graphics.Color;

public class ViewUserActivity extends Activity
{
	protected EditText searchText;
	protected ListView userList;
	protected ArrayList<User> users = new ArrayList<User>();
	protected UserProvider provider;
	
	ListViewAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_user);
		
		//create new database userProvider
		provider = new UserProvider(this);
		
		//get GUI components
		searchText = (EditText) findViewById (R.id.searchText);
        userList = (ListView) findViewById (R.id.list);
        
        //get database info
        List<User> result = provider.getListOfAllUsers();
        
        for(int i = 0; i < result.size(); i++)
        	users.add(result.get(i));
        
        // Pass results to ListViewAdapter Class
        adapter = new ListViewAdapter(this, users);
        
        // Binds the Adapter to the ListView
 		userList.setAdapter(adapter);
     	
 		// Capture Text in EditText
		searchText.addTextChangedListener(new TextWatcher() 
		{
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				String text = searchText.getText().toString().toLowerCase(Locale.getDefault());
				adapter.filter(text);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
			}
		});
		provider.close();
	}

}
