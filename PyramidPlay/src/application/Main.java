package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class Main extends Application 
{
	@Override
	public void start(Stage primaryStage)
	{
		try 
		{
			Parent root = FXMLLoader.load(getClass().getResource("LoginScreen.fxml"));
			primaryStage.setTitle("Pyramid Play");
			primaryStage.setScene(new Scene(root, 600, 400));
			primaryStage.setResizable(false);
			primaryStage.show();
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args) 
	{
		launch(args);

	}
}