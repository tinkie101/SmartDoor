package za.co.zebrav.smartdoor.users;

import android.content.Context;

import java.util.List;

import com.db4o.ObjectSet;

public class UserProvider extends Db4oHelper
{
	//------------------------------------------------------------------------CONSTRUCTOR
	public UserProvider(Context context)
	{
		super(context);
	}
	
	//------------------------------------------------------------------------saveUser
	public void saveUser(User user)
	{
		open();
		getDatabase().store(user);
    	commit();
    	close();
	}
	
	//------------------------------------------------------------------------deleteUser
	public void deleteUser(User user) 
	{
		open();
    	getDatabase().delete(user);
    	commit();
    	close();
    }

	//------------------------------------------------------------------------getListOfAllUsers
	public ObjectSet getListOfAllUsers() 
	{
		ObjectSet result;
		open();
		result = getDatabase().queryByExample(new User(null, null, null, null));
	
    	return result;
    } 
	
	//------------------------------------------------------------------------clearAllUsersData
	public void clearAllUsersData()
	{
		open();
    	List<User> list = getListOfAllUsers();
    	
    	for(User u : list){
    		getDatabase().delete(u);
        	commit();
    	}
    	commit();
    	close();
    } 
}
