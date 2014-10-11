package za.co.zebrav.smartdoor.test;

import org.junit.Test;

import za.co.zebrav.smartdoor.R;
import za.co.zebrav.smartdoor.TestFragmentActivity;
import za.co.zebrav.smartdoor.facerecognition.AddCameraFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.test.ActivityInstrumentationTestCase2;

public class CameraFragmentTest extends ActivityInstrumentationTestCase2<TestFragmentActivity>
{
	private TestFragmentActivity mActivity;

	public CameraFragmentTest()
	{
		super(TestFragmentActivity.class);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		mActivity = getActivity();
	}

	@Test
	private Fragment startFragment(Fragment fragment)
	{
		FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
		transaction.add(R.id.activity_test_fragment_linearlayout, fragment, "tag");
		transaction.commit();
		getInstrumentation().waitForIdleSync();
		Fragment frag = mActivity.getFragmentManager().findFragmentByTag("tag");
		return frag;
	}

	@Test
	public void testAddCameraFragment()
	{
		assertNotNull(mActivity);
		AddCameraFragment fragment = new AddCameraFragment(1);
		{
			// Override methods and add assertations here.
		};
		assertNotNull(fragment);
		
		Fragment frag = startFragment(fragment);
		assertNotNull(frag);
	}
	
	public void testSearchCameraFragment()
	{
		assertNotNull(mActivity);
		AddCameraFragment fragment = new AddCameraFragment(-1);
		{
			// Override methods and add assertations here.
		};
		assertNotNull(fragment);
		
		Fragment frag = startFragment(fragment);
		assertNotNull(frag);
	}
}