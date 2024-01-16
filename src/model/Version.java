package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Version {
	String versionPath = null;
	
	int versionId;
	
	Project project;
	String version;
	String description;
	String time;
	File configFile = null;
	
//	public static void main(String[] args) {
//		Project project = new Project("Test");
//		Version version = new Version(project, "1.1.1", "create new version", "2024-01-09 17:58:07");
//		File file = new File("D:\\pom.xml");
//		version.setConfigFile(file);
//		version.save(false);
//	
//	}
	
	public void setConfigFile(File configFile) {
		this.configFile = configFile;
	}
	
	@Override
	public String toString() {
		return project.toString() + ":" + version;
	}
	
	public Version(Project project, String version, String description, String time) {
		this.versionPath = project.projectPath + version + "\\";
		
		this.project = project;
		this.version = version;
		this.description = description;
		this.time = time;
		
	}
	
	
	public Version(Project project, String version) {
		this.project = project;
		this.version = version;
		
		String sql = "select * from VERSION "
				+ "where project = ? and version = ?";
		List<Object> arg = new ArrayList<>();
		arg.add(project.name);
		arg.add(version);
	
		try {
			ResultSet resultSet = Database.query(sql, arg);
			if( resultSet.next()) {
				this.versionId = resultSet.getInt("version_id");
				this.description = resultSet.getString("description");
				this.time = resultSet.getString("time");
				this.versionPath = project.projectPath + version + "\\";
			}
			else {
				System.out.println("ERROR: Không tìm thầy version model trong db");
			}
			resultSet.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public Version(int version_id) {
		String sql = "select * from VERSION "
				+ "where version_id = ?";
		
		List<Object> arg = new ArrayList<>();
		arg.add(this.versionId);
		
		try {
			ResultSet resultSet = Database.query(sql, arg);
			if( resultSet.next()) {
				this.versionId = resultSet.getInt("version_id");
				this.project = new Project(resultSet.getString("project"));
				this.version = resultSet.getString("version");
				this.description = resultSet.getString("description");
				this.time = resultSet.getString("time");
			}
			else {
				System.out.println("Không tìm thầy version model trong db");
			}
			resultSet.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		versionPath = project.projectPath + version + "\\";

	}
	
	public Boolean exists() {
		if( existsInDb() || existsInLocal() ) return true;
		return false;
	}
	
	private Boolean existsInDb() {
		String sqlChk = "SELECT * FROM VERSION "
				+ "WHERE project = ? and version = ?;";
		List<Object> argChk = new ArrayList<>();
		argChk.add(this.project.name);
		argChk.add(this.version);
		
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
		if( versionPath == null ) return false;
		
		File dir = new File(versionPath);
		if( dir.exists() ) return true;
		return false;
	}
	
	
	// Lưu version --------------------------------
	public void save(boolean update) {
		try {
			saveDb(update);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("ERROR: version.saveInDb");
		}
		
		saveLocal();
		
		saveDependencyDepth1();
		
		System.out.println("Successful - Version.save: " + toString());
	}
	
	private void saveDb(boolean update) throws SQLException {
		if( existsInDb()) {
			if(update) {
				String sql = "UPDATE VERSION "
						+ "SET description = ?, time = ? "
						+ "WHERE project = ? and version = ?;";
				List<Object> arg = new ArrayList<>();
				arg.add(this.description);
				arg.add(this.time);
				arg.add(this.project.name);
				arg.add(this.version);
				Database.modify(sql, arg);
			}
		}
		else {
			String sql = "INSERT INTO VERSION(project, version, description, time) "
					+ "VALUES (?, ?, ?, ?);";
			List<Object> arg = new ArrayList<>();
			arg.add(this.project.name);
			arg.add(this.version);
			arg.add(this.description);
			arg.add(this.time);
			Database.modify(sql, arg);
		}
	}
	
	private void saveLocal() {
		File dir = new File(versionPath);
		if( !existsInLocal()) 
			dir.mkdirs();
		
		//Sao chép config file vào thư mục lưu trữ
		if( configFile != null) {
			Root.getInstance(project.type).copyToLocal(configFile, versionPath);
			Root.getInstance(project.type).saveDependencyTreeToTxt(versionPath);
		}
	}
	
	public List<Library> getDependencyDepth1(){
		List<Library> list = new ArrayList<>();
		Map<Library, Library> tree = Root.getInstance(this.project.type).getDependency(versionPath);
		for(Library keyLib : tree.keySet()) {
			if( tree.get(keyLib) == null ) {
				list.add(keyLib);
			}
		}
		return list;
	}
	public void saveDependencyDepth1() {
		for(Library keyLib : getDependencyDepth1()) {
			if(keyLib.type.equals("maven")) keyLib.setDescription("Maven Repository");
			else if(keyLib.type.equals("npm")) keyLib.setDescription("Npm Repository");
			keyLib.save(false);
		}
	}
	
	public Project getProject() {
		return project;
	}
	
	public String getVersionPath() {
		return versionPath;
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
	public int getVersionId() {
		// TODO Auto-generated method stub
		return versionId;
	}
	
	
	// Thêm 1 version mới --------------------
	public static void addNewVerrsion(Project project, String version, String description, String time, File configFile) {
		Version newVersion = new Version(project, version, description, time);
		newVersion.setConfigFile(configFile);
		newVersion.save(false);
    	
    	// Lưu thay đổi đầu tiên
		newVersion = new Version(project, version);
		
    	Change change = new Change(newVersion.getVersionId(), time, "create version", "create version");
    	change.saveDb();
	}
	
//	public static ObservableList<Version> getVersionOfProject(Project project ){
//		ObservableList<Version> listVersion = FXCollections.observableArrayList();
//		String sql = "Select * from VERSION "
//				+ "where project like ? "
//				+ "order by time DESC ";
//		List<Object> arg = new ArrayList<>();
//		arg.add(project.name);
//		
//		try {
//			ResultSet resultSet = Database.query(sql, arg);
//			while( resultSet.next()) {
//				String version = resultSet.getString("version");
//				
//				Version verModel = new Version(
//							project,
//							version
//				);
//				listVersion.add(verModel);
//			}
//			resultSet.close();
//			
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return listVersion;
//	}
	public void setDescription(String text) {
		// TODO Auto-generated method stub
		this.description = text;
	}
	
	public void saveChangeConfigFile(String changeTime, String content) throws IOException {
		System.out.println("Bắt đầu lưu những thay đổi");
		List<Library> oldDependencis = getDependencyDepth1();
    	List<Library> newDependencis;
    	
    	this.configFile = new File(versionPath, Root.getInstance(project.type).getFileType());
    	try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            writer.write(content);
            writer.flush(); // Đảm bảo dữ liệu được ghi vào file ngay lập tức
            writer.close(); // Đóng Writer để giải phóng tài nguyên
            
            String type = this.project.getType();
            Root.getInstance(type).saveDependencyTreeToTxt(versionPath);
            
            saveDependencyDepth1(); // Lưu lại những gói mới
            newDependencis = getDependencyDepth1();
       
            
            // tìm những library được update or insert
            for( Library library: newDependencis) {
            	Boolean check = false;
            	String newName = library.getName(), newVersion = library.getVersion();
            	
            	for(Library old: oldDependencis) {
            		String oldName = old.getName(), oldVersion = old.getVersion();
            		
            		// check update
            		if( newName.equals(oldName)) {
            			check = true;
            			if( !newVersion.equals(oldVersion)){
	            			Change change = new Change(versionId, changeTime, "update", newName +":" + oldVersion + ">>" + newVersion);
	            			change.saveDb();
	            		}
            			break;
            		}
            		
            	}
            	
            	// check insert
            	if( !check ) {
            		Change change = new Change(versionId, changeTime, "insert", newName + ":" + newVersion);
        			change.saveDb();
            	}
            }
            
            // check remove
            for( Library old: oldDependencis) {
            	Boolean check = false;
            	for(Library library: newDependencis) {
            		if(old.getName().equals(library.getName())) {
            			check = true;
            			break;
            		}
            	}
            	if( !check ) {
            		Change change = new Change(versionId, changeTime, "remove", old.getName() + ":" + old.getVersion());
        			change.saveDb();
            	}
            }
		}
    	

		// Cập nhật trường thời gian cho version
		this.time = changeTime;
		this.save(true);
		
		System.out.println("Cập nhật thời gian cho version");
	}
	
	public int[] getRecentChange() {
		String sql = "select * from CHANGE "
				+ "where version_id = ?"
				+ "order by time desc;";
		List<Object> arg = new ArrayList<>();
		arg.add(versionId);
		
		int [] num = {0, 0, 0};
		
		String time = null;
		try {
			ResultSet resultSet = Database.query(sql, arg);
			
			while( resultSet.next()) {
				String t = resultSet.getString("time");
				if( time == null || time.equals(t)) {
					time = t;
					String type = resultSet.getString("type");
					if( type.equals("insert")) num[0] += 1;
					else if( type.equals("remove")) num[1] += 1;
					else if( type.equals("update")) num[2] += 1;
				}
			}
				
			resultSet.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return num;
		
	}
}
