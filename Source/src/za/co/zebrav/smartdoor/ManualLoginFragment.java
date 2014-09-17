package za.co.zebrav.smartdoor;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class ManualLoginFragment extends Fragment
{
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	 {
		  View view = inflater.inflate(R.layout.manual_login, null);
		  
		  return view;
	 }
}
