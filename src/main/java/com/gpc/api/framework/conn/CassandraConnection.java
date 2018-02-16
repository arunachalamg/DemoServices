package com.gpc.api.framework.conn;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AuthenticationException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.LoadBalancingPolicy;

/**
 * This will make connection for cassandra
 * 
 * @author Arunachalam Govindasamy
 *
 */
@Configuration
public class CassandraConnection {
	private static Logger log = Logger.getLogger(CassandraConnection.class.getName());

	@Autowired
	CassandraConnProps cassandraConnProps;
		
	private Session session;
    private Cluster cluster;
    
	/**
	 * This would help to hold a single instance for connection manager.
	 */
    @Bean
	public boolean initilizeCassandraConnection() {
		cluster = buildCluster();
		session = connectSession(cluster);
		return true;
	}
	
    
	public Cluster getCluster() {
		return cluster;
	}
    
	public Session getSession() {
		return session;
	}
	
	/**
     * This method returns a builder that builds Cluster instances with the
     * provided cluster name and other parameters set as provided values.
     */
    private Cluster buildCluster() {

        Cluster.Builder builder= null;
         try {
            int port = Integer.parseInt(cassandraConnProps.getPort());
            builder = Cluster
                    .builder()
                    .withLoadBalancingPolicy(getLoadBalancingPolicy())
                    .addContactPoints(cassandraConnProps.getHosts())
                    .withProtocolVersion(getProtocolVersion())
                    .withAuthProvider(new PlainTextAuthProvider(cassandraConnProps.getUsername(),cassandraConnProps.getPassword()))
                    .withPort(port);
            this.getBuilderOptions(builder);
            log.warn("Cassandra connection has established successfully..");
        } catch (SecurityException e) {
           e.printStackTrace();
           log.error("Permission denied by security manager. Please Validate ...");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            log.error("Could not contact nodes as none of the nodes IPs could be found...");
        }
         return builder.build();
    }
    /**
     * Creates and returns the Cassandra {@link Session}
     *
     * @param cluster the {@link Cluster}
     * @return the new {@link Session}
     */
    private Session connectSession(Cluster cluster) {
        try {
        	if(null!=cluster) {
        		return cluster.connect();
        	}
        } catch (NoHostAvailableException e) {
            e.printStackTrace();
            log.error("Cluster is not initialized or no host amongst contact points can be reached..");
        } catch (IllegalStateException e) {
            e.printStackTrace();
            log.error("Cluster is closed ...");
        } catch (AuthenticationException e) {
           e.printStackTrace();
           log.error("Authentication failed while contacting the initial contact points..");
        }
        log.error("Cluster is not initialized or no host amongst contact points can be reached..");
       return null; 
    }
    
    
    /**
     * This method sets all the builder options like pooling options, query
     * options
     *
     * @param builder       the {@link ClusterBuilder}
     * @param daoProperties the {@link DAOProperties}
     */
    private void getBuilderOptions(Cluster.Builder builder) {
        // set the query options
        builder.withQueryOptions(getQueryOptions());

        // set poolingOptions
        PoolingOptions poolingOptions = this.setPoolingOptions();
        if (poolingOptions != null) {
            builder.withPoolingOptions(poolingOptions);
        }
    }
    private QueryOptions getQueryOptions() {
        QueryOptions queryOptions = null;
        queryOptions = new QueryOptions();
        queryOptions.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        return queryOptions;
    }

   
    /**
     * This method sets the various pooling options during connection. few
     * options are 1. Max number of connection pool 2. Connection timeout in
     * milliseconds
     *
     * @param daoProperties the {@link DAOProperties}
     * @return the {@link PoolingOptions}
     */
    private PoolingOptions setPoolingOptions() {
        PoolingOptions poolingOptions = new PoolingOptions();
        try {
            
            if (!StringUtils.isEmpty(cassandraConnProps.getMaxconn())) {
                int maxConnection = Integer.parseInt(cassandraConnProps.getMaxconn());
                poolingOptions.setMaxConnectionsPerHost(HostDistance.LOCAL, maxConnection);
            }
            if (!StringUtils.isEmpty(cassandraConnProps.getConnecttimeout())) {
                int connectionTimeOut = Integer.parseInt(cassandraConnProps.getConnecttimeout());
                poolingOptions.setPoolTimeoutMillis(connectionTimeOut);
            }
            if (!StringUtils.isEmpty(cassandraConnProps.getMaxreqperconn())) {
                int maxRequests = Integer.parseInt(cassandraConnProps.getMaxreqperconn());
                poolingOptions.setMaxRequestsPerConnection(HostDistance.LOCAL, maxRequests);
            } 
        } catch (NumberFormatException ex) {
        }
        return poolingOptions;
    }
	/**
	 * Set protocol version.
	 * 
	 * @return
	 */
    private ProtocolVersion getProtocolVersion() {
    	return ProtocolVersion.V3;
	}
    
  /**
   * Set all load balancing properties.
   * @return
   */
    
    private LoadBalancingPolicy getLoadBalancingPolicy() {
        LoadBalancingPolicy policy = null;
        DCAwareRoundRobinPolicy.Builder builder = DCAwareRoundRobinPolicy.builder();
        policy = builder.build();
           return policy;
    }
     
}
