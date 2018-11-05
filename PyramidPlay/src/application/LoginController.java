package application;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import com.google.gson.Gson;

/**
 * Controller class for login page.
 * 
 */
public class LoginController implements Initializable
{
	public static int requestID;
	
	@FXML
	/**
	 * Root pane for window.
	 */
	private AnchorPane RootPane;

	@FXML
	/**
	 * Pane for sign in
	 */
	private Pane SignInPane;

	@FXML
	/**
	 * Pane for registration.
	 */
	private Pane RegisterPane;

	@FXML
	/**
	 * Text field for username.
	 */
	private TextField UsernameTextField;

	@FXML
	/**
	 * Text field for password.
	 */
	private PasswordField PasswordTextField;

	@FXML
	/**
	 * Registration button
	 */
	private Button RegisterButton;

	@FXML
	/**
	 * Sign in button when registering.
	 */
	private Button SignInOrRegisterButton;

	@FXML
	/**
	 * Cancel button when registering.
	 */
	private Button RegisterOrCancelButton;

	@FXML
	/**
	 * First name input field.
	 */
	private TextField AddFirstNameTextField;

	@FXML
	/**
	 * Last name input field.
	 */
	private TextField AddLastNameTextField;

	@FXML
	/**
	 * Username input field.
	 */
	private TextField AddUsernameTextField;

	@FXML
	/**
	 * Password input field.
	 */
	private TextField AddPasswordTextField;

	@FXML
	/**
	 * Label shown if there is an invalid sign in.
	 */
	private Label InvalidSignInLabel;
	
	@FXML
	/**
	 * Label shown if username unavailable.
	 */
	private Label UsernameUnavailableLabel;
	
	/**
	 * User object created by creating account.
	 */
	private User verifiedUser;
	
	/**
	 * Socket to interact with server.
	 */
	DatagramSocket socket;
	
	/**
	 * GSON object for deserializing and serializing json.
	 */
	Gson gson;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) 
	{
		socket = null;
		gson = new Gson();
		
		try {
			System.out.println("Initializing Client Socket.");
			
			//create a socket with no specific port we listen on
			socket = new DatagramSocket();
			socket.setSoTimeout(500);
			System.out.println("Socket created with port " + socket.getLocalPort());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Signs in a user with an existing account
	 * @param username - username entered in the username textfield
	 * @param password - password entered in the password textfield
	 * @param event - the event that triggered this function
	 */
	public void signIn(String username, String password, ActionEvent event) {
		//check for empty username/password
		if (username.isEmpty() || password.isEmpty()) {
			InvalidSignInLabel.setText("Invalid Username/Password");
			InvalidSignInLabel.setVisible(true);
			return;
		}
		
		//create message to send to server
		String[] arr = {username, password};
		
		//initialize buffer
		byte[] buffer = new byte[5000];
		try {
			Message loginMsg = new Message(1, requestID++, OpID.LOGIN, arr, InetAddress.getLocalHost(), 1);
			
			//we can only send bytes, so flatten the string to a byte array
			byte[] msg = gson.toJson(loginMsg).getBytes();				
			
			System.out.println("Sending request.");
			//initialize and send request packet using port 1234, the port the server is listening on
			DatagramPacket request = new DatagramPacket(msg, msg.length, loginMsg.getAddress() , 1234);
			socket.send(request);
			System.out.println("request port: " + request.getPort());
					
			//initialize reply from server and receive it
					
			/* without specifying a port in this datagram packet, the OS will
			 * randomly assign a port to the reply for the program to listen on
			 */
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			
			//keep sending reply until server responds
			for (int i = 0; i < 5; i++) {
				System.out.println("Awaiting response from server...");
				try {
					socket.receive(reply);		
					System.out.println("Response received from port " + reply.getPort() + "!");
					break;
				} catch (SocketTimeoutException e) {
					if (i == 4) {
						System.out.println("Giving up on request");
						InvalidSignInLabel.setText("Unable to connect to server");
						InvalidSignInLabel.setVisible(true);
						return;
					}
					System.out.println("No response from server, sending request again.");
					socket.send(request);
				}
			}
			
			User returnedUser = gson.fromJson(new String(buffer).trim(), User.class);
			
			//if server responds with acknowledgement "VERIFIED" switch to song view
			if(returnedUser.getUsername().equals(username))
			{
				//switch to Song View screen on successful login
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource("SongView.fxml"));
				Parent songViewParent = loader.load();
		
				Scene songViewScene = new Scene(songViewParent);
		
				//access the SongViewController and call initUser() to pass user information
				SongViewController controller = loader.getController();
				controller.initUser(returnedUser);
				
				Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
				window.setResizable(false);
				window.setScene(songViewScene);
			}
			else
			{
				InvalidSignInLabel.setText("Invalid Username/Password");
				InvalidSignInLabel.setVisible(true);
			}
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Requests the server to register a new user.
	 * If the username is unique, the server will add a new user then sign into the app.
	 * If the username already exists, the function will display invalid username on the window.
	 * @param fn - First Name
	 * @param ln - Last Name
	 * @param uName - Unique username
	 * @param pw - password
	 * @param event - the event that triggered this function
	 */
	public void register(String fn, String ln, String uName, String pw, ActionEvent event) 
	{
		//create message to send to server
		String[] arr = {fn, ln, uName, pw};
		
		//initialize buffer
		byte[] buffer = new byte[5000];
		try 
		{
			//create register request and convert to byte array
			Message loginMsg = new Message(1, requestID++, OpID.REGISTER, arr, InetAddress.getLocalHost(), 1);
			byte[] msg = gson.toJson(loginMsg).getBytes();				
			
			System.out.println("Sending request.");
			
			//initialize and send request packet to the port the server is listening on, port 1234
			DatagramPacket request = new DatagramPacket(msg, msg.length, loginMsg.getAddress() , 1234);
			socket.send(request);
			System.out.println("request port: " + request.getPort());
					
			//initialize reply from server and receive it
					
			/* without specifying a port in this datagram packet, the OS will
			 * randomly assign a port to the reply for the program to listen on
			 */
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
						
			//keep sending reply until server responds
			for (int i = 0; i < 5; i++) {
				System.out.println("Awaiting response from server...");
				try {
					socket.receive(reply);		
					System.out.println("Response received from port " + reply.getPort() + "!");
					break;
				} catch (SocketTimeoutException e) {
					if (i == 5) {
						System.out.println("Giving up on request");
						UsernameUnavailableLabel.setText("Unable to connect to server");
						UsernameUnavailableLabel.setVisible(true);
						return;
					}
					System.out.println("No response from server, sending request again.");
					socket.send(request);
				}
			}
			
			System.out.println("Response received from port " + reply.getPort() + "!");
			
			User returnedUser = gson.fromJson(new String(buffer).trim(), User.class);
			
			//if username is valid, then server responds with a new user object
			//client will log in with new user
			if(returnedUser.getUsername().equals(uName))
			{
				//switch to Song View screen on successful login
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource("SongView.fxml"));
				Parent songViewParent = loader.load();
		
				Scene songViewScene = new Scene(songViewParent);
		
				//access the SongViewController and call initUser() to pass user information
				SongViewController controller = loader.getController();
				controller.initUser(returnedUser);
				
				Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
				window.setResizable(false);
				window.setScene(songViewScene);
			}
			else
			{
				//username already exists and is not available, inform user
				UsernameUnavailableLabel.setText("Username already in use");
				UsernameUnavailableLabel.setVisible(true);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	/**
	 * Event handler for text fields and sign in button
	 * @param event - an enter in password text field or a click on signIn/register button
	 */
	public void signInOrRegister(ActionEvent event)
	{
		//attempts to sign in the user with the given username and password
		if( SignInOrRegisterButton.getText().equals("Sign In") )
		{
			//user is attempting to sign in
			signIn(UsernameTextField.getText(), PasswordTextField.getText(), event);
		}
		else
		{
			//user is attempting to register a new account
			register(AddFirstNameTextField.getText(), AddLastNameTextField.getText(), AddUsernameTextField.getText(), AddPasswordTextField.getText(), event);
		}
	}

	@FXML
	/**
	 * MEthod to execute when registration button clicked.
	 * @param event
	 */
	public void OnRegisterOrCancelClicked(ActionEvent event)
	{
		if( SignInOrRegisterButton.getText().equals("Sign In"))
		{
			// Transition to Register page from Sign In page
			SignInOrRegisterButton.setText("Register Account");
			RegisterOrCancelButton.setText("Cancel");

			this.clearSignInText();

			//hide Sign In Panel
			SignInPane.setVisible(false);
			SignInPane.setMouseTransparent(true);

			//reset error label
			InvalidSignInLabel.setVisible(false);

			//show Register Panel
			RegisterPane.setVisible(true);
			RegisterPane.setMouseTransparent(false);
		}
		else
		{
			// Transition to Sign In page from Register page
			SignInOrRegisterButton.setText("Sign In");
			RegisterOrCancelButton.setText("Create an Account");

			this.clearRegisterText();
			
			//reset error label
			UsernameUnavailableLabel.setVisible(false);

			//show Sign In Panel
			SignInPane.setVisible(true);
			SignInPane.setMouseTransparent(false);

			//hide Register Panel
			RegisterPane.setVisible(false);
			RegisterPane.setMouseTransparent(true);
		}
	}

	/**
	 * Clears username and password text input fields.
	 */
	public void clearSignInText()
	{
		UsernameTextField.setText("");
		PasswordTextField.setText("");
	}

	/**
	 * Clears all registration text input fields.
	 */
	public void clearRegisterText()
	{
		AddFirstNameTextField.setText("");
		AddLastNameTextField.setText("");
		AddUsernameTextField.setText("");
		AddPasswordTextField.setText("");
	}
}