package application;

import java.util.ArrayList;

/**
 * This class holds a list of songs
 * @author Jacob Parcell
 *
 */
public class Playlist 
{
	private ArrayList<Song> songs;
	
	/**
	 * Default constructor
	 */
	public Playlist()
	{
		songs = new ArrayList<Song>();
	}
	
	/**
	 * Constructor for Playlist to initialize song list with given values
	 * @param s		given list of songs
	 */
	public Playlist(ArrayList<Song> s)
	{
		songs = new ArrayList<Song>(s);
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
