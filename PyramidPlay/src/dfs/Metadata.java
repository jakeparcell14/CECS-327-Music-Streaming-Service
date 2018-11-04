package dfs;
import java.util.ArrayList;

public class Metadata {
	private ArrayList<File> files;
	
	/**
	 * Reads a particular chunk from a particular file.
	 * 
	 * @param fileName Name of the file to read from.
	 * @param i-th index chunk to get from this file.
	 * @return Returns the chunk object at that index.
	 */
	public byte[] getChunk(String fileName, int index) {
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
	 */
	public void append(String fileName, byte[] content) {
		File f = getFile(fileName);
		
		if (null == f) 
			f = new File(fileName);
		
		f.append(content);
		
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
}
