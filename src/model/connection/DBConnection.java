package model.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
	static String db = "fcduelbot";
	static String login = "fcduelbot";
	static String password = "RDD3FuimgPRPgAb";
	static String url = "jdbc:mysql://localhost/"+db;
	
	Connection conn = null;
	
	public DBConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			conn = DriverManager.getConnection(url,login,password);
			
			if (conn != null) {
				System.out.println("Connected to data base");
			}
		}
		catch(SQLException e) {
			System.out.println(e);
		}catch (ClassNotFoundException e) {
			System.out.println(e);
		}catch(Exception e) {
			System.out.println(e);
		}
	}
	
	public Connection getConnection() {
		return conn;
	}
	public void disconnect() {
		conn = null;
	}
}
