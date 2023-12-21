package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CreateNewProject {
	static ResultSet resultSet;
	
	public static void create(String name, String groupid, String artifactid, String version, String type, String time, String description) {
		String sql_newproject = "INSERT INTO PROJECT(name, groupid, artifactid, type, time, description) "
				+ "VALUES (?, ?, ?, ?, ? ,?);";
		
		String sql_addversion = "INSERT INTO CONFIGFILE(projectid, version)"
				+ "VALUES (?, ?);";
		
		String [] arg1 = {name, groupid, artifactid, type, time, description};
		try {
			query("executeUpdate", sql_newproject, arg1);
			
			String[] arg2 = {name};
			query("executeQuery", "Select * from PROJECT WHERE name = ?", arg2);
			
			int projectid = resultSet.getInt("ID");
			resultSet.close();
			
			Connection connection = DatabaseConnection.getInstance().getConnection();
			PreparedStatement statement = connection.prepareStatement(sql_addversion);
			statement.setInt(1, projectid);
			statement.setString(2, version);
			
			statement.executeUpdate();
			
			statement.close();
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	static void query(String type, String sql, String[] arg) throws SQLException {
		Connection connection = DatabaseConnection.getInstance().getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		int i = 1;
		for(String str: arg) {
			statement.setString(i, str);
			i++;
		}
		if( type == "executeUpdate" )
			statement.executeUpdate();
		else if(type == "executeQuery")
			resultSet = statement.executeQuery();
	}
}
