package com.gpc.api.framework.conn;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.net.ssl.SSLException;

import org.apache.http.NoHttpResponseException;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

/**
 * 
 * The retry request handler that allows dynamic retry configuration that may be based on the service configured.
 * 
 * @author AXG8965
 *
 */
public class HttpServiceRetryHandler extends DefaultHttpRequestRetryHandler {
	private static Logger log = Logger.getLogger(HttpServiceRetryHandler.class.getName());

	/**
	 * This holds the dynamic try configurations
	 */
	public HttpServiceRetryHandler() {
		super(3, false, Arrays.asList(
				// Removed InterruptedIOException from the default list because that includes the socket timeout exception which needs to be retried
				UnknownHostException.class, ConnectException.class,
				SSLException.class));
	}

	/**
	 * Reset the retry Count at service level configuration. 
	 */
	@Override
	public boolean retryRequest(IOException exception, int executionCount,HttpContext context) {
        Object retryCount = context.getAttribute("RETRY_COUNT");
        if (retryCount == null || executionCount > (int) retryCount) {
            // Do not retry if not retry count set or if over max retry count
             return false;
        }
         if (exception instanceof NoHttpResponseException) {
             // Retry if the server dropped connection on us
        	 log.warn("retryRequest :: NoHttpResponseExceptions, retry Count = "+retryCount +" And Execution count = "+ executionCount);
             return true;
         }
         log.warn("retryRequest :: retry Count = "+retryCount +" And Execution count = "+ executionCount);
       return super.retryRequest(exception, executionCount, context);
	}

}