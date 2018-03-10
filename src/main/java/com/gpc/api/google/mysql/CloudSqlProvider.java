package com.gpc.api.google.mysql;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class CloudSqlProvider {
	private Connection connection;
	
	public void initilize() {
		String instanceConnectionName = "as-arung-test:us-east1:arun-test-mysql";
		String databaseName = "guestbook";
	    String username = "axg8965";
	    String password = "test1234";
	    String jdbcUrl = String.format(
	            "jdbc:mysql://google/%s?cloudSqlInstance=%s&"
	                + "socketFactory=com.google.cloud.sql.mysql.SocketFactory&useSSL=false",
	            databaseName,
	            instanceConnectionName);
	     
	        try {
				connection = DriverManager.getConnection(jdbcUrl, username, password);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    
	        
	}
	/**
	 * get Connection.
	 * @return
	 */
	public Connection getConn() {
		if(null==connection) {
			initilize();
		}
		return connection;
	}
	
	/**
	 * 
	 */
	public List<String> getData() {
	List<String> list = new ArrayList();
		try (Statement statement = getConn().createStatement()) {
		      ResultSet resultSet = statement.executeQuery("select * from entries");
		      while (resultSet.next()) {
		    	  System.out.println("name ="+resultSet.getString(1));
		    	  list.add(resultSet.getString(1));
		      }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
}
