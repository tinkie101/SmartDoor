package za.co.zebrav.smartdoor.database;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

import android.content.Context;

public class PKprovider extends Db4oAdapter
{	
	//----------------------------------------------------------CONSTRUCTOR
	public PKprovider(Context context)
	{
		super(context);
		oneLargestPK();
	}

	//----------------------------------------------------------OneLargestPK
	private void oneLargestPK()
	{
		open();
		ObjectContainer db = getDatabase();
		ObjectSet<LargestPrimaryKey> result = db.queryByExample(new LargestPrimaryKey());
		
		//if Empty add one LargestPrimaryKey
		if(result.isEmpty())
		{
			db.store(new LargestPrimaryKey());
			db.commit();
		}
		close();
	}
	
	//----------------------------------------------------------getLargestPK
	public long getLargestPK()
	{
		long temp = 0;
		open();
		
		ObjectContainer db = getDatabase();
		ObjectSet<LargestPrimaryKey> result = db.queryByExample(new LargestPrimaryKey());
		
		temp = result.get(0).getPK();
		
		close();
		return temp;
	}
	
	public void increment()
	{
		open();
		
		ObjectContainer db = getDatabase();
		ObjectSet<LargestPrimaryKey> result = db.queryByExample(new LargestPrimaryKey());
		
		LargestPrimaryKey temp = result.get(0);
		temp.setPK(temp.getPK() + 1);
		db.store(temp);
		db.commit();
		
		close();
	}
}
