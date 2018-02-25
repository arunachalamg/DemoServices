package com.gpc.api.framework.conn;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "http-connection")
public class HttpServiceProps {

	@Valid
	@NotNull
	private int  connpoolTimeout;
	private int  connpoolReadTimeout;
	private int  connpoolSocketTimeout;
	private int  connpoolMaxConnection;
	private int  connpoolMaxConnectionPerRoute;
	private int  connpoolKeepAliveInactive;
	
	public int getConnpoolTimeout() {
		return connpoolTimeout;
	}
	public void setConnpoolTimeout(int connpoolTimeout) {
		this.connpoolTimeout = connpoolTimeout;
	}
	public int getConnpoolReadTimeout() {
		return connpoolReadTimeout;
	}
	public void setConnpoolReadTimeout(int connpoolReadTimeout) {
		this.connpoolReadTimeout = connpoolReadTimeout;
	}
	public int getConnpoolSocketTimeout() {
		return connpoolSocketTimeout;
	}
	public void setConnpoolSocketTimeout(int connpoolSocketTimeout) {
		this.connpoolSocketTimeout = connpoolSocketTimeout;
	}
	public int getConnpoolMaxConnection() {
		return connpoolMaxConnection;
	}
	public void setConnpoolMaxConnection(int connpoolMaxConnection) {
		this.connpoolMaxConnection = connpoolMaxConnection;
	}
	public int getConnpoolMaxConnectionPerRoute() {
		return connpoolMaxConnectionPerRoute;
	}
	public void setConnpoolMaxConnectionPerRoute(int connpoolMaxConnectionPerRoute) {
		this.connpoolMaxConnectionPerRoute = connpoolMaxConnectionPerRoute;
	}
	public int getConnpoolKeepAliveInactive() {
		return connpoolKeepAliveInactive;
	}
	public void setConnpoolKeepAliveInactive(int connpoolKeepAliveInactive) {
		this.connpoolKeepAliveInactive = connpoolKeepAliveInactive;
	}
		
}
