package za.co.zebrav.smartdoor;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SettingsFragment extends Fragment
{
	private LinearLayout chooseSettingsLayout;
	private TableLayout trainSettings;
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
		trainSettings = (TableLayout) view.findViewById(R.id.trainSettings);
		
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
		
		Button cancelButton1 = (Button)view.findViewById(R.id.cancelButton1);
		cancelButton1.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
            {
				done();
            }
        });
		
		Button cancelButton2 = (Button)view.findViewById(R.id.cancelButton2);
		cancelButton2.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
            {
				done();
            }
        });
		
		return view;
	}
	
	private void done()
	{
		MainActivity m = (MainActivity) getActivity();
		m.switchToLoggedInFrag(-6);
	}
	
	//-------------------------------------------------------------------------------------settings
	private void trainSettings()
	{
		trainSettings.setVisibility(View.VISIBLE);
		chooseSettingsLayout.setVisibility(View.GONE);
		
		//getPreferences and display current settings
		String trainPhotoNum = settings.getString("face_TrainPhotoNum", "");
		((EditText) view.findViewById(R.id.TrainPhotoNumET)).setText(trainPhotoNum);
		
		String recogPhotoNum = settings.getString("face_RecogPhotoNum", "");
		((EditText) view.findViewById(R.id.RecogPhotoNumET)).setText(recogPhotoNum);
		
		String saveTrainThres = settings.getString("face_recognizerThreshold", "");
		((EditText) view.findViewById(R.id.recognizerThresholdET)).setText(saveTrainThres);
		
		int imageScale = Integer.parseInt(settings.getString("face_ImageScale", "1"));
		((Spinner) view.findViewById(R.id.ImageScaleSP)).setSelection(imageScale - 1);
		
		int algorithm = Integer.parseInt(settings.getString("face_faceRecognizerAlgorithm", "1"));
		((Spinner) view.findViewById(R.id.faceRecognizerAlgorithmSP)).setSelection(algorithm - 1);
		
		String detectEyes = settings.getString("face_detectEyes", "");
		if(detectEyes.equals("true"))
			((CheckBox) view.findViewById(R.id.faceDetectEyes)).setChecked(true);
		else
			((CheckBox) view.findViewById(R.id.faceDetectEyes)).setChecked(false);
		
		String detectNose = settings.getString("face_detectNose", "");
		if(detectNose.equals("true"))
			((CheckBox) view.findViewById(R.id.faceDetectNose)).setChecked(true);
		else
			((CheckBox) view.findViewById(R.id.faceDetectNose)).setChecked(false);
		
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
		if(noneEmpty())
		{
			SharedPreferences.Editor editor = settings.edit();
			
			String trainPhotosNum = ((EditText) view.findViewById(R.id.TrainPhotoNumET)).getText().toString();
		    editor.putString("face_TrainPhotoNum", trainPhotosNum);
		    editor.commit();
		    
		    String recogPhotosNum = ((EditText) view.findViewById(R.id.RecogPhotoNumET)).getText().toString();
		    editor.putString("face_RecogPhotoNum", recogPhotosNum);
		    editor.commit();
		    
		    String recognizerThreshold = ((EditText) view.findViewById(R.id.recognizerThresholdET)).getText().toString();
		    editor.putString("face_recognizerThreshold", recognizerThreshold);
		    editor.commit();
		    
		    String imageScale = ((Spinner) view.findViewById(R.id.ImageScaleSP)).getSelectedItem().toString();
		    editor.putString("face_ImageScale", imageScale);
		    editor.commit();
		    
		    String algorithm = ((Spinner) view.findViewById(R.id.ImageScaleSP)).getSelectedItem().toString();
		    if(algorithm.equals("LBPFace"))
		    	editor.putString("face_faceRecognizerAlgorithm", "1");
		    else if(algorithm.equals("FisherFace"))
		    	editor.putString("face_faceRecognizerAlgorithm", "2");
		    else
		    	editor.putString("face_faceRecognizerAlgorithm", "3");
		    editor.commit();
		    
		    if(((CheckBox) view.findViewById(R.id.faceDetectEyes)).isChecked())
		    	editor.putString("face_detectEyes", "true");
		    else
		    	editor.putString("face_detectEyes", "false");
		    editor.commit();
		    
		    if(((CheckBox) view.findViewById(R.id.faceDetectNose)).isChecked())
		    	editor.putString("face_detectNose", "true");
		    else
		    	editor.putString("face_detectNose", "false");
		    editor.commit();
		    
		    
			Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
		}
		else
			Toast.makeText(getActivity(), "Empty field", Toast.LENGTH_SHORT).show();
		
		
	}
	
	private boolean noneEmpty()
	{
		if(((EditText) view.findViewById(R.id.TrainPhotoNumET)).getText().toString().equals(""))
			return false;
		else if(((EditText) view.findViewById(R.id.RecogPhotoNumET)).getText().toString().equals(""))
			return false;
		else if(((EditText) view.findViewById(R.id.recognizerThresholdET)).getText().toString().equals(""))
			return false;
		else if(((Spinner) view.findViewById(R.id.ImageScaleSP)).getSelectedItem().toString().equals(""))
			return false;
		else if(((Spinner) view.findViewById(R.id.ImageScaleSP)).getSelectedItem().toString().equals(""))
			return false;
		return true;
			
	}
	
}
