package application;

import java.util.ArrayList;

/**
 * This class holds a list of songs
 * @author Jacob Parcell
 *
 */
public class Playlist 
{
	private String name;
	private ArrayList<Song> songs;
	
	/**
	 * Default Constructor
	 */
	public Playlist()
	{
		name = "";
		songs = new ArrayList<Song>();
	}
	
	/**
	 * Constructor for Playlist to initialize name of playlist but no songs
	 */
	public Playlist(String n)
	{
		name = n;
		songs = new ArrayList<Song>();
	}
	
	/**
	 * Constructor for Playlist to initialize song list with given values
	 * @param s		given list of songs
	 */
	public Playlist(String n, ArrayList<Song> s)
	{
		name = n;
		songs = new ArrayList<Song>(s);
	}
	
	/**
	 * Retrieves name of playlist
	 * @return		name of playlist
	 */
	public String getPlaylistName()
	{
		return name;
	}
	
	/**
	 * Sets name of playlist to given value
	 * @param n		given name of playlist
	 */
	public void setPlaylistName(String n)
	{
		name = n;
	}
	
	/**
	 * Retrieve songs list
	 * @return	list of songs
	 */
	public ArrayList<Song> getSongs()
	{
		return songs;
	}
	
	/**
	 * Sets songs to given arraylist of songs
	 * @param s		given list of songs
	 */
	public void setSongs(ArrayList<Song> s)
	{
		songs = s;
	}
	
	/**
	 * Adds song to list
	 * @param s		given song
	 */
	public void addSong(Song s)
	{
		songs.add(s);
	}
	
	/**
	 * Removes song from list
	 * @param title		title of song to remove
	 * @return			true if song was removed, false if song did not exist
	 */
	public boolean removeSong(String title)
	{
		return songs.remove(this.contains(title));
	}
	
	/**
	 * Checks if a song with a given title exists
	 * @param title		given title of song
	 * @return			Song object with given title, null if Song with given title does not exist
	 */
	public Song contains(String title)
	{
		for(Song s: songs)
		{
			if(s.getTitle().equals(title))
			{
				return s;
			}
		}
		return null;
	}
}
