package za.co.zebrav.smartdoor;

import android.app.Activity;
import android.os.Bundle;

public class OpenDoor extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.door_open);
		super.onCreate(savedInstanceState);
	}
}
