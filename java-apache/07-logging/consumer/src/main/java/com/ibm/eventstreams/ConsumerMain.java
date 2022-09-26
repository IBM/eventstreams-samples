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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Producer is a very simple kafka consumer for an Event Streams instance. It uses
 * an apache kafka consumer client to read and print messages. The consumer runs
 * until the program is stopped with a ctrl-C.
 */
public class ConsumerMain {
	
	// When using a kafka consumer client, we must close the client when we are done
	// with it. Failing to do this can cause a consumer group rebalance, which can
	// prevent other consumers from reading messages from the topic. These variables
	// are used to close the client when the user enters a ctrl-C.
	static volatile boolean shutdown = false;
	static Thread mainThread = null;
    // The kafka client is parameterized with the key and message types. We are
    // using strings in this simple example.
    static KafkaConsumer<String, String> consumer = null;

    //Instantiate the logger object
    private static final Logger logger = LogManager.getLogger(ConsumerMain.class);

    public static void main(String[] args) {
        mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdown();
        }));
        
        final ConsumerCLI cliArgs = ConsumerCLI.parse(args);
        final Properties config = ConsumerConfiguration.makeConfiguration(cliArgs);

        try {
        	// Creation of the consumer may throw an exception, due for example to
        	// bad configuration.
        	consumer = new KafkaConsumer<>(config);
        } catch (Exception e) {
        	logger.error(String.format("Caught exception creating producer : ", e));
        	System.exit(1);
        }
        
        // Any other exceptions will be caught and cleanly close the kafka client
        try {
            // Subscribe to the topic
        	List<String> topics = new ArrayList<String>();
        	topics.add(cliArgs.topicName);
            consumer.subscribe(topics);
            final Duration timeout = Duration.ofSeconds(10);
            
            while (!shutdown) {
            	// Poll kafka. The poll returns zero or more records. 
                final ConsumerRecords<String,String> records = consumer.poll(timeout);
            	// Process the messages received. Note that it is important to process the records
                // quickly and perform another poll. If the consumer takes too long between polls,
                // the kafka server may mark the consumer as inactive, causing a consumer group
                // rebalance.
                for (final ConsumerRecord<String,String> record : records) {
                    logger.info(String.format("Read record key: %s , value: %s , topicPartition:%s-%d , offset:%d ", 
                    record.key(),record.value(),record.topic(),record.partition(),record.offset()));
                }
            }        
        } catch (final WakeupException wex) {
        	// The ctrl-C shutdown calls the consumer's wakeup() method, which causes a waiting
        	// poll() to throw a WakeupException.
        } finally {
        	// Close the consumer. This causes it to be cleanly removed from the consumer group.
            if (consumer != null) {
                consumer.close(Duration.ofSeconds(10));
            }
        }
    }

    static void shutdown() {
        logger.info("Shutting down...");
        if (consumer != null) {
        	// Cause the consumer loop, and any waiting poll(), to exit
            shutdown = true;
            consumer.wakeup();
            try {
                mainThread.join();
            } catch (final InterruptedException e) {
            }
        }
    }
}
