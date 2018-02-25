
package com.gpc.api.framework.conn;

import java.util.Map;

/**
 * This class would help to set the Http Service Connection related attributes by client.
 * 
 * @author Arunachalam G
 *
 */
public class HttpConnectionInfo {

	private String hostname;
	private int port;
	private boolean secure;
	private int timeout;
	private int readTimeout;
	private boolean uesProxy = false;
	private String proxyHostName;
	private int proxyPort;
	private Map<String,Object> headers = null;
	
	public HttpConnectionInfo(){
	}
	
	public HttpConnectionInfo(String hostname,int port){
		this(hostname,port,0);
	}
			
	public HttpConnectionInfo(String hostname, int port, int timeout) {
		this(hostname, port, timeout, 0);
	}
	public HttpConnectionInfo(String hostname, int port, int timeout,int readTimeout) {
		this(hostname, port, timeout, readTimeout,false);
	}
	
	public HttpConnectionInfo(String hostname, int port, int timeout,int readTimeout,boolean secure) {
		this(hostname, port, timeout, readTimeout,secure,false,null,0);
	}
	
	public HttpConnectionInfo(String hostname, int port, boolean useProxy,String proxyHostName,int proxyPort) {
		this(hostname, port,0,0,false,useProxy,proxyHostName,proxyPort);
	}
	
	public HttpConnectionInfo(String hostname, int port, int timeout,int readTimeout,boolean secure,boolean useProxy,String proxyHostName,int proxyPort) {
		this.hostname=hostname;
		this.port=port;
		this.timeout=timeout;
		this.readTimeout=readTimeout;
		this.secure=secure;
		this.uesProxy=useProxy;
		this.proxyHostName=proxyHostName;
		this.proxyPort=proxyPort;
	}
	
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public boolean isSecure() {
		return secure;
	}
	public void setSecure(boolean secure) {
		this.secure = secure;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public int getReadTimeout() {
		return readTimeout;
	}
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
	public boolean isUesProxy() {
		return uesProxy;
	}
	public void setUesProxy(boolean uesProxy) {
		this.uesProxy = uesProxy;
	}
	public String getProxyHostName() {
		return proxyHostName;
	}
	public void setProxyHostName(String proxyHostName) {
		this.proxyHostName = proxyHostName;
	}
	public int getProxyPort() {
		return proxyPort;
	}
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}
		
}
