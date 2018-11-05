package dfs;

import java.io.Serializable;

public class Chunk implements Serializable {
	private int guid;
	private byte[] data;
	
	
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
	
	
}
