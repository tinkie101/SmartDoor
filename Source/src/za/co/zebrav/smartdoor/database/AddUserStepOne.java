package za.co.zebrav.smartdoor.database;

import java.util.List;

import za.co.zebrav.smartdoor.AbstractActivity;
import za.co.zebrav.smartdoor.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class AddUserStepOne extends Fragment
{
	private AbstractActivity activity;

	// editTexts
	private EditText firstname;
	private EditText surname;
	private EditText username;
	private EditText pass1;
	private EditText pass2;
	private CheckBox adminRights;
	private Db4oAdapter db;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.add_user_step_one, null);

		// editText GUI
		firstname = (EditText) view.findViewById(R.id.addUser_fname_et);
		surname = (EditText) view.findViewById(R.id.addUser_sname_et);
		username = (EditText) view.findViewById(R.id.addUser_uname_et);
		pass1 = (EditText) view.findViewById(R.id.addUser_pass1_et);
		pass2 = (EditText) view.findViewById(R.id.addUser_pass2_et);
		adminRights = (CheckBox) view.findViewById(R.id.giveAdminRights);

		activity = (AddUserActivity) getActivity();
		db = activity.getDatabase();
		
		Button btnDone = (Button) view.findViewById(R.id.stepOneDoneButton);
		btnDone.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (validate())
				{
					activity.setActiveUser(new User(getFirstName(), getSurname(), getUsername(), getPass(), getAminRights(),
										getPK(), null));
					((AddUserActivity) activity).switchFragToStep2();
				}
			}
		});
		
		return view;
	}

	/**
	 * User is saved to database
	 */
	private int getPK()
	{
		LastPK lastPK = null;
		int newPK = 0;

		List<Object> results = db.load(new LastPK(0));

		// If list is empty, then PK has not been instantiated yet
		if (results.isEmpty())
		{
			lastPK = new LastPK(1);
			newPK = 1;
			db.save(lastPK);
		}
		else
		{
			lastPK = (LastPK) results.get(0);
			newPK = lastPK.getPK() + 1;
			LastPK temp = new LastPK(newPK);
			db.replace(lastPK, temp);
		}
		return newPK;
	}

	/**
	 * Make sure the user name does not exists already
	 * check that all fields are filled in
	 * check that password1 and password2 match
	 * 
	 * @return boolean
	 */
	public boolean validate()
	{
		boolean valid = true;

		// Check if user name already exists
		valid = !usernameExists();
		if (!valid)
		{
			((AddUserActivity) activity).alertMessage("A user with that username already exists!");
			return false;
		}

		// Check if all fields are filled
		valid = allFieldsFilled();
		if (!valid)
		{
			((AddUserActivity) activity).alertMessage("Empty field!");
			return false;
		}

		// check if the passwords match
		valid = passMatch();
		if (!valid)
		{
			((AddUserActivity) activity).alertMessage("Passwords do not match!");
			return false;
		}

		return valid;
	}

	/**
	 * checks in the database if entered user name already exists
	 * 
	 * @return
	 */
	public boolean usernameExists()
	{
		String username = getUsername();
		boolean exists = false;
		exists = db.exists(new User(null, null, username, null, null, 0, null));
		return exists;
	}

	/**
	 * All editText fields where the user enters data to register must contain at least some text.
	 * 
	 * @return
	 */
	public boolean allFieldsFilled()
	{
		if (firstname.getText().toString().equals(""))
			return false;
		else if (surname.getText().toString().equals(""))
			return false;
		else if (username.getText().toString().equals(""))
			return false;
		else if (pass1.getText().toString().equals(""))
			return false;
		else if (pass2.getText().toString().equals(""))
			return false;
		else
			return true;
	}

	/**
	 * Tests if password one and two are the same, this is necessary to minimize the chance of a user registering
	 * with a miss spelled password.
	 * 
	 * @return boolean
	 */
	public boolean passMatch()
	{
		String p1 = pass1.getText().toString();
		String p2 = pass2.getText().toString();
		if (p1.equals(p2))
			return true;
		else
			return false;
	}

	/**
	 * Sends back content of first name input box
	 * 
	 * @return
	 */
	public String getFirstName()
	{
		return firstname.getText().toString();
	}

	public Boolean getAminRights()
	{
		return adminRights.isChecked();
	}

	/**
	 * Sends back content of surname input box
	 * 
	 * @return
	 */
	public String getSurname()
	{
		return surname.getText().toString();
	}

	/**
	 * Sends back content of Username input box
	 * 
	 * @return
	 */
	public String getUsername()
	{
		return username.getText().toString();
	}

	/**
	 * Sends back the chosen password
	 * 
	 * @return
	 */
	public String getPass()
	{
		return pass1.getText().toString();
	}

	/**
	 * clear content of boxes
	 */
	public void clearEditBoxes()
	{
		this.firstname.setText("");
		this.surname.setText("");
		this.username.setText("");
		this.pass1.setText("");
		this.pass2.setText("");
		this.adminRights.setChecked(false);
	}
}
