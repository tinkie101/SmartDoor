package za.co.zebrav.smartdoor.test;

import za.co.zebrav.smartdoor.ManualLogin;
import za.co.zebrav.smartdoor.R;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ManualLoginTest extends ActivityInstrumentationTestCase2<ManualLogin>
{
	private ManualLogin loginActivity;
	
	//GUI components
	private TextView tvH;
	private TextView tv1;
	private EditText et1;
	private TextView tv2;
	private EditText et2;
	private Button loginButton;
	
	//GUI menu components
	private LinearLayout contentLayout;
	private ListView menuDrawer;
	
	public ManualLoginTest()
	{
		super(ManualLogin.class);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		setActivityInitialTouchMode(true);

		//Find GUI components
		loginActivity = getActivity();
		tvH = (TextView) loginActivity.findViewById(R.id.ManualLogin_tvH);
		tv1 = (TextView) loginActivity.findViewById(R.id.ManualLogin_tv1);
		et1 = (EditText) loginActivity.findViewById(R.id.ManualLogin_et1);
		tv2 = (TextView) loginActivity.findViewById(R.id.ManualLogin_tv2);
		et2 = (EditText) loginActivity.findViewById(R.id.ManualLogin_et2);
		loginButton = (Button) loginActivity.findViewById(R.id.ManualLogin_button1);
		
		//find GUI menu components
		contentLayout = (LinearLayout) loginActivity.findViewById(R.id.content_frame);
		menuDrawer = (ListView) loginActivity.findViewById(R.id.drawer_list);
		
	}

	@MediumTest
	public void testPreCondtions()
	{
		//Initial GUI components that should not be null
		assertNotNull(loginActivity);
		//assertNotNull(tvH);
		assertNotNull(tv1);
		assertNotNull(et1);
		assertNotNull(tv2);
		assertNotNull(et2);
		assertNotNull(loginButton);
		
		//For CustomMenu to be operational the following must be present in the layout
		assertNotNull(contentLayout);
		assertNotNull(menuDrawer);
	}
}
