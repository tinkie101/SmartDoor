package za.co.zebrav.smartdoor;

import za.co.zebrav.smartdoor.database.User;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class OpenDoor extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.door_open);
		super.onCreate(savedInstanceState);
		
		User user = null;
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null)
			user = (User) bundle.getSerializable("user");
		
		Toast.makeText(this, user.getUsername(), Toast.LENGTH_SHORT).show();
	}
}
