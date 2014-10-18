package za.co.zebrav.smartdoor;

import java.util.List;

import za.co.zebrav.smartdoor.database.User;
import za.co.zebrav.voice.VoiceAuthenticator;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingsFragment extends Fragment
{
	private ScrollView chooseSettingsLayout;
	private LinearLayout faceSettings;
	private LinearLayout serverSettings;
	private LinearLayout voiceSettings;
	private LinearLayout twitterSettings;
	private View view;
	
	private SharedPreferences settings = null;
	private static String PREFS_NAME;
	private String[] commandOptions;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		view = inflater.inflate(R.layout.settings_layout, container, false);
		PREFS_NAME =  getResources().getString((R.string.settingsFileName));
		settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
		
		chooseSettingsLayout = (ScrollView)view.findViewById(R.id.chooseSettings);
		faceSettings = (LinearLayout) view.findViewById(R.id.FaceSettings);
		serverSettings = (LinearLayout) view.findViewById(R.id.ServerSettings);
		voiceSettings = (LinearLayout) view.findViewById(R.id.VoiceSettings);
		twitterSettings = (LinearLayout) view.findViewById(R.id.TwitterSettings);
		
		Button faceSettingsButton = (Button) view.findViewById(R.id.trainingSetButton);
		faceSettingsButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	faceSettings();
            }
        });
		
		Button serverSettigsButton = (Button) view.findViewById(R.id.ServerSetButton);
		serverSettigsButton.setOnClickListener(new OnClickListener() 
		{
			@Override
            public void onClick(View v) 
            {
            	serverSettings();
            }
        });
		
		Button twitterSetButton = (Button)view.findViewById(R.id.twitterSetButton);
		twitterSetButton.setOnClickListener(new View.OnClickListener() 
		{
			@Override
            public void onClick(View v) 
            {
            	twitterSettings();
            }
        });
		
		Button voiceSetButton = (Button)view.findViewById(R.id.voiceSetButton);
		voiceSetButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	voiceSettings();
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
		//TODO
		//commandOptions = new String[]{"Face","Voice","Server","Twitter","Back"};
		
		return view;
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		//TODO
		//((MainActivity) getActivity()).startListeningForCommands(commandOptions);
	}
	
	private void done()
	{
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
		      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
		
		MainActivity m = (MainActivity) getActivity();
		m.switchToLoggedInFrag();
	}
	
	private int getIndexInString(String[] array, String text)
	{
		int i = -1;
		for(int j = 0; j < array.length; j++)
		{
			i++;
			if(text.equals(array[j]))
				return i;
		}
		return i;
	}
	
	//-------------------------------------------------------------------------------------train settings
	private void faceSettings()
	{
		faceSettings.setVisibility(View.VISIBLE);
		chooseSettingsLayout.setVisibility(View.GONE);
		
		//getPreferences and display current settings
		String trainPhotoNum = settings.getString("face_TrainPhotoNum", "");
		String[] numPhotos = getResources().getStringArray(R.array.NumPhotos);
		int index = getIndexInString(numPhotos, trainPhotoNum);
		((Spinner) view.findViewById(R.id.TrainPhotoNumSP)).setSelection(index);
		
		String recogPhotoNum = settings.getString("face_RecogPhotoNum", "");
		index = getIndexInString(numPhotos, recogPhotoNum);
		((Spinner) view.findViewById(R.id.RecogPhotoNumSP)).setSelection(index);
		
		String saveTrainThres = settings.getString("face_recognizerThreshold", "");
		((EditText) view.findViewById(R.id.recognizerThresholdET)).setText(saveTrainThres);
		
		String groupRectangleThreshold = settings.getString("face_GroupRectangleThreshold", "");
		Log.d("missing", "text: " + groupRectangleThreshold);
		((EditText) view.findViewById(R.id.GroupRectangleET)).setText(groupRectangleThreshold);
		
		int imageScale = Integer.parseInt(settings.getString("face_ImageScale", "1"));
		((Spinner) view.findViewById(R.id.ImageScaleSP)).setSelection(imageScale - 1);
		
		int algorithm = Integer.parseInt(settings.getString("face_faceRecognizerAlgorithm", "1"));
		((Spinner) view.findViewById(R.id.faceRecognizerAlgorithmSP)).setSelection(algorithm - 1);
		
		//get device available resolutions
		Camera c = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
		Parameters parameters = c.getParameters();
		List<Size> sizes = parameters.getSupportedPreviewSizes();
		c.release();
		String[] data = new String[sizes.size()];
		for(int i = 0; i < sizes.size(); i++)
		{
			String temp = sizes.get(i).width + " x " + sizes.get(i).height;
			data[i] = temp;
		}
        final Spinner resolutionSpinner = (Spinner) view.findViewById(R.id.face_resolutionSP);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),R.layout.spinner_textview, data);
        adapter.setDropDownViewResource(R.layout.spinner_textview);
        resolutionSpinner.setAdapter(adapter);
        int resIndex = Integer.parseInt(settings.getString("face_resolution", "0"));
		resolutionSpinner.setSelection(resIndex);
		
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
		ImageButton face_photoNumTrainHelpButton = (ImageButton) view.findViewById(R.id.face_photoNumTrainHelpButton);
		face_photoNumTrainHelpButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	displayHelp("Help_facePhotoNumTrain");
            }
        });
		
		ImageButton face_photoNumTrainHelpVoiceButton = (ImageButton) view.findViewById(R.id.face_photoNumTrainHelpVoiceButton);
		face_photoNumTrainHelpVoiceButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	voiceHelp("Help_facePhotoNumTrain");
            }
        });
		
		ImageButton face_photoNumRecogHelpButton = (ImageButton) view.findViewById(R.id.face_photoNumRecogHelpButton);
		face_photoNumRecogHelpButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	displayHelp("Help_facePhotoNumRecog");
            }
        });
		
		ImageButton face_photoNumRecogHelpVoiceButton = (ImageButton) view.findViewById(R.id.face_photoNumRecogHelpVoiceButton);
		face_photoNumRecogHelpVoiceButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	voiceHelp("Help_facePhotoNumRecog");
            }
        });
		
		ImageButton face_recThresholdHelpButton = (ImageButton) view.findViewById(R.id.face_recThresholdHelpButton);
		face_recThresholdHelpButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	displayHelp("Help_faceThreshold");
            }
        });
		
		ImageButton face_recThresholdHelpVoiceButton = (ImageButton) view.findViewById(R.id.face_recThresholdHelpVoiceButton);
		face_recThresholdHelpVoiceButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	voiceHelp("Help_faceThreshold");
            }
        });
		
		ImageButton face_imgScaleHelpButton = (ImageButton) view.findViewById(R.id.face_imgScaleHelpButton);
		face_imgScaleHelpButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	displayHelp("Help_faceImageScale");
            }
        });
		
		ImageButton face_imgScaleHelpVoiceButton = (ImageButton) view.findViewById(R.id.face_imgScaleHelpVoiceButton);
		face_imgScaleHelpVoiceButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	voiceHelp("Help_faceImageScale");
            }
        });
		
		ImageButton face_recAlgorithmHelpButton = (ImageButton) view.findViewById(R.id.face_recAlgorithmHelpButton);
		face_recAlgorithmHelpButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	displayHelp("Help_faceAlgorithm");
            }
        });
		
		ImageButton face_recAlgorithmHelpVoiceButton = (ImageButton) view.findViewById(R.id.face_recAlgorithmHelpVoiceButton);
		face_recAlgorithmHelpVoiceButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	voiceHelp("Help_faceAlgorithm");
            }
        });
		
		ImageButton face_resolutionHelpButton = (ImageButton) view.findViewById(R.id.face_resolutionHelpButton);
		face_resolutionHelpButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	displayHelp("Help_faceResolution");
            }
        });
		
		ImageButton face_resolutionHelpVoiceButton = (ImageButton) view.findViewById(R.id.face_resolutionHelpVoiceButton);
		face_resolutionHelpVoiceButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	voiceHelp("Help_faceResolution");
            }
        });
		
		ImageButton face_groupRectangleHelpButton = (ImageButton) view.findViewById(R.id.face_groupRectangleHelpButton);
		face_groupRectangleHelpButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	displayHelp("Help_faceGroupRectangle");
            }
        });
		
		ImageButton face_groupRectangleHelpVoiceButton = (ImageButton) view.findViewById(R.id.face_groupRectangleHelpVoiceButton);
		face_groupRectangleHelpVoiceButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	voiceHelp("Help_faceGroupRectangle");
            }
        });
		
		ImageButton face_eyeDetectionHelpButton = (ImageButton) view.findViewById(R.id.face_eyeDetectionHelpButton);
		face_eyeDetectionHelpButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	displayHelp("Help_faceDetectEyes");
            }
        });
		
		ImageButton face_eyeDetectionHelpVoiceButton = (ImageButton) view.findViewById(R.id.face_eyeDetectionHelpVoiceButton);
		face_eyeDetectionHelpVoiceButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	voiceHelp("Help_faceDetectEyes");
            }
        });
		
		ImageButton face_noseDetectionHelpButton = (ImageButton) view.findViewById(R.id.face_noseDetectionHelpButton);
		face_noseDetectionHelpButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	displayHelp("Help_faceDetectNose");
            }
        });
		
		ImageButton face_noseDetectionHelpVoiceButton = (ImageButton) view.findViewById(R.id.face_noseDetectionHelpVoiceButton);
		face_noseDetectionHelpVoiceButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	voiceHelp("Help_faceDetectNose");
            }
        });
		
		
		
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
		if(trainNoneEmpty())
		{
			SharedPreferences.Editor editor = settings.edit();
			
			String trainPhotosNum = ((Spinner) view.findViewById(R.id.TrainPhotoNumSP)).getSelectedItem().toString();
		    editor.putString("face_TrainPhotoNum", trainPhotosNum);
		    editor.commit();
		    
		    String recogPhotosNum = ((Spinner) view.findViewById(R.id.RecogPhotoNumSP)).getSelectedItem().toString();
		    editor.putString("face_RecogPhotoNum", recogPhotosNum);
		    editor.commit();
		    
		    String groupRecThres = ((EditText) view.findViewById(R.id.GroupRectangleET)).getText().toString();
		    editor.putString("face_GroupRectangleThreshold", groupRecThres);
		    editor.commit();
		    
		    String recognizerThreshold = ((EditText) view.findViewById(R.id.recognizerThresholdET)).getText().toString();
		    editor.putString("face_recognizerThreshold", recognizerThreshold);
		    editor.commit();
		    
		    String imageScale = ((Spinner) view.findViewById(R.id.ImageScaleSP)).getSelectedItem().toString();
		    editor.putString("face_ImageScale", imageScale);
		    editor.commit();
		    
		    String algorithm = ((Spinner) view.findViewById(R.id.faceRecognizerAlgorithmSP)).getSelectedItem().toString();
		    if(algorithm.equals("LBPFace"))
		    	editor.putString("face_faceRecognizerAlgorithm", "1");
		    else if(algorithm.equals("FisherFace"))
		    	editor.putString("face_faceRecognizerAlgorithm", "2");
		    else if(algorithm.equals("EigenFace"))
		    	editor.putString("face_faceRecognizerAlgorithm", "3");
		    editor.commit();
		    
		    
		    int resolutionIndex = ((Spinner) view.findViewById(R.id.face_resolutionSP)).getSelectedItemPosition();
		    int currentSetting = Integer.parseInt(settings.getString("face_resolution", "0"));
		    if(resolutionIndex != currentSetting)
		    {
		    	if(isEmptyDB())
		    	{
		    		editor.putString("face_resolution", resolutionIndex+"");
				    editor.commit();
		    	}
		    	else
		    		Toast.makeText(getActivity(), "DB must be empty to make change to resolution", Toast.LENGTH_SHORT).show();
		    }
		    
		    
		    	
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
	
	private boolean isEmptyDB()
	{
		MainActivity m = (MainActivity)getActivity();
		List a = m.activityDatabase.load(new User(null, null, null, null, null, 0, null));
		if(a.size() == 0)
			return true;
		else
			return false;
	}
	
	private boolean trainNoneEmpty()
	{
		if(((Spinner) view.findViewById(R.id.TrainPhotoNumSP)).getSelectedItem().toString().equals(""))
			return false;
		else if(((Spinner) view.findViewById(R.id.RecogPhotoNumSP)).getSelectedItem().toString().equals(""))
			return false;
		else if(((EditText) view.findViewById(R.id.recognizerThresholdET)).getText().toString().equals(""))
			return false;
		else if(((Spinner) view.findViewById(R.id.ImageScaleSP)).getSelectedItem().toString().equals(""))
			return false;
		else if(((Spinner) view.findViewById(R.id.ImageScaleSP)).getSelectedItem().toString().equals(""))
			return false;
		return true;
	}
	
	//-------------------------------------------------------------------------------------server settings
	private void serverSettings()
	{
		serverSettings.setVisibility(View.VISIBLE);
		chooseSettingsLayout.setVisibility(View.GONE);
		
		//getPreferences and display current settings
		String ip = settings.getString("server_IP", "");
		((EditText) view.findViewById(R.id.IP_ET)).setText(ip);
		
		String port = settings.getString("server_Port", "");
		((EditText) view.findViewById(R.id.Port_ET)).setText(port);
		
		//configure buttons
		ImageButton serverIP_HelpButton = (ImageButton) view.findViewById(R.id.server_IPAddressHelpButton);
		serverIP_HelpButton.setOnClickListener(new View.OnClickListener() 
		{
			@Override
            public void onClick(View v) 
            {
            	displayHelp("Help_serverIPAddress");
            }
        });
		
		ImageButton serverIP_VoiceHelpButton = (ImageButton) view.findViewById(R.id.server_IPAddressHelpVoiceButton);
		serverIP_VoiceHelpButton.setOnClickListener(new View.OnClickListener() 
		{
			@Override
            public void onClick(View v) 
            {
            	voiceHelp("Help_serverIPAddress");
            }
        });
		
		ImageButton serverPort_HelpButton = (ImageButton) view.findViewById(R.id.server_PortHelpButton);
		serverPort_HelpButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	displayHelp("Help_serverIPort");
            }
        });
		
		ImageButton serverPort_VoiceHelpButton = (ImageButton) view.findViewById(R.id.server_IPortHelpVoiceButton);
		serverPort_VoiceHelpButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	voiceHelp("Help_serverIPort");
            }
        });
		
		Button saveServerSettings = (Button) view.findViewById(R.id.saveServerSettingsButton);
		saveServerSettings.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	saveServerData();
            }
        });
		
		Button cancelServerSettingsButton = (Button) view.findViewById(R.id.cancelServerSettingsButton);
		cancelServerSettingsButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	done();
            }
        });
	}
	
	public void saveServerData()
	{
		if(serverNoneEmpty())
		{
			SharedPreferences.Editor editor = settings.edit();
			
			String ip = ((EditText) view.findViewById(R.id.IP_ET)).getText().toString();
		    editor.putString("server_IP", ip);
		    editor.commit();
		    
		    String port = ((EditText) view.findViewById(R.id.Port_ET)).getText().toString();
		    editor.putString("server_Port", port);
		    editor.commit();
		    Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
		}
		else
			Toast.makeText(getActivity(), "Empty field", Toast.LENGTH_SHORT).show();
	}
	
	public boolean serverNoneEmpty()
	{
		if(((EditText) view.findViewById(R.id.IP_ET)).getText().toString().equals(""))
			return false;
		else if(((EditText) view.findViewById(R.id.Port_ET)).getText().toString().equals(""))
			return false;
		return true;
	}
	
	//--------------------------------------------------------------------------------------voiceSettings
	private void voiceSettings()
	{
		voiceSettings.setVisibility(View.VISIBLE);
		chooseSettingsLayout.setVisibility(View.GONE);
		
		autoCalibrateET = (EditText) view.findViewById(R.id.voice_calibration_ET);
		String voice_calibration = settings.getString("voice_Calibration", "");
		autoCalibrateET.setText(voice_calibration);
		
		//configure buttons
		Button autoCalibrateButton = (Button) view.findViewById(R.id.voice_autoCalibrateButton);
		autoCalibrateButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	progress = new ProgressDialog(getActivity(), ProgressDialog.STYLE_HORIZONTAL);
            	progress.setMessage("Wait for auto calibration.");
        	    progress.setCancelable(false);
        	    
        	    progress.show();
            	
        	    RunThread thread = new RunThread();
        	    thread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
		
		ImageButton voiceCal_HelpButton = (ImageButton) view.findViewById(R.id.voice_CalibrationHelpButton);
		voiceCal_HelpButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	displayHelp("Help_voiceCalibration");
            }
        });
		
		ImageButton IP_VoiceHelpButton = (ImageButton) view.findViewById(R.id.voice_CalibrationHelpVoiceButton);
		IP_VoiceHelpButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	voiceHelp("Help_voiceCalibration");
            }
        });
		
		Button saveSettingsButton  = (Button) view.findViewById(R.id.voice_saveSettings);
		saveSettingsButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	saveVoiceData();
            }
        });
		
		Button cancelSettingsButton  = (Button) view.findViewById(R.id.voice_cancelSettings);
		cancelSettingsButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	done();
            }
        });
		
	}
	private ProgressDialog progress;
	private EditText autoCalibrateET;
	private class RunThread extends AsyncTask<Void, Void, Integer>
	{

		@Override
		protected Integer doInBackground(Void... params)
		{
			VoiceAuthenticator voiceAuthenticator = new VoiceAuthenticator();
        	Integer result = voiceAuthenticator.autoCalibrateActivation();
        	progress.dismiss();
			return result;
		}
		
		@Override
		protected void onPostExecute(Integer result)
		{
    		autoCalibrateET.setText(result.toString());
		}
		
	}
	
	private void voiceHelp(String settingType)
	{
		String help = "";
		if(settingType.equals("Help_voiceCalibration"))
			help = getResources().getString((R.string.Help_voiceCalibration));
		else if(settingType.equals("Help_serverIPAddress"))
			help = getResources().getString((R.string.Help_serverIPAddress));
		else if(settingType.equals("Help_serverIPort"))
			help = getResources().getString(R.string.Help_serverIPort);
		else if(settingType.equals("Help_facePhotoNumTrain"))
			help = getResources().getString(R.string.Help_facePhotoNumTrain);
		else if(settingType.equals("Help_facePhotoNumRecog"))
			help = getResources().getString(R.string.Help_facePhotoNumRecog);
		else if(settingType.equals("Help_faceThreshold"))
			help = getResources().getString(R.string.Help_faceThreshold);
		else if(settingType.equals("Help_faceImageScale"))
			help = getResources().getString(R.string.Help_faceImageScale);
		else if(settingType.equals("Help_faceResolution"))
			help = getResources().getString(R.string.Help_faceResolution);
		else if(settingType.equals("Help_faceAlgorithm"))
			help = getResources().getString(R.string.Help_faceAlgorithm);
		else if(settingType.equals("Help_faceGroupRectangle"))
			help = getResources().getString(R.string.Help_faceGroupRectangle);
		else if(settingType.equals("Help_faceDetectEyes"))
			help = getResources().getString(R.string.Help_faceDetectEyes);
		else if(settingType.equals("Help_faceDetectNose"))
			help = getResources().getString(R.string.Help_faceDetectNose);
		else if(settingType.equals("Help_twitterKey"))
			help = getResources().getString(R.string.Help_twitterKey);
		else if(settingType.equals("Help_twitterSecret"))
			help = getResources().getString(R.string.Help_twitterSecret);
		else if(settingType.equals("Help_twitterTokenKey"))
			help = getResources().getString(R.string.Help_twitterTokenKey);
		else if(settingType.equals("Help_twitterTokenString"))
			help = getResources().getString(R.string.Help_twitterTokenString);
		
		MainActivity m = (MainActivity) getActivity();
		m.speakOut(help);
	}
	
	private void displayHelp(String settingType)
	{
		String help = "";
		if(settingType.equals("Help_voiceCalibration"))
			help = getResources().getString((R.string.Help_voiceCalibration));
		else if(settingType.equals("Help_serverIPAddress"))
			help = getResources().getString((R.string.Help_serverIPAddress));
		else if(settingType.equals("Help_serverIPort"))
			help = getResources().getString(R.string.Help_serverIPort);
		else if(settingType.equals("Help_facePhotoNumTrain"))
			help = getResources().getString(R.string.Help_facePhotoNumTrain);
		else if(settingType.equals("Help_facePhotoNumRecog"))
			help = getResources().getString(R.string.Help_facePhotoNumRecog);
		else if(settingType.equals("Help_faceThreshold"))
			help = getResources().getString(R.string.Help_faceThreshold);
		else if(settingType.equals("Help_faceImageScale"))
			help = getResources().getString(R.string.Help_faceImageScale);
		else if(settingType.equals("Help_faceResolution"))
			help = getResources().getString(R.string.Help_faceResolution);
		else if(settingType.equals("Help_faceAlgorithm"))
			help = getResources().getString(R.string.Help_faceAlgorithm);
		else if(settingType.equals("Help_faceGroupRectangle"))
			help = getResources().getString(R.string.Help_faceGroupRectangle);
		else if(settingType.equals("Help_faceDetectEyes"))
			help = getResources().getString(R.string.Help_faceDetectEyes);
		else if(settingType.equals("Help_faceDetectNose"))
			help = getResources().getString(R.string.Help_faceDetectNose);
		else if(settingType.equals("Help_twitterKey"))
			help = getResources().getString(R.string.Help_twitterKey);
		else if(settingType.equals("Help_twitterSecret"))
			help = getResources().getString(R.string.Help_twitterSecret);
		else if(settingType.equals("Help_twitterTokenKey"))
			help = getResources().getString(R.string.Help_twitterTokenKey);
		else if(settingType.equals("Help_twitterTokenString"))
			help = getResources().getString(R.string.Help_twitterTokenString);
		
		
		
		
		
		
		
		//AlertDialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(help)
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() 
		       {
		           public void onClick(DialogInterface dialog, int id) 
		           {
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void saveVoiceData()
	{
		if(voiceNoneEmpty())
		{
			SharedPreferences.Editor editor = settings.edit();
			
			String voiceCalibration = ((EditText) view.findViewById(R.id.voice_calibration_ET)).getText().toString();
		    editor.putString("voice_Calibration", voiceCalibration);
		    editor.commit();
		    Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
		}
		else
			Toast.makeText(getActivity(), "Empty field", Toast.LENGTH_SHORT).show();
		
	}
	
	private boolean voiceNoneEmpty()
	{
		if(((EditText) view.findViewById(R.id.voice_calibration_ET)).getText().toString().equals(""))
			return false;
		return true;
	}
	
	//--------------------------------------------------------------------------------------twitterSettings
	private void twitterSettings()
	{
		twitterSettings.setVisibility(View.VISIBLE);
		chooseSettingsLayout.setVisibility(View.GONE);
		
		//getPreferences and display current settings
		String key = settings.getString("twitter_Key", "");
		((EditText) view.findViewById(R.id.twitter_KeyET)).setText(key);
		
		String secret = settings.getString("twitter_Secret", "");
		((EditText) view.findViewById(R.id.twitter_SecretET)).setText(secret);
		
		String tokenKey = settings.getString("twitter_TokenKey", "");
		((EditText) view.findViewById(R.id.twitter_TokenKeyET)).setText(tokenKey);
		
		String tokenSecret = settings.getString("twitter_TokenSecret", "");
		((EditText) view.findViewById(R.id.twitter_TokenSecretET)).setText(tokenSecret);
		
		//configure buttons
		ImageButton twitterKey_HelpButton = (ImageButton) view.findViewById(R.id.twitter_keyHelpButton);
		twitterKey_HelpButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	displayHelp("Help_twitterKey");
            }
        });
				
		ImageButton twitter_keyHelpVoiceButton = (ImageButton) view.findViewById(R.id.twitter_keyHelpVoiceButton);
		twitter_keyHelpVoiceButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	voiceHelp("Help_twitterKey");
            }
        });
		
		ImageButton twitter_SecretHelpButton = (ImageButton) view.findViewById(R.id.twitter_SecretHelpButton);
		twitter_SecretHelpButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	displayHelp("Help_twitterSecret");
            }
        });
				
		ImageButton twitter_SecretHelpVoiceButton = (ImageButton) view.findViewById(R.id.twitter_SecretHelpVoiceButton);
		twitter_SecretHelpVoiceButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	voiceHelp("Help_twitterSecret");
            }
        });
		
		ImageButton twitter_TokenKeyHelpButton = (ImageButton) view.findViewById(R.id.twitter_TokenKeyHelpButton);
		twitter_TokenKeyHelpButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	displayHelp("Help_twitterTokenKey");
            }
        });
				
		ImageButton twitter_TokenKeyHelpVoiceButton = (ImageButton) view.findViewById(R.id.twitter_TokenKeyHelpVoiceButton);
		twitter_TokenKeyHelpVoiceButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	voiceHelp("Help_twitterTokenKey");
            }
        });
		
		ImageButton twitter_TokenSecretHelpButton = (ImageButton) view.findViewById(R.id.twitter_TokenSecretHelpButton);
		twitter_TokenSecretHelpButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	displayHelp("Help_twitterTokenString");
            }
        });
				
		ImageButton twitter_TokenSecretHelpVoiceButton = (ImageButton) view.findViewById(R.id.twitter_TokenSecretHelpVoiceButton);
		twitter_TokenSecretHelpVoiceButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	voiceHelp("Help_twitterTokenString");
            }
        });
				
				
				
		Button saveTwitterSettings = (Button) view.findViewById(R.id.saveTwitterButton);
		saveTwitterSettings.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	saveTwitterData();
            }
        });
				
		Button cancelTwitterSettingsButton = (Button) view.findViewById(R.id.cancelButtonTwitter);
		cancelTwitterSettingsButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	done();
            }
        });
		
		Button loadDefaultsTwitterButton = (Button) view.findViewById(R.id.defaultTwitter);
		loadDefaultsTwitterButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
            	loadTwitterDefaults();
            }
        });
	}
	
	private void saveTwitterData()
	{
		if(twitterNoneEmpty())
		{
			SharedPreferences.Editor editor = settings.edit();
			
			String key = ((EditText) view.findViewById(R.id.twitter_KeyET)).getText().toString();
		    editor.putString("twitter_Key", key);
		    editor.commit();
		    
		    String secret = ((EditText) view.findViewById(R.id.twitter_SecretET)).getText().toString();
		    editor.putString("twitter_Secret", secret);
		    editor.commit();
		    
		    String tokenKey = ((EditText) view.findViewById(R.id.twitter_TokenKeyET)).getText().toString();
		    editor.putString("twitter_TokenKey", tokenKey);
		    editor.commit();
		    
		    String tokenSecret = ((EditText) view.findViewById(R.id.twitter_TokenSecretET)).getText().toString();
		    editor.putString("twitter_TokenSecret", tokenSecret);
		    editor.commit();
		    
		    Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
		}
		else
			Toast.makeText(getActivity(), "Empty field", Toast.LENGTH_SHORT).show();
	}
	
	private boolean twitterNoneEmpty()
	{
		if(((EditText) view.findViewById(R.id.twitter_KeyET)).getText().toString().equals(""))
			return false;
		else if(((EditText) view.findViewById(R.id.twitter_SecretET)).getText().toString().equals(""))
			return false;
		else if(((EditText) view.findViewById(R.id.twitter_TokenKeyET)).getText().toString().equals(""))
			return false;
		else if(((EditText) view.findViewById(R.id.twitter_TokenSecretET)).getText().toString().equals(""))
			return false;
		return true;
	}
	
	private void loadTwitterDefaults()
	{
		String key = getResources().getString(R.string.twitter_Key);
		String secret = getResources().getString(R.string.twitter_Secret);
		String tokenKey = getResources().getString(R.string.twitter_TokenKey);
		String tokenSecret = getResources().getString(R.string.twitter_TokenSecret);
		
		((EditText) view.findViewById(R.id.twitter_KeyET)).setText(key);
		((EditText) view.findViewById(R.id.twitter_SecretET)).setText(secret);
		((EditText) view.findViewById(R.id.twitter_TokenKeyET)).setText(tokenKey);
		((EditText) view.findViewById(R.id.twitter_TokenSecretET)).setText(tokenSecret);
	    
	    
	}
}
