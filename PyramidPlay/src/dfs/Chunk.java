package dfs;

import java.io.IOException;
import java.io.Serializable;

import p2p.PeerToPeer;

public class Chunk implements Serializable {
	private int guid;
	private String first;
	private String last;
	
	/**
	 * Creates a Chunk instance.
	 * @param guid GUID of this chunk.
	 * @param first First object stored in this chunk.
	 * @param last Last object stored in this chunk.
	 */
	public Chunk(int guid, String first, String last) {
		this.guid = guid;
		this.first = first;
		this.last = last;
	}
	
	/**
	 * 
	 * @return Returns first object stored in this chunk.
	 */
	public String getFirst() {
		return first;
	}
	
	/**
	 * 
	 * @return Returns last object stored in this chunk.
	 */
	public String getLast() {
		return last;
	}
	
	/**
	 * 
	 * @return Returns the GUID for this chunk.
	 */
	public int getGUID() {
		return guid;
	}
	
	/**
	 * 
	 * @return Gets all the bytes stored in this chunk.
	 */
	public byte[] getData() {
		PeerToPeer p2p = PeerToPeer.getInstance();

		try {
			return (byte[]) p2p.Get(guid);
		} catch (ClassNotFoundException | IOException e) {
			return null;
		}
	}
	
	
}
