package dfs;

import java.io.Serializable;

public class Chunk implements Serializable {
	private int guid;
	private byte[] data;
	private String first;
	private String last;
	
	public Chunk(int guid, byte[] data) {
		this.guid = guid;
		this.data = data;
	}
	
	/**
	 * Gets the data of this chunk.
	 * 
	 * @return Return's chunk's data.
	 */
	public byte[] getData() {
		return data;
	}
	
	public String getFirst() {
		return first;
	}
	
	public String getLast() {
		return last;
	}
	
	
}
