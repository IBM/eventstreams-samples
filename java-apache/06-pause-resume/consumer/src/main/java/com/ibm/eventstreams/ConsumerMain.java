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
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

/**
 * Consumer illustrating strategies for ensuring that polling is done frequently enough
 * to prevent consumer group rebalance, and the use of pause and resume. This runs until
 * stopped with a ctrl-C.
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
        

        // One strategy for meeting the consumer polling requirements is to limit the number
        // of records that will be returned by a poll. The appropriate number will depend on
        // the application, and often can only be found experimentally.
	    config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, cliArgs.pollRecords);

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
            
            // This consumer uses a pause/resume strategy. When paused, the poll operation
            // should return no new messages, so we will use a short polling interval.
            // When not paused, the poll operation will return messages and may need to
            // interact with the Kafka brokers, so we use a longer interval.
            final Duration pausedTimeout = Duration.ofSeconds(0);
            final Duration unpausedTimeout = Duration.ofSeconds(10);
            
            // Each time we poll, the returned records will be added to this queue for processing.
            // (Because of the use of pause and resume, we anticipate that messages will only
            // be added when the queue is empty.)
            LinkedList<ConsumerRecord<String, String>> queue = new LinkedList<ConsumerRecord<String, String>>();
           
            while (!shutdown) {
            	// Poll kafka, with the timeout depending on whether the consumer is paused.
            	final boolean isPaused = !consumer.paused().isEmpty();
                final ConsumerRecords<String,String> records = consumer.poll(isPaused ? pausedTimeout : unpausedTimeout);
                System.out.printf("** poll returned %d records (consumer is %s)\n", records.count(), isPaused ? "paused" : "not paused");
                
                // Add the records to the queue
                records.iterator().forEachRemaining(queue::add);
                
                // Record time of the last poll operation
                final long startTime = System.currentTimeMillis();
                
                // Process records from the queue, stopping if the user hits ctrl-C
                while (!queue.isEmpty() && !shutdown) {
                	// If we have taken more than intervalSeconds, stop processing to perform another poll
                	if (System.currentTimeMillis() - startTime > 1000*cliArgs.intervalSeconds) {
                		System.out.printf("** message processing has taken more than %d seconds, breaking to poll again\n", cliArgs.intervalSeconds);
                		break;
                	}
                	
                	// Simulate the processing delay for one record
                	try {
                		Thread.sleep(1000*cliArgs.processingSeconds);
                	} catch (InterruptedException e) {
                		// don't worry about it
                	}
                	ConsumerRecord<String,String> record = queue.remove();
                	System.out.printf("read record key='%s', value='%s', topicPartition=%s-%d offset=%d\n",
                			record.key(),
                			record.value(),
                			record.topic(),
                			record.partition(),
                			record.offset());
                }
                
                // If the user has hit ctrl-C we're done
                if (shutdown) break;
                
                // Otherwise we either emptied the queue, or hit the time limit.
                if (queue.isEmpty()) {
                	// We have processed all the records, and are able to receive more with the next poll; so if
                	// we are paused, resume normal polling.  The argument to the resume method is the collection 
                	// of partitions which we want to unpause, which is all of the ones that are paused.
                	if (isPaused) {
                		System.out.println("** Consumer resuming");
                		consumer.resume(consumer.paused());
                	}
                } else {
                	// We hit the processing time limit, so should pause the consumer so we do not receive more
                	// messages on the next poll. The argument to the pause method is the colleciton of partitions
                	// from which we do not wish to receive messages, which is all of the ones which have been
                	// assigned to the consumer.
                	if (!isPaused) {
                		System.out.println("** Consumer pausing");
                		consumer.pause(consumer.assignment());
               		
                	}
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
