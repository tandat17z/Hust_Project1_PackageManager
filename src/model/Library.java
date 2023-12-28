package model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import database.Database;
import database.ProjectRoot;

public class Library {
	String pomPath = "D:\\Hust_project1_PackageManager\\Library\\";
	String repoMaven = "https://repo1.maven.org/maven2/"; // để truy cập repo chứa Pom của maven
	
	String type;
	String name;
	String version;
	String description;
	String edit;
	
//	public static void main(String[] args) {
//		Library library = new Library(
//				"maven", 
//				"org.openjfx:javafx-controls", 
//				"21.0.1",
//				"javafx",
//				"no");
//		System.out.println(library.pomPath);
//		library.save();
//	}
	
	@Override
	public String toString() {
		if( type.equals("maven") ){ 
			String[] str = name.split(":"); // groupId:artifactId
			return str[0].replace('.', '\\') + "\\"
					+ str[1] + "\\" + version + "\\" ;
		}
		return " ";
	}
	
	public Library(String type, String name, String version, String description, String edit) {
		this.type = type;
		this.name = name;
		this.version = version;
		this.description = description;
		this.edit = edit;
		
		String[] str = name.split(":");
		if( str.length >= 2) {
			this.pomPath += type + "\\" + toString();
			this.repoMaven += toString().replace("\\", "/") + str[1] + "-" + version+ ".pom";
		}
		
	}
	
	public void save() {
		saveLocal();
		saveDb();
		System.out.println("Successful - Library.save: " + toString());
	}

	private void saveDb() {
		String sqlChk = "SELECT * FROM LIBRARY "
				+ "WHERE type = ? and name = ? and version = ?";
		String[] argChk = {type, name, version};
		
		try {
			Boolean check = Database.getInstance().sqlQuery(sqlChk, argChk);
			if( check ) {
				System.out.println("--" + toString() + "existed in Db");
				return;
			}
			
			String sql = "INSERT INTO LIBRARY(type, name, version, description, edit) "
					+ "VALUES (?, ?, ?, ?, ?);";
			String[] arg = {type, name, version, description, edit};
			Database.getInstance().sqlModify(sql, arg);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: Library.saveDb");
			e.printStackTrace();
		}
	}
	
	private void saveLocal() {
		File dir = new File(pomPath);
		if( dir.exists() ) {
        	System.out.println( "--" + toString() + " existed trong Local");
        	return;
		}
		
		dir.mkdirs();
		try {
	        downloadPom();
	        ProjectRoot.getInstance(type).saveDependencyTree(pomPath);
		}
		catch(Exception e) {
			System.out.println("Error: Library don't get pom");
		}
	}
	
	private void downloadPom() {
		HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(repoMaven);

		try {
			HttpResponse response;
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String pomContent =  EntityUtils.toString(entity);
				
				File file = new File(pomPath + "pom.xml");
	            FileWriter writer = new FileWriter(file);
	            writer.write(pomContent);
	            writer.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEdit() {
		return edit;
	}

	public void setEdit(String edit) {
		this.edit = edit;
	}

	public String getPomPath() {
		// TODO Auto-generated method stub
		return pomPath;
	}

	
}
