package application;

import java.io.Serializable;
import java.util.ArrayList;

public class DataStore implements Serializable
{
	ArrayList<User> users;
	ArrayList<Song> songs;
	
	/**
	 * Default constructor
	 */
	public DataStore()
	{
		users = new ArrayList<User>();
		songs = new ArrayList<Song>();
	}
	
	/**
	 * Constructor given user and song lists
	 * @param u		given user list
	 * @param s		given song list
	 */
	public DataStore(ArrayList<User> u, ArrayList<Song> s)
	{
		users = new ArrayList<User>(u);
		songs = new ArrayList<Song>(s);
	}
	
	/**
	 * Retrieves User list
	 * @return		list of users
	 */
	public ArrayList<User> getUsers()
	{
		return users;
	}
	
	/**
	 * Sets User list
	 * @param u		given list of users
	 */
	public void setUsers(ArrayList<User> u)
	{
		users = u;
	}
	
	/**
	 * Retrieves song list
	 * @return		list of songs
	 */
	public ArrayList<Song> getSongs()
	{
		return songs;
	}
	
	/**
	 * Sets song list
	 * @param s		given list of songs
	 */
	public void setSongs(ArrayList<Song> s)
	{
		songs = s;
	}
}
