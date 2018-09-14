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


	@Override
	public void initialize(URL arg0, ResourceBundle arg1) 
	{
		
	}

	@FXML
	public void OnSignInOrRegisterClicked(ActionEvent event)
	{
		if( SignInOrRegisterButton.getText().equals("Sign In"))
		{
			System.out.println("Sign In Pressed");

			String u = UsernameTextField.getText();
			String p = PasswordTextField.getText();

			try 
			{
				if(UserRepository.IsUsernameAndPasswordCorrect(u, p))
				{
					Parent x = FXMLLoader.load(getClass().getResource("SongView.fxml"));
					x.setStyle("-fx-background-color: #a50000");
		            Scene y = new Scene(x);
		            Stage w = (Stage)((Node)event.getSource()).getScene().getWindow();
		            w.setResizable(false);
		            w.setScene(y);
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
			System.out.println("Register Account Pressed");
		}
	}

	@FXML
	public void OnRegisterOrCancelClicked(ActionEvent event)
	{
		if( SignInOrRegisterButton.getText().equals("Sign In"))
		{
			SignInOrRegisterButton.setText("Register Account");
			RegisterOrCancelButton.setText("Cancel");

			this.clearSignInText();

			//hide Sign In Panel
			SignInPane.setVisible(false);
			SignInPane.setMouseTransparent(true);

			//show Register Panel
			RegisterPane.setVisible(true);
			RegisterPane.setMouseTransparent(false);
		}
		else
		{
			SignInOrRegisterButton.setText("Sign In");
			RegisterOrCancelButton.setText("Create an Account");

			this.clearRegisterText();

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