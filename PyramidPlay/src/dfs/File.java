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
	public void append(byte[] content, PeerDHT peer) throws IOException {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");	
			
			//hash the data to get the guid
			int guid = ByteBuffer.wrap(md.digest(content)).getInt();
			
			Chunk chunk = new Chunk(guid, content);
			//add a new file to the chunk.
			chunks.add(chunk);
			
			FuturePut put = peer.add(new Number160(guid)).data(new Data(chunk)).start();
			put.awaitUninterruptibly();
			
		} catch (NoSuchAlgorithmException e) {
			
		}
	}
}
