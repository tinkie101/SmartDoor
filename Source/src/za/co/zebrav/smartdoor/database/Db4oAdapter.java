package za.co.zebrav.smartdoor.database;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.ObjectSet;

public class Db4oAdapter implements DatabaseAdaptee
{
	private ObjectContainer database = null;
	protected Context context = null;
	private String DATABASE_NAME = "smartdoor_users.db4o";
	private boolean isDbOpen = false;
	
	//------------------------------------------------------------------------CONSTRUCTOR
	public Db4oAdapter(Context context) 
	{
		this.context = context;
	}
	
	//------------------------------------------------------------------------open
	public void open()
	{
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
	
	//------------------------------------------------------------------------commit
	protected void commit() 
	{
		database.commit();
	}
	
	//------------------------------------------------------------------------rollBack
	public void rollBack() 
	{
		database.rollback();
	}
	
	//------------------------------------------------------------------------close
	public void close() 
	{
		if (this.database != null) 
		{
			this.database.close();
			isDbOpen = false;
		}
	}
	
	//------------------------------------------------------------------------close
	public boolean isOpen() 
	{
		return isDbOpen;
	}
	//------------------------------------------------------------------------getDatabase
	public ObjectContainer getDatabase() 
	{
		return this.database;
	}

	//------------------------------------------------------------------------LOAD
	@Override
	public List<Object> load(Object object)
	{ 
		List<Object> result = getDatabase().queryByExample(object);
		return result;
	}

	//------------------------------------------------------------------------SAVE
	@Override
	public void save(Object object)
	{
		getDatabase().store(object);
    	commit();
	}
	
	//------------------------------------------------------------------------exists
	/**
	 * @param username to check if user already exists with this username
	 * @return true if user exists, false if it does not
	 */
	public boolean exists(Object object)
	{
		boolean exists = false;
		ObjectSet<Object> result = getDatabase().queryByExample(object);
		
		if(!result.isEmpty())
			exists = true;
		return exists;
	}
	
	//--------------------------------------------------------------------------DELETE
	public boolean delete(Object object)
	{
		boolean found = false;
		
		//get user from database
		ObjectSet result = getDatabase().queryByExample(object);
		
		if(result.isEmpty())
			return false;
		else
		{
			for(Object o: result)
			{
				getDatabase().delete(result.get(0));
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
