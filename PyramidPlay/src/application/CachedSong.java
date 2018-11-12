package application;

public class CachedSong 
{
	private int GUID;
	public int count;
	private byte[] bytes;
	
	public CachedSong(int id, byte[] b)
	{
		GUID = id;
		count = 0;
		bytes = b;
	}
	
	public int getGUID()
	{
		return GUID;
	}
	
	public void setGUID(int g)
	{
		GUID = g;
	}
	
	public byte[] getBytes()
	{
		return bytes;
	}
	
	public void setBytes(byte[] b)
	{
		bytes = b;
	}
}
