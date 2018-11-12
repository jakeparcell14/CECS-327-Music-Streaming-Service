package dfs;

import java.io.IOException;
import java.io.Serializable;

import p2p.PeerToPeer;

public class Chunk implements Serializable {
	private int guid;
	private String first;
	private String last;
	private PeerToPeer p2p = PeerToPeer.getInstance();
	
	public Chunk(int guid, String first, String last) {
		this.guid = guid;
		this.first = first;
		this.last = last;
	}
	
	public String getFirst() {
		return first;
	}
	
	public String getLast() {
		return last;
	}
	
	public int getGUID() {
		return guid;
	}
	
	public byte[] getData() {
		try {
			return (byte[]) p2p.Get(guid);
		} catch (ClassNotFoundException | IOException e) {
			return null;
		}
	}
	
	
}
