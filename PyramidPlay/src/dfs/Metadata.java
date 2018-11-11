package dfs;
import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.Gson;

import application.Album;
import application.Artist;
import application.Song;
import net.tomp2p.dht.PeerDHT;

public class Metadata {
	private ArrayList<File> files;
	private static Gson gson = new Gson();
	
	/**
	 * Reads a particular chunk from a particular file.
	 * 
	 * @param fileName Name of the file to read from.
	 * @param i-th index chunk to get from this file.
	 * @return Returns the chunk object at that index.
	 */
	public Chunk getChunk(String fileName, int index) {
		for (int i = 0; i<files.size(); i++)  {
			if (files.get(i).getFileName().equals(fileName)) {
				return files.get(i).getChunk(i);
			}
		}
		return null;
	}
	
	/**
	 * Appends data to a file. If the file does not exist, a new
	 * file is created.
	 * 
	 * @param fileName Name of the file.
	 * @param content Data to append to the file.
	 * @throws IOException 
	 */
	public void append(String fileName, byte[] content, PeerDHT peer) throws IOException {
		File f = getFile(fileName);
		
		if (null == f) 
			f = new File(fileName);
		
		f.append(content, peer);
		
	}
	
	/**
	 * Private helper method that gets a file of a specific name.
	 * 
	 * @param fileName Name of the file to get.
	 * @return Returns the file objec with that filename.
	 */
	private File getFile(String fileName) {
		for (int i = 0; i<files.size(); i++) {
			if (files.get(i).getFileName().equals(fileName)) {
				return files.get(i);
			}
		}
		
		return null;
	}
	
	/**
	 * Gets all files in the DFS.
	 * @return Returns arraylist of all files in the DFS.
	 */
	public ArrayList<File> ls() {
		return files;
	}
	
	public byte[] SearchForChunk(String fileName, String search) {
		if (fileName.equals("songs_inverted_index")) {
			File file = getFile(fileName);
			
			for (int i = 0; i < file.getNumberOfChunks(); i++) {
				Chunk chunk = file.getChunk(i);
				Song first = gson.fromJson(chunk.getFirst(), Song.class);
				Song last = gson.fromJson(chunk.getLast(), Song.class);
				
				if (first.getTitle().compareTo(search) >= 0 && last.getTitle().compareTo(search) <= 0) {
					return chunk.getData();
				}
			}
		} else if (fileName.equals("albums_inverted_index")) {
			File file = getFile(fileName);
			
			for (int i = 0; i < file.getNumberOfChunks(); i++) {
				Chunk chunk = file.getChunk(i);
				Album first = gson.fromJson(chunk.getFirst(), Album.class);
				Album last = gson.fromJson(chunk.getLast(), Album.class);
				
				if (first.getName().compareTo(search) >= 0 && last.getName().compareTo(search) <= 0) {
					return chunk.getData();
				}
			}

		} else {
			File file = getFile(fileName);
			
			for (int i = 0; i < file.getNumberOfChunks(); i++) {
				Chunk chunk = file.getChunk(i);
				Artist first = gson.fromJson(chunk.getFirst(), Artist.class);
				Artist last = gson.fromJson(chunk.getLast(), Artist.class);
				
				if (first.getName().compareTo(search) >= 0 && last.getName().compareTo(search) <= 0) {
					return chunk.getData();
				}
			}
		
		}
		
		return null;
	}
}
