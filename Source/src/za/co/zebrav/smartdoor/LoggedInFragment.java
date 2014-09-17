package za.co.zebrav.smartdoor;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LoggedInFragment extends Fragment
{
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	 {
		  View view = inflater.inflate(R.layout.logged_in, null);
		  
			
		  return view;
	 }
}
