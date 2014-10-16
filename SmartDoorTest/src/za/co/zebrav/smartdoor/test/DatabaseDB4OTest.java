package za.co.zebrav.smartdoor.test;

import org.junit.Test;
import za.co.zebrav.smartdoor.TestFragmentActivity;
import za.co.zebrav.smartdoor.database.Db4oAdapter;
import za.co.zebrav.smartdoor.database.User;
import android.test.ActivityInstrumentationTestCase2;

public class DatabaseDB4OTest extends ActivityInstrumentationTestCase2<TestFragmentActivity>
{
	private TestFragmentActivity mActivity;
	private Db4oAdapter db;

	public DatabaseDB4OTest()
	{
		super(TestFragmentActivity.class);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		mActivity = getActivity();
		db = Db4oAdapter.getInstance(mActivity);
	}
	
	/*
	 * Test getInstance
	 * Test isOpen
	 * Test open
	 * Test close
	 */
	@Test
	public void testStartActivities()
	{
		assertNotNull(mActivity);
		
		db = null;
		//Test getInstance
		db = Db4oAdapter.getInstance(mActivity);
		assertNotNull("DB object should not be null" , db);
		
		//Test open, close and isOpen
		assertEquals("Database open, should not be.",false, db.isOpen());
		db.open();
		assertEquals("Databse not open, should be open.",true, db.isOpen());
		db.close();
		assertEquals("Database open, should not be after close",false, db.isOpen());
	}
	
	/*
	 * Test save
	 * Test load
	 */
	@Test 
	public void testSaveAndLoad()
	{
		db.open();
		int size = (db.load(new Object())).size();
		db.save(new User(null, null, null, null, null, 0, null));
		assertEquals("Save or load does not work",size+1, (db.load(new Object())).size());
		db.close();
	}
	
	/*
	 * test delete
	 */
	@Test
	public void testDeletes()
	{
		db.open();
		db.delete(new Object());
		
		//make 3 objects
		db.save(new User(null, null, null, null, null, 0, null));
		db.save(new User(null, null, null, null, null, 0, null));
		db.save(new User(null, null, null, null, null, 0, null));
		assertEquals(3,(db.load(new User(null, null, null, null, null, 0, null))).size());
		
		//Test if only one gets deleted
		db.delete(new User(null, null, null, null, null, 0, null));
		assertEquals(0,(db.load(new User(null, null, null, null, null, 0, null))).size());
		
		db.close();
	}
	
	/*
	 * test exist
	 */
	@Test
	public void testExists()
	{
		db.open();
		db.delete(new Object());
		assertEquals("No such object should exists",false, db.exists(new User(null, null, null, null, null, 0, null)));
		db.save(new User(null, null, null, null, null, 0, null));
		assertEquals("Such user added, does exist.",true, db.exists(new User(null, null, null, null, null, 0, null)));
		db.close();
	}
	
	/*
	 * test replace
	 */
	@Test
	public void testReplace()
	{
		db.open();
		db.delete(new Object());
		db.save(new User("Ann", "A", null, null, null, 1, null));
		assertEquals("Only one such user should be in db.",1, (db.load(new User("Ann", "A", null, null, null, 1, null))).size());
		assertEquals("No such user should be in db.",0, (db.load(new User("Bane", "B", null, null, null, 1, null))).size());
		
		db.replace(new User("Ann", "A", null, null, null, 1, null), new User("Bane", "B", null, null, null, 1, null));
		assertEquals("Only one such user should be in db.",1, (db.load(new User("Bane", "B", null, null, null, 1, null))).size());
		assertEquals("No such user should be in db.",0, (db.load(new User("Ann", "A", null, null, null, 1, null))).size());
		
		db.close();
	}
}
