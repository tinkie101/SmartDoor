package za.co.zebrav.smartdoor.test;

import za.co.zebrav.smartdoor.MainActivity;
import za.co.zebrav.smartdoor.R;
import android.app.Fragment;
import android.app.FragmentManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;

public class CameraUnitTest extends
		ActivityInstrumentationTestCase2<MainActivity>
{
	private MainActivity mainActivity;
	private Fragment cameraFragment;
	FragmentManager fragmentManager;

	public CameraUnitTest()
	{
		super(MainActivity.class);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		setActivityInitialTouchMode(true);

		mainActivity = getActivity();
		fragmentManager = mainActivity.getFragmentManager();
		cameraFragment = fragmentManager.findFragmentById(R.id.cameraFragment);
	}

	@MediumTest
	public void testPreCondtions()
	{
		// These should not be null
		assertNotNull(mainActivity);
		assertNotNull(cameraFragment);
	}
}
