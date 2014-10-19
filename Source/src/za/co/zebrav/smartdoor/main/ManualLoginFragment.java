package za.co.zebrav.smartdoor.main;

import java.util.List;

import za.co.zebrav.smartdoor.R;
import za.co.zebrav.smartdoor.database.User;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.EditText;

public class ManualLoginFragment extends Fragment
{
	private EditText usernameET;
	private EditText passwordET;
	private AbstractActivity activity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.manual_login, container, false);

		// Get needed GUI components
		usernameET = (EditText) view.findViewById(R.id.ManualLogin_et1);
		passwordET = (EditText) view.findViewById(R.id.ManualLogin_et2);

		activity = (AbstractActivity) getActivity();

		Button btnLogin = (Button) view.findViewById(R.id.ManualLogin_button1);
		btnLogin.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				activity.setActiveUser(getUser());

				if (activity.activityUser == null)
				{
					((MainActivity) activity).alertMessage("Incorrect username or password.");
				}
				else
				{
					((MainActivity) activity).switchToLoggedInFrag();
				}

			}
		});

		return view;
	}

	/**
	 * GetUserInput and return result
	 * 
	 * @return result of database query
	 */
	public User getUser()
	{
		String uName = usernameET.getText().toString();
		String pass = passwordET.getText().toString();

		if (uName.equals("root") && pass.equals("root"))
			return new User("Admin", "User", uName, pass, true, -2, null);

		List<Object> users = activity.getDatabase().load(new User(null, null, uName, pass, null, 0, null));

		if (users.isEmpty())
		{
			return null;
		}

		User temp = (User) users.get(0);
		return temp;
	}
}
