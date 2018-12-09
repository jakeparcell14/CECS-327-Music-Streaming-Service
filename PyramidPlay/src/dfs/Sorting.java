package dfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;

import application.Song;
import p2p.PeerToPeer;

public class Sorting {
	Map<Integer, List<String>> peer1Map = new TreeMap<Integer, List<String>>();
	Map<Integer, List<String>> peer2Map = new TreeMap<Integer, List<String>>();
	Map<Integer, List<String>> peer3Map = new TreeMap<Integer, List<String>>();
	
	private final int PEER_1_GUID = 10000;
	private final int PEER_2_GUID = 20000;
	private final int PEER_3_GUID = 30000;
	
	public File mapReduce (File file) {
		return null;
	}
	
	private File map(File file) {
		return null;
	}
	

	private static class Handler extends Thread
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

	
	private void emitMap(Integer key, String value, Counter counter) {
		if (value.compareTo("A") >= 0 && value.compareTo("L") <= 0) {
			addToMap(peer1Map, key, value, counter);
		} else if (value.compareTo("M") >= 0 && value.compareTo("X") <=0) {
			addToMap(peer2Map, key, value, counter);
		} else {
			addToMap(peer3Map, key, value, counter);
		}
	}
	
	private void addToMap(Map<Integer, List<String>> map, Integer key, String value, Counter counter) {
		if (map.containsKey(key)) {
			map.get(key).add(value);
		} else {
			map.put(key, new ArrayList<String>(Arrays.asList(value)));
		}
		
		counter.decrement();
	}
	
/*	private void mapContext(Integer page, MapInterface mapper, Counter counter) {
		PeerToPeer p2p = PeerToPeer.getInstance();
		Chunk ch = (Chunk) p2p.Get(page);
		
		byte[] chunkByte = ch.getData();
		
		String[] song = new String(chunkByte).trim().split("\\r?\\n");
		
		int n = 0;
		
		for(int i = 0; i < song.length; i++) {
		
			String songLine = song[i];
			
			String[] songInfo = songLine.split(";");
			
			String key = songInfo[0];
			
			String value = songInfo[1] + ";" + songInfo[2] + ";" + songInfo[3];
		
			n++;
		}
		
		
		counter.increment(page, n);
		
		
	}*/
	
	
}
