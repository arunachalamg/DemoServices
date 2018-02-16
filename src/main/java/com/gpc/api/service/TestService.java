package com.gpc.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.gpc.api.framework.conn.CassandraQueryProvider;

/**
 * Service process
 * @author Arunachalam Govindasamy
 *
 */
@Service
public class TestService {

	@Autowired
	CassandraQueryProvider cassandraQueryProvider;
	
	public List<String> process() throws Exception{
		ResultSet rs =cassandraQueryProvider.executeSelectQuery("SELECT * FROM SHIPSRVC.SHIP_DETA_INVENTORY LIMIT 10");
		List<String> list = new ArrayList<String>();
		for(Row r : rs.all()) {
				list.add(r.getString("id") +" : " + r.getString("Name"));
		}
		return list;
	}
}
