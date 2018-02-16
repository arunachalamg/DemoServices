package com.gpc.api.framework.conn;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.ResultSet;

/**
 * 
 * @author Arunachalam Govindasamy
 *
 */
@Component
public class CassandraQueryProvider {
	private static Logger log = Logger.getLogger(CassandraQueryProvider.class.getName());

	@Autowired
	private CassandraConnection cassandraConnection;
	
	/**
	 * Execute Select query.
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public ResultSet executeSelectQuery(String query) throws Exception{
		ResultSet resultSet = null;
		if(null!=cassandraConnection.getSession()) {
	    	resultSet = cassandraConnection.getSession().execute(query);
		}else {
			log.error("Cassandra Connection is not established..");
		}
		return resultSet;
	}
}
