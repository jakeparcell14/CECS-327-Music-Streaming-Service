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
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class Main extends Application 
{
	@Override
	public void start(Stage primaryStage) 
	{
		try 
		{
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,400,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args) 
	{
		String fileName = "DataStore.dat";
		File f = new File(fileName);
		DataStore data;

		if(f.exists())
		{
			data = readFromFile(f);
		}
		else
		{
			data = new DataStore();
		}

		launch(args);

		//saves data to file
		writeToFile(f, data);
	}

	/**
	 * Extracts DataStore object from a given file name
	 * @param fileName name of a given file
	 * @return data DataStore object from given file
	 */
	public static DataStore readFromFile(File fileName)
	{
		DataStore data = new DataStore();

		if(!fileName.exists())
		{
			//file with given name does not exist
			return data;
		}
		else
		{
			//file with given name exists
			try 
			{
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));

				try
				{
					data = (DataStore) in.readObject();
				}
				catch(ClassCastException e)
				{

				}

				in.close();
			} 
			catch (FileNotFoundException e) 
			{
				System.out.println("File Not Found");
			} 
			catch (IOException e) 
			{
				System.out.print(e);
				System.out.println("Error Retrieving Data from File");
			} 
			catch (ClassNotFoundException e) 
			{
				System.out.println("Error Retrieving Data from File");
			}

			return data;
		}
	}

	/**
	 * saves DataStore object to a file with a given file name
	 * @param fileName file to which User object is saved
	 * @param data DataStore object that is saved
	 */
	public static void writeToFile(File fileName, DataStore data)
	{
		try 
		{
			if(!fileName.exists())
			{
				fileName.createNewFile();
			}
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));

			out.writeObject(data);

			out.close();
		} 
		catch (FileNotFoundException e) 
		{
			System.out.println("File Not Found");
		} 
		catch (IOException e) 
		{
			System.out.println(e);
			System.out.println("Error Writing to File");
		}
	}
}