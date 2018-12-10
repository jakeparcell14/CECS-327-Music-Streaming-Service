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

public class Sorting {
	
	private ArrayList<String> songs = new ArrayList<String>(Arrays.asList("", "", ""));
	private ArrayList<String> albums = new ArrayList<String>(Arrays.asList("", "", ""));
	private ArrayList<String> artists = new ArrayList<String>(Arrays.asList("", "", ""));

	
	public File mapReduce (File file) {
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
				t.interrupt();
			} catch (InterruptedException e) {
				
			}
		}
		System.out.println("Songs:" + songs.size());
		for (String s : songs) {
			System.out.println(s);
		}
		System.out.println("Albums:");

		for (String s : albums) {
			System.out.println(s);
		}
		System.out.println("Artists:");

		for (String s : artists) {
			System.out.println(s);
		}
		System.out.println("<<<<<DONE>>>>>");
		
		reduce(file);
		
		for (String s : songs) {
			System.out.println(s);
		}
		System.out.println("Albums:");

		for (String s : albums) {
			System.out.println(s);
		}
		System.out.println("Artists:");

		for (String s : artists) {
			System.out.println(s);
		}
		
		return null;
	}
	

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
			map(fileName, chunk);
		}
	}

	private File map(File file) {
		return null;
	}
	

	private class Handler extends Thread
	{
		private File file;
		private String content;
		private int id;

		public Handler(File givenFile, String content, int id)
		{
			this.file = givenFile;
			this.content = content;
			this.id = id;
		}

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

	
	private void map(String fileName, Chunk chunk) {
		ArrayList<Object> objs = (ArrayList<Object>) createObjects(new String (chunk.getData()), fileName);
		
		if (fileName.equals("songs_inverted_index.json")) {
			ArrayList<Song> songs = (ArrayList<Song>) objs.stream().map(e -> (Song) e).collect(Collectors.toList());
			emitSong(createSongMap(fileName, songs));
		} else if (fileName.equals("albums_inverted_index.json")) {
			ArrayList<Album> albums = (ArrayList<Album>) objs.stream().map(e -> (Album) e).collect(Collectors.toList());
			emitAlbum(createAlbumMap(fileName, albums));
		} else if (fileName.equals("artists_inverted_index.json")) {
			ArrayList<Artist> artists = (ArrayList<Artist>)objs.stream().map(e -> (Artist) e).collect(Collectors.toList());
			emitArtist(createArtistMap(fileName, artists));
		}
	}
	
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
	

	private TreeMap<String, List<Song>> createSongMap(String fileName, ArrayList<Song> obj) {
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
	
	private TreeMap<String, List<Album>> createAlbumMap(String fileName, ArrayList<Album> obj) {
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
	
	private TreeMap<String, List<Artist>> createArtistMap(String fileName, ArrayList<Artist> obj) {
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
	
	
	
	private void emitSong(TreeMap<String, List<Song>> map) {
		Iterator<Entry<String, List<Song>>> iter = map.entrySet().iterator();
		Gson gson = new Gson();
		while (iter.hasNext()) {
			Map.Entry<String, List<Song>> pair = (Map.Entry<String, List<Song>>) iter.next();
			String value = pair.getKey();
			for (Song song : pair.getValue()) {
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
	}
	
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
