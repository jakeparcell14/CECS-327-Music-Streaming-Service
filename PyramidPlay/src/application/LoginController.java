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

public class LoginController implements Initializable
{

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


	@Override
	public void initialize(URL arg0, ResourceBundle arg1) 
	{

	}
	
	@FXML
	public void OnSignInOrRegisterClicked(ActionEvent event)
	{
		//attempts to sign in the user with the given username and password
		if( SignInOrRegisterButton.getText().equals("Sign In"))
		{
			//user is attempting to sign in
			String u = UsernameTextField.getText();
			String p = PasswordTextField.getText();

			try 
			{
				if(UserRepository.IsUsernameAndPasswordCorrect(u, p))
				{
					//switch to Song View screen on successful login
					verifiedUser = UserRepository.getUser(u);
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
		else
		{
			//user is attempting to register a new account

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
					
					// switch to Song View screen on successful registration
					Parent x = FXMLLoader.load(getClass().getResource("SongView.fxml"));
					Stage s = (Stage)((Node)event.getSource()).getScene().getWindow();
					s.setResizable(false);
					s.setScene(new Scene(x));
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
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