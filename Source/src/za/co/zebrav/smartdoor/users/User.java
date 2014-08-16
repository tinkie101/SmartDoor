package za.co.zebrav.smartdoor.users;

public class User
{
	private String id = "";
	
	public User(String id)
	{
		this.id = id;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public String getId()
	{
		return id;
	}
}
