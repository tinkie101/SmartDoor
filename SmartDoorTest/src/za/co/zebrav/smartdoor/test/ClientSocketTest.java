package za.co.zebrav.smartdoor.test;

import org.junit.Before;
import org.junit.Test;

import za.co.zebrav.smartdoor.main.ClientSocket;
import za.co.zebrav.smartdoor.main.TestFragmentActivity;
import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

public class ClientSocketTest extends ActivityInstrumentationTestCase2<TestFragmentActivity>
{
	public ClientSocketTest()
	{
		super(TestFragmentActivity.class);
	}

	ClientSocket clientSocket;

	@Before
	public void setUp() throws Exception
	{
			Activity activity = getActivity();
			clientSocket = new ClientSocket(activity);
	}

	@Test
	public void test()
	{
		assertNotNull("ClientSocket should not be null" , clientSocket);
		assertNotNull("Server IP should not be null" , clientSocket.getServerIP());
		assertNotNull("Server Port should not be null" , clientSocket.getServerPort());
		//fail("For no apparent reason");
	}
	

}
