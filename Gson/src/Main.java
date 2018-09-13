import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.*;

import com.google.gson.internal.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
public class Main {

	public static void main(String[] args) {
		ArrayList<User> users = getUsers();
		System.out.format("Username: admin Password: pw %s\n", IsUsernameAndPasswordCorrect("admin", "pw"));
		System.out.format("Username: wslates Password: pw %s\n", IsUsernameAndPasswordCorrect("wslates", "pw"));
		System.out.format("Username: wslates Password: password %s\n", IsUsernameAndPasswordCorrect("wslates", "password"));
		for (int i = 0; i<users.size(); i++) {
			System.out.format("%s %s %s\n", users.get(i).getFirstName(), users.get(i).getlastName(), users.get(i).getPassword());
		}
		User wesley = getUser("wslates");
		
		System.out.println(wesley.getFirstName());
	}
	
	public static void AddUser(User user) {
		FileWriter out = null;
		try {
			Gson gson = new Gson();
			out = new FileWriter("users.json", true);
			
			out.write(gson.toJson(user)+"\n");
			
			if (out != null)
				out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
	}
	
	private static ArrayList<User> getUsers() {
		Gson gson = new Gson();
		ArrayList<User> users = new ArrayList<User>();
		try {
			File file = new File("users.json");
			Scanner scanner = new Scanner(file);
			
			while(scanner.hasNextLine()) {
				users.add(gson.fromJson(scanner.nextLine(), User.class));
			}
			scanner.close();
		} catch (IOException e) {
			
		}
		return users;
	}
	
	public static boolean IsUsernameAndPasswordCorrect(String username, String password) {
		return getUsers().contains(new User(null, null, username, password));
	}
	
	public static User getUser(String username) {
		ArrayList<User> users = getUsers();
		for (int i = 0; i<users.size(); i++) {
			if (users.get(i).getUsername().equals(username))
				return users.get(i);
		}
		return null;
	}
}
