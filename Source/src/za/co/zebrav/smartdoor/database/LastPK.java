package za.co.zebrav.smartdoor.database;

public class LastPK
{
	private int pk = 0;
	
	/**
	 * This is the constructor, set PK
	 * @param pk
	 */
	public LastPK(int pk)
	{
		this.pk = pk;
	}
	
	/**
	 * Getter function
	 * @return PK
	 */
	public int getPK()
	{
		return pk;
	}
}
