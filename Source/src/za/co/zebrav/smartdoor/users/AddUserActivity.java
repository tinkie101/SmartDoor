package za.co.zebrav.smartdoor.users;

import za.co.zebrav.smartdoor.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class AddUserActivity extends Activity
{
	private android.app.FragmentManager fm;
	private android.app.FragmentTransaction ft;
	
	//tabs (buttons)
	private Button stepOne;
	private Button stepTwo;
	private Button stepThree;
	
	private String fname;
	private String sname;
	private String uname;
	private String password;
	
	private AlertDialog.Builder alert;
	private AddUserStepOne addUserStepOne;
	private UserProvider provider;
	//UserProvider provider;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_user);
		
		provider = new UserProvider(this);
		
		//provider = new UserProvider(this);
		alert = new AlertDialog.Builder(this);
		addUserStepOne = new AddUserStepOne();
		
		//initialize GUI - buttons
		stepOne = (Button) findViewById(R.id.stepOne);
		stepTwo = (Button) findViewById(R.id.stepTwo);
		stepThree = (Button) findViewById(R.id.stepThree);
		
		//this part is necessary to avoid null pointer exceptions when EditText.getText() is used
		 //Dialog dialog = new Dialog(this);
         //dialog.setContentView(R.layout.add_user_step_one);
		
		//start with 
		fm = getFragmentManager();
		switchFragToStep1();   
	}
	
	/**
	 * This function is called the moment the user presses the 'Cancel' button at the top left.
	 * This function exits the current activity, removing it from the stack.
	 * @param v
	 */
	public void goBack(View v)
	{
		this.finish();
	}

	//----------------------------------------------------------------------------------------step1 - transfer to step 2
	/**
	 * This function is called the moment the user presses the 'Done' button at step 1
	 * @param v
	 */
	public void doneStepOneAddUser(View v)
	{
		if(validateStep1())
		{
			getValidUserStep1Info();
			addUserStepOne.clearEditBoxes();
			switchFragToStep2();
			enableStep2Button();
			disableStep1Button();
		}
	}
	
	
	
	/**
	 * 
	 */
	private void getValidUserStep1Info()
	{
		this.fname = addUserStepOne.getFirstName();
		this.sname = addUserStepOne.getSurname();
		this.uname = addUserStepOne.getUsername();
		this.password = addUserStepOne.getPass();
	}
	
	/**
	 * Make sure the user name does not exists already
	 * check that all fields are filled in
	 * check that password1 and password2 match
	 * @return boolean
	 */
	public boolean validateStep1()
	{
		boolean valid = true;
		
		//Check if username already exists
		valid = !usernameExists();
		if(!valid)
		{
			alertMessage("A user with that username already exists!");
			return false;
		}
		
		//Check if all fields are filled
		valid = addUserStepOne.allFieldsFilled();
		if(!valid)
		{
			alertMessage("Empty field!");
			return false;
		}
		
		//check if the passwords match
		valid = addUserStepOne.passMatch();
		if(!valid)
		{
			alertMessage("Passwords do not match!");
			return false;
		}
		
		return valid;
	}
	
	/**
	 * checks in the database if entered username already exists
	 * @return
	 */
	public boolean usernameExists()
	{
		String username = addUserStepOne.getUsername();
		return provider.userExists(username);
	}
	
	/**
	 * Alerts the specified message in dialogue box.
	 */
	public void alertMessage(String message)
	{
		alert.setTitle("Alert").setMessage(message).setNeutralButton("OK", null).show();
	}
	
	/**
	 * switch current frameLayout to represent the layout of step 2 - Camera
	 */
	public void switchFragToStep2()
	{
		AddUserStepTwo fv = new AddUserStepTwo();
		
		ft = fm.beginTransaction();
		ft.replace(R.id.layoutToReplace, fv);
		ft.commit();
	}
	
	/**
	 * When the activity starts, tab of step 2 and 3 are disabled 
	 * This is to ensure that step 1 is firsts completed successfully before user is able to proceed
	 */
	public void enableStep2Button()
	{
		stepTwo.setEnabled(true);
	}
	
	/**
	 * When in step 1 tab and move to step 2, step 1 is disabled to better show switch to step 2
	 */
	public void disableStep1Button()
	{
		stepOne.setEnabled(false);
	}
	
	//----------------------------------------------------------------------------------------step2 - transfer to step 3
	/**
	 * This function is called the moment the user presses the 'Done' button at step 2
	 * @param v
	 */
	public void doneStepTwoAddUser(View v)
	{
		switchFragToStep3();
		enableStep3Button();
		disableStep2Button();
	}
	
	/**
	 * switch current frameLayout to represent the layout of step 3 - Twitter
	 */
	public void switchFragToStep3()
	{
		AddUserStepThree fv = new AddUserStepThree();
		ft = fm.beginTransaction();
		ft.replace(R.id.layoutToReplace, fv);
		ft.commit();
	}
	
	/**
	 * When the activity starts, tab of step 2 and 3 are disabled 
	 * This is to ensure that step 1 and 2 is firsts completed successfully before user is able to proceed
	 */
	public void enableStep3Button()
	{
		stepThree.setEnabled(true);
	}
	
	/**
	 * When in step 2 tab and move to step 3, step 2 is disabled to better show transition between steps
	 */
	public void disableStep2Button()
	{
		stepTwo.setEnabled(false);
	}
	
	//----------------------------------------------------------------------------------------step3 - save user
	/**
	 * This function is called the moment the user presses the 'Done' button at step 3
	 * User is saved, inputs are cleared, switch to step 1 for a different new user
	 * @param v
	 */
	public void doneStepThreeAddUser(View v)
	{
		saveNewUser();
		Toast.makeText(this.getApplicationContext(), "Saved new user successfully", Toast.LENGTH_SHORT).show();
		switchFragToStep1();
		enableStep1Button();
		disableStep3Button();
		clearInputData();
	}
	
	/**
	 * User is saved to database
	 */
	private void saveNewUser()
	{
		User user = new User(fname, sname, uname, password);
		provider.saveUser(user);
	}

	/**
	 * User input in editTexts needs to be cleared, 
	 * as well private strings containing data
	 */
	private void clearInputData()
	{
		fname = "";
		sname = "";
		uname = "";
		password = "";
	}
	
	
	/**
	 * switch current frameLayout to represent the layout of step 1 - insert basic data such as name etc.
	 */
	public void switchFragToStep1()
	{
		ft = fm.beginTransaction();
		ft.replace(R.id.layoutToReplace, this.addUserStepOne);
		ft.commit();
	}
	
	/**
	 * Enables tab Step 1 to help the vision of effect of transition to step 1
	 */
	public void enableStep1Button()
	{
		stepOne.setEnabled(true);
	}
	
	/**
	 * To disable the tab representing step 3 again after a user has been stored.
	 */
	public void disableStep3Button()
	{
		stepThree.setEnabled(false);
	}
	
	
	
	
	
}
