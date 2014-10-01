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
	private Button saveButton;
	private Button cancelButton2;
	
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
		saveButton = (Button) view.findViewById(R.id.saveSettings02);
		cancelButton2 = (Button) view.findViewById(R.id.cancel01);
		
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
		
		saveButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	saveSpecifiedDetails();
            }
        });	
		
		cancelButton2.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	done();
            }
        });	
		
		return view;
	}
	
	private void inputSettings()
	{	
		GridLayout g = (GridLayout)view.findViewById(R.id.grid);
		specifyButton.setVisibility(View.GONE);
		defaultButton.setVisibility(View.GONE);
		cancelButton.setVisibility(View.GONE);
		g.setVisibility(View.VISIBLE);
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
	
	private void saveSpecifiedDetails()
	{
		settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
		String key = ((EditText)view.findViewById(R.id.keyET)).getText().toString();
		String secret = ((EditText)view.findViewById(R.id.secretET)).getText().toString();
		String tokenKey = ((EditText)view.findViewById(R.id.tokenKeyET)).getText().toString();
		String tokenSecret = ((EditText)view.findViewById(R.id.tokenSecretET)).getText().toString();
		
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
		m.switchToLoggedInFrag(-6);
	}
}
