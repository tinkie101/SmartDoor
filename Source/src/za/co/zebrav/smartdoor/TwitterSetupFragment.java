package za.co.zebrav.smartdoor;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class TwitterSetupFragment extends Fragment 
{
	private Button specifyButton;
	private Button defaultButton;
	private Button cancelButton;
	private Button tester;
	private TextView keyTV;
	private SharedPreferences settings = null;
	private static final String PREFS_NAME = "MyPrefsFile";
	private View view;
	private TextView flipper;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		view = inflater.inflate(R.layout.twitter_setup, container, false);
		
		specifyButton = (Button) view.findViewById(R.id.setupTwitterButton);
		defaultButton = (Button) view.findViewById(R.id.defaultSetupTwitter);
		cancelButton = (Button) view.findViewById(R.id.setupCancelButton);
	 tester = (Button) view.findViewById(R.id.tester);
	 
	 
	 if(cancelButton == null)
			Toast.makeText(getActivity(), "null cancel", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(getActivity(), " not null cancel", Toast.LENGTH_SHORT).show();
	 if(defaultButton == null)
			Toast.makeText(getActivity(), "null default", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(getActivity(), " not null default", Toast.LENGTH_SHORT).show();
	 if(specifyButton == null)
			Toast.makeText(getActivity(), "null spec", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(getActivity(), " not null spec", Toast.LENGTH_SHORT).show();
	 
		if(tester == null)
			Toast.makeText(getActivity(), "null tester", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(getActivity(), " not null tester", Toast.LENGTH_SHORT).show();
		
		specifyButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	inputSettings();
            }
        });
		
		cancelButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	done();
            }
        });
		
		defaultButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	useDefaults();
            }
        });		
		
		return view;
	}
	
	private void inputSettings()
	{
		/*ViewGroup layout = (ViewGroup) this.cancelButton.getParent();
		if(null!=layout) //for safety only  as you are doing onClick
		{
		  layout.removeView(cancelButton);
		  layout.removeView(defaultButton);
		  layout.removeView(specifyButton);*/
		  
		if(flipper == null)
			Toast.makeText(getActivity(), "null2", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(getActivity(), " not null2", Toast.LENGTH_SHORT).show();
		
		//flipper.showNext();
		  
		  
		//}
	}
	
	 class flipMyView implements View.OnClickListener 
	 {
		  public void onClick(View view) {
		   
		  }
		 }
	
	private void useDefaults() 
	{
		settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
		String key = getResources().getString(R.string.twitterKey);
		String secret = getResources().getString(R.string.twitterSecret);
		String tokenKey = getResources().getString(R.string.twitterToken);
		String tokenSecret = getResources().getString(R.string.twitterTokenKey);
		
		SharedPreferences.Editor editor = settings.edit();
	    editor.putString("key", key);
	    editor.commit();
	    editor.putString("secret", secret);
	    editor.commit();
	    editor.putString("tokenKey", tokenKey);
	    editor.commit();
	    editor.putString("tokenSecret", tokenSecret);
	    editor.commit();
	    
		MainActivity m = (MainActivity) getActivity();
		m.tryTwitter();
	}
	
	private void done()
	{
		MainActivity m = (MainActivity) getActivity();
		m.switchBackToLoggedInFrag();
	}
}
