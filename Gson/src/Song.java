

import java.io.Serializable;

/**
 * This class holds information to identify a song
 * @author Jacob Parcell
 *
 */
public class Song implements Comparable<Song>, Serializable
{
	private String title, artist, album, fileSource;
	
	/**
	 * Default constructor for Song
	 */
	public Song()
	{
		title = "";
		artist = "";
		album = "";
		fileSource = "";
	}
	
	/**
	 * Constructor for Song with just song title and file source
	 * @param t		song title
	 * @param fs	file source
	 */
	public Song(String t, String fs)
	{
		title = t;
		artist = "";
		album = "";
		fileSource = fs;
	}
	
	/**
	 * Constructor for song with all information
	 * @param t		song title
	 * @param ar	artist of the song	
	 * @param al	album title
	 * @param fs	file source
	 */
	public Song(String t, String ar, String al, String fs)
	{
		title = t;
		artist = ar;
		album = al;
		fileSource = fs;
	}
	
	/**
	 * Returns title of the song
	 * @return	song title
	 */
	public String getTitle()
	{
		return title;
	}
	
	/**
	 * Sets title of the song to given value
	 * @param t		given title
	 */
	public void setTitle(String t)
	{
		title = t;
	}
	
	/**
	 * Returns artist of the song
	 * @return	song artist
	 */
	public String getArtist()
	{
		return artist;
	}
	
	/**
	 * Sets artist of the song to given value
	 * @param ar	given artist name
	 */
	public void setArtist(String ar)
	{
		artist = ar;
	}
	
	/**
	 * Returns album of the song
	 * @return	album of the song
	 */
	public String getAlbum()
	{
		return album;
	}
	
	/**
	 * Sets album of the song to given value
	 * @param al	given album name
	 */
	public void setAlbum(String al)
	{
		album = al;
	}
	
	/**
	 * Returns source of the song file
	 * @return	song file
	 */
	public String getFileSource()
	{
		return fileSource;
	}
	
	/**
	 * Sets file source of the song to given value
	 * @param fs	given file source
	 */
	public void setFileSource(String fs)
	{
		fileSource = fs;
	}

	@Override
	/**
	 * Compares songs to each other based on song title
	 */
	public int compareTo(Song s) 
	{
		return this.getTitle().compareTo(s.getTitle());
	}
}