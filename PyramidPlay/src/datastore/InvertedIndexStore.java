package datastore;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.Gson;

import application.Song;
import dfs.Metadata;
import p2p.PeerToPeer;

public class InvertedIndexStore {

	
	// Throwaway code used to put the inverted indexes to the peers.
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//writeSongs();
		//Metadata md = new Metadata();
		//Metadata md = Metadata.GetMetadata();
		//Song song = md.getSong("Never");
		//System.out.println(song);
		writeSongs();
		Metadata md = Metadata.GetMetadata();
		writeArtists(md);
		md = Metadata.GetMetadata();
		writeAlbums(md);
		
		//Song song = md.getSong("Nasty");
		//System.out.println(song);
		/*
		System.out.println();
		ArrayList<Song> songs = md.getAlbum("Only");
		for (int i = 0; i < songs.size(); i++) {
			System.out.println(songs.get(i));
		}
		//writeAlbums(md);
		//writeArtists(md);
		System.out.println();

		ArrayList<Song> artistsongs = md.getArtist("Rick");
		for (int i = 0; i < artistsongs.size(); i++) {
			System.out.println(artistsongs.get(i));
		}
		*/
		PeerToPeer.getInstance().close();
	}
	
	public static void writeSongs() {
		java.io.File file = new java.io.File("songs_inverted_index.json");
		
		Scanner scanner = null;		
		try {
			scanner = new Scanner(file);
			String chunk1 = "";
			String chunk2 = "";
			String chunk3 = "";
			int i = 1;
			while(scanner.hasNextLine()) {
				if (i < 6) {
					chunk1 += scanner.nextLine() + "\n";
				} else if (i < 11) {
					chunk2 += scanner.nextLine() + "\n";
				} else
					chunk3 += scanner.nextLine() + "\n";
				i++;
			}
			
			Metadata md = new Metadata();
			md.append("songs_inverted_index.json", chunk1.getBytes());
			md.append("songs_inverted_index.json", chunk2.getBytes());
			md.append("songs_inverted_index.json", chunk3.getBytes());
			md.writeMetadata();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		
		
		scanner.close();
	}
	
	public static void writeAlbums(Metadata md) {
		java.io.File file = new java.io.File("albums_inverted_index.json");
		
		Scanner scanner = null;		
		try {
			scanner = new Scanner(file);
			String chunk1 = "";
			String chunk2 = "";
			String chunk3 = "";
			int i = 1;
			while(scanner.hasNextLine()) {
				if (i < 5) {
					chunk1 += scanner.nextLine() + "\n";
				} else if (i < 9) {
					chunk2 += scanner.nextLine() + "\n";
				} else
					chunk3 += scanner.nextLine() + "\n";
				i++;
			}
			//System.out.println("Chunk 1:\n" + chunk1);
			//System.out.println("Chunk 2:\n" + chunk2);
			//System.out.println("Chunk 3:\n" + chunk3);

			md.append("albums_inverted_index.json", chunk1.getBytes());
			md.append("albums_inverted_index.json", chunk2.getBytes());
			md.append("albums_inverted_index.json", chunk3.getBytes());
			md.writeMetadata();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		
		
		scanner.close();
	}

	public static void writeArtists (Metadata md) {
		java.io.File file = new java.io.File("artists_inverted_index.json");
		
		Scanner scanner = null;		
		try {
			scanner = new Scanner(file);
			String chunk1 = "";
			String chunk2 = "";
			String chunk3 = "";
			int i = 1;
			while(scanner.hasNextLine()) {
				if (i < 4) {
					chunk1 += scanner.nextLine() + "\n";
				} else if (i < 7) {
					chunk2 += scanner.nextLine() + "\n";
				} else
					chunk3 += scanner.nextLine() + "\n";
				i++;
			}
			//System.out.println("Chunk 1:\n" + chunk1);
			//System.out.println("Chunk 2:\n" + chunk2);
			//System.out.println("Chunk 3:\n" + chunk3);

			md.append("artists_inverted_index.json", chunk1.getBytes());
			md.append("artists_inverted_index.json", chunk2.getBytes());
			md.append("artists_inverted_index.json", chunk3.getBytes());
			md.writeMetadata();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		
		
		scanner.close();
	}
}
