package za.co.zebrav.smartdoor.database;

import java.util.List;

public interface DatabaseAdaptee 
{
	/**
	 * This loads all objects that look like given object
	 * @param object, this is an example object of the type you want to load
	 * @return a list filled with objects, save to list<of specific object type> if needed
	 */
	public List<Object> load(Object object);
	
	public void save(Object object);
}
