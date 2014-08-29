package za.co.zebrav.smartdoor.database;

import za.co.zebrav.smartdoor.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

public class AddUserStepOne extends Fragment 
{

	//editTexts
	private EditText firstname;
	private EditText surname;
	private EditText username;
	private EditText pass1;
	private EditText pass2;
	 
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	 {
		  View view = inflater.inflate(R.layout.add_user_step_one, null);
		  
		  //editText GUI
		  firstname = (EditText) view.findViewById(R.id.addUser_fname_et);
		  surname = (EditText) view.findViewById(R.id.addUser_sname_et);
		  username = (EditText) view.findViewById(R.id.addUser_uname_et);
		  pass1 = (EditText) view.findViewById(R.id.addUser_pass1_et);
		  pass2 = (EditText) view.findViewById(R.id.addUser_pass2_et);
		  
		  return view;
	 }
	 
	 /**
	 * All editText fields where the user enters data to register must contain at least some text.
	 * @return
	 */
	public boolean allFieldsFilled()
	{
		if(firstname.getText().toString().equals(""))
			return false;
		else if(surname.getText().toString().equals(""))
			return false;
		else if(username.getText().toString().equals(""))
			return false;
		else if(pass1.getText().toString().equals(""))
			return false;
		else if(pass2.getText().toString().equals(""))
			return false;
		else
			return true;
	}
	
	/**
	 * Tests if password one and two are the same, this is necessary to minimize the chance of a user registering 
	 * with a miss spelled password. 
	 * @return boolean
	 */
	public boolean passMatch()
	{
		String p1 = pass1.getText().toString();
		String p2 = pass2.getText().toString();
		if(p1.equals(p2))
			return true;
		else 
			return false;
	}
	
	/**
	 * Sends back content of first name input box
	 * @return
	 */
	public String getFirstName()
	{
		return firstname.getText().toString();
	}
	
	/**
	 * Sends back content of surname input box
	 * @return
	 */
	public String getSurname()
	{
		return surname.getText().toString();
	}
	
	/**
	 * Sends back content of Username input box
	 * @return
	 */
	public String getUsername()
	{
		return username.getText().toString();
	}
	
	/**
	 * Sends back the chosen password
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
	}
}
