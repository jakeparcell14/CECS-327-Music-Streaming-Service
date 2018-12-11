package dfs;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.Gson;

import application.Album;
import application.Artist;
import application.Song;

public class Metadata {
	private ArrayList<File> files = new ArrayList<File>();
	
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
	public void append(String fileName, byte[] content) throws IOException {
		File f = getFile(fileName);
		
		if (null == f) {
			f = new File(fileName);
			files.add(f);
		}
		
		f.append(content);
		
	}
	
	public void RemoveAllFiles() {
		files.clear();
	}
	
	public void AddFile(File file) {
		files.add(file);
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
	
	/**
	 * Searches for the chunk that contains the search criteria.
	 * @param fileName File that you are searching.
	 * @param search Search criteria
	 * @return Returns the chunk that most contains that search criteria if it exists.
	 */
	private byte[] SearchForChunk(String fileName, String search) {
		Gson gson = new Gson();
		if (fileName.equals("songs_inverted_index.json")) {
			File file = getFile(fileName);
			

			
			//iterate through file's chunks
			for (int i = 0; i < file.getNumberOfChunks(); i++) {
				
				//get this chunk
				Chunk chunk = file.getChunk(i);

				
				//get the chunks first and last items, this allows us to check if the search is in this chunk
				Song first = gson.fromJson(chunk.getFirst(), Song.class);
				Song last = gson.fromJson(chunk.getLast(), Song.class);
								
				//if the search criteria is smaller than the title, let's just compare the first n letters of the title with the length n search
				if (first.getTitle().length() > search.length() && last.getTitle().length() > search.length()) {
					if (first.getTitle().substring(0, search.length()).toLowerCase().compareTo(search) <= 0 && last.getTitle().substring(0, search.length()).toLowerCase().compareTo(search) >= 0) {
						
						return chunk.getData();
					}
				//otherwise, we can't compare substrings, so let's just compare alphabetically now
				} else {
					if (first.getTitle().toLowerCase().compareTo(search) <= 0 && last.getTitle().toLowerCase().compareTo(search) >= 0) {
						return chunk.getData();
					}
				}
			}
		} else if (fileName.equals("albums_inverted_index.json")) {
			File file = getFile(fileName);
			
			//iterate through file's chunks
			for (int i = 0; i < file.getNumberOfChunks(); i++) {
				
				//get this chunk
				Chunk chunk = file.getChunk(i);
				
				//gets the chunk's first and last items
				Album first = gson.fromJson(chunk.getFirst(), Album.class);
				Album last = gson.fromJson(chunk.getLast(), Album.class);
				
				//if the search criteria is smaller than the title, let's just compare the first n letters of the title with the length n search
				if (first.getName().length() > search.length() && last.getName().length() > search.length()) {
					if (first.getName().substring(0, search.length()).toLowerCase().compareTo(search.toLowerCase()) <= 0 && last.getName().substring(0, search.length()).toLowerCase().compareTo(search.toLowerCase()) >= 0) {
						return chunk.getData();
					}
				//otherwise, we can't compare substrings, so let's just compare alphabetically now
				} else {
					if (first.getName().toLowerCase().compareTo(search) <= 0 && last.getName().toLowerCase().compareTo(search) >= 0) {
						return chunk.getData();
					}
				}
			}

		} else if (fileName.equals("artists_inverted_index.json")){
			File file = getFile(fileName);
						
			//iterate through file's chunks
			for (int i = 0; i < file.getNumberOfChunks(); i++) {
				//get this chunk
				Chunk chunk = file.getChunk(i);
				
				//get the chunks first and last items, this allows us to check if the search is in this chunk
				Artist first = gson.fromJson(chunk.getFirst(), Artist.class);
				Artist last = gson.fromJson(chunk.getLast(), Artist.class);
				
				//if the search criteria is smaller than the title, let's just compare the first n letters of the title with the length n search
				if (first.getName().length() > search.length() && last.getName().length() > search.length()) {
					if (first.getName().substring(0, search.length()).toLowerCase().compareTo(search) <= 0 && last.getName().substring(0, search.length()).toLowerCase().compareTo(search) >= 0) {
						return chunk.getData();
					}
				//otherwise, we can't compare substrings, so let's just compare alphabetically now
				} else {
					if (first.getName().toLowerCase().compareTo(search) <= 0 && last.getName().toLowerCase().compareTo(search) >= 0) {
						return chunk.getData();
					}
				}
				
			}
		
		}
		
		return null;
	}
	
	/**
	 * Gets the metadata object from the file system.
	 * @return Returns instantiated metadata object.
	 */
	public static Metadata GetMetadata() {
		
		Gson gson = new Gson();
		java.io.File file = new java.io.File("metadata.json");
		
		Scanner scanner = null;
		Metadata metadata = null;
		
		try {
			scanner = new Scanner(file);
			metadata = gson.fromJson(scanner.nextLine(), Metadata.class);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		
		
		scanner.close();

		return metadata;
	}
	
	/**
	 * Writes metadata to disk.
	 */
	public void writeMetadata() {
		FileWriter out = null;
		Gson gson = new Gson();
		try {
			out = new FileWriter("metadata.json");
			out.write(gson.toJson(this));
			out.close();
		} catch (IOException e) {
		} 
			
	}
	
	/**
	 * Gets the artist that starts with the specified search criteria.
	 * @param name String that is being searched in artists.
	 * @return Returns arraylist of all songs from that artist.
	 */
	public ArrayList<Song> getArtist(String name){
		Gson gson = new Gson();
		
		//get the chunk that contains this search criteria
		byte[] chunkByte = SearchForChunk("artists_inverted_index.json", name);
		if(chunkByte!=null) {
			//split it at new line characters, getting each artist json
			String[] artists = new String(chunkByte).trim().split("\\r?\\n");
			
			ArrayList<Song> songs = new ArrayList<Song>();
			
			for (int i = 0; i < artists.length; i++) {
				Artist artist = gson.fromJson(artists[i], Artist.class);
				
				//if this artist starts with the search criteria...
				//YOU CAN CHANGE THIS LOGIC
				if (artist.getName().toLowerCase().startsWith(name)) {
					
					ArrayList<Album> albums = artist.getSongs();
					
					//get all their songs
					for (int j = 0; j < albums.size(); j++) {
						songs.addAll(albums.get(j).getSongs());
					}
					return songs;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Gets all songs from a specific album
	 * @param title Search criteria
	 * @return Returns all songs from the album that starts with the search.
	 */
	public ArrayList<Song> getAlbum(String title){
		Gson gson = new Gson();

		//get the chunk that contains this search criteria
		byte[] chunkByte = SearchForChunk("albums_inverted_index.json", title);
		if(chunkByte!=null) {

			//split it at new line characters, getting each album json
			String[] albums = new String(chunkByte).trim().split("\\r?\\n");
			System.out.println(albums==null);
			
			for (int i = 0; i < albums.length; i++) {
				Album album = gson.fromJson(albums[i], Album.class);
				
				//if this album name starts with the search criteria...
				//YOU CAN CHANGE THIS LOGIC
				if (album.getName().toLowerCase().startsWith(title)) {
					
					//return all the songs from this album
					return album.getSongs();
				}
			}
		}
		return null;
	}
	
	/**
	 * Gets song with that name.
	 * 
	 * @param title Search criteria
	 * @return Returns song that starts with the search
	 */
	public Song getSong(String title){
		Gson gson = new Gson();

		//get the chunk that contains this search criteria
		byte[] chunkByte = SearchForChunk("songs_inverted_index.json", title);
		if(chunkByte!=null) {
			//split it at new line characters, getting each song json
			String[] chunk = new String(chunkByte).trim().split("\\r?\\n");
			
			for (int i = 0; i < chunk.length; i++) {
				Song thisSong = gson.fromJson(chunk[i], Song.class);
				
				//if this song name starts with the search criteria...
				//YOU CAN CHANGE THIS LOGIC
				if (thisSong.getTitle().toLowerCase().startsWith(title)) {
					return thisSong;
				}
			}
		}		
		return null;
		
	}
}
