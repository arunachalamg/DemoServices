package com.gpc.api.framework.conn;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cassandra")
public class CassandraConnProps {
	
	@NotNull
    @Valid
    private String hosts;
    @NotNull
    @Valid
    private String port;
    private String username;

    private String password;
    
    private String connectionpool;

    private String consistencyLevel;

    private String useTokenAware;
    
    private String maxconn;
    private String connecttimeout;
    private String maxreqperconn;
    

	public String getHosts() {
		return hosts;
	}

	public void setHosts(String hosts) {
		this.hosts = hosts;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	
	public String getConnectionpool() {
		return connectionpool;
	}

	public void setConnectionpool(String connectionpool) {
		this.connectionpool = connectionpool;
	}

	public String getConsistencyLevel() {
		return consistencyLevel;
	}

	public void setConsistencyLevel(String consistencyLevel) {
		this.consistencyLevel = consistencyLevel;
	}

	public String getUseTokenAware() {
		return useTokenAware;
	}

	public void setUseTokenAware(String useTokenAware) {
		this.useTokenAware = useTokenAware;
	}

	public String getMaxconn() {
		return maxconn;
	}

	public void setMaxconn(String maxconn) {
		this.maxconn = maxconn;
	}

	public String getConnecttimeout() {
		return connecttimeout;
	}

	public void setConnecttimeout(String connecttimeout) {
		this.connecttimeout = connecttimeout;
	}

	public String getMaxreqperconn() {
		return maxreqperconn;
	}

	public void setMaxreqperconn(String maxreqperconn) {
		this.maxreqperconn = maxreqperconn;
	}    
}
