package application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import com.google.gson.Gson;


/**
 * Class for downloading songs from the server.
 * 
 * @author Wesley Slates
 *
 */
public class SongDownloader {
	
	/**
	 * Local GSON object for writing Gson.
	 */
	private static Gson gson = new Gson();
	
	/**
	 * Size of packets in bytes that we will be requesting from the server.
	 */
	private static final long PACKETSIZE = 4096;
	private static final String CACHEDSONGNAME = "cached_song.mp3";
	
	/**
	 * Downloads request song from server and caches it on client machine.
	 * 
	 * @param socket Socket to use to communicate with server.
	 * @param song Song you are requesting from the server.
	 * @param requestID Request ID for this request.
	 * @return Returns cached song file.
	 * @throws SocketTimeoutException
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public static File DownloadSong(DatagramSocket socket, Song song, int requestID) throws SocketTimeoutException, UnknownHostException, IOException {
		long fragments = getNumberOfFragments(socket, song, requestID);
		byte[] bytes = getBytes(socket, song, fragments, requestID);
		return getFile(bytes);
	}
	
	/**
	 * Gets the number of fragments we will be requesting from the server.
	 * 
	 * @param socket Socket to use to communicate with server.
	 * @param song Song you are requesting from the server.
	 * @param requestID Request ID for this request.
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws SocketTimeoutException
	 */
	private static long getNumberOfFragments(DatagramSocket socket, Song song, int requestID) throws UnknownHostException, IOException, SocketTimeoutException {
		//create arguments for remote method
		String[] arg = {gson.toJson(song), gson.toJson(PACKETSIZE)};
		
		Message msg = new Message(0, requestID, OpID.GETNUMBEROFFRAGMENTS, arg, InetAddress.getLocalHost(), 1);
		
		//flatten to byte array for send
		byte[] msgBytes = gson.toJson(msg).getBytes();
		
		//Send request to server
		DatagramPacket request = new DatagramPacket(msgBytes, msgBytes.length, InetAddress.getLocalHost(), 1234);				
		socket.send(request);
			
		//response will be a long, so it will be 8 bytes
		byte[] resp = new byte[8];
		
		//receive response
		DatagramPacket response = new DatagramPacket(resp, resp.length);
		socket.receive(response);
		
		//allocate response into a buffer
		ByteBuffer buff = ByteBuffer.allocate(Long.BYTES);				
		buff.put(resp);
		buff.flip();
		
		return buff.getLong();
	}
	
	/**
	 * Gets a byte array representation of the song that is being requested from the server.
	 * 
	 * @param socket Socket to use to communicate with server.
	 * @param song Song you are requesting from the server.
	 * @param fragments Number of fragments to request from the server.
	 * @param requestID Request ID for this request.
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws SocketTimeoutException
	 */
	private static byte[] getBytes(DatagramSocket socket, Song song, long fragments, int requestID) throws UnknownHostException, IOException, SocketTimeoutException {
		byte[] songBytes = new byte[(int) (fragments * PACKETSIZE)];
		byte[] msgBytes;
		
		for (long i = 0; i < fragments; i++) {
			//create arguments for remote arguments
			String[] arguments = {gson.toJson(song), gson.toJson(PACKETSIZE * i), gson.toJson(PACKETSIZE)};
			
			//create message to send request
			Message msg = new Message(0, requestID, OpID.GETSONGBYTES, arguments, InetAddress.getLocalHost(), 1);
			
			//flatten message for sending
			msgBytes = gson.toJson(msg).getBytes();
			
			//create and send request
			DatagramPacket req = new DatagramPacket(msgBytes, msgBytes.length, InetAddress.getLocalHost(), 1234);
			socket.send(req);
			
			//create buffer to receive response
			byte[] buffer = new byte[(int) PACKETSIZE];
			
			//receive response
			DatagramPacket resp = new DatagramPacket(buffer, buffer.length);	
			socket.receive(resp);
			
			//Concatenate into song bytes array
			System.arraycopy(buffer, 0, songBytes, (int) (i * PACKETSIZE), (int) PACKETSIZE);
		}
		
		return songBytes;
	}
	

	/**
	 * Builds client-side cached song after it's downloaded from the server.
	 * 
	 * @param bytes Byte array representation of the song.
	 * @return Returns file object of cached song.
	 * @throws IOException
	 */
	private static File getFile(byte[] bytes) throws IOException {
		//create file
		File f = new File(CACHEDSONGNAME);
		f.createNewFile();
		
		//write file
		OutputStream stream = new FileOutputStream(f, false);
		stream.write(bytes);
		
		//close file
		stream.close();
		
		return f;
	}
	
	
}
