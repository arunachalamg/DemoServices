package com.gpc.api.framework.conn;

import java.nio.charset.Charset;

import javax.annotation.PostConstruct;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author Arunachalam Govindasamy
 * This is simple way of initializing connection manager
 */
@Repository
public class HttpRestClientManager {

	@Value("${http-connection.connpoolMaxConnection}")
    private int maxPoolConnections;

    @Value("${http-connection.connpoolMaxConnectionPerRoute}")
    private int maxConnectionsPerRoute;

    @Value("${http-connection.connpoolReadTimeout}")
    private int socketReadTimeout;

    @Value("${http-connection.connpoolSocketTimeout}")
    private int socketConnectTimeout;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        RequestConfig config = RequestConfig.custom()
                .setSocketTimeout(socketReadTimeout)
                .setConnectTimeout(socketConnectTimeout)
                .build();

        connectionManager.setMaxTotal(maxPoolConnections);
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
        HttpClient defaultHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).setConnectionManager(connectionManager).build();
        ClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(defaultHttpClient);

        restTemplate = new RestTemplate(clientHttpRequestFactory);
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));

    }

    public int getMaxPoolConnections() {
        return maxPoolConnections;
    }

    public void setMaxPoolConnections(int maxPoolConnections) {
        this.maxPoolConnections = maxPoolConnections;
    }

    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    public int getSocketReadTimeout() {
        return socketReadTimeout;
    }

    public void setSocketReadTimeout(int socketReadTimeout) {
        this.socketReadTimeout = socketReadTimeout;
    }

    public int getSocketConnectTimeout() {
        return socketConnectTimeout;
    }

    public void setSocketConnectTimeout(int socketConnectTimeout) {
        this.socketConnectTimeout = socketConnectTimeout;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
