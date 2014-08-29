package za.co.zebrav.smartdoor.database;

/**
 * Singleton pattern
 */
public class LargestPrimaryKey
{
	private long pk = 0;

	/**
	 * @param pk
	 */
	public void setPK(long pk)
	{
		this.pk = pk;
	}
	
	/**
	 * Retrieves largest primary key in the database
	 * @return
	 */
	public long getPK()
	{
		return pk;
	}
}
