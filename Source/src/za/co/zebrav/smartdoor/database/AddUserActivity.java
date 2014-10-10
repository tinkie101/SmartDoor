package za.co.zebrav.smartdoor.database;


import za.co.zebrav.smartdoor.AbstractActivity;
import za.co.zebrav.smartdoor.AddVoiceFragment;
import za.co.zebrav.smartdoor.R;
import za.co.zebrav.smartdoor.facerecognition.AddCameraFragment;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class AddUserActivity extends AbstractActivity
{
	private static final String LOG_TAG = "AddUserActivity";
	// tabs (buttons)
	private Button stepOne;
	private Button stepTwo;
	private Button stepThree;

	private AlertDialog.Builder alert;
	private AddUserStepOne addUserStepOne;
	private AddVoiceFragment addVoiceFragment;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.add_user);

		alert = new AlertDialog.Builder(this);

		// initialize GUI - buttons
		stepOne = (Button) findViewById(R.id.stepOne);
		stepTwo = (Button) findViewById(R.id.stepTwo);
		stepThree = (Button) findViewById(R.id.stepThree);

		switchFragToStep1();
	}
	
	
	/**
	 * switch current frameLayout to represent the layout of step 1 - insert basic data such as name etc.
	 */
	public void switchFragToStep1()
	{
		addUserStepOne = new AddUserStepOne();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.layoutToReplace, this.addUserStepOne);
		fragmentTransaction.commit();
		enableStep1Button();
		disableStep3Button();
	}
	
	
	//------------------------------------------------------------------------------------------Step Two
	/**
	 * switch current frameLayout to represent the layout of step 2 - Camera
	 */
	public void switchFragToStep2()
	{
		speakOut("Detecting a face. Please stand still in the middel of the front camera's view.");
		//alter tabs
		enableStep2Button();
		disableStep1Button();
		
		AddCameraFragment f = new AddCameraFragment(getUser().getID());
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.layoutToReplace, f);
		fragmentTransaction.commit();
	}
	
	/**
	 * switch current frameLayout to represent the layout of step 3 - Voice
	 */
	public void switchFragToStep3()
	{	
		enableStep3Button();
		disableStep2Button();
		
		addVoiceFragment = new AddVoiceFragment();

		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.layoutToReplace, addVoiceFragment);
		fragmentTransaction.commit();
	}
	
	//------------------------------------------------------------------------------------------Step Three
	/**
	 * This function is called the moment the user presses the 'Done' button at step 3
	 * User is saved, inputs are cleared, switch to step 1 for a different new user
	 * 
	 * @param v
	 */
	public void doneStepThreeAddUser()
	{
		saveUser();
		speakOut("Successfully added user. Another user can be added or click cancel to cancel adding another user.");
		Toast.makeText(this.getApplicationContext(), "Saved new user successfully", Toast.LENGTH_SHORT).show();
		switchFragToStep1();
	}
	
	
	//-------------------------------------------------------------------------------------------OTHER
	/**
	 * This function is called the moment the user presses the 'Cancel' button at the top left.
	 * This function exits the current activity, removing it from the stack.
	 * 
	 * @param v
	 */
	public void goBack(View v)
	{
		this.finish();
	}
	
	/**
	 * Alerts the specified message in dialogue box.
	 */
	public void alertMessage(String message)
	{
		alert.setTitle("Alert").setMessage(message).setNeutralButton("OK", null).show();
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

}
