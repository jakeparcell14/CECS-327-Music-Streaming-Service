package application;

/**
 * This class saves the byte array when the server requests the bytes of the mp3 from a peer
 * @author Jacob Parcell
 *
 */
public class CachedSong 
{
	/**
	 * Global Unique Identifier for the song
	 */
	private int GUID;
	
	/**
	 * counts how many people are currently requesting the song
	 */
	public int count;
	
	/**
	 * mp3 file of the song represented by bytes
	 */
	private byte[] bytes;
	
	/**
	 * Constructor for cached song
	 * @param id	given guid of the song
	 * @param b		byte array of the song
	 */
	public CachedSong(int id, byte[] b)
	{
		GUID = id;
		count = 0;
		bytes = b;
	}
	
	/**
	 * retrieve GUID
	 * @return		GUID
	 */
	public int getGUID()
	{
		return GUID;
	}
	
	/**
	 * sets GUID
	 * @param g		given GUID
	 */
	public void setGUID(int g)
	{
		GUID = g;
	}
	
	/**
	 * retrieves byte array
	 * @return		byte array of the song
	 */
	public byte[] getBytes()
	{
		return bytes;
	}
	
	/**
	 * sets byte array
	 * @param b		byte array of the song
	 */
	public void setBytes(byte[] b)
	{
		bytes = b;
	}
}
