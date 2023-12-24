package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {
	public static Database getInstance() {
		return new Database();
	}
	
	public Connection getConnection(){
        Connection conn=null;
        try {
			Class.forName("org.sqlite.JDBC");
			String url="jdbc:sqlite:src\\database\\DatabasePM0.db";
			conn= DriverManager.getConnection(url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("connection = Null");
			e.printStackTrace();
		}
        return conn;
    }
	
	public void sqlModify(String sql, String[] arg) throws SQLException{
		Connection connection = getConnection();
		PreparedStatement statement;
		statement = connection.prepareStatement(sql);
		int i = 1;
		for( String a: arg) {
			statement.setString(i, a);
			i++;
		}
		statement.executeUpdate();
		statement.close();
		connection.close();
	}
	
	public Boolean sqlQuery(String sql, String[] arg) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		int i = 1;
		for( String a: arg) {
			statement.setString(i, a);
			i++;
		}
		ResultSet resultSet = statement.executeQuery();
		Boolean check = false;
		if( resultSet.next()) check = true;
//		while(resultSet.next()) {
//			System.out.println(resultSet.getString("name"));
//		}
		statement.close();
		connection.close();
		return check;
	}
}
