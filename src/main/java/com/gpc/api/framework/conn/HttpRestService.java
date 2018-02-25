package com.gpc.api.framework.conn;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * 
 * @author Arunachalam Govindasamy
 *
 */
@Service
public class HttpRestService {

	private static final Logger LOGGER = Logger.getLogger(HttpRestService.class);
	
	@Autowired
    HttpRestClientManager httpRestClientManager;
	
	
	 @SuppressWarnings({ "rawtypes", "unchecked" })
	public  HttpEntity<String> executeGETRequest(String url,HttpHeaders headers) {
		 HttpEntity<?> entity = null;
		
		 try {
			 entity = new HttpEntity(headers);
			 HttpEntity<String> response = httpRestClientManager.getRestTemplate().exchange(url, HttpMethod.GET, entity, String.class);
			return response;
	        } catch (HttpClientErrorException httpExp) {
	            LOGGER.error("API Request failed. Message - " + httpExp.getMessage() + ". URL - " + url);
	            return new ResponseEntity<String>(httpExp.getResponseBodyAsString(), httpExp.getStatusCode());
	        }  catch (HttpServerErrorException httpSrvExp) {
	            LOGGER.error("API Request failed: url:" + url, httpSrvExp);
	            return new ResponseEntity<String>(httpSrvExp.getResponseBodyAsString(), httpSrvExp.getStatusCode());
	        } catch (ResourceAccessException rax) {
	        	LOGGER.error("API Request failed. Message - " + rax.getMessage() + ". URL - " + url);
	            return new ResponseEntity<String>(rax.getMessage(), HttpStatus.NOT_FOUND);
	        }
	 }
	/**
	 * execute the get request
	 * @param url
	 * @return
	 */
    public ResponseEntity<String> executeGETRequest(String url) {
    	ResponseEntity<String> responseEntity = new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        try {
            responseEntity = httpRestClientManager.getRestTemplate().getForEntity(url, String.class);
            return responseEntity;
        } catch (HttpClientErrorException httpExp) {
            LOGGER.error("API Request failed. Message - " + httpExp.getMessage() + ". URL - " + url);
            return new ResponseEntity<String>(httpExp.getResponseBodyAsString(), httpExp.getStatusCode());
        }  catch (HttpServerErrorException httpSrvExp) {
            LOGGER.error("API Request failed: url:" + url, httpSrvExp);
            return new ResponseEntity<String>(httpSrvExp.getResponseBodyAsString(), httpSrvExp.getStatusCode());
        } catch (ResourceAccessException rax) {
        	LOGGER.error("API Request failed. Message - " + rax.getMessage() + ". URL - " + url);
            return new ResponseEntity<String>(rax.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
