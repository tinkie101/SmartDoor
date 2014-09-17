package za.co.zebrav.smartdoor.database;

import java.io.Serializable;
import java.util.ArrayList;

import at.fhooe.mcm.smc.math.vq.Codebook;


public class User implements Serializable
{
	private int id = -1;
	private String username = "";//user names must be unique
	private String password = "";
	private String firstnames = "";
	private String surname = "";
	private ArrayList<Codebook> codeBook;
	
	
	
	/**
	 * Use this for database queries.
	 * @param firstnames
	 * @param surname
	 * @param username
	 * @param password
	 */
	/*public User(String firstnames, String surname, String username, String password)
	{
		this.username = username;
		this.password = password;
		this.firstnames = firstnames;
		this.surname = surname;
	}*/
	
	public User(String firstnames, String surname, String username, String password, int pk, ArrayList<Codebook> cb)
	{
		this.username = username;
		this.password = password;
		this.firstnames = firstnames;
		this.surname = surname;
		this.id = pk;
		
		if(cb != null)
			this.codeBook = new ArrayList<Codebook>(cb);
	}
	
	public ArrayList<Codebook> getCodeBook()
	{
		ArrayList<Codebook> result = null;
		
		if(codeBook != null)
			result = new ArrayList<Codebook>(codeBook);
		
		return result;
	}
	
	public void setCodeBook(ArrayList<Codebook> cb)
	{
		this.codeBook = new ArrayList<Codebook>(cb);
	}
	
	//------------------------------------------------------------------------getID
	public int getID()
	{
		return id;
	}
	
	//------------------------------------------------------------------------setID
	public void setID(int value)
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
