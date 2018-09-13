package application;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

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
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) 
	{
		// TODO Auto-generated method stub
		
	}
	
	@FXML
	public void OnRegisterClicked(MouseEvent event) throws IOException
	{
		System.out.println("Register Button Clicked!");
	}
	
	@FXML
	public void OnSignInOrRegisterClicked(MouseEvent event)
	{
		
	}
	
	@FXML
	public void OnRegisterOrCancelClicked(MouseEvent event)
	{
		
	}
}