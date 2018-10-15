package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;

import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
	public Logger logger;
	FileHandler fh;
	public Log(String file_name) throws SecurityException, IOException {
		File f=new File(file_name);
		System.out.println(f.exists());
		if(!f.exists()) {
			f.createNewFile();
		}
		BufferedReader br = new BufferedReader(new FileReader(f));
		String st;
		int i=0;
		/*
		while ((st = br.readLine()) != null){
			   i++;
		}
		while(i>50) {
			Scanner fileScanner = new Scanner(f);
			fileScanner.nextLine();
			FileWriter fileStream = new FileWriter(file_name);
			BufferedWriter out = new BufferedWriter(fileStream);
			while(fileScanner.hasNextLine()) {
			    String next = fileScanner.nextLine();
			    if(next.equals("\n")) 
			       out.newLine();
			    else 
			       out.write(next);
			    out.newLine();   
			}
			out.close();
			i--;
		}
		*/
		  
		fh=new FileHandler(file_name,true);
		logger=Logger.getLogger("test");
		logger.addHandler(fh);
		SimpleFormatter formatter=new SimpleFormatter();
		fh.setFormatter(formatter);
	
		//FileWriter out = null;
		//out = new FileWriter(file_name+".txt", true);
		//out.write("\n" + "hi");
	}
	
}
