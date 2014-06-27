//Authors:
//Eduan Bekker - 12214834
//Zuhnja Riekert - 12040593
//Albert Volschenk - 12054519

//This is the main activity for the Smart Door Application
package za.co.zebrav.smartdoor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity
{
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	/*
	 * On click button handler.
	 * Go to the TwitterActivity.
	 */
	public void gotoTwitter(View v)
	{
		Intent intent = new Intent(this, TwitterActivity.class);
		startActivity(intent);
	}
	
}
