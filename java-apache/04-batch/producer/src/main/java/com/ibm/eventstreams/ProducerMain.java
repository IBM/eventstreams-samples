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
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Producer demonstrates various ways to control the batching and asynchronous send
 * of messages using the Apache Kafka producer client.
 */
public class ProducerMain {
	// Used to stop the producer and close the client when the user enters a ctrl-C.
    static volatile boolean shutdown = false;
    
    public static void main(String[] args) {
        // Register a shutdown hook to stop the producer loop with ctrl-C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            shutdown = true;
        }));

    	final ProducerCLI cliArgs = ProducerCLI.parse(args);
    	final Properties config = ProducerConfiguration.makeConfiguration(cliArgs);
    	
    	// One way to control the batching is to set the `linger.ms` property of the client.
    	// This is the maximum time the client will wait before sending the batched messages.
    	boolean lingering = cliArgs.lingerMs > 0;
        if (lingering) {
        	config.put(ProducerConfig.LINGER_MS_CONFIG, cliArgs.lingerMs);
        }

        // The kafka client is parameterized with the key and message types. We are
        // using strings in this simple example.
        KafkaProducer<String, String> producer = null;
        try {
        	// Creation of the producer may throw an exception, due for example to
        	// bad configuration.
        	producer = new KafkaProducer<>(config);
        } catch (Exception e) {
        	System.err.printf("Caught exception creating producer: %s\n", e.toString());
        	System.exit(1);
        }

        // The number of messages that have been sent since we last resolved a future, or flushed the producer
        int futureCount = 0, flushCount = 0;
        
     	for (int i = 0; !shutdown && i < cliArgs.messageCount; i++) {
     		final int messageNum = i;
        	final ProducerRecord<String, String> record = new ProducerRecord<String,String>(
        			cliArgs.topicName,
        			String.format(cliArgs.keyContent, messageNum),
        			String.format(cliArgs.messageContent, messageNum));
        	
     		// Send the message defining a callback that will be called when the brokers respond to the send.
        	// This returns a Future, but (unlike previous producer samples) we do not always resolve the
        	// future. This allows the client to buffer the messages and send them in larger batches.
        	System.out.printf("** Sending message %d\n", messageNum);
        	Future<RecordMetadata> lastFuture = producer.send(record,
        			// The callback function's two arguments are a RecordMetadata for success, and an Exception for failure.
        			// (These are the same possible results of resolving the future, which either returns the RecordMetadata
        			// or throws an exception.)
        			(metadata, exception) -> {
        				if (exception != null) {
        					// The message send failed. An application would typically try to re-send the message.
        					System.out.printf("** Callback FAILURE for message %d: %s\n", messageNum, exception.toString());
        				} else {
        					// The message has been sent and metadata about its disposition (partition, offset, etc.) returned.
        					System.out.printf("** Callback SUCCESS for %d, offset %d\n", messageNum, metadata.offset());
        				}
        			});
        	
        	futureCount++;
        	flushCount++;
        	
        	// If the user requested that futures be resolved to cause buffered messages to be sent, do so.
        	// The result of the future, including any exception, is ignored -- it will be reported by the
        	// messages callback.
        	if (cliArgs.futureCount > 0 && futureCount == cliArgs.futureCount) {
        		futureCount = 0;
        		System.out.println("** Resolving the last future to cause buffered messages to be sent");
        		try { lastFuture.get();  } catch (Exception e) {}
        	}
        	
        	// If the user requested flushing to cause buffered messages to be sent, do so
        	if (cliArgs.flushCount > 0 && flushCount == cliArgs.flushCount) {
        		flushCount = 0;
        		System.out.println("** Calling the flush method to cause buffered messages to be sent");
        		producer.flush();
        	}
        	
        	// Small delay between sends, to allow linger.ms to have a noticable effect
        	if (lingering) {
        		try { Thread.sleep(100); } catch (Exception e) {}
        	}
    	}
     	
     	// The final close operation will also flush any buffered messages. Allow 10 seconds for the
     	// broker request and response. Messages may be lost if they cannot be transmitted in that time.
     	System.out.println("** Closing the producer, which will cause any buffered messages to be sent");
     	producer.close(Duration.ofSeconds(10));
    }
}
