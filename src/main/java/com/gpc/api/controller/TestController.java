package com.gpc.api.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.pubsub.model.ReceivedMessage;
import com.gpc.api.google.datastore.DataStoreProvider;
import com.gpc.api.google.mysql.CloudSqlProvider;
import com.gpc.api.google.pubsub.PubSubMessageProvider;
import com.gpc.api.service.TestService;


/**
 * 
 * @author Arunachalam Govindasamy
 *
 */
@RestController
@RequestMapping("/test")
public class TestController {

	@Autowired
	TestService testService;
	
	@Autowired
	PubSubMessageProvider pubSubMessageProvider;
	
	@Autowired
	DataStoreProvider dataStoreProvider;
	
	@Autowired
	CloudSqlProvider cloudSqlProvider;
	
	/**
	 * Greeting Services
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/greetings", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<String>> Greeting() throws Exception {
		System.out.println("greetings...");
		List<String> list = testService.process();
		return new ResponseEntity<>(list,HttpStatus.OK);
		
	}
	
	@RequestMapping(value = "/myPubSub", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<ReceivedMessage>> pubsunMessaging() throws Exception {
		System.out.println("pubsub...");
		List<ReceivedMessage> list = pubSubMessageProvider.pullMessagesFromSubscription("projects/as-arung-test/subscriptions/testing-rithvik");
		if(null == list || list.isEmpty()) {
			list = new ArrayList<ReceivedMessage>();
			ReceivedMessage rm = new ReceivedMessage();
			rm.set("status", "No sunscription message is available at this movement.");
			list.add(rm);
		}
		return new ResponseEntity<>(list,HttpStatus.OK);
		
	}
	
	/**
	 * Greeting Services
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/datastore", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<String>> dataStore() throws Exception {
		System.out.println("Data Store...");
		dataStoreProvider.listData();
		return new ResponseEntity<>(new ArrayList(),HttpStatus.OK);
		
	}
	
	/**
	 * Greeting Services
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/cloudsql", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<String>> cloudsqltest() throws Exception {
		System.out.println("cloudsqltest.....");
		List<String> list = cloudSqlProvider.getData();
		return new ResponseEntity<>(list,HttpStatus.OK);
	}
}
