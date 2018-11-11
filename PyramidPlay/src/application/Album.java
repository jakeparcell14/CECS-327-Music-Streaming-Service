package application;

import java.io.Serializable;
import java.util.ArrayList;

public class Album  implements Comparable<Album>, Serializable
{
	private String name;
	private ArrayList<Song> songs;
	
	/**
	 * constructor for album
	 * @param n		given name of the album
	 */
	public Album(String n)
	{
		name = n;
		songs = new ArrayList<Song>();
	}
	
	/**
	 * retrieves name of album
	 * @return		name of album
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * set name of album
	 * @param n		name of album
	 */
	public void setName(String n)
	{
		name = n;
	}
	
	/**
	 * retrieve song list
	 * @return		song list
	 */
	public ArrayList<Song> getSongs()
	{
		return songs;
	}
	
	/**
	 * sets list of songs
	 * @param s		given list of songs
	 */
	public void setSongs(ArrayList<Song> s)
	{
		songs = s;
	}
	
	/**
	 * add song to list
	 * @param s		song to be added
	 * @return		updated songs list
	 */
	public ArrayList<Song> addSong(Song s)
	{
		songs.add(s);
		return songs;
	}
	
	/**
	 * remove song from list
	 * @params s		song to be removed
	 * @return 		updated songs list
	 */
	public ArrayList<Song> removeSong(Song s)
	{
		songs.remove(s);
		return songs;
	}
	
	public String toString()
	{
		return songs.toString();
	}

	@Override
	public int compareTo(Album arg0) 
	{
		return this.getName().compareTo(arg0.getName());
	}
}
