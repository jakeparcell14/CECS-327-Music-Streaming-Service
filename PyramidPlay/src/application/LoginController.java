package application;

import java.awt.Button;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

public class LoginController implements Initializable
{

	@FXML
	private AnchorPane rootPane;
	
	@FXML
	private BorderPane registerPane;
	
	@FXML
	private TextField usernameTextField;
	
	@FXML
	private PasswordField passwordTextField;
	
	@FXML
	private Label registerAccountLabel;
	
	@FXML
	private BorderPane buttonPane;
	
	@FXML
	private Button registerButton;
	
	
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
}