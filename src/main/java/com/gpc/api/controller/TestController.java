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

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
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
	
	@RequestMapping(value = "/greetings", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<String>> Greeting() throws Exception {
		System.out.println("greetings...");
		List<String> list = testService.process();
		return new ResponseEntity<>(list,HttpStatus.OK);
		
	}
}
