package za.co.zebrav.smartdoor.users;

import java.util.ArrayList;
import java.util.Locale;

import za.co.zebrav.smartdoor.R;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

public class ViewUserActivity extends Activity
{
	protected EditText searchText;
	protected ListView userList;
	protected String[] test;
	protected ArrayList<User> users = new ArrayList<User>();
	
	ListViewAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_user);
		
		searchText = (EditText) findViewById (R.id.searchText);
        userList = (ListView) findViewById (R.id.list);
        
        test = new String[] { "chess", "chessboard", "down", "downhill", "uphill", "uptown", "fill", "filling", "fillingstation", "tooth filling" };
        
        for (int i = 0; i < test.length; i++) 
		{
			User us = new User(test[i]);
			// Binds all strings into an array
			users.add(us);
		}
        
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
	}
	
	public void search(View view) 
	{
		
	}
}
