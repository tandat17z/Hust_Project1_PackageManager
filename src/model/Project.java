package model;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.Database;

public class Project{
	String projectDir = "D:\\Hust_project1_PackageManager\\" + "Project\\";
	
	String name;
	String type;
	String description;
	String time;
	
//	public static void main(String[] args) {
//		Project project = new Project(
//				"FirstProject",
//				"maven",
//				"first project",
//				"2023:12:24 12:12:12"
//			);
//		project.save();
//	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return type + ":" + name;
	}
	
	public Project(String name, String type, String description, String time) {
		this.name = name;
		this.type = type;
		this.time = time;
		this.description = description;
		
		this.projectDir += name + "\\";
	}
	
	// Tìm kiếm Project từ name
	public Project(String projectName) {
		// TODO Auto-generated constructor stub
		String sql = "select * from PROJECT "
				+ "where name = ?";
		String [] arg = {projectName};
		
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
				this.name = projectName;
				this.type = resultSet.getString("type");
				this.description = resultSet.getString("description");
				this.time = resultSet.getString("time");
			}
			else {
				System.out.println("Không tìm thầy project model trong db");
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.projectDir += name + "\\";
	}

	public Boolean exists() {
		if( existsInDb() || existsInLocal() ) return true;
		
		return false;
	}
	
	private Boolean existsInDb() {
		String sqlChk = "SELECT * FROM PROJECT "
				+ "WHERE name = ?";
		String[] argChk = {name};
		
		try {
			return Database.getInstance().sqlQuery(sqlChk, argChk);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	private Boolean existsInLocal() {
		File dir = new File(projectDir);
		if( dir.exists() ) return true;
		return false;
	}
	
	public void save() {
		saveDb();
		saveLocal();
		System.out.println("Successful - Project.save: " + toString());
	}
	
	private void saveDb() {
		if( existsInDb() ) return;
		try {
			String sql = "INSERT INTO PROJECT(name, type, description, time) "
					+ "VALUES (?, ?, ?, ?);";
			String[] arg = {name, type, description, time};
			Database.getInstance().sqlModify(sql, arg);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: Project.saveDb");
			e.printStackTrace();
		}
	}
	
	private void saveLocal() {
		if( existsInLocal()) return;
		
		File dir = new File(projectDir);
		dir.mkdirs();
	}

	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	public String getType() {
		// TODO Auto-generated method stub
		return type;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return description;
	}

	public String getTime() {
		// TODO Auto-generated method stub
		return time;
	}
	
}
