package com.gpc.api.framework.conn;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.PubsubScopes;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;

@Configuration
public class GoogleCloudConnection {
	
	private String APPLICATION_NAME = "ArunDemo";
  
    private String GOOGLE_CREDENTIAL_FILENAME;


	
	
	/**
	 * Build Compute services instance
	 * 
	 * @return
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	@Bean
	public Pubsub buildPubSubClient() throws IOException, GeneralSecurityException {
		HttpTransport transport = null;
		JsonFactory jsonFactory = null;
		GoogleCredential credential = null;
		
		transport = GoogleNetHttpTransport.newTrustedTransport();
		jsonFactory = new JacksonFactory();
		if (StringUtils.isNotEmpty(GOOGLE_CREDENTIAL_FILENAME)) {
            Resource resource = new ClassPathResource(GOOGLE_CREDENTIAL_FILENAME);
            InputStream is = resource.getInputStream();
            credential = GoogleCredential.fromStream(is, transport, jsonFactory);
		}else {
			credential = GoogleCredential.getApplicationDefault(transport, jsonFactory);
		}
		if (credential.createScopedRequired()) {
			credential = credential.createScoped(PubsubScopes.all());
		}	
		System.out.println("Pub sub Configuration..");
		return new Pubsub.Builder(transport, jsonFactory, credential).setApplicationName(APPLICATION_NAME).build();
	}
	
	/**
	 * Build Compute services instance
	 * 
	 * @return
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	@Bean
	public Compute buildComputeService() throws IOException, GeneralSecurityException {
		HttpTransport transport = null;
		JsonFactory jsonFactory = null;
		GoogleCredential credential = null;
		
		transport = GoogleNetHttpTransport.newTrustedTransport();
		jsonFactory = new JacksonFactory();
		if (StringUtils.isNotEmpty(GOOGLE_CREDENTIAL_FILENAME)) {
            Resource resource = new ClassPathResource(GOOGLE_CREDENTIAL_FILENAME);
            InputStream is = resource.getInputStream();
            credential = GoogleCredential.fromStream(is, transport, jsonFactory);
		}else {
			credential = GoogleCredential.getApplicationDefault(transport, jsonFactory);
		}
		if (credential.createScopedRequired()) {
			credential = credential.createScoped(ComputeScopes.all());
		}		
		return new Compute.Builder(transport, jsonFactory, credential).setApplicationName(APPLICATION_NAME).build();
	}
	
	/**
	 * Build Storage services instance
	 * 
	 * @return
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	@Bean
	public Storage buildStorageService() throws IOException, GeneralSecurityException {
		HttpTransport transport = null;
		JsonFactory jsonFactory = null;
		GoogleCredential credential = null;
		Collection<String> scopes = null;
		
		transport = GoogleNetHttpTransport.newTrustedTransport();
		jsonFactory = new JacksonFactory();
		if (StringUtils.isNotEmpty(GOOGLE_CREDENTIAL_FILENAME)) {
            Resource resource = new ClassPathResource(GOOGLE_CREDENTIAL_FILENAME);
            InputStream is = resource.getInputStream();
            credential = GoogleCredential.fromStream(is, transport, jsonFactory);
		}else {
			credential = GoogleCredential.getApplicationDefault(transport, jsonFactory);
		}
		if (credential.createScopedRequired()) {
			scopes = StorageScopes.all();
			credential = credential.createScoped(scopes);
		}
		return new Storage.Builder(transport, jsonFactory, credential).setApplicationName(APPLICATION_NAME).build();
	}
}
