package application;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.sun.glass.events.KeyEvent;

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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import com.google.gson.Gson;

public class LoginController implements Initializable
{
	public static int requestID;
	
	@FXML
	private AnchorPane RootPane;

	@FXML
	private Pane SignInPane;

	@FXML
	private Pane RegisterPane;

	@FXML
	private TextField UsernameTextField;

	@FXML
	private PasswordField PasswordTextField;

	@FXML
	private Button RegisterButton;

	@FXML
	private Button SignInOrRegisterButton;

	@FXML
	private Button RegisterOrCancelButton;

	@FXML
	private TextField AddFirstNameTextField;

	@FXML
	private TextField AddLastNameTextField;

	@FXML
	private TextField AddUsernameTextField;

	@FXML
	private TextField AddPasswordTextField;

	@FXML
	private Label InvalidSignInLabel;
	
	@FXML
	private Label UsernameUnavailableLabel;
	
	private User verifiedUser;
	
	DatagramSocket socket;
	
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
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
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
		//create message to send to server
		String[] arr = {username, password};
		
		//initialize buffer
		byte[] buffer = new byte[1000];
		try {
			Message loginMsg = new Message(1, requestID++, OpID.LOGIN, arr, InetAddress.getLocalHost());
			
			//convert to json
			String json = gson.toJson(loginMsg);
			
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
			System.out.println("Awaiting response from server...");
			socket.receive(reply);		
			System.out.println("Response received!");
			System.out.println(gson.fromJson(new String(buffer).trim(), String.class));
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try 
		{
			//if server responds with acknowledgement "VERIFIED" switch to song view
			if(gson.fromJson(new String(buffer).trim(), String.class).equals("VERIFIED"))
			{
				//switch to Song View screen on successful login
				verifiedUser = UserRepository.getUser(username);
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource("SongView.fxml"));
				Parent songViewParent = loader.load();
		
				Scene songViewScene = new Scene(songViewParent);
		
				//access the SongViewController and call initUser() to pass user information
				SongViewController controller = loader.getController();
				controller.initUser(verifiedUser);
				
				Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
				window.setResizable(false);
				window.setScene(songViewScene);
			}
			else
			{
				InvalidSignInLabel.setVisible(true);
			}
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Registers a new user and adds user information to users.json
	 * @param fn - First Name
	 * @param ln - Last Name
	 * @param uName - Unique username
	 * @param pw - password
	 * @param event - the event that triggered this function
	 */
	public void register(String fn, String ln, String uName, String pw, ActionEvent event) 
	{
		try 
		{
			if(UserRepository.userExists(AddUsernameTextField.getText()))
			{
				//username already exists and is not available
				UsernameUnavailableLabel.setVisible(true);
			}
			else
			{
				//username is available and ready to be added to the repository
				User newUser = new User(AddFirstNameTextField.getText(), AddLastNameTextField.getText(), AddUsernameTextField.getText(), AddPasswordTextField.getText());
				
				//add user to the user repository
				UserRepository.AddUser(newUser);
				
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource("SongView.fxml"));
				Parent songViewParent = loader.load();
		
				Scene songViewScene = new Scene(songViewParent);
		
				//access the SongViewController and call initUser() to pass user information
				SongViewController controller = loader.getController();
				controller.initUser(newUser);
				
				Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
				window.setResizable(false);
				window.setScene(songViewScene);
			}
		}
		catch(IOException e)
		{
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

	public void clearSignInText()
	{
		UsernameTextField.setText("");
		PasswordTextField.setText("");
	}

	public void clearRegisterText()
	{
		AddFirstNameTextField.setText("");
		AddLastNameTextField.setText("");
		AddUsernameTextField.setText("");
		AddPasswordTextField.setText("");
	}
}