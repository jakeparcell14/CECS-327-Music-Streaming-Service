package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.ArrayList;

import com.google.gson.Gson;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Server {
	/**
     * The appplication main method, which just listens on a port and
     * spawns handler threads.
     */
	private static DatagramSocket socket = null;
	private static Gson gson = new Gson();
    public static void main(String[] args) throws Exception {		
		try {
			//create a socket listening on port 1234
			socket = new DatagramSocket(1234);
			
			while(true) {
				System.out.println("Waiting for a request...");
				Request req = getRequest();
				
				System.out.println("Received a request!\nCreating new thread!");
				//create a new thread to handle a client's requests
				new Handler(req).start();
			}
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			System.out.println("Error creating socket: " + e1.getMessage());
		} finally  {
			if (socket != null) 
				socket.close();
		}
    }
    
    /**
     * Handler class that will handle a single request from a client.
     * @author Matthew
     *
     */
    public static class Handler extends Thread {
    	private Request req;
    	private DatagramSocket reqSocket;
    	private Gson gson;
    	
    	/**
    	 * Constructor for Handler class.
    	 * @param req - request from a client.
    	 */
    	public Handler(Request req) {
    		this.req = req;
    		gson = new Gson();
    		//create a new socket to handle requests from the client
    		try {
				reqSocket = new DatagramSocket();
				System.out.println(reqSocket.getLocalPort());
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	public void run() {
    		System.out.println("New handler running and handling request");
    		Message msg = gson.fromJson(new String(req.data).trim(), Message.class);
    		switch(msg.getProtocolID()) {
    			case 0:
    				break;
    			case 1:
					try {
						RequestReplyProtocol(msg, req);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    			
    		}
  
			
    		//socket should close at the end/destruction of this thread
    	}
    	
    }
    
    private static void RequestReplyProtocol(Message msg, Request req) throws SocketException, UnknownHostException, IOException {
		byte[] result = ChooseAndExecuteOperation(msg);
		SendReply(result, InetAddress.getLocalHost(), req.port);
	}
    
    
    private static byte[] ChooseAndExecuteOperation(Message msg) throws IOException {
    	switch(msg.getOperationID()) {
			case LOGIN:
				return verifyAccount(msg);
			case SEARCHMYSONGS:
				//searchMySongs function goes here
				return null;
			case SEARCHMYPLAYLISTS:
				//searchMyPlaylists function goes here
				return null;
			case SEARCHCURRENTPLAYLIST:
				//searchCurrentPlaylist function goes here
				return null;
			case ADDPLAYLIST:
				return addPlaylist(msg);
			case DELETEPLAYLIST:
				return deletePlaylist(msg);
			case ADDSONGTOPLAYLIST:
				return addSong(msg);
			case DELETESONGFROMPLAYLIST:
				return deleteSong(msg);
			default:
				return null;
			
    	}
    }
    
    
    public static String searchMySongs(String q, User user) {
		String query=q;
		Playlist savedSongsPlaylist=user.getSavedSongs();
		ArrayList<Song> savedSongs = savedSongsPlaylist.getSongs();
		String msgList="";
		for(int i=0; i<savedSongs.size();i++) {
			//checks if query matches the title of the current song
			if(savedSongs.get(i).getTitle()!=null && savedSongs.get(i).getTitle().toLowerCase().contains(query.toLowerCase())) {
				String temp=msgList;
				msgList=temp+"."+savedSongs.get(i).getTitle();
			}
			//checks if query matches the album of the current song
			else if(savedSongs.get(i).getAlbum()!=null && savedSongs.get(i).getAlbum().toLowerCase().contains(query.toLowerCase())) {
				String temp=msgList;
				msgList=temp+"."+savedSongs.get(i).getTitle();
			}
			//checks if query matches the artist of the current song
			else if(savedSongs.get(i).getArtist()!=null && savedSongs.get(i).getArtist().toLowerCase().contains(query.toLowerCase())) {
				String temp=msgList;
				msgList=temp+"."+savedSongs.get(i).getTitle();
			}
		}
		return msgList;
	}
	/**
	 * searches and displays user myplaylists on userlibrarylist
	 * @param query-user inputted query
	 */
	public static String searchMyPlaylists(String query,User user) {
		String msgList="";
		ArrayList<Playlist> playlists=user.getPlaylists();
		for (int i = 0; i<playlists.size(); i++) {
			if(playlists.get(i).getPlaylistName()!=null) {
				//check if query matches the playlist title
				if(playlists.get(i).getPlaylistName().toLowerCase().contains(query.toLowerCase())) {
					String temp=msgList;
					msgList=temp+"."+playlists.get(i).getPlaylistName();
				}
			}
		}
		return msgList;
	}
	/**
	 * searches current playlists and displays on userlibrarylist
	 * @param query
	 */
	public static String searchCurrentPlaylist(String query,User user ,String currentPlaylist) {
		String msgList="";
		ArrayList<Playlist> playlists=user.getPlaylists();
		Playlist cp = new Playlist();
		if(currentPlaylist.equals("saved")) {
			cp=user.getSavedSongs();
		}
		else {
			for (int i = 0; i<playlists.size(); i++) {
				if(playlists.get(i).getPlaylistName()!=null) {
					//check if query matches the playlist title
					if(playlists.get(i).getPlaylistName().toLowerCase().equals(currentPlaylist.toLowerCase())) {
						cp=playlists.get(i);
					}
				}
			}
		}
		ArrayList<Song> songs = cp.getSongs();
		for(int i=0; i<songs.size();i++) {
			//checks if query matches the title of the current song
			if(songs.get(i).getTitle()!=null && songs.get(i).getTitle().toLowerCase().contains(query.toLowerCase())) {
				String temp=msgList;
				msgList=temp+"."+songs.get(i).getTitle();
			}
			//checks if query matches the album of the current song
			else if(songs.get(i).getAlbum()!=null && songs.get(i).getAlbum().toLowerCase().contains(query.toLowerCase())) {
				String temp=msgList;
				msgList=temp+"."+songs.get(i).getTitle();
			}
			//checks if query matches the artist of the current song
			else if(songs.get(i).getArtist()!=null && songs.get(i).getArtist().toLowerCase().contains(query.toLowerCase())) {
				String temp=msgList;
				msgList=temp+"."+songs.get(i).getTitle();
			}
		}
		return msgList;
	}
    
    public static byte[] verifyAccount(Message msg) {
    	Gson gson = new Gson();
    	
    	try 
		{
			if(UserRepository.IsUsernameAndPasswordCorrect(msg.getArgs()[0], msg.getArgs()[1]))
			{
				//send acknowledgement back to login client
				return gson.toJson("VERIFIED").getBytes();
			}
			else {
				//send acknowledgement back to login client
				return gson.toJson("INCORRECT").getBytes();
			}
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
    
    /**
	 * Sends a reply to the client.
	 * 
	 * @param reply Object returning as a reply flattened to a byte array.
	 * @param addr Address we are sending to. (In this case, localhost)
	 * @param port Client's port number we are sending this to.
	 * @param socket Server socket we are sending through.
	 * @throws IOException
	 * @throws SocketException
	 */
	public static void SendReply(byte[] reply, InetAddress addr, int port) throws IOException, SocketException {
		/* create reply packet.
		 * will be sent to the port that the client is listening to a response on.
		 */
		DatagramPacket rply = new DatagramPacket(reply, reply.length, addr, port);
		System.out.printf("Sending Reply. Port %d, InetAddr: %s\n", rply.getPort(), rply.getAddress());		
		socket.send(rply);
	}
	
	/**
	 * listens for a request from a client.
	 * @param socket This server's socket.
	 * @return Returns a request object when it receives a request on port 1234.
	 * @throws IOException
	 * @throws SocketException
	 */
	public static Request getRequest() throws IOException, SocketException {
		byte[] buff = new byte[1000];
		System.out.println("Getting Request");
		//listen to request on port 1234 (will block until it gets a request.)
		DatagramPacket request = new DatagramPacket(buff, buff.length, InetAddress.getLocalHost(), 1234);
		socket.receive(request);
		System.out.println("Client port: " + request.getPort());
		Request req = new Request();
		req.data = request.getData();
		req.port = request.getPort();
		//socket.close();
		return req;
	}
	
	/**
	 * Gets all of a user's playlist.
	 * @param msg Message for this request.
	 * @return Returns byte array containing JSON string of playlist array.
	 * @throws IOException
	 */
	private static byte[] getPlaylists(Message msg) throws IOException {
		String username = msg.getArgs()[0];
		User user = UserRepository.getUser(username);
		return gson.toJson((Playlist[]) user.getPlaylists().toArray(), Playlist[].class).getBytes();
	}
	
	/**
	 * Adds a playlist to a user account.
	 * @param msg Message for this request.
	 * @return  Returns byte array containing JSON string of playlist array.
	 * @throws IOException
	 */
	private static byte[] addPlaylist(Message msg) throws IOException {
		User user = UserRepository.getUser(msg.getArgs()[0]);
		user.addPlaylist(gson.fromJson(msg.getArgs()[1], Playlist.class));
		UserRepository.UpdateUser(user);
		ArrayList<Playlist> p = user.getPlaylists();
		System.out.println(gson.toJson((Playlist[]) p.toArray(new Playlist[p.size()])).getBytes().length);
		return gson.toJson((Playlist[]) p.toArray(new Playlist[p.size()])).getBytes();
	}
	
	/**
	 * Deletes a given playlist from a user account.
	 * 
	 * @param msg Message for this request.
	 * @return  Returns byte array containing JSON string of playlist array.
	 * @throws IOException
	 */
	private static byte[] deletePlaylist(Message msg) throws IOException {
		User user = UserRepository.getUser(msg.getArgs()[0]);
		user.removePlaylist(gson.fromJson(msg.getArgs()[1], Playlist.class).getPlaylistName());
		UserRepository.UpdateUser(user);
		ArrayList<Playlist> p = user.getPlaylists();
		return gson.toJson((Playlist[]) p.toArray(new Playlist[p.size()])).getBytes();
	}
	
	/**
	 * Adds a song to a given playlist.
	 * 
	 * @param msg Message for this request.
	 * @return  Returns byte array containing JSON string of playlist array.
	 * @throws IOException
	 */
	private static byte[] addSong(Message msg) throws IOException {
		User user = UserRepository.getUser(msg.getArgs()[0]);
		Song song = gson.fromJson(msg.getArgs()[1], Song.class);
		Playlist playlist = gson.fromJson(msg.getArgs()[3], Playlist.class);
		playlist.addSong(song);		
		user.removePlaylist(playlist.getPlaylistName());
		user.addPlaylist(playlist);
		UserRepository.UpdateUser(user);
		return gson.toJson((Playlist[]) user.getPlaylists().toArray(), Playlist[].class).getBytes();
	}
	
	/**
	 * Deletes the given song from a given playlist.
	 * 
	 * @param msg Message for this request.
	 * @return  Returns byte array containing JSON string of playlist array.
	 * @throws IOException
	 */
	private static byte[] deleteSong(Message msg) throws IOException {
		User user = UserRepository.getUser(msg.getArgs()[0]);
		Song song = gson.fromJson(msg.getArgs()[1], Song.class);
		Playlist playlist = gson.fromJson(msg.getArgs()[3], Playlist.class);
		playlist.removeSong(song);	
		user.removePlaylist(playlist.getPlaylistName());
		user.addPlaylist(playlist);
		UserRepository.UpdateUser(user);
		return gson.toJson((Playlist[]) user.getPlaylists().toArray(), Playlist[].class).getBytes();
	}
}