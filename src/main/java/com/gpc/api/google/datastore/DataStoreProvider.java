package com.gpc.api.google.datastore;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;

import com.google.cloud.datastore.Cursor;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.EntityQuery.Builder;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;


@Repository
public class DataStoreProvider {
	private static Logger log = Logger.getLogger(DataStoreProvider.class.getName());
	private Datastore datastore;
	private KeyFactory keyFactory;
	
	@Bean
	public Datastore DataStoreConfig() {
		datastore = DatastoreOptions.getDefaultInstance().getService();
		return datastore;
	}
	
	@Bean
	public KeyFactory getFactory() {
		keyFactory = datastore.newKeyFactory().setKind("arun.test-kind");
		return keyFactory;
	}
	
	public void listData() {
		Builder queryBuilder = Query.newEntityQueryBuilder()
				 .setNamespace("arun-db")
				 .setKind("testkind");
		 
		Query<Entity> queryEntity = queryBuilder.build();
		QueryResults<Entity> resultList = datastore.run(queryEntity);
		while(resultList.hasNext()) {
			Entity entity = resultList.next();
			
			System.out.println("Id "+entity.getString("id"));
			System.out.println("Name "+entity.getString("name"));
					
		
		}
	}
	
	
}
