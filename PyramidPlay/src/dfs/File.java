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
	
	public int getNumberOfChunks() {
		return chunks.size();
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
			
			System.out.println("Adding chunk");
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
