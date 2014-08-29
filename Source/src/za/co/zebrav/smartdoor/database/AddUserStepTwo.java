package za.co.zebrav.smartdoor.database;

import za.co.zebrav.smartdoor.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class AddUserStepTwo extends Fragment 
{
	 
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	 {
		  View view = inflater.inflate(R.layout.add_user_step_two, null);
		  Bundle bundle = this.getArguments();
		  long userID = bundle.getLong("userID", -1);
		  Toast.makeText(this.getActivity(),"After" + Long.toString(userID), Toast.LENGTH_SHORT).show();
		  return view;
	 }
}
