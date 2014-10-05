package za.co.zebrav.smartdoor;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;

public class TwitterSetupFragment extends Fragment 
{
	private Button specifyButton;
	private Button defaultButton;
	private Button cancelButton;
	private Button saveButton;
	private Button cancelButton2;
	
	private SharedPreferences settings = null;
	private String PREFS_NAME;
	private View view;
	private TextView flipper;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		view = inflater.inflate(R.layout.twitter_setup, container, false);
		
		PREFS_NAME = getResources().getString((R.string.settingsFileName));
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
		
		settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
		EditText keyET = (EditText)view.findViewById(R.id.keyET);
		keyET.setText(settings.getString("twitter_Key", ""));
		
		EditText secretET = (EditText)view.findViewById(R.id.secretET);
		secretET.setText(settings.getString("twitter_Secret", ""));
		
		EditText tokenKeyET = (EditText)view.findViewById(R.id.tokenKeyET);
		tokenKeyET.setText(settings.getString("twitter_TokenKey", ""));
		
		EditText tokenSecretET = (EditText)view.findViewById(R.id.tokenSecretET);
		tokenSecretET.setText(settings.getString("twitter_TokenSecret", ""));
		
		
		g.setVisibility(View.VISIBLE);
	}
	
	private void useDefaults() 
	{
		settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
		String key = getResources().getString(R.string.twitter_Key);
		String secret = getResources().getString(R.string.twitter_Secret);
		String tokenKey = getResources().getString(R.string.twitter_TokenKey);
		String tokenSecret = getResources().getString(R.string.twitter_TokenSecret);
		
		SharedPreferences.Editor editor = settings.edit();
	    editor.putString("twitter_Key", key);
	    editor.commit();
	    editor.putString("twitter_Secret", secret);
	    editor.commit();
	    editor.putString("twitter_TokenKey", tokenKey);
	    editor.commit();
	    editor.putString("twitter_TokenSecret", tokenSecret);
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
	    editor.putString("twitter_Key", key);
	    editor.commit();
	    editor.putString("twitter_Secret", secret);
	    editor.commit();
	    editor.putString("twitter_TokenKey", tokenKey);
	    editor.commit();
	    editor.putString("twitter_TokenSecret", tokenSecret);
	    editor.commit();
	    
		MainActivity m = (MainActivity) getActivity();
		m.tryTwitter();
		
	}
	
	private void done()
	{
		MainActivity m = (MainActivity) getActivity();
		m.switchToLoggedInFrag();
	}
}
