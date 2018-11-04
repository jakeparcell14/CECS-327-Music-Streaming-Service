package dfs;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class File {
	private String fileName;
	private ArrayList<Chunk> chunks;

	
	public File(String fileName) {
		this.fileName = fileName;
		chunks = new ArrayList<Chunk>();
	}
	
	/**
	 * Gets a particular chunk of this file.
	 * @param i Index of the chunk to return.
	 * @return Returns the given chunk of that file.
	 */
	public byte[] getChunk(int i) {
		return chunks.get(i).getData();
	}
	
	/**
	 * Gets this file's name.
	 * @return Returns filename of this file.
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Appends data to this file.
	 * @param content
	 */
	public void append(byte[] content) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");	
			
			//hash the data to get the guid
			int guid = ByteBuffer.wrap(md.digest(content)).getInt();
			
			//add a new file to the chunk.
			chunks.add(new Chunk(guid, content));
			
		} catch (NoSuchAlgorithmException e) {
			
		}
	}
}
