package za.co.zebrav.smartdoor.users;

import za.co.zebrav.smartdoor.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AddUserStepTwo extends Fragment 
{
	 
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	 {
		  View view = inflater.inflate(R.layout.add_user_step_two, null);
		  return view;
	 }
}
