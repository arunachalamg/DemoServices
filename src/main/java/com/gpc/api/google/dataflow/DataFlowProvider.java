package com.gpc.api.google.dataflow;

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.log4j.Logger;


/**
 * Data flow examples
 * 
 * @author Arunachalam Govindasamy
 *
 */

public class DataFlowProvider {
	private static Logger log = Logger.getLogger(DataFlowProvider.class.getName());

	public static void main(String as[]) {
		DataflowPipelineOptions options = PipelineOptionsFactory.fromArgs(as).withValidation().as(DataflowPipelineOptions.class);
										
	}
	
}
