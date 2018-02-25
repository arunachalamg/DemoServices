package com.gpc.api.framework.conn;

import org.apache.http.HttpHost;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class HttpConnPoolState {
		
	private HttpHost targetHost;

	private Integer allowedMaxConnections;

	private Integer available;

	private Integer leased;

	private Integer pending;

	
	public HttpHost getTargetHost() {
		return targetHost;
	}

	public void setTargetHost(HttpHost targetHost) {
		this.targetHost = targetHost;
	}

	public Integer getAllowedMaxConnections() {
		return allowedMaxConnections;
	}

	public void setAllowedMaxConnections(Integer allowedMaxConnections) {
		this.allowedMaxConnections = allowedMaxConnections;
	}

	public Integer getAvailable() {
		return available;
	}

	public void setAvailable(Integer available) {
		this.available = available;
	}

	public Integer getLeased() {
		return leased;
	}

	public void setLeased(Integer leased) {
		this.leased = leased;
	}

	public Integer getPending() {
		return pending;
	}

	public void setPending(Integer pending) {
		this.pending = pending;
	}

}
