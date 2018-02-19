package com.gpc.api.google.pubsub;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.api.services.pubsub.model.AcknowledgeRequest;
import com.google.api.services.pubsub.model.PullRequest;
import com.google.api.services.pubsub.model.PullResponse;
import com.google.api.services.pubsub.model.ReceivedMessage;
import com.gpc.api.framework.conn.GoogleCloudConnection;

/**
 * Pubsub message provider
 * 
 * @author Arun G
 *
 */
@Component
public class PubSubMessageProvider {
	private static Logger log = Logger.getLogger(PubSubMessageProvider.class.getName());
		
	@Autowired
	private GoogleCloudConnection googleCloudConnection;
	
	
	/**
	 * pull request pubsub message.
	 * 
	 * @param subscriptionName
	 * @return
	 */
	public List<ReceivedMessage> pullMessagesFromSubscription(String subscriptionName){
		List<String> ackIds  = null;
		
		try {
			 PullRequest pullRequest = new PullRequest().setReturnImmediately(true).setMaxMessages(10);
	         PullResponse pullResponse = googleCloudConnection.buildPubSubClient().projects().subscriptions().pull(subscriptionName, pullRequest).execute();

			 if (!pullResponse.isEmpty()) {
				   ackIds = new ArrayList<String>(); 
	               System.out.println(decodeMessages(pullResponse.getReceivedMessages(),"UTF-8"));
	               for(ReceivedMessage rm:pullResponse.getReceivedMessages()) {
	             	   ackIds.add(rm.getAckId());
	               }
	               this.ackMessage(ackIds,subscriptionName);
	               return pullResponse.getReceivedMessages();
	            }
	        } catch (NullPointerException e) {
	           	e.printStackTrace();
	            log.error("Pubsub Message is Null "+subscriptionName);
	        } catch (IOException e) {
	        	e.printStackTrace();
	        	 log.error("Pubsub Message is IO Exception "+subscriptionName);
	        } catch (IllegalArgumentException e) {
	           log.error("Subscription error for : " + subscriptionName);
	        } catch (Exception e) {
	        	log.error("Error will connecting pubsubs..");
	        }
		return null;
	}
	
	/**
     * Decode the Messages recieved from PubSub
     *
     * @param messages
     * @param charsetName
     * @return
     */
    public List<String> decodeMessages(List<ReceivedMessage> messages, String charsetName) {
        return messages.stream().map(s -> {
            String data = null;
            try {
                data = new String(s.getMessage().decodeData(), charsetName);
            } catch (UnsupportedEncodingException e) {
                log.error("An exception occurred with processing message: "
                        + s.getMessage().getMessageId()
                        + " and exception: " + e.getMessage());
                throw new RuntimeException("Unsupported Encoding charset");
            }
            return data;
        }).collect(Collectors.toList());
    }
	
    public void ackMessage(List<String> ackIds, String subscriptionName) {
    	 if (CollectionUtils.isNotEmpty(ackIds)) {
             AcknowledgeRequest ackRequest = new AcknowledgeRequest().setAckIds(ackIds);
             try {
            	 googleCloudConnection.buildPubSubClient().projects().subscriptions().acknowledge(subscriptionName, ackRequest).execute();
            	 log.warn("Sucessfully acknowledged :"+ackIds);
             } catch (NullPointerException e) {
            	 log.error("pubsub ack issues..");
               } catch (Exception e) {
                 log.info("Ack IDS: "+ ackIds.toString());
                 log.info("Ack request: "+ ackRequest.toString());
                 log.error("IO Error while acking request", e);
             }
         }else {
        	 log.warn("No pubsub ack..");
         }

    }
}
