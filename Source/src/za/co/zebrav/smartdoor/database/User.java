package za.co.zebrav.smartdoor.database;

public class User
{
	private static long id = -1;
	private String username = "";//user names must be unique
	private String password = "";
	private String firstnames = "";
	private String surname = "";
	
	//------------------------------------------------------------------------CONSTRUCTOR
	/**
	 * When (addPK == true) the largest PK is updated in the database
	 * Use this for storing user in database (with addPK as true)
	 * @param firstnames
	 * @param surname
	 * @param username
	 * @param password
	 * @param addPK
	 */
	public User(String firstnames, String surname, String username, String password, boolean addPK)
	{
		this.username = username;
		this.password = password;
		this.firstnames = firstnames;
		this.surname = surname;
		
		//if addPK == true, update nextPK
		if(addPK)
		{
			
		}
	}
	
	/**
	 * Use this for database queries.
	 * @param firstnames
	 * @param surname
	 * @param username
	 * @param password
	 */
	public User(String firstnames, String surname, String username, String password)
	{
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
	
	//------------------------------------------------------------------------setID
	public void setID(long value)
	{
		id = value;
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
