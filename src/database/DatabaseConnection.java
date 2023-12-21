package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConnection {
	public static DatabaseConnection getInstance() {
		return new DatabaseConnection();
	}
	
	public Connection getConnection(){
        Connection conn=null;
        try {
			Class.forName("org.sqlite.JDBC");
			String url="jdbc:sqlite:src\\database\\DatabasePM.db";
			conn= DriverManager.getConnection(url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("connection = Null");
			e.printStackTrace();
		}
        return conn;
    }
	
	public void sqlModify(String sql, String[] arg) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		int i = 1;
		for( String a: arg) {
			statement.setString(i, a);
			i++;
		}
		statement.executeUpdate();
		statement.close();
		connection.close();
	}
	
	public ResultSet sqlQuery(String sql, String[] arg) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement statement = connection.prepareStatement(sql);
		int i = 1;
		for( String a: arg) {
			statement.setString(i, a);
			i++;
		}
		ResultSet resultSet = statement.executeQuery();
//		while(resultSet.next()) {
//			System.out.println(resultSet.getString("name"));
//		}
		statement.close();
		connection.close();
		return resultSet;
	}
}
