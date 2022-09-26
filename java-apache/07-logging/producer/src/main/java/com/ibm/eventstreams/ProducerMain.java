// Copyright 2022 IBM
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.ibm.eventstreams;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Producer is a very simple kafka producer for an Event Streams instance. It uses
 * an apache kafka producer client to send messages synchronously. The message content
 * is a string.
 */
public class ProducerMain {
	// Used to stop the producer and close the client when the user enters a ctrl-C.
    static volatile boolean shutdown = false;

	//Instantiate the logger object
	private static final Logger logger = LogManager.getLogger(ProducerMain.class);
    
    public static void main(String[] args) {
        // Register a shutdown hook to stop the producer loop with ctrl-C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("Shutting down...");
            shutdown = true;
        }));

    	final ProducerCLI cliArgs = ProducerCLI.parse(args);
    	final Properties config = ProducerConfiguration.makeConfiguration(cliArgs);

        // The kafka client is parameterized with the key and message types. We are
        // using strings in this simple example.
        KafkaProducer<String, String> producer = null;
        try {
        	// Creation of the producer may throw an exception, due for example to
        	// bad configuration.
        	producer = new KafkaProducer<>(config);
        } catch (Exception e) {
        	logger.error(String.format("Caught exception creating producer : ", e));
        	System.exit(1);
        }

     	for (int i = 0; !shutdown && i < cliArgs.messageCount; i++) {
     		final int messageNum = i;
            // Production of each message is also in a try block, in case there are any non-recoverable
            // errors in sending the message or receiving the acknowledgment from the server.
	        try {
	        	ProducerRecord<String, String> record = new ProducerRecord<String,String>(
	        			cliArgs.topicName,
	        			String.format(cliArgs.keyContent, messageNum),
	        			String.format(cliArgs.messageContent, messageNum));
	        	// This implementation uses a synchronous send. We get a Future from the send
	        	// operation, and wait for it to complete, meaning that the kafka client has
	        	// sent the message and received the acknowledgment from the kafka brokers.
	        	Future<RecordMetadata> f = producer.send(record);
	        	RecordMetadata metadata = f.get();
				logger.info(String.format("Successfully sent message %s, offset %s\n", messageNum,metadata.offset()));
	        } catch (Exception e) {
				logger.warn(String.format("Caught exception sending message", e));
	        }
     	}
     	
     	// The apache clients should be closed after use, though this is not critical
     	// for the synchronous message production used in this example.  The 10 second
     	// delay gives the client time to send the last pending message, if any.
     	producer.close(Duration.ofSeconds(10));
    }
}
