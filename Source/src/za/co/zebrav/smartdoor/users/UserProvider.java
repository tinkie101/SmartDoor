package za.co.zebrav.smartdoor.users;

import android.content.Context;
import java.util.List;

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
		getDatabase().store(user);
    	commit();
	}
	
	//------------------------------------------------------------------------deleteUser
	public void deleteUser(User user) 
	{
    	getDatabase().delete(user);
    	commit();
    }

	//------------------------------------------------------------------------getListOfAllUsers
	public List getListOfAllUsers() 
	{
    	return getDatabase().query(User.class);
    } 
	
	//------------------------------------------------------------------------clearAllUsersData
	public void clearAllUsersData()
	{
    	List<User> list = getListOfAllUsers();
    	
    	for(User u : list){
    		deleteUser(u);
    	}
    	commit();
    } 
}
