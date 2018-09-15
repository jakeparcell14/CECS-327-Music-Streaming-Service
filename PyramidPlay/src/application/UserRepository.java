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
			
		out.write(gson.toJson(user)+"\n");
			
		if (out != null)
			out.close();
	
	}
		
	private static ArrayList<User> getUsers() throws IOException {
		Gson gson = new Gson();
		ArrayList<User> users = new ArrayList<User>();
		File file = new File("users.json");
		Scanner scanner = new Scanner(file);
			
		while(scanner.hasNextLine()) {
			users.add(gson.fromJson(scanner.nextLine(), User.class));
		}
		scanner.close();

		return users;
	}
	
	/**
	 * Checks if a username/password combination is correct.
	 * @param username Username of user.
	 * @param password Password of user.
	 * @return Returns true if username and password combination is correct, false if it is not.
	 * @throws IOException
	 */
	public static boolean IsUsernameAndPasswordCorrect(String username, String password) throws IOException {
		return getUsers().contains(new User(null, null, username, password));
	}
	
	public static boolean userExists(String username) throws IOException
	{
		return getUsers().contains(new User(null, null, username, null));
	}
	
	/**
	 * Gets a specific user.
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
}
