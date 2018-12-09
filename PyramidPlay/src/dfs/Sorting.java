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
	Map<Integer, List<String>> peer1Map = new TreeMap<Integer, List<String>>();
	Map<Integer, List<String>> peer2Map = new TreeMap<Integer, List<String>>();
	Map<Integer, List<String>> peer3Map = new TreeMap<Integer, List<String>>();
	
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
		private Chunk chunk;
		private File f;
		private Gson gson;

		public Handler(File givenFile, Chunk c)
		{
			this.chunk = c;
			this.f = givenFile;
			gson = new Gson();
		}

		public void run()
		{
			//get the songs from a specific chunk
			Song[] temp=gson.fromJson(new String(chunk.getData()).trim(), Song[].class);

			ArrayList<Song> updatedSongs = new ArrayList<Song>();

			for(int i = 0; i < temp.length; i++)
			{
				if(!updatedSongs.contains(temp[i]))
				{
					//song being tested is not a duplicate
					updatedSongs.add(temp[i]);
				}
			}

			//sort songs from chunk
			Collections.sort(updatedSongs);

			byte[] newChunk =  gson.toJson((Song[]) updatedSongs.toArray(new Song[updatedSongs.size()]), Song[].class).getBytes();

			try 
			{
				//add reduced chunk to file
				f.append(newChunk);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private File reduce(File file) {
		//get chunks from file, get file data, add to treemap to remove duplicates, sort map, overwrite onto peer
		File updatedFile = new File(file.getFileName());
		Gson gson = new Gson();
		
		ArrayList<Handler> threads = new ArrayList<Handler>();

		//reduce each chunk in its own thread
		for(int i = 0; i < file.getNumberOfChunks(); i++)
		{
			Handler h = new Handler(updatedFile, file.getChunk(i));

			threads.add(h);
			
			h.start();
		}
		
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
