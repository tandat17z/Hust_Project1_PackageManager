package model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.Database;

public class Change {
	int version_id;
	String time;
	String type;
	String detail;
	
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
			List<Object> arg = new ArrayList<>();
			arg.add(this.version_id);
			arg.add(time);
			arg.add(type);
			arg.add(detail);
			
			Database.modify(sql, arg);
			System.out.println("Lưu thành công change: " + type);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: Change.saveDb");
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
