package za.co.zebrav.smartdoor.database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import za.co.zebrav.smartdoor.R;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.graphics.Color;

public class ViewUserActivity extends Activity
{
	private static final String TAG = "Database::ViewUserActivity";
	protected EditText searchText;
	protected ListView userList;
	protected ArrayList<User> users = new ArrayList<User>();
	protected Db4oAdapter provider;

	ListViewAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_user);

		// create new database userProvider
		provider = new Db4oAdapter(this);

		// get GUI components
		searchText = (EditText) findViewById(R.id.searchText);
		userList = (ListView) findViewById(R.id.list);

		// get database info
		provider.open();
		List<Object> result = provider.load(new User(null, null, null, null, 0, null));

		for (int i = 0; i < result.size(); i++)
		{
			users.add((User) result.get(i));
		}

		// Pass results to ListViewAdapter Class
		adapter = new ListViewAdapter(this, users);

		// Binds the Adapter to the ListView
		userList.setAdapter(adapter);

		// Capture Text in EditText
		searchText.addTextChangedListener(new TextWatcher()
		{
			// the moment some text is edited to the editText field, it is detected by this function
			@Override
			public void afterTextChanged(Editable arg0)
			{
				String text = searchText.getText().toString().toLowerCase(Locale.getDefault());
				adapter.filter(text);
			}

			// Useless function that are needs to be implemented (present) for TextWatcher
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			// Useless function that are needs to be implemented (present) for TextWatcher
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
			}
		});
		provider.close();
	}

	/**
	 * This function is called the moment the user presses the 'Cancel' button at the top left.
	 * This function exits the current activity, removing it from the stack.
	 * 
	 * @param v
	 */
	public void goBack(View v)
	{
		Log.d(TAG, "file found");
		this.finish();
	}

	public void deleteAllUsers(View v)
	{
		provider.open();
		provider.delete(new User(null, null, null, null, 0, null));
		provider.close();
		File path = getDir("data", 0);
		File dir = new File(path + "/photos/");
		String[] children = dir.list();
		for (int i = 0; i < children.length; i++)
		{
			File file = new File(path + "/photos/" + children[i]);
			Log.d(TAG, "file name:" + file.toString());
			if(file.exists())
			{
				file.delete();
				Log.d(TAG, "file found");
			}
		}
		// The directory is now empty so delete it
		dir.delete();

		adapter.clearDisplay();
	}

}
