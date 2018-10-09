package application;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import com.google.gson.*;

public class UserRepository {

	/**
	 * Adds user to the user database.
	 * @param user User to be added to database.
	 * @throws IOException
	 */
	public static void AddUser(User user) throws IOException {
		FileWriter out = null;
		Gson gson = new Gson();
		out = new FileWriter("users.json", true);
		out.write("\n" + gson.toJson(user));
			
		if (out != null)
			out.close();
	
	}
	
	/**
	 * Gets all users.
	 * 
	 * @return Returns all users from the json database.
	 * @throws IOException
	 */
	private static ArrayList<User> getUsers() throws IOException {
		Gson gson = new Gson();
		ArrayList<User> users = new ArrayList<User>();
		File file = new File("users.json");
		Scanner scanner = new Scanner(file);
			
		while(scanner.hasNextLine()) {
			String jsonLine = scanner.nextLine();
			//System.out.println(jsonLine);
			User user = gson.fromJson(jsonLine, User.class);
			if (user != null)
				users.add(user);
		}
		scanner.close();

		return users;
	}
	
	/**
	 * Gets all songs in our json database.
	 * 
	 * @return Returns an arraylist of all songs.
	 * @throws IOException
	 */
	private static ArrayList<Song> getSongs() throws IOException {
		Gson gson = new Gson();
		ArrayList<Song> songs = new ArrayList<Song>();
		File file = new File("songs.json");
		Scanner scanner = new Scanner(file);
			
		while(scanner.hasNextLine()) {
			Song song = gson.fromJson(scanner.nextLine(), Song.class);
			if (song!=null)
				songs.add(song);
		}
		scanner.close();

		return songs;
	}
	
	/**
	 * Checks if a username/password combination is correct.
	 * @param username Username of user.
	 * @param password Password of user.
	 * @return Returns true if username and password combination is correct, false if it is not.
	 * @throws IOException
	 */
	public static boolean IsUsernameAndPasswordCorrect(String username, String password) throws IOException {
		return getUsers().contains(new User(username, password));
	}
	
	/***
	 * Checks if a user exists.
	 * @param username Username to check for.
	 * @return Returns true if that username exists, false if it doesn't.
	 * @throws IOException
	 */
	public static boolean userExists(String username) throws IOException
	{
		return getUsers().contains(new User(null, null, username, null));
	}
	
	/**
	 * Gets a specific user.
	 * 
	 * @param username Username of user to fetch.
	 * @return Returns user with that username.Returns null if that username does not exist.
	 */
	public static User getUser(String username) throws IOException {
		ArrayList<User> users = getUsers();
		for (int i = 0; i<users.size(); i++) {
			if (users.get(i).getUsername().equals(username))
				return users.get(i);
		}
		return null;
	}
	
	/**
	 * Gets all songs.
	 * 
	 * @return Returns arraylist of all songs.
	 * @throws IOException
	 */
	public static ArrayList<Song> getAllSongs() throws IOException
	{
		return getSongs();
	}
	
	/**
	 * Updates a given user.
	 * 
	 * @param user Updated user object to replace in json database.
	 * @throws IOException
	 */
	public static void UpdateUser(User user) throws IOException {
		ArrayList<User> users = getUsers();
		User temp = getUser(user.getUsername());
		users.remove(temp);
		users.add(user);
		UpdateUsers(users);
	}
	
	/**
	 * Updates all users in the database.
	 * 
	 * @param users ARraylist of all users to update.
	 * @throws IOException
	 */
	private static void UpdateUsers(ArrayList<User> users) throws IOException {
		FileWriter out = null;
		Gson gson = new Gson();
		out = new FileWriter("users.json");
		for (int i = 0; i<users.size(); i++) {
			out.write(gson.toJson(users.get(i))+"\n");
		}
			
		if (out != null)
			out.close();
	}
}
