package dfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import application.Album;
import application.Artist;
import application.Song;
import p2p.PeerToPeer;

/**
 * 
 * abstraction that contains the map and reduce methods for the distributed hash table
 *
 */
public class Sorting {
	static private Object lock = new Object();
	/**
	 * list that determines which songs will be stored ini which peer
	 */
	private ArrayList<String> songs = new ArrayList<String>(Arrays.asList("", "", ""));
	/**
	 * list that determines which albums will be stored in which peer
	 */
	private ArrayList<String> albums = new ArrayList<String>(Arrays.asList("", "", ""));
	/**
	 * list that determines which artists will be stored in which peer
	 */
	private ArrayList<String> artists = new ArrayList<String>(Arrays.asList("", "", ""));

	/**
	 * ties together the map and reduce functions and creates the new inverted index
	 * @param file the file to map and reduce
	 * @return the new file that has been mapped and 0reduced
	 */
	public File mapReduce (File file) {
		File newFile = new File(file.getFileName());
		System.out.println("FILE NAME: " + file.getFileName());
		map(file);
		
		System.out.println("<<<<<MAP DONE>>>>>");
		System.out.println("songs: ");
		for (String i : songs) {
			System.out.println(i);
		}

		System.out.println("albums: ");
		for (String i : albums) {
			System.out.println(i);
		}

		System.out.println("artists: ");

		for (String i : artists) {
			System.out.println(i);
		}
		
		reduce(file);

		System.out.println("<<<<<MAP REDUCE DONE>>>>>");
		System.out.println("songs: ");
		for (String i : songs) {
			System.out.println(i);
		}

		System.out.println("albums: ");
		for (String i : albums) {
			System.out.println(i);
		}

		System.out.println("artists: ");

		for (String i : artists) {
			System.out.println(i);
		}


		for (int i = 0; i < file.getChunkGuids().length; i++) {

			file.removeChunk(file.getChunkGuids()[i]);

		}


		try {
			if (file.getFileName().equals("songs_inverted_index.json")) {
				for (int i = 0; i < songs.size(); i++) { 
					if (!songs.get(i).isEmpty())
						newFile.append(songs.get(i).getBytes());
				}
			} else if (file.getFileName().equals("albums_inverted_index.json")) {
				for (int i = 0; i < albums.size(); i++) { 
					if (!albums.get(i).isEmpty())
						newFile.append(albums.get(i).getBytes());
				}
			} else if (file.getFileName().equals("artists_inverted_index.json")) {
				for (int i = 0; i < artists.size(); i++) {
					if (!artists.get(i).replace("\n", "").isEmpty())
						newFile.append(artists.get(i).getBytes());
				}				}	
		} catch (IOException e) {

		}


		return newFile;
	}


	/**
	 * map function for the distributed hash table. implements multithreading to send chunks to the correct peers
	 * @param file the file chosen to map
	 */
	private void map(File file) {
		ArrayList<Thread> threads = new ArrayList<Thread>(3);

		for (int i = 0; i<file.getNumberOfChunks(); i++) {
			MapThread thread = new MapThread(file.getFileName(), file.getChunk(i));
			threads.add(thread);
		}

		for (Thread t : threads) {
			t.start();
		}

		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {

			}
		}
	}
	/**
	 * thread to map the files
	 */
	private class MapThread extends Thread {
		private String fileName;
		private Chunk chunk;
		public MapThread(String fileName, Chunk chunk) {
			this.fileName = fileName;
			this.chunk = chunk;
			super.setDaemon(true);
		}
		@Override
		public void run() {
			synchronized (lock) {
				map(fileName, chunk);
			}
		}
	}
	
	/**
	 * handler to sort file
	 */
	private class Handler extends Thread
	{
		private File file;
		private String content;
		private int id;

		/**
		 * constructor that sets the file, content and id
		 * @param givenFile the file needed to sort
		 * @param content the conte
		 * @param id
		 */
		public Handler(File givenFile, String content, int id)
		{
			this.file = givenFile;
			this.content = content;
			this.id = id;
		}
		
		/**
		 * 
		 */
		public void run()
		{
			ArrayList<String> arr = new ArrayList<String>(Arrays.asList(content.split("\n")));
			Collections.sort(arr);

			for (int i = 0; i < arr.size(); i++) {
				while (i+1 < arr.size()) {
					if (arr.get(i).equals(arr.get(i+1))) {
						arr.remove(i+1);
					}
					else {
						break;
					}
				}
			}

			String sorted = "";
			for (int i = 0; i < arr.size(); i++) {
				sorted += arr.get(i) + "\n";
			}

			//turn back into string and give back to arraylist
			switch (file.getFileName()) {
			case "songs_inverted_index.json":
				songs.set(id, sorted);
				break;
			case "albums_inverted_index.json":
				albums.set(id, sorted);
				break;
			case "artists_inverted_index.json":
				artists.set(id, sorted);
			}
		}
	}
	
	/**
	 * implements multithreading to remove duplicates in a file
	 * @param file the file to reduce
	 * @return
	 */
	private File reduce(File file) {
		//get chunks from file, get file data, add to treemap to remove duplicates, sort map, overwrite onto peer
		String fileName = file.getFileName();
		File updatedFile = new File(fileName);
		Gson gson = new Gson();

		ArrayList<Handler> threads = new ArrayList<Handler>();

		//reduce each chunk in its own thread
		if (fileName.equals("songs_inverted_index.json")) {
			for(int i = 0; i < songs.size(); i++)
			{
				Handler h = new Handler(updatedFile, songs.get(i), i);

				threads.add(h);

				h.start();
			}
		}
		else if (fileName.equals("albums_inverted_index.json")) {
			for(int i = 0; i < albums.size(); i++)
			{
				Handler h = new Handler(updatedFile, albums.get(i), i);

				threads.add(h);

				h.start();
			}
		}
		else if (fileName.equals("artists_inverted_index.json")) {
			for(int i = 0; i < artists.size(); i++)
			{
				Handler h = new Handler(updatedFile, artists.get(i), i);

				threads.add(h);

				h.start();
			}
		}

		//join threads to put back into updated file
		for(Handler thread : threads)
		{
			try 
			{
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return updatedFile;

	}

	/**
	 * maps the chunk in a file
	 * @param fileName file to map the chunk to
	 * @param chunk to be mapped
	 */
	private void map(String fileName, Chunk chunk) {
		System.out.println(new String(chunk.getData()));
		ArrayList<Object> objs = (ArrayList<Object>) createObjects(new String (chunk.getData()), fileName);

		if (fileName.equals("songs_inverted_index.json")) {
			ArrayList<Song> songs = (ArrayList<Song>) objs.stream().map(e -> (Song) e).collect(Collectors.toList());
			emitSong(createSongMap(songs));
		} else if (fileName.equals("albums_inverted_index.json")) {
			ArrayList<Album> albums = (ArrayList<Album>) objs.stream().map(e -> (Album) e).collect(Collectors.toList());
			emitAlbum(createAlbumMap(albums));
		} else if (fileName.equals("artists_inverted_index.json")) {
			ArrayList<Artist> artists = (ArrayList<Artist>)objs.stream().map(e -> (Artist) e).collect(Collectors.toList());
			emitArtist(createArtistMap(artists));
		}
	}

	/**
	 * transforms json string to an object. Based on the file, determines what type the object is
	 * @param json string to transform to an object
	 * @param fileName file for type decision
	 * @return list of objects from json string
	 */
	private List<Object> createObjects(String json, String fileName) {
		Gson gson = new Gson();

		ArrayList<String> strs = new ArrayList<String>(Arrays.asList(json.split("\\r?\\n")));

		if (fileName.equals("songs_inverted_index.json")) {
			return strs.stream().map(d -> gson.fromJson(d, Song.class)).collect(Collectors.toList());
		} else if (fileName.equals("albums_inverted_index.json")) {
			return strs.stream().map(d -> gson.fromJson(d, Album.class)).collect(Collectors.toList());
		} else if (fileName.equals("artists_inverted_index.json")) {
			return strs.stream().map(d -> gson.fromJson(d, Artist.class)).collect(Collectors.toList());
		}

		return null;
	}

	/**
	 * creates a treemap of song lists
	 * @param obj arraylist of songs
	 * @return treemap of songs from the arraylist
	 */
	private TreeMap<String, List<Song>> createSongMap(ArrayList<Song> obj) {
		TreeMap<String, List<Song>> map = new TreeMap<String, List<Song>>();

		for (Song song : obj) {
			if (map.containsKey(song.getTitle())) {
				map.get(song.getTitle()).add(song);
			} else {
				map.put(song.getTitle(), new ArrayList<Song>(Arrays.asList(song)));
			}
		}
		return map;
	}

	/**
	 * creates a treemap of Album lists
	 * @param obj arraylist of Albums
	 * @return treemap of Albums from the arraylist
	 */
	private TreeMap<String, List<Album>> createAlbumMap(ArrayList<Album> obj) {
		TreeMap<String, List<Album>> map = new TreeMap<String, List<Album>>();

		for (Album album : obj) {
			if (map.containsKey(album.getName())) {
				map.get(album.getName()).add(album);
			} else {
				map.put(album.getName(), new ArrayList<Album>(Arrays.asList(album)));
			}
		}

		return map;
	}

	/**
	 * creates a treemap of artist lists
	 * @param obj arraylist of artists
	 * @return treemap of artists from the arraylist
	 */
	private TreeMap<String, List<Artist>> createArtistMap(ArrayList<Artist> obj) {
		TreeMap<String, List<Artist>> map = new TreeMap<String, List<Artist>>();

		for (Artist artist : obj) {
			if (map.containsKey(artist.getName())) {
				map.get(artist.getName()).add(artist);
			} else {
				map.put(artist.getName(), new ArrayList<Artist>(Arrays.asList(artist)));
			}
		}

		return map;
	}

	/**
	 * determines which peer each element in a song map is sent to
	 * @param map tree map with list of songs 
	 */
	private void emitSong(TreeMap<String, List<Song>> map) {
		Iterator<Entry<String, List<Song>>> iter = map.entrySet().iterator();
		Gson gson = new Gson();
		while (iter.hasNext()) {
			Map.Entry<String, List<Song>> pair = (Map.Entry<String, List<Song>>) iter.next();
			String value = pair.getKey();
			for (Song song : pair.getValue()) {
				System.out.println(pair.getKey() + " " + pair.getValue());
				System.out.println(value.compareTo("A") >= 0 && value.compareTo("L") <= 0);
				System.out.println(value.compareTo("M") >= 0 && value.compareTo("X") <=0);
				System.out.println();
				if (value.compareTo("A") >= 0 && value.compareTo("L") <= 0) {

					String temp = songs.get(0);
					temp += gson.toJson(song, Song.class) + "\n";
					songs.set(0, temp);
				} else if (value.compareTo("M") >= 0 && value.compareTo("X") <=0) {
					String temp = songs.get(1);
					temp += gson.toJson(song, Song.class) + "\n";
					songs.set(1, temp);
				} else {
					String temp = songs.get(2);
					temp += gson.toJson(song, Song.class) + "\n";
					songs.set(2, temp);
				}
			}
		}
		
		System.out.println(songs);
	}

	/**
	 * determines which peer each element in a album map is sent to
	 * @param map tree map with list of albums 
	 */
	private void emitAlbum(TreeMap<String, List<Album>> map) {
		Iterator<Entry<String, List<Album>>> iter = map.entrySet().iterator();
		Gson gson = new Gson();
		while (iter.hasNext()) {
			Map.Entry<String, List<Album>> pair = (Map.Entry<String, List<Album>>) iter.next();
			String value = pair.getKey();
			for (Album album : pair.getValue()) {
				if (value.compareTo("A") >= 0 && value.compareTo("L") <= 0) {
					String temp = albums.get(0);
					temp += gson.toJson(album, Album.class) + "\n";
					albums.set(0, temp);				
				} else if (value.compareTo("M") >= 0 && value.compareTo("X") <=0) {
					String temp = albums.get(1);
					temp += gson.toJson(album, Album.class) + "\n";
					albums.set(1, temp);
				} else {
					String temp = albums.get(2);
					temp += gson.toJson(album, Album.class) + "\n";
					albums.set(2, temp);
				}
			}
		}	
	}

	/**
	 * determines which field each item in the artist map is sent to
	 * @param map
	 */
	private void emitArtist(TreeMap<String, List<Artist>> map) {
		Iterator<Entry<String, List<Artist>>> iter = map.entrySet().iterator();
		Gson gson = new Gson();
		while (iter.hasNext()) {
			Map.Entry<String, List<Artist>> pair = (Map.Entry<String, List<Artist>>) iter.next();
			String value = pair.getKey();
			for (Artist artist : pair.getValue()) {
				if (value.compareTo("A") >= 0 && value.compareTo("L") <= 0) {
					String temp = artists.get(0);
					temp += gson.toJson(artist, Artist.class) + "\n";
					artists.set(0, temp);				
				} else if (value.compareTo("M") >= 0 && value.compareTo("X") <=0) {
					String temp = artists.get(1);
					temp += gson.toJson(artist, Artist.class) + "\n";
					artists.set(1, temp);
				} else {
					String temp = artists.get(2);
					temp += gson.toJson(artist, Artist.class) + "\n";
					artists.set(2, temp);
				}
			}
		}	
	}
}
