package model;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Library implements Comparable<Library> {
	String libraryPath = "D:\\Hust_project1_PackageManager\\Library\\";
	
	String type;
	String name;
	String version;
	String description;
	String edit;

	@Override
	public int compareTo(Library other) {
		int result = this.description.compareTo(other.description);
        return result;
	}

//	public static void main(String[] args) {
//		Library library = new Library("maven", "org.xerial:sqlite-jdbc", "3.44.1.0", "maven", "no");
//		library.saveLocal();
//	}
	
	@Override
	public String toString() {
		if( type.equals("maven") ){ 
			String[] str = name.split(":"); // groupId:artifactId
			return str[0].replace('.', '\\') + "\\"
					+ str[1] + "\\" + version + "\\" ;
		}
		else if (type.equals("npm")){
			return name + "\\" + version + "\\";
		}
		return " ";
	}
	
	public Library(String type, String name, String version, String description, String edit) {
		if(type.equals("gradle"))this.type = "maven";
		else this.type = type;
		
		this.name = name;
		this.version = version;
		this.description = description;
		this.edit = edit;
		
		this.libraryPath += this.type + "\\" + toString();
	}
	
	public Library(String type, String name) {
		if(type.equals("gradle")) this.type = "maven";
		else this.type = type;
		
		this.name = name;
		
		String sql = "Select * from LIBRARY "
				+ "where type= ? and name = ?;";
		List<Object> arg = new ArrayList<>();
		arg.add(this.type);
		arg.add(this.name);
		
		try {
			ResultSet resultSet = Database.query(sql, arg);
			if( resultSet.next()) {
				this.version = resultSet.getString("version");
				this.description = resultSet.getString("description");
				this.edit = resultSet.getString("edit");
			}
			else {
				System.out.println("Không tìm thầy library model trong db");
			}
			resultSet.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.libraryPath += this.type + "\\" + toString();
	}
	
	public Boolean exists() {
		if( existsInDb() || existsInLocal() ) return true;
		return false;
	}
	
	private Boolean existsInDb() {
		String sqlChk = "SELECT * FROM LIBRARY "
				+ "WHERE type = ? and name = ? and version = ?";
		List<Object> argChk = new ArrayList<>();
		argChk.add(this.type);
		argChk.add(this.name);
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
		File dir = new File(libraryPath);
		if( dir.exists() ) return true;
		return false;
	}
	
	public void save(boolean update) {
		saveLocal();
		try {
			saveDb(update);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("ERROR: Library.saveInDb");
		}
	}

	private void saveDb(boolean update) throws SQLException {
		if( existsInDb() ) {
			if( update ) {
				String sql = "UPDATE LIBRARY "
						+ "SET description = ?, edit = ? "
						+ "WHERE type = ? and name = ? and version = ?;";
				List<Object> arg = new ArrayList<>();
				arg.add(this.description);
				arg.add(this.edit);
				arg.add(this.type);
				arg.add(this.name);
				arg.add(this.version);
				Database.modify(sql, arg);
			}
		}
		else {
			String sql = "INSERT INTO LIBRARY(type, name, version, description, edit) "
					+ "VALUES (?, ?, ?, ?, ?);";
			List<Object> arg = new ArrayList<>();
			arg.add(this.type);
			arg.add(this.name);
			arg.add(this.version);
			arg.add(this.description);
			arg.add(this.edit);
			Database.modify(sql, arg);
			System.out.println(toString() + " - Lưu thành công in Db");
		}
	}
	
	private void saveLocal() { // download file cấu hình và tạo file cây
		if( existsInLocal()) {
			System.out.println(toString() + "Đã lưu ");
			return;
		}
		File dir = new File(libraryPath);
		dir.mkdirs();
		
		Root.getInstance(type).downloadConfigFile(this);
		Root.getInstance(type).saveDependencyTreeToTxt(libraryPath);
		System.out.println("Library: Lưu local " + toString());
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

	public String getLibraryPath() {
		// TODO Auto-generated method stub
		return libraryPath;
	}

	
	
	public static ObservableList<Library> getAll(){
		ObservableList<Library> listLibrary = FXCollections.observableArrayList();
		String sql = "Select DISTINCT TYPE, NAME from LIBRARY ";

		try {
			ResultSet resultSet = Database.query(sql, null);
			while( resultSet.next()) {
				String type = resultSet.getString("type");
				String name = resultSet.getString("name");
				
				Library library = new Library(
							type,
							name
						);
				listLibrary.add(library);
			}
			
			resultSet.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listLibrary;
	}

	public List<Library> getVersionOfLibrary(){
		List<Library> list = new ArrayList<>();
		String sql = "Select * from LIBRARY "
				+ "where type = ? and name = ? "
				+ "order by version desc;";
		List<Object> arg = new ArrayList<>();
		arg.add(this.type);
		arg.add(this.name);
		
		try {
			ResultSet resultSet = Database.query(sql, arg);
			while( resultSet.next()) {
				Library lib= new Library(
						resultSet.getString("type"),
						resultSet.getString("name"),
						resultSet.getString("version"),
						resultSet.getString("description"),
						resultSet.getString("edit")
				);
				list.add(lib);
			}
			resultSet.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
}
