package datastore;

import application.UserRepository;
import application.Song;
import p2p.PeerToPeer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Program to initialize peers and put all .mp3 files to peers
 * @author Matthew
 *
 */
public class PeerInitializer {

	public static void main(String[] args) throws IOException {
		//create PeerToPeer instance
		PeerToPeer p2p = PeerToPeer.getInstance();
		
		//get array list of all .mp3 files
		ArrayList<Song> allSongs = UserRepository.getAllSongs();
		
		//array list to test getting songs from the peers
		ArrayList<byte[]> getter = new ArrayList<byte[]>();
		
		for (int i = 0; i < allSongs.size(); i++) {
			File f = new File (allSongs.get(i).getFileSource());
			byte[] b = Files.readAllBytes(f.toPath());
			
			//putting .mp3 files to peers, then getting them back from peers
			try {
				p2p.Put(b, allSongs.get(i).getGUID());
				
				System.out.println("Getting song with guid: " + allSongs.get(i).getGUID());
				getter.add( (byte[]) p2p.Get(allSongs.get(i).getGUID()) );
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("Closing p2p");
		p2p.close();
	}

}
