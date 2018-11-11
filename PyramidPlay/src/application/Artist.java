package application;

import java.io.Serializable;
import java.util.ArrayList;

public class Artist  implements Serializable
{
	private String name;
	private ArrayList<Album> albums;

	public Artist(String n)
	{
		name = n;
		albums = new ArrayList<Album>();
	}

	/**
	 * retrieves name of artist
	 * @return		name of artist
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * set name of artist
	 * @param n		name of artist
	 */
	public void setName(String n)
	{
		name = n;
	}

	/**
	 * retrieve album list
	 * @return		album list
	 */
	public ArrayList<Album> getSongs()
	{
		return albums;
	}

	/**
	 * sets list of albums
	 * @param a		given list of albums
	 */
	public void setSongs(ArrayList<Album> a)
	{
		albums = a;
	}
	
	/**
	 * add album to list
	 * @param a		album to be added
	 * @return		updated albums list
	 */
	public ArrayList<Album> addAlbum(Album a)
	{
		albums.add(a);
		return albums;
	}
	
	/**
	 * remove album from list
	 * @param a		album to be removed
	 * @return 		updated albums list
	 */
	public ArrayList<Album> removeAlbum(Album a)
	{
		albums.remove(a);
		return albums;
	}
}
