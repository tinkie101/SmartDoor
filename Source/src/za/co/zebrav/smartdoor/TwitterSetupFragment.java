package za.co.zebrav.smartdoor;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class TwitterSetupFragment extends Fragment 
{
	private Button specifyButton;
	private Button defaultButton;
	private Button cancelButton;
	private SharedPreferences settings = null;
	private static final String PREFS_NAME = "MyPrefsFile";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.twitter_setup, container, false);
		
		specifyButton = (Button) view.findViewById(R.id.setupTwitterButton);
		defaultButton = (Button) view.findViewById(R.id.defaultSetupTwitter);
		cancelButton = (Button) view.findViewById(R.id.setupCancelButton);
		
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
