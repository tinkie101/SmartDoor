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
import android.widget.LinearLayout;
import android.widget.Toast;

public class SettingsFragment extends Fragment
{
	private LinearLayout chooseSettingsLayout;
	private GridLayout trainSettings;
	private View view;
	
	private SharedPreferences settings = null;
	private static String PREFS_NAME;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		view = inflater.inflate(R.layout.settings_layout, container, false);
		PREFS_NAME =  getResources().getString((R.string.settingsFileName));
		settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
		
		chooseSettingsLayout = (LinearLayout)view.findViewById(R.id.chooseSettings);
		trainSettings = (GridLayout) view.findViewById(R.id.trainSettings);
		
		Button trainSettingsButton = (Button) view.findViewById(R.id.trainingSetButton);
		trainSettingsButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	trainSettings();
            }
        });
		
		Button twitterSetButton = (Button)view.findViewById(R.id.twitterSetButton);
		twitterSetButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	MainActivity m = (MainActivity) getActivity();
            	m.switchToTwitterSetup();
            }
        });
		return view;
	}
	
	//-------------------------------------------------------------------------------------settings
	private void trainSettings()
	{
		trainSettings.setVisibility(View.VISIBLE);
		chooseSettingsLayout.setVisibility(View.GONE);
		
		//getPreferences
		String photoNum = settings.getString("Train/photoNum", "Not");
		if(!photoNum.equals("Not"))
		{
			EditText photoNumET = (EditText) view.findViewById(R.id.NumberOfPhotosET);
			photoNumET.setText(photoNum);
		}
		
		//configure buttons
		Button saveTrain = (Button) view.findViewById(R.id.saveTrainButton);
		saveTrain.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	saveTrainingData();
            }
        });
	}
	
	private void saveTrainingData()
	{
		//Number of photo's during training
		String numOfPhotos = ((EditText) view.findViewById(R.id.NumberOfPhotosET)).getText().toString();
		if(!numOfPhotos.equals(""))
		{
			SharedPreferences.Editor editor = settings.edit();
		    editor.putString("Train/photoNum", numOfPhotos);
		    editor.commit();
			Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
		}
		else
			Toast.makeText(getActivity(), "Empty field", Toast.LENGTH_SHORT).show();
		
	}
}
