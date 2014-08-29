package za.co.zebrav.smartdoor.database;

import android.content.Context;
import android.widget.Toast;

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
		this.context = context;
	}
	
	//------------------------------------------------------------------------saveUser
	/**
	 * Saves a user to the database and commits action.
	 * @param user to be stored
	 */
	public void saveUser(User user)
	{	
		PKprovider pk = new PKprovider(context);
		
		long largestPK = pk.getLargestPK();
		pk.increment();
		user.setID(largestPK + 1);
		
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
		//get user from database
		ObjectSet result = getDatabase().queryByExample(new User(null, null, user.getUsername(), null));
		User u = (User)result.get(0);
		
		//delete
    	getDatabase().delete(u);
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
			exists = true;
		
		close();
		return exists;
	}
	
	//------------------------------------------------------------------------validatePassword
	/**
	 * If a user exists in this database with the given username and password then the password is valid
	 * @param uName
	 * @param pass
	 * @return true if valid password, false if user does not exists or otherwise password is incorrect.
	 */
	public User getUserOnPassword(String username, String password)
	{
		User user = null;
		open();
		ObjectSet result = getDatabase().queryByExample(new User(null, null, username, password));
		Toast.makeText(context, "IN HERE1", Toast.LENGTH_SHORT).show();
		if(!result.isEmpty())
		{
			Toast.makeText(context, "IN HERE2", Toast.LENGTH_SHORT).show();
			User userA = (User) result.get(0);
			user = new User(null, null, null, null);
			user.setFirstnames(userA.getFirstnames());
			user.setPassword(password);
			user.setUsername(username);
			user.setSurname(userA.getSurname());
		}
		close();
		return user;
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
