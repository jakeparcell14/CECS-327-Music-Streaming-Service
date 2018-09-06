package application;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class stores user information
 * @author Jacob Parcell
 * 
 * TODO update javadocs
 *
 */
public class User implements Comparable<User>
{
	private String firstName, lastName, username, password;
	
	private ArrayList<Playlist> playlists;
	
	private Playlist savedSongs; //TODO implement
	
	/**
	 * Default constructor for User
	 */
	public User()
	{
		firstName = "";
		lastName = "";
		username = "";
		password = "";
		playlists = new ArrayList<Playlist>();
		savedSongs = new Playlist("saved", null, null);
	}
	
	/**
	 * Constructor for User
	 * @param u		given username
	 * @param p		given password
	 */
	public User(String fn, String ln, String u, String p)
	{
		firstName = fn;
		lastName = ln;
		username = u;
		password = p;
		playlists = new ArrayList<Playlist>();
		savedSongs = new Playlist("saved", null, null);
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
	public String getlastName()
	{
		return lastName;
	}
	
	/**
	 * Sets the email to a given value
	 * @param e		given email
	 */
	public void setlastName(String ln)
	{
		lastName = ln;
	}
	
	/**
	 * Retrieves email
	 * @return	email
	 */
	public String getFirstName()
	{
		return firstName;
	}
	
	/**
	 * Sets the email to a given value
	 * @param e		given email
	 */
	public void setFirstName(String fn)
	{
		firstName = fn;
	}
	
	/**
	 * Retrieves playlists
	 * @return		list of playlists
	 */
	public ArrayList<Playlist> getPlaylists()
	{
		return playlists;
	}
	
	/**
	 * Sets list of playlists
	 * @param p		given list of playlists
	 */
	public void setPlaylists(ArrayList<Playlist> p)
	{
		playlists = p;
	}
	
	/**
	 * Retrieves saved songs
	 * @return		saved songs
	 */
	public Playlist getSavedSongs()
	{
		return savedSongs;
	}
	
	/**
	 * Sets playlist of saved songs
	 * @param s		given playlist
	 */
	public void setSavedSongs(Playlist p)
	{
		savedSongs = p;
	}
	
	/**
	 * Adds a song to the savedSongs playlist
	 * @param s		song to be saved
	 */
	public void saveSong(Song s)
	{
		savedSongs.addSong(s);
	}
	
	/**
	 * Adds playlist to user list
	 * @param p		given playlist
	 */
	public void addPlaylist(Playlist p)
	{
		playlists.add(p);
	}
	
	/**
	 * Checks for playlist with given playlist name
	 * @param playlistName		given playlist name
	 * @return					playlist with given name, null if playlist with the given name does not exist 
	 */
	public Playlist contains(String playlistName)
	{
		for(Playlist p : playlists)
		{
			if(p.getPlaylistName().equals(playlistName))
			{
				return p;
			}
		}
		
		return null;
	}
	
	/**
	 * removes playlist with given name
	 * @param playlistName		playlist name to be removed
	 * @return					true if song is removed, false if song does not exist
	 */
	public boolean removePlaylist(String playlistName)
	{
		return playlists.remove(this.contains(playlistName));
	}

	@Override
	public int compareTo(User u) 
	{
		return this.getUsername().compareTo(u.getUsername());
	}
}
