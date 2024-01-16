package model;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Project{
	String projectPath = Root.rootDir + "Project\\";
	
	String name;
	String type;
	String description;
	String time;
	
//	public static void main(String[] args) {
//		String name = "project_6";
//    	String type = "npm";
//    	String time = "2024-01-11 08:26:52";
//    	String description = "test add project";
//    	String version = "1.0.0";
//    	File file = new File("D:\\package.json");
//    	
////    	Root.getInstance("npm").saveDependencyTreeToTxt("D:\\Hust_project1_PackageManager\\Project\\npm\\project_5\\1.0.0\\");
//    	createNewProject(name, type, time, description, version, file);
//	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return type + ":" + name;
	}
	
	public Project(String name, String type, String description, String time) {
		this.projectPath += type + "\\" + name + "\\";
		
		this.name = name;
		this.type = type;
		this.time = time;
		this.description = description;
	}
	
	// Tìm kiếm Project từ name
	public Project(String projectName) {
		this.name = projectName;
		
		String sql = "select * from PROJECT "
				+ "where name = ?";
		List<Object> arg = new ArrayList<>();
		arg.add(projectName);
		
		ResultSet resultSet = null;
		try {
			resultSet = Database.query(sql, arg);
			if( resultSet.next()) {
				this.type = resultSet.getString("type");
				this.description = resultSet.getString("description");
				this.time = resultSet.getString("time");
				
				this.projectPath +=  type + "\\" + name + "\\";
			}
			else {
//				throw new Exception("ERROR: Không tìm thầy project model trong db");
//				System.out.println("ERROR: Không tìm thầy project model trong db");
			}
			resultSet.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public Boolean exists() {
		if( existsInDb() || existsInLocal() ) return true;
		return false;
	}
	
	private Boolean existsInDb() {
		String sqlChk = "SELECT * FROM PROJECT "
				+ "WHERE name = ?";
		List<Object> argChk = new ArrayList<>();
		argChk.add(name);
		
		boolean check = false;
		try {
			ResultSet resultSet = Database.query(sqlChk, argChk);
			check = resultSet.next();
			resultSet.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return check;
	}
	
	private Boolean existsInLocal() {
		if(projectPath.equals(Root.rootDir + "Project\\")) return false;
		
		File dir = new File(projectPath);
		if( dir.exists() ) return true;
		return false;
	}
	
	// Lưu lại thông tin project -----------------
	public void save(boolean update) {
		try {
			saveDb(update);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("ERROR: project.saveinDb");
		}
		saveLocal();
		System.out.println("Successful - Project.save: " + toString());
	}
	
	private void saveDb(boolean update) throws SQLException {
		if( existsInDb() ) {
			System.out.println("In db: Đã tồn tại rồi");
			if(update) {
				String sql = "UPDATE PROJECT "
						+ "SET type = ? , description = ?, time = ? "
						+ "WHERE name = ?";
				List<Object> arg = new ArrayList<>();
				arg.add(this.type);
				arg.add(this.description);
				arg.add(this.time);
				arg.add(this.name);
				Database.modify(sql, arg);
			}
		}
		else {
			String sql = "INSERT INTO PROJECT(name, type, description, time) "
					+ "VALUES (?, ?, ?, ?);";
			List<Object> arg = new ArrayList<>();
			arg.add(this.name);
			arg.add(this.type);
			arg.add(this.description);
			arg.add(this.time);
			
			Database.modify(sql, arg);
		}
	}
	
	private void saveLocal() {
		if( existsInLocal()) {
			System.out.println("In local: Đã tồn tại rồi");
			return;
		}
		
		File dir = new File(projectPath);
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
	
	// Tạo project mới --------------------------
	public static void createNewProject(String name, String type, String time, String description, String version, File configFile) {
		// Lưu project
    	Project newProject = new Project(name, type, description, time);
    	newProject.save(false);
    	
    	// Lưu version đầu tiên
    	Version.addNewVerrsion(newProject, version, description, time, configFile);
	}
	
	// Lấy tất cả các project ở db---------------
	public static ObservableList<Project> getAll(){
		ObservableList<Project> listProject = FXCollections.observableArrayList();
		String sql = "Select * from PROJECT ";
		try {
			ResultSet resultSet = Database.query(sql, null);
			while( resultSet.next()) {
				String projectName = resultSet.getString("name");
				String type = resultSet.getString("type");
				String description = resultSet.getString("description");
				String time = resultSet.getString("time");
				Project project = new Project(projectName, type, description, time);
				
				listProject.add(project);
				
			}
			resultSet.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listProject;
	}

	public void setDescription(String text) {
		// TODO Auto-generated method stub
		this.description = text;
	}
	
	public ObservableList<Version> getVersionOfProject(){
		ObservableList<Version> listVersion = FXCollections.observableArrayList();
		String sql = "Select * from VERSION "
				+ "where project like ? "
				+ "order by time DESC ";
		List<Object> arg = new ArrayList<>();
		arg.add(this.name);
		
		try {
			ResultSet resultSet = Database.query(sql, arg);
			while( resultSet.next()) {
				String version = resultSet.getString("version");
				
				Version verModel = new Version(
							this,
							version
				);
				listVersion.add(verModel);
			}
			resultSet.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listVersion;
	}
}
