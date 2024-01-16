package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Database {
	static String dbFile = "DatabasePM0.db";
	
	static ResultSet resultSet = null;
	static Connection conn = null;
	static PreparedStatement statement = null; 
	
	private static void setConnection(){
        try {
			Class.forName("org.sqlite.JDBC");
			String url="jdbc:sqlite:src\\database\\" + dbFile;
			conn = DriverManager.getConnection(url);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Không kết nối được với Database");
			e.printStackTrace();
		}
    }
	
	private static void reset() throws SQLException {
		if( resultSet != null ) resultSet.close();
		if( statement != null) statement.close();
		if( conn != null) conn.close();
			
		resultSet = null;
		statement = null;
		conn = null;
	}
	
	public static void modify(String sql, List<Object> arg) throws SQLException{
//		reset();
		setConnection();
		statement = conn.prepareStatement(sql);
		int i = 1;
		for( Object item: arg) {
			if (item instanceof Integer) {
                // Xử lý kiểu int
                int intValue = (int) item;
                statement.setInt(i, intValue);
            } else if (item instanceof String) {
                // Xử lý kiểu String
                String stringValue = (String) item;
                statement.setString(i, stringValue);
            }
			i++;
		}
		statement.executeUpdate();
		reset();
	}

	public static ResultSet query(String sql, List<Object> arg) throws SQLException {
//		reset();
		setConnection();
		
		statement = conn.prepareStatement(sql);
		if(arg != null) {
			int i = 1;
			for( Object item: arg) {
				if (item instanceof Integer) {
					// Xử lý kiểu int
					int intValue = (int) item;
					statement.setInt(i, intValue);
				} else if (item instanceof String) {
					// Xử lý kiểu String
					String stringValue = (String) item;
					statement.setString(i, stringValue);
				}
				i++;
			}
		}
		resultSet = statement.executeQuery();
		return resultSet;
	}
}
