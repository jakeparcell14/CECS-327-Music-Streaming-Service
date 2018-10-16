package application;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.google.gson.Gson;
import com.sun.glass.ui.Window.Level;


public class Server {
	static Log server_log;
	/**
	 * Socket used to get incoming requests.
	 */
	private static DatagramSocket socket = null;

	/**
	 * GSON object to serialize and deserialize objects.
	 */
	private static Gson gson = new Gson();


	public static void main(String[] args) throws Exception {		
		try {			
			//create a socket listening on port 1234
			socket = new DatagramSocket(1234);

			while(true) {
				Request req = getRequest();
        
				//create a new thread to handle a client's requests
				new Handler(req).start();
			}
		} catch (SocketException e1) {
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
		 * Constructor for Handler class that creates a socket to 
		 * handle the request from the client
		 * @param req - request from a client.
		 */
		public Handler(Request req) {
			this.req = req;
			gson = new Gson();

			//create a new socket to handle requests from the client
			try {
				reqSocket = new DatagramSocket();
				//System.out.println(reqSocket.getLocalPort());
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void run() {
			//System.out.println("New handler running and handling request...");
			//System.out.println(new String(req.data).trim());
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

	/**
	 * Handles logic for request-reply protocol.
	 * 
	 * @param msg Message received by the server.
	 * @param req Request data received by server.
	 * @throws SocketException
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private static void RequestReplyProtocol(Message msg, Request req) throws SocketException, UnknownHostException, IOException {
		byte[] result = ChooseAndExecuteOperation(msg);

		// if the result is an empty array, the process does not send a reply because it is a duplicate
		if(result.length > 0)
		{
			//the reply being sent is not a duplicate
			SendReply(result, InetAddress.getLocalHost(), req.port);
		}
	}

	/***
	 * Chooses an operation based on the operation id in the message object sent to the server.
	 * 
	 * @param msg Message the server received.
	 * @return Returns result in the form of a flattened byte array ready to be sent.
	 * @throws IOException
	 */  
	private static byte[] ChooseAndExecuteOperation(Message msg) throws IOException {
		switch(msg.getOperationID()) {
		case LOGIN:
			//System.out.println("Login!");
			return verifyAccount(msg);
		case REGISTER:
			return registerAccount(msg);
		case SEARCHALLSONGS:
			return searchAllSongs(msg);
		case SEARCHMYSONGS:
			return searchMySongs(msg);
		case SEARCHMYPLAYLISTS:
			return searchMyPlaylists(msg);
		case SEARCHCURRENTPLAYLIST:
			return searchCurrentPlaylist(msg);
		case ADDPLAYLIST:
			return addPlaylist(msg);
		case DELETEPLAYLIST:
			return deletePlaylist(msg);
		case ADDSONGTOPLAYLIST:
			return addSong(msg);
		case DELETESONGFROMPLAYLIST:
			return deleteSong(msg);
		case GETNUMBEROFFRAGMENTS:
			return getNumberOfFragments(msg);
		case GETSONGBYTES:
			return getSongBytes(msg);
		default:
			//System.out.println("NULL>");
			return null;
		}
	}

	/**
	 * Executes a search of all songs.
	 * @param m Message sent to the server.
	 * @return Returns result as flattened byte array ready to be sent.
	 */
	public static byte[] searchAllSongs(Message m) {
		String userName=m.getArgs()[0];
		ArrayList<Song> allSongs;
		Playlist validSongs = new Playlist("valid");
		try {
			log(userName,"RECEIVED MESSAGE: "+m.toString()+"\n\n");
			allSongs = UserRepository.getAllSongs();
			String query=m.getArgs()[1];


			if (!query.isEmpty()) 
			{
				for (int i = 0; i < allSongs.size(); i++) {
					if (validSongs.getSongs().size() <= 20) {
						//checks if query matches the title of the current song
						if (allSongs.get(i).getTitle() != null
								&& allSongs.get(i).getTitle().length() >= query.length()) {
							// the song title is at least as long as the query
							if (allSongs.get(i).getTitle().substring(0, query.length()).toLowerCase()
									.equals(query.toLowerCase())) {
								//the query matches the song title
								validSongs.addSong(allSongs.get(i));
							}
						}

						//checks if query matches the album name of the current song
						if (!validSongs.contains(allSongs.get(i).getTitle())) {
							// the song has not been added to the list of valid songs yet
							if (allSongs.get(i).getAlbum() != null
									&& allSongs.get(i).getAlbum().length() >= query.length()) {
								// the album name is at least as long as the query
								if (allSongs.get(i).getAlbum().substring(0, query.length()).toLowerCase()
										.equals(query.toLowerCase())) {
									//the query matches the album name
									validSongs.addSong(allSongs.get(i));
								}
							}
						}

						//checks if query matches the artist name of the current song
						if (!validSongs.contains(allSongs.get(i).getTitle())) {
							// the song has not been added to the list of valid songs yet
							if (allSongs.get(i).getArtist() != null
									&& allSongs.get(i).getArtist().length() >= query.length()) {
								// the artist name is at least as long as the query
								if (allSongs.get(i).getArtist().substring(0, query.length()).toLowerCase()
										.equals(query.toLowerCase())) {
									//the query matches the artist name
									validSongs.addSong(allSongs.get(i));
								}
							}
						}
					}
				} 
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String log_message="";
		for(int i=0;i<validSongs.getLength();i++) {
			log_message=log_message+" "+validSongs.getSongs().get(i).toString();
		}
		log(userName,"SENT MESSAGE (ID = " + m.getRequestID() + ") "+log_message+"\n\n");
		return gson.toJson(validSongs.getSongs().toArray(new Song[validSongs.getSongs().size()])).getBytes();

	}

	/**
	 * Executes search in a user's my songs library.
	 * 
	 * @param m Message sent to the server.
	 * @return Returns result as flattened byte array ready to be sent.
	 */
	public static byte[] searchMySongs(Message m) {
		String userName=m.getArgs()[0];
		String query=m.getArgs()[1];
		ArrayList<Song> msgList=new ArrayList<Song>();
		User user;
		try {
			user = UserRepository.getUser(userName);
			Playlist savedSongsPlaylist=user.getSavedSongs();
			ArrayList<Song> savedSongs = savedSongsPlaylist.getSongs();
			
			//get all lines from the current session
			ArrayList<String> logText = readLog(user.getUsername() + ".log");
			ArrayList<String> currentSession = getCurrentSession(logText);

			//current message has been received already
			if(processAlreadyExecuted(currentSession, m))
			{
				//the current process is a duplicate and a previous copy has already executed
				byte[] emptyArray = {};
				return emptyArray;
			}
			else
			{
				log(userName,"RECEIVED MESSAGE: "+m.toString()+"\n\n");
			}
			
			if(query.equals(" ")) {
				for(int i=0; i<savedSongs.size();i++) {
					msgList.add(savedSongs.get(i));
				}
			}
			else {
				for(int i=0; i<savedSongs.size();i++) {
					//checks if query matches the title of the current song
					if(savedSongs.get(i).getTitle()!=null && savedSongs.get(i).getTitle().toLowerCase().contains(query.toLowerCase())) {
						msgList.add(savedSongs.get(i));
					}
					//checks if query matches the album of the current song
					else if(savedSongs.get(i).getAlbum()!=null && savedSongs.get(i).getAlbum().toLowerCase().contains(query.toLowerCase())) {
						msgList.add(savedSongs.get(i));
					}
					//checks if query matches the artist of the current song
					else if(savedSongs.get(i).getArtist()!=null && savedSongs.get(i).getArtist().toLowerCase().contains(query.toLowerCase())) {
						msgList.add(savedSongs.get(i));
					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String log_message="";
		for(int i=0;i<msgList.size();i++) {
			log_message=log_message+" "+msgList.get(i).toString();
		}
		log(userName,"SENT MESSAGE (ID = " + m.getRequestID() + ") "+log_message+"\n\n");	
		return gson.toJson(msgList.toArray(new Song[msgList.size()])).getBytes();
	}
	/**
	 * searches and displays user myplaylists on userlibrarylist
	 * @param query-user inputted query
	 */
	public static byte[] searchMyPlaylists(Message m) {
		String userName=m.getArgs()[0];
		String query=m.getArgs()[1];
		ArrayList<Playlist> msgList= new ArrayList<Playlist>();
		User user;
		try {
			user = UserRepository.getUser(userName);
			ArrayList<Playlist> playlists=user.getPlaylists();
			
			//get all lines from the current session
			ArrayList<String> logText = readLog(user.getUsername() + ".log");
			ArrayList<String> currentSession = getCurrentSession(logText);

			//current message has been received already
			if(processAlreadyExecuted(currentSession, m))
			{
				//the current process is a duplicate and a previous copy has already executed
				byte[] emptyArray = {};
				return emptyArray;
			}
			else
			{
				log(userName,"RECEIVED MESSAGE: "+m.toString()+"\n\n");
			}
			
			if(query.equals(" ")) {
				for(int i=0; i<playlists.size();i++) {
					msgList.add(playlists.get(i));
				}
			}
			else {
				for (int i = 0; i<playlists.size(); i++) {
					if(playlists.get(i).getPlaylistName()!=null) {
						//check if query matches the playlist title
						if(playlists.get(i).getPlaylistName().toLowerCase().contains(query.toLowerCase())) {
							msgList.add(playlists.get(i));
						}
					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		String log_message="";
		for(int i=0;i<msgList.size();i++) {
			log_message=log_message+" "+msgList.get(i).getPlaylistName();
		}
		log(userName,"SENT MESSAGE (ID = " + m.getRequestID() + ") "+log_message+"\n\n");
		return gson.toJson(msgList.toArray(new Playlist[msgList.size()])).getBytes();
	}
	/**
	 * searches current playlists and displays on userlibrarylist
	 * @param query
	 */
	public static byte[] searchCurrentPlaylist(Message m) {
		String userName=m.getArgs()[0];
		String query=m.getArgs()[1];
		String currentPlaylist=m.getArgs()[2];
		ArrayList<Song> msgList=new ArrayList<Song>();
		User user;
		try {
			user = UserRepository.getUser(userName);
			ArrayList<Playlist> playlists=user.getPlaylists();
			Playlist cp = new Playlist();
			
			//get all lines from the current session
			ArrayList<String> logText = readLog(user.getUsername() + ".log");
			ArrayList<String> currentSession = getCurrentSession(logText);

			//current message has been received already
			if(processAlreadyExecuted(currentSession, m))
			{
				//the current process is a duplicate and a previous copy has already executed
				byte[] emptyArray = {};
				return emptyArray;
			}
			else
			{
				log(userName,"RECEIVED MESSAGE: "+m.toString()+"\n\n");
			}
			
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
					msgList.add(songs.get(i));			
				}
				//checks if query matches the album of the current song
				else if(songs.get(i).getAlbum()!=null && songs.get(i).getAlbum().toLowerCase().contains(query.toLowerCase())) {
					msgList.add(songs.get(i));		
				}
				//checks if query matches the artist of the current song
				else if(songs.get(i).getArtist()!=null && songs.get(i).getArtist().toLowerCase().contains(query.toLowerCase())) {
					msgList.add(songs.get(i));		
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String log_message="";
		for(int i=0;i<msgList.size();i++) {
			log_message=log_message+" "+msgList.get(i).toString();
		}
		log(userName,"SENT MESSAGE (ID = " + m.getRequestID() + ") "+log_message+"\n\n");
		return gson.toJson(msgList.toArray(new Song[msgList.size()])).getBytes();
	}

	/**
	 * Function to verify a username and password combination from a message
	 * @param msg - message sent from client containing username and password
	 * 				args array should be [username, password]
	 */
	public static byte[] verifyAccount(Message msg) {
		gson = new Gson();
		String userName=msg.getArgs()[0];
		try 
		{
			if(UserRepository.IsUsernameAndPasswordCorrect(msg.getArgs()[0], msg.getArgs()[1]))
			{
				log(userName,"RECEIVED MESSAGE: "+msg.toString()+"\n\n");
				//send acknowledgement back to login client
				log(userName,"SENT MESSAGE: VERIFIED\n\n");
				return gson.toJson(UserRepository.getUser(msg.getArgs()[0])).getBytes();
			}
			else {
				//send acknowledgement back to login client
				return gson.toJson(new User()).getBytes();
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
	 * Function to register a new account to the json file
	 * @param msg - message sent from client containing user information
	 */
	public static byte[] registerAccount(Message msg) {
		Gson gson = new Gson();
		String userName=msg.getArgs()[2];
		//msg args structure = [firstName, lastName, userName, password]
		try 
		{
			if(!UserRepository.userExists(msg.getArgs()[2]))
			{
				//username is available and ready to be added to the repository
				User newUser = new User(msg.getArgs()[0], msg.getArgs()[1], msg.getArgs()[2], msg.getArgs()[3]);

				//get all lines from the current session
				ArrayList<String> logText = readLog(newUser.getUsername() + ".log");
				ArrayList<String> currentSession = getCurrentSession(logText);

				//current message has been received already
				if(processAlreadyExecuted(currentSession, msg))
				{
					//the current process is a duplicate and a previous copy has already executed
					byte[] emptyArray = {};
					return emptyArray;
				}
				else
				{
					log(userName,"RECEIVED MESSAGE: "+msg.toString()+"\n\n");
				}
				
				//add user to the user repository
				UserRepository.AddUser(newUser);

				//return new user
				log(userName,"SENT MESSAGE (ID = " + msg.getRequestID() + ") "+newUser.getUsername() +"\n\n");
				return gson.toJson(newUser).getBytes();
			}
			else
			{
				//return a user with empty attributes to let client know
				//that username is already taken
				return gson.toJson(new User()).getBytes();
			}
		}
		catch(IOException e)
		{
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
		//System.out.printf("Sending Reply. Port %d, InetAddr: %s\n", rply.getPort(), rply.getAddress());		
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
		byte[] buff = new byte[5000];
		//System.out.println("Getting Request");
		//listen to request on port 1234 (will block until it gets a request.)
		DatagramPacket request = new DatagramPacket(buff, buff.length, InetAddress.getLocalHost(), 1234);
		socket.receive(request);
		//System.out.println("Client port: " + request.getPort());
		Request req = new Request();
		req.data = request.getData();
		req.port = request.getPort();
		//socket.close();
		return req;
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
		return gson.toJson((Playlist[]) user.getPlaylists().toArray(new Playlist[user.getPlaylists().size()])).getBytes();
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
		//System.out.println(gson.toJson((Playlist[]) user.getPlaylists().toArray(new Playlist[user.getPlaylists().size()])));
		return gson.toJson((Playlist[]) user.getPlaylists().toArray(new Playlist[user.getPlaylists().size()])).getBytes();
	}

	/**
	 * Adds a song to a given playlist.
	 * 
	 * @param msg Message for this request.
	 * @return  Returns byte array containing JSON string of playlist array if not a duplicate. Duplicates return empty array
	 * @throws IOException
	 */
	private static byte[] addSong(Message msg) throws IOException {
		User user = UserRepository.getUser(msg.getArgs()[0]);
		Song song = gson.fromJson(msg.getArgs()[1], Song.class);
		Playlist playlist = gson.fromJson(msg.getArgs()[2], Playlist.class);
		ArrayList<Playlist> p = new ArrayList<Playlist>();

		//get all lines from the current session
		ArrayList<String> logText = readLog(user.getUsername() + ".log");
		ArrayList<String> currentSession = getCurrentSession(logText);

		//current message has been received already
		if(processAlreadyExecuted(currentSession, msg))
		{
			//the current process is a duplicate and a previous copy has already executed
			byte[] emptyArray = {};
			return emptyArray;
		}
		else
		{
			log(user.getUsername(),"RECEIVED MESSAGE: "+msg.toString()+"\n\n");
		}

		if(playlist.getPlaylistName().equals("saved"))
		{
			Playlist saved = user.getSavedSongs();
			saved.addSong(playlist.getSongs().get(0));
			user.setSavedSongs(saved);
			UserRepository.UpdateUser(user);
			p.add(user.getSavedSongs());
			log(user.getUsername(),"SENT MESSAGE (ID = " + msg.getRequestID() + ") "+saved.getSongs().toString()+"\n\n");
			return gson.toJson((Playlist[]) p.toArray(new Playlist[p.size()]), Playlist[].class).getBytes();
		}
		else
		{
			playlist.addSong(song);		
			user.removePlaylist(playlist.getPlaylistName());
			user.addPlaylist(playlist);
			UserRepository.UpdateUser(user);
			p.addAll(user.getPlaylists());

			log(user.getUsername(),"SENT MESSAGE (ID = " + msg.getRequestID() + ") "+playlist.getSongs().toString()+"\n\n");
			return gson.toJson((Playlist[]) p.toArray(new Playlist[p.size()]), Playlist[].class).getBytes();
		}
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
		Playlist playlist = gson.fromJson(msg.getArgs()[2], Playlist.class);

		if(playlist.getPlaylistName().equals("saved"))
		{
			//remove song from playlist
			playlist.removeSong(song);
			user.setSavedSongs(playlist);;
			UserRepository.UpdateUser(user);

			ArrayList<Playlist> p = new ArrayList<Playlist>();
			p.add(playlist);

			return gson.toJson((Playlist[]) p.toArray(new Playlist[p.size()])).getBytes();
		}
		else
		{
			//arraylist of playlists from the user
			ArrayList<Playlist> p = user.getPlaylists();

			//search for playlist by comparing names
			int playlistIndex = 0;
			while(!playlist.getPlaylistName().equals( p.get(playlistIndex).getPlaylistName() ) 
					&& playlistIndex < p.size()) 
			{
				playlistIndex++;
			}

			//remove song from playlist
			playlist.removeSong(song);
			//System.out.println("Removing playlist at index " + playlistIndex);

			//remove old playlist
			p.remove(playlistIndex);

			//replace old playlist with the updated one
			if(playlistIndex >= p.size())
			{
				p.add(playlist);
			}
			else
			{
				p.set(playlistIndex, playlist);
			}

			user.setPlaylists(p);
			UserRepository.UpdateUser(user);

			return gson.toJson((Playlist[]) p.toArray(new Playlist[p.size()])).getBytes();
		}
	}

	/**
	 * Gets the number of fragments needed to download this entire song.
	 * 
	 * @param msg Message sent by client.
	 * @return Returns byte array to send back to client.
	 */
	public static byte[] getNumberOfFragments(Message msg) {	
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		Song song = gson.fromJson(msg.getArgs()[0], Song.class);
		int size = gson.fromJson(msg.getArgs()[1], int.class);
		buffer.putLong(new File(song.getFileSource()).length() / size);
		return buffer.array();
	}

	/**
	 * Gets a byte fragment of the song at a certain offset.
	 * 
	 * @param msg Message sent by client.
	 * @return Returns byte array containing this fragment to send back to client.
	 * @throws IOException
	 */
	public static byte[] getSongBytes(Message msg) throws IOException {
		Song song = gson.fromJson(msg.getArgs()[0], Song.class);
		int offset = gson.fromJson(msg.getArgs()[1], int.class);
		int bytes = gson.fromJson(msg.getArgs()[2], int.class);
		byte[] b = new byte[bytes];
		File f = new File (song.getFileSource());
		FileInputStream fs = new FileInputStream(f);
		fs.skip(offset);
		fs.read(b, 0, bytes);
		fs.close();
		return b;
	}

	/**
	 * adds to log for each user and creates log if it doesn't exist
	 * @param fileName the name  of the file for the log
	 * @param l the string to add to the log
	 */
	public static void log(String fileName,String l) {
		Logger logger = Logger.getLogger("MyLog");  
		FileHandler fh;  
		try {  
			// This block configure the logger with handler and formatter  
			fh = new FileHandler(fileName+".log",100000,1,true);  
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();  
			fh.setFormatter(formatter);  

			// the following statement is used to log any messages  
			logger.info(l);
			fh.close();

		} catch (SecurityException e) {  
			e.printStackTrace();  
		} catch (IOException e) {  
			e.printStackTrace();  
		}  
	}

	/**
	 * reads from log and returns arraylist with contents of log
	 * @param fileName the filename of the log to read
	 * @return arraylist with the contents of the log
	 */
	public static ArrayList<String> readLog(String fileName)
	{
		try 
		{
			BufferedReader in = new BufferedReader(new FileReader(fileName));

			String line = "";
			ArrayList<String> logText = new ArrayList<String>();

			while(true)
			{
				line = in.readLine();

				if(line == null)
				{
					// file has been read
					break;
				}
				else
				{
					logText.add(line);
				}
			}

			in.close();
			return logText;

		} catch (FileNotFoundException e) 
		{
			return null;
		} catch (IOException e) 
		{
			e.printStackTrace();
		}

		// an exception was thrown
		return null;
	}

	/**
	 * Searches for the most recent successful login for the current user to narrow down the search for duplicate processes
	 * @param logText	a list of every line of the log
	 * @return			list of all lines after and including the most recent successful sign in request
	 */
	public static ArrayList<String> getCurrentSession(ArrayList<String> logText)
	{
		int currentSessionStart = 0;
		for(int i = 1; i < logText.size(); i += 2)
		{
			if(logText.get(i).contains("INFO: SENT MESSAGE: VERIFIED"))			
			{
				currentSessionStart = i - 5;
			}
		}
		
		ArrayList<String> currentSession = new ArrayList<String>(logText.subList(currentSessionStart, logText.size()));
		
		return currentSession;
	}
	
	/**
	 * Checks if the current process has already been received by the server in another thread
	 * @param currentSession	all lines of the log since the most recent sign in
	 * @param msg				the message being checked
	 * @return					true if the message has already been received by the server
	 */
	public static boolean processAlreadyExecuted(ArrayList<String> currentSession, Message msg)
	{
		for(int i = 1; i < currentSession.size(); i += 2)
		{
			if(currentSession.get(i).contains("RECEIVED MESSAGE: "+msg.toString()))
			{
				//the log contains proof that the specific process has already executed
				return true;
			}
		}

		return false;
	}
}