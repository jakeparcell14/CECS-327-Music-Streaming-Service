package dfs;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import p2p.PeerToPeer;

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
	public Chunk getChunk(int i) {
		return chunks.get(i);
	}
	
	/**
	 * 
	 * @return Gets total number of chunks in this file.
	 */
	public int getNumberOfChunks() {
		return chunks.size();
	}
	
	/**
	 * Gets guids of all chunks in file
	 * @return Returns an array of chunk guids
	 */
	public int[] getChunkGuids() {
		int[] guids = new int[chunks.size()];
		
		for (int i = 0; i < chunks.size(); i++) {
			guids[i] = chunks.get(i).getGUID();
		}
		
		return guids;
	}
	
	/**
	 * Gets this file's name.
	 * @return Returns filename of this file.
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Removes a chunk from this file and from the peer
	 * 
	 * @param guid GUID of chunk to remove.
	 * @return Returns true if successfully removed, false if it wasn't found.
	 */
	public boolean removeChunk(int guid) {
		if (chunks.stream().anyMatch( d -> d.getGUID() == guid)) {
			PeerToPeer p2p = PeerToPeer.getInstance();
			chunks.removeIf( d -> d.getGUID() == guid);
			p2p.remove(guid);
			return true;
		}
		return false;
	}
	
	/**
	 * Appends data to this file.
	 * @param content
	 * @throws IOException 
	 */
	public void append(byte[] content) throws IOException {
		PeerToPeer p2p = PeerToPeer.getInstance();

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");	
			
			//hash the data to get the guid
			int guid = ByteBuffer.wrap(md.digest(content)).getInt();
			
			String[] firstLast = getFirstLast(content);
			Chunk chunk = new Chunk(guid, firstLast[0], firstLast[1]);
			
			//add a new file to the chunk.
			chunks.add(chunk);
			
			p2p.Put(content, guid);
			
		} catch (NoSuchAlgorithmException e) {
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String[] getFirstLast(byte[] content) {
		String str = new String(content);
		String[] strs = str.split("\\r?\\n");
		return new String[]{strs[0], strs[strs.length - 1]};
	}
}
