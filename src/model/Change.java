package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import database.Database;

public class Change {
	private int version_id;
	private String time;
	private String type;
	private String detail;
	
	public Change(int version_id, String time, String type, String detail) {
		this.version_id = version_id;
		this.time = time;
		this.type = type;
		this.detail = detail;
	}
	
	public void saveDb() {
		try {
			String sql = "INSERT INTO CHANGE(version_id, time, type, detail) "
					+ "VALUES (?, ?, ?, ?);";
			
			Connection connection = Database.getInstance().getConnection();
			PreparedStatement statement;
			statement = connection.prepareStatement(sql);
			statement.setInt(1, version_id);
			statement.setString(2, time);
			statement.setString(3, type);
			statement.setString(4, detail);
			System.out.println("Lưu thành công change: " + type);
			
			statement.executeUpdate();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: Project.saveDb");
			e.printStackTrace();
		}
	}

	public int getVersion_id() {
		return version_id;
	}

	public void setVersion_id(int version_id) {
		this.version_id = version_id;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}
	
	
}
