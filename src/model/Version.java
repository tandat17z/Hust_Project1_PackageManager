package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import database.Database;
import database.ProjectRoot;

public class Version {
	String versionDir;
	
	Project project;
	String version;
	String description;
	String time;
	
	File configFile;
	
//	public static void main(String[] args) {
//		Project project = new Project(
//				"FirstProject",
//				"maven",
//				"first project",
//				"2023:12:24 12:12:12"
//			);
//		Version version = new Version(
//				project,
//				"1.0.1",
//				"first verion",
//				"2023:12:24 12:12:12"
//			);
//		File configFile = new File("C:\\Users\\tandat17z\\eclipse-workspace\\Hust_Project1_PackageManager\\pom.xml");
//		version.save(configFile);
//	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return project.toString() + ":" + version;
	}
	
	public Version(Project project, String version, String description, String time) {
		this.project = project;
		this.version = version;
		this.description = description;
		this.time = time;
		
		versionDir = project.projectDir + version + "\\";
	}
	
	public Version(String projectName, String version) {
		String sql = "select * from VERSION "
				+ "where project = ? and version = ?";
		String [] arg = {projectName, version};
		
		Connection connection = Database.getInstance().getConnection();
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(sql);
			int i = 1;
			for( String a: arg) {
				statement.setString(i, a);
				i++;
			}
			ResultSet resultSet = statement.executeQuery();
			if( resultSet.next()) {
				this.project = new Project(projectName);
				this.version = version;
				this.description = resultSet.getString("description");
				this.time = resultSet.getString("time");
			}
			else {
				System.out.println("Không tìm thầy version model trong db");
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		versionDir = project.projectDir + version + "\\";
	}
	
	public void save(File configFile) {
		this.configFile = configFile;
		saveDb();
		saveLocal();
		System.out.println("Successful - Version.save: " + toString());
	}
	
	private void saveDb() {
		String sqlChk = "SELECT * FROM VERSION "
				+ "WHERE project = ? and version = ?";
		String[] argChk = {project.name, version};
		try {
			Boolean check = Database.getInstance().sqlQuery(sqlChk, argChk);
			if( check ) {
				System.out.println("--" + toString() + "existed in Db");
				return;
			}
			
			String sql = "INSERT INTO VERSION(project, version, description, time) "
					+ "VALUES (?, ?, ?, ?);";
			String[] arg = {project.name, version, description, time};
			Database.getInstance().sqlModify(sql, arg);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: Version.saveDb");
			e.printStackTrace();
		}
	}
	
	private void saveLocal() {
		File dir = new File(versionDir);
		if( dir.exists() ) {
        	System.out.println("--" +  toString() + " existed trong Local");
        	return;
		}
		dir.mkdirs();
		//Sao chép config file vào thư mục lưu trữ
    	try {
			Files.copy(
					configFile.toPath(), 
					new File(versionDir, "pom.xml").toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	// Tạo file txt lưu dependency tree
    	ProjectRoot.getInstance(project.type).saveDependencyTree(versionDir);
    	try {
    		System.out.println("---- SAVE DEPENDENCY ----");
			saveDependency();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: Version.saveDependency");
			e.printStackTrace();
		}
	}
	
	private void saveDependency() throws FileNotFoundException {
		File treeFile = new File(versionDir + "DependencyTree.txt");
    	int deep = 1;
		Scanner myReader;
		try {
			myReader = new Scanner(treeFile);
			boolean check = false; // Bắt đầu tìm đến đoạn text chứa thông tin cây
			while ( myReader.hasNextLine() ) { 
				String data = myReader.nextLine();
				if(check) {
					while( deep != 0) {
						String str = "\\[INFO\\] .{" + (deep*3 - 1) + "}\\s(.+)";
						Pattern pattern = Pattern.compile(str);
						Matcher matcher = pattern.matcher(data);
						if( matcher.find()) {
							String packageName = matcher.group(1);
							String[] info = matcher.group(1).split(":");
							
							String name = info[0] + ":" + info[1];
							String version = info[info.length - 2];
							
			    			Library library = new Library(project.type, name, version, "Maven Repository", "no");
			    			library.save();
							
							deep += 1;
							break;
						}
						deep -= 1;
					}
				}
				if (deep == 0) break;
				
				if(data.equals("[INFO] com.packagemanager:MavenProjectRoot:jar:1.0-SNAPSHOT"))
					check = true;
				
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public Project getProject() {
		return project;
	}
	
	public String getVersionDir() {
		return versionDir;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getTime() {
		return time;
	}
	
	public String  getDescription() {
		return description;
	}
}
