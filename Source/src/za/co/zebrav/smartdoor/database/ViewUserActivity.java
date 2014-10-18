package za.co.zebrav.smartdoor.database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import za.co.zebrav.smartdoor.AbstractActivity;
import za.co.zebrav.smartdoor.GlobalApplication;
import za.co.zebrav.smartdoor.R;
import za.co.zebrav.smartdoor.TTS;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

public class ViewUserActivity extends AbstractActivity
{
	private static final String TAG = "Database::ViewUserActivity";
	protected EditText searchText;
	protected ListView userList;
	protected ArrayList<User> users = new ArrayList<User>();
	private AlertDialog.Builder alert;

	protected TTS textToSpeech;
	ListViewAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_user);
		
		alert = new AlertDialog.Builder(this);
		textToSpeech = new TTS(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		// get GUI components
		searchText = (EditText) findViewById(R.id.searchText);
		userList = (ListView) findViewById(R.id.list);

		// get database info
		List<Object> result = activityDatabase.load(new User(null, null, null, null, null, 0, null));

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
		speakOut("Delete ALL Users? Are you sure?");
		alert.setTitle("Are you sure you want to delete ALL users?");
		alert.setNegativeButton("Cancel", null);
		alert.setPositiveButton("Delete", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				activityDatabase.delete(new User(null, null, null, null, null, 0, null));

				File path = getDir("data", 0);
				File dir = new File(path + "/photos/");
				if (dir.exists())
				{
					String[] children = dir.list();
					for (int i = 0; i < children.length; i++)
					{
						File file = new File(path + "/photos/" + children[i]);
						Log.d(TAG, "file name:" + file.toString());
						if (file.exists())
						{
							file.delete();
							Log.d(TAG, "file found");
						}
					}
					// The directory is now empty so delete it
					dir.delete();
				}

				adapter.clearDisplay();
			}
		});

		alert.show();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		textToSpeech.destroy();
	}
	
	@Override
	protected void onPause() 
	{
		GlobalApplication application = (GlobalApplication)getApplication();
		application.trainPersonRecogniser(activityDatabase);
		super.onPause();
	}
	
	/**
	 * @param text
	 *            , The text to be spoken out loud by the device
	 */
	public void speakOut(String text)
	{
		textToSpeech.talk(text);
	}

}
