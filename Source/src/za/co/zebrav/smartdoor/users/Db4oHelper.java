package za.co.zebrav.smartdoor.users;

import android.content.Context;
import android.util.Log;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.ObjectSet;

public class Db4oHelper
{
	private ObjectContainer database = null;
	private Context context = null;
	private String DATABASE_NAME = "smartdoor_users.db4o";
	
	//------------------------------------------------------------------------CONSTRUCTOR
	public Db4oHelper(Context context) 
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
			}
		} 
		catch (Exception ie) 
		{
			Log.e(Db4oHelper.class.getName(), ie.getMessage());
		}
	}
	 
	//------------------------------------------------------------------------Config
	private EmbeddedConfiguration config() 
	{
		EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
		return configuration;
	}
	
	//------------------------------------------------------------------------db4oBDFullPath
	private String db4oDBFullPath(Context ctx) 
	{
		return ctx.getDir("data", 0) + "/" + DATABASE_NAME;
	}
	
	//------------------------------------------------------------------------commit
	public void commit() 
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
		}
	}
	
	//------------------------------------------------------------------------getDatabase
	public ObjectContainer getDatabase() 
	{
		return this.database;
	}
}
