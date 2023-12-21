package model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

import database.MavenProjectRoot;

public class Project extends MavenProjectRoot {
	String projectDir ;
	
	String name;
	String version;
	String type;
	String time;
	String description;
	
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Project(String name, String version, String type, String description, String time) {
		super();
		this.name = name;
		this.version = version;
		this.type = type;
		this.time = time;
		this.description = description;
		
		this.projectDir = "D:\\Hust_project1_PackageManager\\Project" 
				+ "\\" + name + "\\" + version;
	}
	
	public void saveDb() {
		String sql = "INSERT INTO PROJECT(name, version, type, description, time) "
				+ "VALUES (?, ?, ?, ?, ?);";
		String[] arg = {name, version, type, description, time};
		try {
			sqlModify(sql, arg);
			System.out.println("Lưu thành công Project trong db");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Lưu Project trong db FAIL --------");
			e.printStackTrace();
		}
		
	}
	
	public void saveLocal(File configFile) {
		// Tạo thư mục mới
		File dir = new File(projectDir);
		boolean check = dir.mkdirs();
		if( !check ) 
			System.out.println("Không tạo thành công thư mục lưu trữ new project");
		
		//Sao chép config file vào thư mục lưu trữ
    	try {
			Files.copy(
					configFile.toPath(), 
					new File(projectDir, "pom.xml").toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	// Tạo file txt lưu dependency tree
    	dependencyTree(projectDir);
    	System.out.println("Lưu thành công ở Local");
	}
	
	public void saveLibrary() {
		
	}
}
