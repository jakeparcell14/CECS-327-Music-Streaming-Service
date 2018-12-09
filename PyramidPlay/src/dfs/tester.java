package dfs;

import java.util.ArrayList;

public class tester {

	public static void main(String[] args) {
		Sorting sort = new Sorting();
		Metadata md = Metadata.GetMetadata();
		ArrayList<File> files = md.ls();
		for (File file : files) {
			sort.mapReduce(file);
		}
		
		System.out.println("done?");
	}

}
