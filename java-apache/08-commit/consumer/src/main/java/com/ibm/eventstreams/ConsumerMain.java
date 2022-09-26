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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

/**
 * Consumer illustrating manual commit, using the strategy of committing after every `c` messages. 
 * This runs until stopped with a ctrl-C, or until it processes `f` messages.
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
        	System.err.printf("Caught exception creating consumer: %s\n", e.toString());
        	System.exit(1);
        }
        
        // Any other exceptions will be caught and cleanly close the kafka client
        try {
            // Subscribe to the topic
        	List<String> topics = new ArrayList<String>();
        	topics.add(cliArgs.topicName);
            consumer.subscribe(topics);
       
            while (!shutdown) {
            	// Poll kafka, with the timeout depending on whether the consumer is paused.
                final ConsumerRecords<String,String> records = consumer.poll(Duration.ofSeconds(10));
                System.out.printf("** poll returned %d records\n", records.count());
                // Queue the records for convenience in processing
                LinkedList<ConsumerRecord<String, String>> queue = new LinkedList<ConsumerRecord<String, String>>();
                records.iterator().forEachRemaining(queue::add);

                // Consumers can subscribe to multiple partitions, and can commit any or all of them in a
                // single operation. The commit information is a map from the topic-partitions to the offsets.
                // In this sample there will be only one entry in the map.
                Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<TopicPartition, OffsetAndMetadata>();
                
                // Counters for the commit strategy and for the simulated failure
                int messagesSinceLastCommit = 0;
                int messagesProcessed = 0;
                           
                // Process records from the queue, stopping if the user hits ctrl-C
                while (!queue.isEmpty() && !shutdown) {
                	ConsumerRecord<String,String> record = queue.remove();
                	System.out.printf("** processing record key='%s', value='%s', topicPartition=%s-%d offset=%d\n",
                			record.key(),
                			record.value(),
                			record.topic(),
                			record.partition(),
                			record.offset());
                	// The record is processed, so record that in the commit information (without committing).
                	// The next record will overwrite the commit value, so when we do commit we will use the
                	// last record processed. 
                	// Since this consumer only subscribes to one topic, the TopicPartition key will be
                	// the same for all records.
                	// We add 1 to the record offset, since the commit offset is the next record that will
                	// be returned when we poll (one past the last record processed).
                	offsetsToCommit.put(
                			new TopicPartition(record.topic(), record.partition()), 
                			new OffsetAndMetadata(record.offset() + 1));
                	
                	messagesSinceLastCommit++;
                	messagesProcessed++;
                	
                	if (messagesSinceLastCommit == cliArgs.commitCount) {
                		// Get the ofset from the only entry in the map
                		long offset = offsetsToCommit.values().iterator().next().offset();
                		System.out.printf("** committing to offset %d\n", offset);
                		// This sample uses the synchronous commit, so the method will not return until
                		// the brokers have updated the commit offset
                		consumer.commitSync(offsetsToCommit, Duration.ofSeconds(10));
                		messagesSinceLastCommit = 0;
                	}
                	
                	if (cliArgs.failureCount > 0 && messagesProcessed == cliArgs.failureCount) {
                		System.out.printf("** Simulating a consumer failure after %d messsages\n", messagesProcessed);
                		throw new RuntimeException("Simulated fault");
                	}
                }
                
                // If the user has hit ctrl-C we're done
                if (shutdown) break;
                
                // Otherwise the queue is emptied. We may have processed some messages since the last
                // commit, so commit the new offset!
                if (messagesSinceLastCommit > 0) {
            		long offset = offsetsToCommit.values().iterator().next().offset();
                	System.out.printf("** Committing to offset %d\n", offset);
                	consumer.commitSync(offsetsToCommit, Duration.ofSeconds(10));
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
        System.out.println("Shutting down...");
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
