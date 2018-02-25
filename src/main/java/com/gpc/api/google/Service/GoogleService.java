package com.gpc.api.google.Service;


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpc.api.framework.conn.HttpServiceConn;
import com.gpc.api.framework.conn.HttpServiceResponse;
import com.gpc.api.framework.exception.ServiceException;
import static com.gpc.api.google.GoogleServiceConstants.*;

/**
 * Google related Services 
 * 
 * @author Arunachalam Govindasamy
 *
 */
@Service
public class GoogleService{
	private static Logger log = Logger.getLogger(GoogleService.class.getName());
	
	private String zone = null;
	private String region = null;
	
	@Autowired
	HttpServiceConn httpServiceConn;
	
	/**
	 * Initialize the google esrvices values.
	 * @throws ApiException
	 */
	private GoogleService(){
		
	}
	
	/**
	 * Returns Zone where application deployed is.
	 * @return
	 * @throws ApiException
	 */
	public String getInstanceZone() {
		if(null == zone) {
			executeMetadataZoneService();
		}
		return zone;
	}
	
	/**
	 * Returns Zone where application deployed is.
	 * @return
	 * @throws ApiException
	 */
	public String getInstanceRegion() {
		if(null == region) {
			executeMetadataZoneService();
		}
		return region;
	}
	
	/**
	 * Execute the google Metadata zone service.
	 * 
	 * @return
	 * @throws ApiException
	 */
	public void executeMetadataZoneService(){
		Map<String,Object> headers = null;
		HttpServiceResponse httpServiceResponse = null;
		String response = null;
		 
		try {
			headers = new HashMap<String,Object>();
			headers.put(GCP_METADATA_FLAVOR, GCP_METADATA_FLAVOR_VALUE);
			httpServiceResponse = httpServiceConn.executeGetServiceRequestWithStatus(GCP_METADATA_ZONE_API,true,headers);
			
			if(httpServiceResponse.getStatusCode()==200){// only process is status code is 200. all other case do not process the responses.
				response = httpServiceResponse.getContent();
				if(null != response) {
					zone = response.substring(response.lastIndexOf("/") + 1, response.length());
					log.warn("executeMetadataZoneService : Zone =" +zone);
					if(null!=zone && zone.indexOf("-") !=-1) {
						region = zone.substring(0,zone.lastIndexOf("-"));
						log.warn("executeMetadataZoneService : Region =" +region);
					}
				}
			}
		}catch(ServiceException e) {
			log.error("getInstanceZone : Error while calling oogle Metadata service");
		}
	}
	
}
