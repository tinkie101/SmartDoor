package za.co.zebrav.smartdoor.test;

import za.co.zebrav.smartdoor.R;
import za.co.zebrav.smartdoor.TextToSpeechActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.Button;
import android.widget.EditText;

public class TextToSpeechTest extends ActivityInstrumentationTestCase2<TextToSpeechActivity>
{
	private TextToSpeechActivity mainActivity;
	private Button btnSpeech;
	private EditText editText;

	public TextToSpeechTest()
	{
		super(TextToSpeechActivity.class);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		setActivityInitialTouchMode(true);

		mainActivity = getActivity();
		btnSpeech = (Button) mainActivity.findViewById(R.id.btnSpeak);
		editText = (EditText) mainActivity.findViewById(R.id.editTextTextTTS);
	}

	@MediumTest
	public void testPreCondtions()
	{
		// These should not be null
		assertNotNull(mainActivity);
		assertNotNull(btnSpeech);
		assertNotNull(editText);
	}
}
