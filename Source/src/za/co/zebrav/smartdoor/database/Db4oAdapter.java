package za.co.zebrav.smartdoor.database;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.EmbeddedConfiguration;

public class Db4oAdapter implements DatabaseAdapter
{
	private static final String LOG_TAG = "Db4oAdapter";
	private ObjectContainer database = null;
	protected Context context = null;
	private String DATABASE_NAME = "smartdoor_users.db4o";
	private boolean isDbOpen = false;

	private static Db4oAdapter instance;

	/**
	 * Db4oAdapter is used to open, use and close a db4o database.
	 * 
	 * @precondition Context parameter must not be Null
	 * @precondition This constructor must be called from getInstance
	 * @param context
	 *            , Context of Activity that calls this the get instance function.
	 */
	private Db4oAdapter(Context context)
	{
		// precondition
		if (context == null)
			Log.d(LOG_TAG, "Db4oAdapter constructor parameter should be, but is null.");
		this.context = context;
	}

	/**
	 * This class follows the singleton pattern. To access the only Db4oAdapter in the system
	 * one should call the getInstance function.
	 * 
	 * @precondition inputed parameter should not be null.
	 * @param context
	 * @return instance, the only Db4o object
	 */
	public static Db4oAdapter getInstance(Context context)
	{
		if (instance == null)
			instance = new Db4oAdapter(context);
		return instance;
	}

	/**
	 * Configures and opens the database.
	 * 
	 * @precondition DB should not be open already.
	 * @postcondition DB is open
	 */
	public void open()
	{
		if (isOpen())
			Log.d(LOG_TAG, "Database is open already.");
		try
		{
			if (database == null || database.ext().isClosed())
			{
				database = Db4oEmbedded.openFile(config(), db4oDBFullPath(context));
				isDbOpen = true;
			}
		}
		catch (Exception ie)
		{
		}
	}

	/**
	 * Needed to open a connection to the db
	 * 
	 * @return configuration
	 */
	private EmbeddedConfiguration config()
	{
		EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
		return configuration;
	}

	/**
	 * Uses the activity path to find the path to the database file location
	 * 
	 * @precondition parameter context must not be zero
	 * @param Activity
	 *            context
	 * @return Path to database file is returned
	 */
	private String db4oDBFullPath(Context ctx)
	{
		// precondition 1
		if (ctx == null)
		{
			Log.d(LOG_TAG, "Context is null.");
			return null;
		}
		return ctx.getDir("data", 0) + "/" + DATABASE_NAME;
	}

	/**
	 * Committed changes to database
	 * 
	 * @precondition Database must be open.
	 */
	protected void commit()
	{
		database.commit();
	}

	/**
	 * Rolls back on last operation
	 * 
	 * @precondition Database must be open
	 */
	public void rollBack()
	{
		database.rollback();
	}

	/**
	 * Closes the database
	 * 
	 * @precondition Database musn't be currently open
	 * @postcondition Database closed
	 */
	public void close()
	{
		if (this.database != null)
		{
			this.database.close();
			isDbOpen = false;
			Log.d(LOG_TAG, "Closed Database");
		}
	}

	/**
	 * Checks if the database is currently open.
	 * 
	 * @return true if database is open
	 * @return false if database is closed
	 */
	public boolean isOpen()
	{
		return isDbOpen;
	}

	/**
	 * Fetches and returns the database
	 * 
	 * @return ObjectContainer, the database
	 */
	private ObjectContainer getDatabase()
	{
		return this.database;
	}

	/**
	 * Loads all objects in database that fits the parameter prototype's criteria.
	 * 
	 * @precondition Database must be open
	 * @param Object
	 *            , a prototype of the type of objects to be returned
	 * @return List, returns all Objects in database of the same type and constructor parameters as the parameter
	 */
	@Override
	public List<Object> load(Object object)
	{
		// precondition
		if (!isOpen())
		{
			Log.d(LOG_TAG, "Database isn't open. Must be open to call load function.");
			return null;
		}
		List<Object> result = getDatabase().queryByExample(object);
		return result;
	}

	/**
	 * Saves the object sent as parameter in the DB
	 * 
	 * @param Object
	 * @precondition Database must be open
	 * @postcondition Object saved
	 */
	@Override
	public boolean save(Object object)
	{
		// precondition
		if (!isOpen())
		{
			Log.d(LOG_TAG, "Database isn't open. Must be open to call save function.");
			return false;
		}

		getDatabase().store(object);
		commit();
		return true;
	}

	/**
	 * Checks if the Objects sent as parameter already exists in the database
	 * 
	 * @precondition Database must be open
	 * @param Object
	 *            the object to check if it is already in db
	 * @return True, if object from parameter already exists in db
	 * @return False, if the object from parameter is not present in the db.
	 */
	public boolean exists(Object object)
	{
		// precondition
		if (!isOpen())
		{
			Log.d(LOG_TAG, "Database isn't open. Must be open to call exists function.");
			return false;
		}

		boolean exists = false;
		ObjectSet<Object> result = getDatabase().queryByExample(object);

		if (!result.isEmpty())
			exists = true;
		return exists;
	}

	/**
	 * Deletes all objects in database of the Object type sent in.
	 * 
	 * @param Any
	 *            Object type
	 * @precondition database must be open.
	 * @return true if at least some objects were deleted
	 * @return false if no objects were deleted.
	 */
	public boolean delete(Object object)
	{
		// precondition
		if (!isOpen())
		{
			Log.d(LOG_TAG, "Database isn't open. Must be open to call delete function.");
			return false;
		}

		// get user from database
		ObjectSet result = getDatabase().queryByExample(object);

		if (result.isEmpty())
			return false;
		else
		{
			for (Object o : result)
			{
				getDatabase().delete(o);
				commit();
			}
			return true;
		}
	}

	/**
	 * Deletes only one of the objects of that criteria
	 * 
	 * @param object
	 * @return true, if object deleted
	 * @return false if not deleted.
	 * @precondition Database must be open
	 */
	public boolean deleteThisOne(User object)
	{
		// precondition
		if (!isOpen())
		{
			Log.d(LOG_TAG, "Database isn't open. Must be open to call delete one object function.");
			return false;
		}
		// get user from database
		ObjectSet result = getDatabase().queryByExample(new User(null, null, null, null, null, object.getID(), null));

		if (result.isEmpty())
		{
			Log.d(LOG_TAG, "empty list. No deleted");
			return false;
		}
		else
		{
			Log.d(LOG_TAG, "delete");
			for (Object o : result)
			{
				getDatabase().delete(o);
				commit();
			}
			return true;
		}
	}

	/**
	 * Database replaces all objects that resemble parameter 1 and replace it with parameter 2
	 * 
	 * @precondition database must be open
	 * @postcondition all objects that resemble parameter 1 is replaced with one of parameter 2
	 * @param Object
	 *            oldO, Objects that resemble this will be deleted from the database
	 * @param Object
	 *            newO, Object to replace previously deleted objects
	 */
	public boolean replace(Object oldO, Object newO)
	{
		// precondition
		if (!isOpen())
			Log.d(LOG_TAG, "Database isn't open. Must be open to call update function.");

		delete(oldO);

		save(newO);
		return false;
	}
}
