package za.co.zebrav.smartdoor.users;

import android.content.Context;

import java.util.List;

import com.db4o.ObjectSet;

public class UserProvider extends Db4oHelper
{
	//------------------------------------------------------------------------CONSTRUCTOR
	/**
	 * Needs the activity context
	 * @param context
	 */
	public UserProvider(Context context)
	{
		super(context);
	}
	
	//------------------------------------------------------------------------saveUser
	/**
	 * Saves a user to the database and commits action.
	 * @param user to be stored
	 */
	public void saveUser(User user)
	{
		open();
		getDatabase().store(user);
    	commit();
    	close();
	}
	
	//------------------------------------------------------------------------deleteUser
	/**
	 * Deletes specified user from database and commits action.
	 * @param user
	 */
	public void deleteUser(User user) 
	{
		open();
    	getDatabase().delete(user);
    	commit();
    	close();
    }

	//------------------------------------------------------------------------getListOfAllUsers
	/**
	 * @return result, list of all the users objects stored in database
	 */
	public ObjectSet getListOfAllUsers() 
	{
		ObjectSet result;
		open();
		result = getDatabase().queryByExample(new User(null, null, null, null));
	
    	return result;
    } 
	
	//------------------------------------------------------------------------userExists
	/**
	 * @param username to check if user already exists with this username
	 * @return true if user exists, false if it does not
	 */
	public boolean userExists(String username)
	{
		boolean exists = false;
		open();
		ObjectSet result = getDatabase().queryByExample(new User(null, null, username, null));
		
		if(!result.isEmpty())
		{
			exists = true;
		}
		close();
		return exists;
	}
	
	//------------------------------------------------------------------------clearAllUsersData
	/**
	 * Clears all the users from the database.
	 */
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
