package com.gpc.api.framework.conn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.http.client.config.RequestConfig.Builder;

import com.gpc.api.framework.exception.ServiceException;

/**
 * 
 * @author Arunachalam Govindasamy
 *
 */
@Service
public class HttpServiceConn {

	private static Logger log = Logger.getLogger(HttpServiceConn.class.getName());
	private PoolingHttpClientConnectionManager connectionManager;
	private CloseableHttpClient httpClient;
	private RequestConfig requestConfig;
	
	
	@Autowired
	HttpServiceProps httpServiceProps;
	
	/**
	 * This would help to hold a single instance for connection manager.
	 */
	@PostConstruct
	public void  HttpServiceConnection() {
	
		connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(httpServiceProps.getConnpoolMaxConnection());
		connectionManager.setDefaultMaxPerRoute(httpServiceProps.getConnpoolMaxConnectionPerRoute());
		connectionManager.setValidateAfterInactivity(httpServiceProps.getConnpoolKeepAliveInactive());//default is 2000ms
		/* Defines period of inactivity in milliseconds after which persistent connections must be re-validated prior to being leased to the consumer. 
		 * This check helps detect connections that have become stale (half-closed) while kept inactive in the pool.
		 */
		
		requestConfig = RequestConfig.custom().
		setSocketTimeout(httpServiceProps.getConnpoolSocketTimeout()).
		setConnectTimeout(httpServiceProps.getConnpoolReadTimeout()).
		setConnectionRequestTimeout(httpServiceProps.getConnpoolTimeout()).
		build();

		httpClient = HttpClientBuilder.create().
				setConnectionManager(connectionManager).
				setDefaultRequestConfig(requestConfig).
				setConnectionManagerShared(true).
				setRetryHandler(new HttpServiceRetryHandler()).
				build();
	
	}
		
	
	/**
	 * This will process the Http Responses and return the content as string.
	 * @param httpServiceResponse
	 * @param requestURL
	 * @param returnOnlySuccessRespose
	 * @return
	 * @throws ServiceException
	 */
	public String processServiceResponse(HttpServiceResponse httpServiceResponse,String requestURL,boolean returnOnlySuccessRespose) throws ServiceException{
		String data = null;
		try{
			if(returnOnlySuccessRespose && httpServiceResponse.getStatusCode() != HttpStatus.SC_OK){
				log.info("processServiceResponse : Http service Response was not successful, URL - ["+requestURL +"], status code "+httpServiceResponse.getStatusCode());
				//if the status is not successful then let the caller handle it, instead of throwing exception here
			}else if(httpServiceResponse.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES){//if status is >=300, throw exception
				throw new HttpResponseException(httpServiceResponse.getStatusCode(), httpServiceResponse.getReasonPhrase());
			}else{
				data = httpServiceResponse.getContent();
			}// handling various Exceptions
		}catch(HttpResponseException e){
			this.validateHttpResponseExceptions(e);
		}
		return data;
	}
	
	/**
	 * This would build the Http Get object for the Specified Urls with connection information provided by clients
	 * 
	 * @param connInfo
	 * @param requestURL
	 * @return
	 * @throws ServiceException
	 */
	private HttpGet buildHttpGet(HttpConnectionInfo connInfo,String requestURL)throws ServiceException{
		HttpGet httpGet = null;
		int readTimeout = 0;
		int connTimeout = 0;
		HttpHost httpHost = null;
		Builder reqConfigBuilder = null;
		try{
			httpGet = new HttpGet(requestURL.trim());
			readTimeout = connInfo.getReadTimeout() > 0 ? connInfo.getReadTimeout() : httpServiceProps.getConnpoolSocketTimeout();
			connTimeout = connInfo.getTimeout() > 0 ? connInfo.getTimeout():httpServiceProps.getConnpoolReadTimeout();
			reqConfigBuilder = RequestConfig.custom().setSocketTimeout(readTimeout).setConnectTimeout(connTimeout);
			// set if proxy is enabled
			if(connInfo.isUesProxy() && StringUtils.isNotBlank(connInfo.getProxyHostName()) && connInfo.getProxyPort()>0){
				httpHost = new HttpHost(connInfo.getProxyHostName(),connInfo.getProxyPort());
				reqConfigBuilder.setProxy(httpHost);
			}
			//Read headers and process
			if(null!=connInfo.getHeaders() && !connInfo.getHeaders().isEmpty()){
				for(Map.Entry<String, Object> entry:connInfo.getHeaders().entrySet()){
					httpGet.setHeader(entry.getKey(), (String)entry.getValue());
				}
			}
			
			httpGet.setConfig(reqConfigBuilder.build());
			
		}catch(Exception e){
			log.error("buildHttpGet : Exception while creating HttpGet. Error message is "+e.getMessage());
		}
		return httpGet;
	}
	
	/**
	 * This would build the Http Get object for the Specified Urls with connection information provided by clients
	 * 
	 * @param connInfo
	 * @param requestURL
	 * @return
	 * @throws ServiceException
	 */
	private HttpPost buildHttpPost(HttpConnectionInfo connInfo,String requestURL,String content, String contentType)throws ServiceException{
		HttpPost httpPost = null;
		int readTimeout = 0;
		int connTimeout = 0;
		HttpHost httpHost = null;
		StringEntity reqEntity   = null;
		Builder reqConfigBuilder = null;
		try{
			httpPost = new HttpPost(requestURL.trim());
			reqEntity = new StringEntity(content);
			httpPost.setEntity(reqEntity);
			if(null!=contentType){
				httpPost.setHeader("Accept", contentType);
				httpPost.setHeader("Content-type", contentType);
			}
			readTimeout = connInfo.getReadTimeout() > 0 ? connInfo.getReadTimeout() : httpServiceProps.getConnpoolSocketTimeout();
			connTimeout = connInfo.getTimeout() > 0 ? connInfo.getTimeout():httpServiceProps.getConnpoolReadTimeout();
			reqConfigBuilder = RequestConfig.custom().setSocketTimeout(readTimeout).setConnectTimeout(connTimeout);
			// set if proxy is enabled
			if(connInfo.isUesProxy() && StringUtils.isNotBlank(connInfo.getProxyHostName()) && connInfo.getProxyPort()>0){
				httpHost = new HttpHost(connInfo.getProxyHostName(),connInfo.getProxyPort());
				reqConfigBuilder.setProxy(httpHost);
			}
			httpPost.setConfig(reqConfigBuilder.build());
			
		}catch(Exception e){
			log.error("buildHttpPost : Exception while creating HttpPost. Error message is "+e.getMessage());
		}
		return httpPost;
	}
	
	/**
	 * This method would build the HttpHost for executing the get services.
	 * 
	 * @param connInfo
	 * @return
	 * @throws ServiceException
	 */
	private HttpHost buildHttpHost(HttpConnectionInfo connInfo) throws ServiceException{
		HttpHost httpHost = null;
		if(null!=connInfo){
			try{
				if(connInfo.isSecure()){
					httpHost = new HttpHost(connInfo.getHostname(),connInfo.getPort(),ConnectionConstants.SECURE_HTTP);
				}else{
					httpHost = new HttpHost(connInfo.getHostname(),connInfo.getPort());
				}
			}catch(IllegalArgumentException e){
				log.error("buildHttpHost :IllegalArgumentException - Got an exception while trying to validate host "+connInfo.getHostname() +", port "+connInfo.getPort()+" Error message is "+e.getMessage());                       
				throw new ServiceException(e);
			}catch(Exception e){
				log.error("buildHttpHost :Exception - Got an exception while trying to validate host "+connInfo.getHostname() +", port "+connInfo.getPort()+" Error message is "+e.getMessage());                    
				throw new ServiceException(e);
			}
		}
		return httpHost;
	}
	
	
	
	/**
	 * Handle various type exception.
	 * @param e
	 * @throws ServiceException
	 */
	private void validateHttpResponseExceptions(HttpResponseException e) throws ServiceException{
		switch (e.getStatusCode()) {
			case HttpStatus.SC_BAD_REQUEST:
				log.error(ConnectionConstants.ERR_SERVICE_BAD_REQUEST, e);
				throw new ServiceException(ConnectionConstants.ERR_SERVICE_BAD_REQUEST);
			case HttpStatus.SC_UNAUTHORIZED:
				log.error(ConnectionConstants.ERR_SERVICE_UNAUTHORIZED, e);
				throw new ServiceException(ConnectionConstants.ERR_SERVICE_UNAUTHORIZED);
			case HttpStatus.SC_NOT_FOUND:
				log.error(ConnectionConstants.ERR_SERVICE_RECORD_NOT_FOUND, e);
				throw new ServiceException(ConnectionConstants.ERR_SERVICE_RECORD_NOT_FOUND);
			case HttpStatus.SC_INTERNAL_SERVER_ERROR:
				log.error(ConnectionConstants.ERR_SERVICE_INTERNAL_SERVER, e);
				throw new ServiceException(ConnectionConstants.ERR_SERVICE_INTERNAL_SERVER);
			case HttpStatus.SC_SERVICE_UNAVAILABLE:
				log.error(ConnectionConstants.ERR_SERVICE_SERVICE_UNAVAILABLE, e);
				throw new ServiceException(ConnectionConstants.ERR_SERVICE_SERVICE_UNAVAILABLE);
			default:
				log.error(ConnectionConstants.ERR_SERVICE_HTTP_RESPONSE_CODE, e);
				//throw new ServiceException(ConnectionConstants.ERR_SERVICE_HTTP_RESPONSE_CODE);
				throw new ServiceException(ConnectionConstants.ERR_SERVICE_HTTP_RESPONSE_CODE +" = "+e.getStatusCode());
		}
		
	}
		
	/**
	 * This will convert the Http responses as String.
	 * @param entity
	 * @return
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	private String getResponseData(HttpEntity entity,String requestURL) throws UnsupportedOperationException, IOException{
		StringBuilder data = new StringBuilder();
		InputStream instream = null;
		BufferedReader	bufferedReader = null;
		String inputData;
		
		if(null==entity){
			log.warn("getResponseData : Request was successful but response is empty. URL- ["+requestURL+" ]");
			return data.toString();
		}
		
		try{
			instream = entity.getContent();
			bufferedReader= new BufferedReader(new InputStreamReader(instream,"UTF-8"));
			while ((inputData = bufferedReader.readLine()) != null) {
				data = data.append(inputData);
			}
		}finally{
			if(null!=bufferedReader){
				bufferedReader.close();
			}
			if(null!=instream){
				instream.close();	
			}
		}
		return data.toString();
	}
		
	
	
	/**
	 * This will execute the get service for any requested get Urls. 
	 * This will handle only service related exceptions thrown by httpClients and convert to Service Exceptions.
	 * This will not handle/throw HTTP Response related exceptions.This will send the original responses and status code to client and client should take of this scenario.
	 * And also client can configure with only success responses required or not by providing the returnOnlySuccessRespose flag.
	 * This would also handle execute the secure/non-secure and proxy based urls 
	 * 
	 * @param connInfo
	 * @param requestURL
	 * @param returnOnlySuccessRespose
	 * @return
	 * @throws ServiceException
	 */
	public HttpServiceResponse executeGetServiceRequestWithStatus(HttpConnectionInfo connInfo,String requestURL,int retry, 
			boolean printServiceUrl)throws ServiceException{
		String data    = null;
		long startTime = 0l;
		long endTime   = 0l;
		HttpHost httpHost = null;
		HttpGet httGet    = null;
		HttpEntity entity = null;
		StatusLine statusLine = null;
		CloseableHttpResponse response = null;
		HttpServiceResponse httpServiceResponse = null;
		
		if(StringUtils.isNotBlank(requestURL) &&
				!StringUtils.equals("null",requestURL)){
			try{
				startTime = System.currentTimeMillis();
				httpHost = this.buildHttpHost(connInfo);
				httGet = this.buildHttpGet(connInfo,requestURL);
				
				try{
				//try(CloseableHttpResponse response = httpClient.execute(httpHost,httGet)){
					response = httpClient.execute(httpHost,httGet,this.getHttpContext(retry));
					/*if(retry>0){
						response = httpClient.execute(httpHost,httGet,this.getHttpContext(retry));
					}else{
						response = httpClient.execute(httpHost,httGet);
					}*/
					statusLine= response.getStatusLine();
					httpServiceResponse = new HttpServiceResponse();
					entity = response.getEntity();
					data = this.getResponseData(entity,requestURL);
					
					httpServiceResponse.setContent(data);
					httpServiceResponse.setStatusCode(statusLine.getStatusCode());
					httpServiceResponse.setReasonPhrase(statusLine.getReasonPhrase());
					if(statusLine.getStatusCode()!=HttpStatus.SC_OK){
						log.info("executeGetServiceRequestWithStatus : Request URL - ["+requestURL+"], status =["+statusLine.getStatusCode() +"]");
					}
				}catch(ConnectionPoolTimeoutException e){
					log.error( ConnectionConstants.ERR_SERVICE_CONNECTION_REQUEST_TIMEOUT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_CONNECTION_REQUEST_TIMEOUT);
				}catch(ConnectTimeoutException e){
					log.error(ConnectionConstants.ERR_SERVICE_CONNECT_TIMEOUT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_CONNECT_TIMEOUT);
				}catch(HttpHostConnectException e){
					log.error(ConnectionConstants.ERR_SERVICE_HTTP_HOST_CONNECT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_HTTP_HOST_CONNECT);
				}catch(SocketTimeoutException e){
					log.error(ConnectionConstants.ERR_SERVICE_SOCKET_TIMEOUT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_SOCKET_TIMEOUT);
				}catch(ClientProtocolException e){
					log.error(ConnectionConstants.ERR_ERVICE_CLIENT_PROTOCOL, e);
					throw new ServiceException(ConnectionConstants.ERR_ERVICE_CLIENT_PROTOCOL);
				}catch(UnsupportedOperationException e){
					log.error(ConnectionConstants.ERR_SERVICE_UNSUPORRTEDOPERATION, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_UNSUPORRTEDOPERATION);
				}catch(IOException e){
					log.error(ConnectionConstants.ERR_SERVICE_IO, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_IO);
				}
				
			}catch(ServiceException e) {
				throw e;
			}
			catch(Exception e){
				log.error("executeGetServiceRequestWithStatus : Exception while trying to get the request to "+requestURL, e);                       
				throw new ServiceException(ConnectionConstants.ERR_SERVICE);
			}finally{
				if(null!=response){
					try {
						response.close();
					} catch (IOException e) {
						log.error("executeGetServiceRequestWithStatus : Exception while trying to close the response stream to "+requestURL, e); 
					}
				}
				endTime = System.currentTimeMillis();
				if(printServiceUrl){
					log.warn("PRINT GET Service URL - ["+connInfo.getHostname()+":"+connInfo.getPort()+requestURL+"]. Time taken for the service - ["+(endTime - startTime)+"] ms.");
				}else{
					log.debug("executeGetServiceRequestWithStatus : URL - ["+connInfo.getHostname()+":"+connInfo.getPort()+requestURL+"]. Time taken for the service - ["+(endTime - startTime)+"] ms.");
				}
			}
		}else{
			log.warn("executeGetServiceRequestWithStatus : Exception - Service URL is = "+requestURL);
		}
		return httpServiceResponse;
	}
	
	/**
	 * This will Execute the Service based on the request Url. Request URL should be absolute, otherwise throws exception.
	 * @param requestURL
	 * @param returnOnlySuccessRespose
	 * @return
	 * @throws ServiceException
	 */
	public HttpServiceResponse executeGetServiceRequestWithStatus(String requestURL,boolean printServiceUrl,Map<String,Object> headers)throws ServiceException{
		return this.executeGetServiceRequestWithStatus(requestURL, this.getRetryCount(), printServiceUrl, headers);
	}
	
	/**
	 * This will Execute the Service based on the request Url. Request URL should be absolute, otherwise throws exception.
	 * @param requestURL
	 * @param returnOnlySuccessRespose
	 * @return
	 * @throws ServiceException
	 */
	public HttpServiceResponse executeGetServiceRequestWithStatus(String requestURL,int retry,boolean printServiceUrl,Map<String,Object> headers)throws ServiceException{
		String data    = null;
		long startTime = 0l;
		long endTime   = 0l;
		HttpEntity entity = null;
		StatusLine statusLine = null;
		CloseableHttpResponse response = null;
		HttpServiceResponse httpServiceResponse = null;
		
		try{
			startTime = System.currentTimeMillis();
			if(StringUtils.isNotBlank(requestURL)){
				//try(CloseableHttpResponse response = httpClient.execute(httpHost,httGet)){
				try{
					HttpGet httpGet = new HttpGet(requestURL);
					//Read headers and process
					if(null!=headers && !headers.isEmpty()){
						for(Map.Entry<String, Object> entry:headers.entrySet()){
							httpGet.setHeader(entry.getKey(), (String)entry.getValue());
						}
					}
					response = httpClient.execute(httpGet,this.getHttpContext(retry));
					statusLine= response.getStatusLine();
					httpServiceResponse = new HttpServiceResponse();
					entity = response.getEntity();
					data = this.getResponseData(entity,requestURL);
					
					httpServiceResponse.setContent(data);
					httpServiceResponse.setStatusCode(statusLine.getStatusCode());
					httpServiceResponse.setReasonPhrase(statusLine.getReasonPhrase());
					
					if(statusLine.getStatusCode()!=HttpStatus.SC_OK){
						log.info("executeGetServiceRequestWithStatus : Request URL - ["+requestURL+"], status =["+statusLine.getStatusCode() +"]");
					}
				}catch(ConnectionPoolTimeoutException e){
					log.error( ConnectionConstants.ERR_SERVICE_CONNECTION_REQUEST_TIMEOUT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_CONNECTION_REQUEST_TIMEOUT);
				}catch(ConnectTimeoutException e){
					log.error(ConnectionConstants.ERR_SERVICE_CONNECT_TIMEOUT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_CONNECT_TIMEOUT);
				}catch(HttpHostConnectException e){
					log.error(ConnectionConstants.ERR_SERVICE_HTTP_HOST_CONNECT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_HTTP_HOST_CONNECT);
				}catch(SocketTimeoutException e){
					log.error(ConnectionConstants.ERR_SERVICE_SOCKET_TIMEOUT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_SOCKET_TIMEOUT);
				}catch(ClientProtocolException e){
					log.error(ConnectionConstants.ERR_ERVICE_CLIENT_PROTOCOL, e);
					throw new ServiceException(ConnectionConstants.ERR_ERVICE_CLIENT_PROTOCOL);
				}catch(UnsupportedOperationException e){
					log.error(ConnectionConstants.ERR_SERVICE_UNSUPORRTEDOPERATION, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_UNSUPORRTEDOPERATION);
				}catch(IOException e){
					log.error(ConnectionConstants.ERR_SERVICE_IO, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_IO);
				}
			}else{
				log.warn("executeGetServiceRequestWithStatus : Exception - Service URL is = "+requestURL);
			}
		}catch(ServiceException e) {
			throw e;
		}catch(Exception e){
			log.error("executeGetServiceRequestWithStatus : Exception while trying to get the request to "+requestURL, e);                       
			throw new ServiceException(ConnectionConstants.ERR_SERVICE);
		}finally{
			if(null!=response){
				try {
					response.close();
				} catch (IOException e) {
					log.error("executeGetServiceRequestWithStatus : Exception while trying to close the response stream to "+requestURL, e); 
				}
			}
			endTime = System.currentTimeMillis();
			if(printServiceUrl){
				log.warn(" PRINT GET Service URL - ["+requestURL+"]. Time taken for the service - ["+(endTime - startTime)+"] ms.");
			}else{
				log.debug("executeGetServiceRequestWithStatus : URL - ["+requestURL+"]. Time taken for the service - ["+(endTime - startTime)+"] ms.");
			}
		}
		return httpServiceResponse;
	}
	
	
	/**
	 * This will execute the get service for any requested get Urls. 
	 * This would handle all kind of exception thrown by httpClients and convert to Service Exceptions.
	 * And also client can configure with only success responses required or not by providing the returnOnlySuccessRespose flag.
	 * This would also handle execute the secure/non-secure and proxy based urls 
	 * 
	 * @param connInfo
	 * @param requestURL
	 * @param returnOnlySuccessRespose
	 * @return
	 * @throws ServiceException
	 */
	public HttpServiceResponse executePostServiceRequestWithStatus(HttpConnectionInfo connInfo,String requestURL,String content, 
			String contentType,int retry,boolean printServiceUrl)throws ServiceException{
		String data    = null;
		long startTime = 0l;
		long endTime   = 0l;
		HttpHost httpHost = null;
		HttpPost httPost  = null;
		HttpEntity entity = null;
		StatusLine statusLine = null;
		CloseableHttpResponse response = null;
		HttpServiceResponse httpServiceResponse = null;
		
		if(StringUtils.isNotBlank(requestURL) 
				&& !StringUtils.equals("null",requestURL) 
				&& null!=content){
				
			try{
				startTime = System.currentTimeMillis();
				
				httpHost = this.buildHttpHost(connInfo);
				httPost = this.buildHttpPost(connInfo,requestURL,content,contentType);
				
				try{
				//try(CloseableHttpResponse response = httpClient.execute(httpHost,httPost)){
					response = httpClient.execute(httpHost,httPost,this.getHttpContext(retry));
					statusLine = response.getStatusLine();
					httpServiceResponse = new HttpServiceResponse();
					entity = response.getEntity();
					data = this.getResponseData(entity,requestURL);
					
					httpServiceResponse.setContent(data);
					httpServiceResponse.setStatusCode(statusLine.getStatusCode());
					httpServiceResponse.setReasonPhrase(statusLine.getReasonPhrase());
					
					if(statusLine.getStatusCode()!=HttpStatus.SC_OK){
						log.info("executePostServiceRequestWithStatus : Request URL - ["+requestURL+"], status =["+statusLine.getStatusCode() +"]");
					}
					
				}catch(ConnectionPoolTimeoutException e){
					log.error(ConnectionConstants.ERR_SERVICE_CONNECTION_REQUEST_TIMEOUT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_CONNECTION_REQUEST_TIMEOUT);
				}catch(ConnectTimeoutException e){
					log.error(ConnectionConstants.ERR_SERVICE_CONNECT_TIMEOUT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_CONNECT_TIMEOUT);
				}catch(HttpHostConnectException e){
					log.error(ConnectionConstants.ERR_SERVICE_HTTP_HOST_CONNECT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_HTTP_HOST_CONNECT);
				}catch(SocketTimeoutException e){
					log.error(ConnectionConstants.ERR_SERVICE_SOCKET_TIMEOUT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_SOCKET_TIMEOUT);
				}catch(ClientProtocolException e){
					log.error(ConnectionConstants.ERR_ERVICE_CLIENT_PROTOCOL, e);
					throw new ServiceException(ConnectionConstants.ERR_ERVICE_CLIENT_PROTOCOL);
				}catch(UnsupportedOperationException e){
					log.error(ConnectionConstants.ERR_SERVICE_UNSUPORRTEDOPERATION, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_UNSUPORRTEDOPERATION);
				}catch(IOException e){
					log.error(ConnectionConstants.ERR_SERVICE_IO, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_IO);
				}
				
			}catch(ServiceException e) {
				throw e;
			}
			catch(Exception e){
				log.error("executePostServiceRequestWithStatus : Exception while trying to get the request to "+requestURL, e);                       
				throw new ServiceException(ConnectionConstants.ERR_SERVICE);
			}finally{
				if(null!=response){
					try {
						response.close();
					} catch (IOException e) {
						log.error("executePostServiceRequestWithStatus : Exception while trying to close the response stream to "+requestURL, e); 
					}
				}
				endTime = System.currentTimeMillis();
				if(printServiceUrl){
					log.info("PRINT POST Service URL - ["+connInfo.getHostname()+":"+connInfo.getPort()+requestURL+"]. Time taken for the service - ["+(endTime - startTime)+"] ms.");
				}else{
					log.debug("executePostServiceRequestWithStatus : URL - ["+connInfo.getHostname()+":"+connInfo.getPort()+requestURL+"]. Time taken for the service - ["+(endTime - startTime)+"] ms.");
				}
			}
		}else{
			log.warn("executePostServiceRequestWithStatus : Exception - Service URL is = ["+requestURL+"]   Content size = "+(content==null?"null":content.length()));
		}
		return httpServiceResponse;
	}
	
	
	/**
	 * This will execute the get service for any requested get Urls. 
	 * This would handle all kind of exception thrown by httpClients and convert to Service Exceptions.
	 * And also client can configure with only success responses required or not by providing the returnOnlySuccessRespose flag.
	 * This would also handle execute the secure/non-secure and proxy based urls 
	 * 
	 * @param connInfo
	 * @param requestURL
	 * @param returnOnlySuccessRespose
	 * @return
	 * @throws ServiceException
	 */
	public HttpServiceResponse executePostServiceRequestWithStatus(String requestURL,String content, String contentType,int retry,boolean printServiceUrl)throws ServiceException{
		String data    = null;
		long startTime = 0l;
		long endTime   = 0l;
		HttpPost httPost  = null; 
		StringEntity reqEntity = null;
		HttpEntity entity = null;
		CloseableHttpResponse response = null;
		HttpServiceResponse httpServiceResponse = null;
		
		if(StringUtils.isNotBlank(requestURL) 
				&& !StringUtils.equals("null",requestURL) 
				&& null!=content){
				
			try{
				startTime = System.currentTimeMillis();
				HttpPost httpPost = new HttpPost(requestURL.trim());
				reqEntity = new StringEntity(content);
				httpPost.setEntity(reqEntity);
				if(null!=contentType){
					httpPost.setHeader("Accept", contentType);
					httpPost.setHeader("Content-type", contentType);
				}
				
				try{
				//try(CloseableHttpResponse response = httpClient.execute(httPost)){
					response = httpClient.execute(httPost, this.getHttpContext(retry));
					StatusLine statusLine= response.getStatusLine();
					httpServiceResponse = new HttpServiceResponse();
					entity = response.getEntity();
					data = this.getResponseData(entity,requestURL);
					
					httpServiceResponse.setContent(data);
					httpServiceResponse.setStatusCode(statusLine.getStatusCode());
					httpServiceResponse.setReasonPhrase(statusLine.getReasonPhrase());
					
					if(statusLine.getStatusCode()!=HttpStatus.SC_OK){
						log.info("executePostServiceRequestWithStatus : Request URL - ["+requestURL+"], status =["+statusLine.getStatusCode() +"]");
					}
				}catch(ConnectionPoolTimeoutException e){
					log.error(ConnectionConstants.ERR_SERVICE_CONNECTION_REQUEST_TIMEOUT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_CONNECTION_REQUEST_TIMEOUT);
				}catch(ConnectTimeoutException e){
					log.error(ConnectionConstants.ERR_SERVICE_CONNECT_TIMEOUT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_CONNECT_TIMEOUT);
				}catch(HttpHostConnectException e){
					log.error(ConnectionConstants.ERR_SERVICE_HTTP_HOST_CONNECT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_HTTP_HOST_CONNECT);
				}catch(SocketTimeoutException e){
					log.error(ConnectionConstants.ERR_SERVICE_SOCKET_TIMEOUT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_SOCKET_TIMEOUT);
				}catch(ClientProtocolException e){
					log.error(ConnectionConstants.ERR_ERVICE_CLIENT_PROTOCOL, e);
					throw new ServiceException(ConnectionConstants.ERR_ERVICE_CLIENT_PROTOCOL);
				}catch(UnsupportedOperationException e){
					log.error(ConnectionConstants.ERR_SERVICE_UNSUPORRTEDOPERATION, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_UNSUPORRTEDOPERATION);
				}catch(IOException e){
					log.error(ConnectionConstants.ERR_SERVICE_IO, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_IO);
				}
				
			}catch(ServiceException e) {
				throw e;
			}catch(Exception e){
				log.error("executePostServiceRequestWithStatus : Exception while trying to get the request to "+requestURL, e);                       
				throw new ServiceException(ConnectionConstants.ERR_SERVICE);
			}finally{
				if(null!=response){
					try {
						response.close();
					} catch (IOException e) {
						log.error("executePostServiceRequestWithStatus : Exception while trying to close the response stream to "+requestURL, e); 
					}
				}
				endTime = System.currentTimeMillis();
				if(printServiceUrl){
					log.warn("PRINT POST Service URL - ["+requestURL+"]. Time taken for the service - ["+(endTime - startTime)+"] ms.");
				}else{
					log.debug("executePostServiceRequestWithStatus : URL - ["+requestURL+"]. Time taken for the service - ["+(endTime - startTime)+"] ms.");
				}
			}
		}else{
			log.warn("executePostServiceRequestWithStatus : Exception - Service URL is = ["+requestURL+"]   Content size = "+(content==null?"null":content.length()));
		}
		return httpServiceResponse;
	}

	
	/**
	 * This will execute the get service for any requested get Urls. 
	 * This would handle all kind of exception thrown by httpClients and convert to Service Exceptions.
	 * And also client can configure with only success responses required or not by providing the returnOnlySuccessRespose flag.
	 * This would also handle execute the secure/non-secure and proxy based urls 
	 * 
	 * @param connInfo
	 * @param requestURL
	 * @param returnOnlySuccessRespose
	 * @return
	 * @throws ServiceException
	 */
	public HttpServiceResponse executePutServiceRequestWithStatus(HttpConnectionInfo connInfo,String requestURL,String content, 
			String contentType,int retry,boolean printServiceUrl)throws ServiceException{
		String data    = null;
		long startTime = 0l;
		long endTime   = 0l;
		HttpHost httpHost = null;
		HttpPut httPut  = null;
		HttpEntity entity = null;
		CloseableHttpResponse response = null;
		HttpServiceResponse httpServiceResponse = null;
		StatusLine statusLine = null;
		
		if(StringUtils.isNotBlank(requestURL) 
				&& !StringUtils.equals("null",requestURL) 
				&& null!=content){
				
			try{
				startTime = System.currentTimeMillis();
				httpHost = this.buildHttpHost(connInfo);
				httPut = this.buildHttpPut(connInfo,requestURL,content,contentType);
				
				try{
				//try(CloseableHttpResponse response = httpClient.execute(httpHost,httPost)){
					response = httpClient.execute(httpHost,httPut,this.getHttpContext(retry));
					statusLine = response.getStatusLine();
					httpServiceResponse = new HttpServiceResponse();
					entity = response.getEntity();
					data = this.getResponseData(entity,requestURL);
					
					httpServiceResponse.setContent(data);
					httpServiceResponse.setStatusCode(statusLine.getStatusCode());
					httpServiceResponse.setReasonPhrase(statusLine.getReasonPhrase());
					
					/*if(statusLine.getStatusCode()!=HttpStatus.SC_OK){
						log.info("executePutServiceRequestWithStatus : Request URL - ["+requestURL+"], status =["+statusLine.getStatusCode() +"]");
					}*/
					
				}catch(ConnectionPoolTimeoutException e){
					log.error(ConnectionConstants.ERR_SERVICE_CONNECTION_REQUEST_TIMEOUT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_CONNECTION_REQUEST_TIMEOUT);
				}catch(ConnectTimeoutException e){
					log.error(ConnectionConstants.ERR_SERVICE_CONNECT_TIMEOUT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_CONNECT_TIMEOUT);
				}catch(HttpHostConnectException e){
					log.error(ConnectionConstants.ERR_SERVICE_HTTP_HOST_CONNECT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_HTTP_HOST_CONNECT);
				}catch(SocketTimeoutException e){
					log.error(ConnectionConstants.ERR_SERVICE_SOCKET_TIMEOUT, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_SOCKET_TIMEOUT);
				}catch(ClientProtocolException e){
					log.error(ConnectionConstants.ERR_ERVICE_CLIENT_PROTOCOL, e);
					throw new ServiceException(ConnectionConstants.ERR_ERVICE_CLIENT_PROTOCOL);
				}catch(UnsupportedOperationException e){
					log.error(ConnectionConstants.ERR_SERVICE_UNSUPORRTEDOPERATION, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_UNSUPORRTEDOPERATION);
				}catch(IOException e){
					log.error(ConnectionConstants.ERR_SERVICE_IO, e);
					throw new ServiceException(ConnectionConstants.ERR_SERVICE_IO);
				}
				
			}catch(ServiceException e) {
				throw e;
			}catch(Exception e){
				log.error("executePutServiceRequestWithStatus : Exception while trying to get the request to "+requestURL, e);                       
				throw new ServiceException(ConnectionConstants.ERR_SERVICE);
			}finally{
				if(null!=response){
					try {
						response.close();
					} catch (IOException e) {
						log.error("executePutServiceRequestWithStatus : Exception while trying to close the response stream to "+requestURL, e); 
					}
				}
				endTime = System.currentTimeMillis();
				if(printServiceUrl){
					log.warn("PRINT POST Service URL - ["+connInfo.getHostname()+":"+connInfo.getPort()+requestURL+"]. Time taken for the service - ["+(endTime - startTime)+"] ms.");
				}
			}
		}else{
			log.warn("executePutServiceRequestWithStatus : Exception - Service URL is = ["+requestURL+"]   Content size = "+(content==null?"null":content.length()));
		}
		return httpServiceResponse;
	}
	
	
	/**
	 * This would build the Http put object for the Specified Urls with connection information provided by clients
	 * 
	 * @param connInfo
	 * @param requestURL
	 * @return
	 * @throws ServiceException
	 */
	private HttpPut buildHttpPut(HttpConnectionInfo connInfo,String requestURL,String content, String contentType)throws ServiceException{
		HttpPut httpPut = null;
		int readTimeout = 0;
		int connTimeout = 0;
		HttpHost httpHost = null;
		StringEntity reqEntity   = null;
		Builder reqConfigBuilder = null;
		try{
			httpPut = new HttpPut(requestURL.trim());
			reqEntity = new StringEntity(content);
			httpPut.setEntity(reqEntity);
			if(null!=contentType){
				httpPut.setHeader("Accept", contentType);
				httpPut.setHeader("Content-type", contentType);
			}
			readTimeout = connInfo.getReadTimeout() > 0 ? connInfo.getReadTimeout() : httpServiceProps.getConnpoolSocketTimeout();
			connTimeout = connInfo.getTimeout() > 0 ? connInfo.getTimeout():httpServiceProps.getConnpoolReadTimeout();
			reqConfigBuilder = RequestConfig.custom().setSocketTimeout(readTimeout).setConnectTimeout(connTimeout);
			// set if proxy is enabled
			if(connInfo.isUesProxy() && StringUtils.isNotBlank(connInfo.getProxyHostName()) && connInfo.getProxyPort()>0){
				httpHost = new HttpHost(connInfo.getProxyHostName(),connInfo.getProxyPort());
				reqConfigBuilder.setProxy(httpHost);
			}
			httpPut.setConfig(reqConfigBuilder.build());
			
		}catch(Exception e){
			log.error("buildHttpPut : Exception while creating HttpPut. Error message is "+e.getMessage());
		}
		return httpPut;
	}
	/**
	 * Set retry count with httpContext object.
	 * @param retryCount
	 * @return
	 * @throws ServiceException
	 */
	private HttpContext getHttpContext(int retryCount) throws ServiceException{
		HttpContext ctx = null;
		ctx = HttpClientContext.create();
	    ctx.setAttribute("RETRY_COUNT", retryCount);
		return ctx;
	}
	
	/**
	 * Get ConnectionManager
	 * @return
	 */
	public PoolingHttpClientConnectionManager getConnectionManager() {
		return connectionManager;
	}

	/**
	 * getHttpClient
	 * @return
	 */
	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * getRequestConfig
	 * @return
	 */
	public RequestConfig getRequestConfig() {
		return requestConfig;
	}

	/**
	 * return default retry count.
	 * 
	 * @return
	 */
	private int getRetryCount() {
		return 3;
	}
}
