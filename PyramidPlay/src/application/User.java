package application;

/**
 * This class stores user information
 * @author Jacob Parcell
 *
 */
public class User 
{
	String username;
	String password;
	String email;
	
	/**
	 * Default constructor for User
	 */
	public User()
	{
		username = "";
		password = "";
		email = "";
	}
	
	/**
	 * Constructor for User
	 * @param u		given username
	 * @param p		given password
	 * @param e		given email
	 */
	public User(String u, String p, String e)
	{
		username = u;
		password = p;
		email = e;
	}
	
	/**
	 * Retrieves username
	 * @return	username
	 */
	public String getUsername()
	{
		return username;
	}
	
	/**
	 * Sets the username to a given value
	 * @param u		given username
	 */
	public void setUsername(String u)
	{
		username = u;
	}
	
	/**
	 * Retrieves password
	 * @return	password
	 */
	public String getPassword()
	{
		return password;
	}
	
	/**
	 * Sets the password to a given value
	 * @param p		given password
	 */
	public void setPassword(String p)
	{
		password = p;
	}
	
	/**
	 * Retrieves email
	 * @return	email
	 */
	public String getEmail()
	{
		return email;
	}
	
	/**
	 * Sets the email to a given value
	 * @param e		given email
	 */
	public void setEmail(String e)
	{
		email = e;
	}
}
