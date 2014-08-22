package za.co.zebrav.smartdoor.users;

public class User
{
	private static long id = 0;
	private String username = "";//user names must be unique
	private String password = "";
	private String firstnames = "";
	private String surname = "";
	
	//------------------------------------------------------------------------CONSTRUCTOR
	public User(String username, String password, String firstnames, String surname)
	{
		id++;
		//if ever id went out of range of a long, flip back to zero (MAX_VALUE -> 9,223,372,036,854,775,807)
		if(id < 0)
			id = 0;
		
		this.username = username;
		this.password = password;
		this.firstnames = firstnames;
		this.surname = surname;
	}
	
	//------------------------------------------------------------------------getID
	public long getID()
	{
		return id;
	}
	
	//------------------------------------------------------------------------getUsername
	public String getUsername()
	{
		return username;
	}

	//------------------------------------------------------------------------setUsername
	public void setUsername(String username)
	{
		this.username = username;
	}

	//------------------------------------------------------------------------getPassword
	public String getPassword()
	{
		return password;
	}

	//------------------------------------------------------------------------setPassword
	public void setPassword(String password)
	{
		this.password = password;
	}

	//------------------------------------------------------------------------getFirstnames
	public String getFirstnames()
	{
		return firstnames;
	}

	//------------------------------------------------------------------------setFirstnames
	public void setFirstnames(String firstnames)
	{
		this.firstnames = firstnames;
	}

	//------------------------------------------------------------------------getSurname
	public String getSurname()
	{
		return surname;
	}

	//------------------------------------------------------------------------setSurname
	public void setSurname(String surname)
	{
		this.surname = surname;
	}
}
