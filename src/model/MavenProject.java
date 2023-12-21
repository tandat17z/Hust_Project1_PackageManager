package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MavenProject {
	public static final String projectDir = "D:\\Hust_project1_PackageManager\\Project";
	String name;
	
	String groupId;
	String artifactId;
	String version;

	
	public MavenProject(String name, String version) {
		this.name = name;
		this.version = version;
		
	}
	
	public void getDependencyTree() {
		File file = new File(projectDir + "\\" + name + "\\" + version + "\\dependencyTree.txt");
		int deep = 1;
		
		Scanner myReader;
		try {
			myReader = new Scanner(file);
			boolean check = false; // Bắt đầu tìm đến đoạn text chứa thông tin cây
			
			while ( myReader.hasNextLine() ) { 
			    String data = myReader.nextLine();
			    if(check) {
			    	while( deep != 0) {
			    		String str = "\\[INFO\\] .{" + (deep*3 - 1) + "}\\s(.+)";
			    		Pattern pattern = Pattern.compile(str);
			    		Matcher matcher = pattern.matcher(data);
			    		if( matcher.find()) {
			    			String[] info = matcher.group(1).split(":");
//			    			System.out.println(deep + ": " + info[0] + info[1]);
			    			
			    			deep += 1;
			    			break;
			    		}
			    		deep -= 1;
			    	}
		    	}
			    if (deep == 0) break;
			    
				if(data.equals("[INFO] com.packagemanager:mavenProject:jar:1.0-SNAPSHOT"))
			    	check = true;
				
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
}
