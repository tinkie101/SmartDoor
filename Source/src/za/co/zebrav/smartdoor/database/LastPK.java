package za.co.zebrav.smartdoor.database;

public class LastPK
{
	private long pk = 0;
	
	public LastPK(long pk)
	{
		this.pk = pk;
	}
	
	public long getPK()
	{
		return pk;
	}
}
