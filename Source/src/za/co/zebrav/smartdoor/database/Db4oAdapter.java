package za.co.zebrav.smartdoor.database;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.db4o.Db4o;
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
	
	private Db4oAdapter(Context context)
	{
		this.context = context;
	}
	
	public static Db4oAdapter getInstance(Context context)
	{
		if(instance == null)
			instance = new Db4oAdapter(context);
		return instance;
	}
	public void open()
	{
		try
		{
			if (database == null || database.ext().isClosed())
			{
				database = Db4oEmbedded.openFile(config(), db4oDBFullPath(context));
				isDbOpen = true;
				Log.d(LOG_TAG, "Opened Database");
			}
		}
		catch (Exception ie)
		{
			Log.d(LOG_TAG, "Exceptio");
			Log.e(Db4oAdapter.class.getName(), ie.getMessage());
		}
	}

	private EmbeddedConfiguration config()
	{
		EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
		return configuration;
	}

	private String db4oDBFullPath(Context ctx)
	{
		return ctx.getDir("data", 0) + "/" + DATABASE_NAME;
	}

	protected void commit()
	{
		database.commit();
	}

	public void rollBack()
	{
		database.rollback();
	}

	public void close()
	{
		if (this.database != null)
		{
			this.database.close();
			isDbOpen = false;
			Log.d(LOG_TAG, "Closed Database");
		}
	}

	public boolean isOpen()
	{
		return isDbOpen;
	}

	public ObjectContainer getDatabase()
	{
		return this.database;
	}

	@Override
	public List<Object> load(Object object)
	{
		List<Object> result = getDatabase().queryByExample(object);
		return result;
	}

	@Override
	public void save(Object object)
	{
		getDatabase().store(object);
		commit();
	}

	/**
	 * @param username
	 *            to check if user already exists with this username
	 * @return true if user exists, false if it does not
	 */
	public boolean exists(Object object)
	{
		boolean exists = false;
		ObjectSet<Object> result = getDatabase().queryByExample(object);

		if (!result.isEmpty())
			exists = true;
		return exists;
	}

	public boolean delete(Object object)
	{
		boolean found = false;

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

	public boolean deleteThisOne(User object)
	{
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

	public boolean update(Object oldO, Object newO)
	{
		delete(oldO);

		save(newO);
		return false;
	}
}
