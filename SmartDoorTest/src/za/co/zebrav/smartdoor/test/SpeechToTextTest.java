package za.co.zebrav.smartdoor.test;

import za.co.zebrav.smartdoor.R;
import za.co.zebrav.smartdoor.SpeechToTextActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.Adapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

public class SpeechToTextTest extends ActivityInstrumentationTestCase2<SpeechToTextActivity>
{
	private SpeechToTextActivity mainActivity;
	ImageButton listenSpeech;
	ListView list;
	ProgressBar progress;
	Adapter listAdapter;

	public SpeechToTextTest()
	{
		super(SpeechToTextActivity.class);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		setActivityInitialTouchMode(true);

		mainActivity = getActivity();

		listenSpeech = (ImageButton) mainActivity.findViewById(R.id.listenSpeech);
		list = (ListView) mainActivity.findViewById(R.id.speechToTextList);

		listAdapter = list.getAdapter();

		progress = (ProgressBar) mainActivity.findViewById(R.id.speech_loadingBar);
	}

	@MediumTest
	public void testPreCondtions()
	{
		// These should not be null
		assertNotNull(mainActivity);
		assertNotNull(listenSpeech);
		assertNotNull(progress);

		// The adapter should be null at this point
		assertNull(listAdapter);

		// The progressBar should not be visible at this time
		assertEquals(ProgressBar.GONE, progress.getVisibility());

		// The list should be empty at this point
		assertEquals(0, list.getChildCount());

		// Check if the button is correctly enabled/disabled based on speech recognition availability
		if (mainActivity.getEnableRecognition())
		{
			assertTrue(listenSpeech.isEnabled());
		}
		else
		{
			assertFalse(listenSpeech.isEnabled());
		}
	}
}
