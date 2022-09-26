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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.AlterConfigsResult;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;


/**
 * TopicsMain is a simple application to create, delete, and list topics in an Event Streams
 * instance. It uses an Apache Kafka admin client to interact with the kafka brokers
 */
public class TopicsMain {
    
    public static void main(String[] args) {
    	final TopicsCLI cliArgs = TopicsCLI.parse(args);
    	final Properties config = TopicsConfiguration.makeConfiguration(cliArgs);

    	AdminClient admin = null;
        try {
        	// Creation of the client may throw an exception, due for example to
        	// bad configuration.
        	admin = AdminClient.create(config);
        } catch (Exception e) {
        	System.err.printf("Caught exception creating admin client: %s\n", e.toString());
        	System.exit(1);
        }
        
        // Perform the requested command
        if (cliArgs.command.equals("create")) {
        	createTopic(admin, cliArgs.topic, cliArgs.partitions, cliArgs.retentionTime);
        } else if (cliArgs.command.equals("delete")) {
        	deleteTopic(admin, cliArgs.topic);
        } else if (cliArgs.command.equals("update")) {
        	updateTopic(admin, cliArgs.topic, cliArgs.retentionTime);
        } else if (cliArgs.command.equals("list")) {
        	listTopics(admin);
        } else if (cliArgs.command.equals("describe")) {
        	describeTopic(admin, cliArgs.topic);
        }
	     	
     	// The apache clients should be closed after use.
     	admin.close(Duration.ofSeconds(10));
    }
    
    private static void createTopic(AdminClient admin, String topic, int partitions, long retentionTime) {
    	if (topic.equals("")) {
    		System.out.println("** The create command requires the -t topic argument");
    		return;
    	}
    	try {
    		// The AdminClient createTopics command can create multiple topics; we create just one.
    		// Each topic is specified by a NewTopic object with topic name, number of partitions,
    		// and required replication factor. This code assumes the kafka instance has at least
    		// 3 brokers, to support a replication factor of 3. All Event Streams instances have at
    		// least 3 brokers.
    		NewTopic newTopic = new NewTopic(topic, partitions, (short)3);
    		// The creation also allows specification of configuration values for the topic. This
    		// sample only allows setting the message retention time. If that parameter has been
    		// set on the command line, add the configuration to the NewTopic using a map.
    		if (retentionTime > 0) {
    			Map<String,String> configMap = new HashMap<String,String>();
    			configMap.put(TopicConfig.RETENTION_MS_CONFIG, Long.toString(retentionTime));
    			newTopic.configs(configMap);
    		}

    		// All interactions with the Kafka brokers are asynchronous. Calling createTopics
    		// creates threads which send topic-creation requests to the Kafka brokers.
    		// The methods of the result object return futures which will resolve when the
    		// topic-creation request is completed, throwing exceptions if there is any
    		// error during creation.
    		CreateTopicsResult result = admin.createTopics(Collections.singleton(newTopic));
    		
    		// Allow 10 seconds for topic-creation to complete. No data is returned from the all()
    		// future; a successful (non-exception) return means the topic creation completed.
    		result.all().get(10, TimeUnit.SECONDS);
       		System.out.printf("** Successfully created topic %s with %d partitions\n", topic, partitions);
       	    		
    		// Use the config() method to get the configuration of the resulting topic. This
       		// returns a future, which will resolve immediately since the topic-creation has
       		// already succeeded.
    		Config config = result.config(topic).get(10, TimeUnit.SECONDS);
    		printConfig(config);
    		System.out.println();
    	} catch (Exception e) {
        	System.err.printf("** Caught exception creating topic %s with %d partitions: %s\n", topic, partitions, e.toString());
        }
    }
    
    private static void deleteTopic(AdminClient admin, String topic) {
    	if (topic.equals("")) {
       		System.out.println("** The delete command requires the -t topic argument");
       	     		return;
    	}
    	try {
    		// The AdminClient deleteTopics command can delete multiple topics; we delete just one.
    		DeleteTopicsResult result = admin.deleteTopics(Collections.singleton(topic));
    		// As with topic creation, the interaction is asynchronous, and the result object
    		// methods return futures which will resolve when the deletion is complete.
    		result.all().get(10, TimeUnit.SECONDS);
    		System.out.printf("** Successfully deleted topic %s\n", topic);
    		System.out.println();
    	} catch (Exception e) {
        	System.err.printf("** Caught exception deleting topic %s: %s\n", topic, e.toString());
        }
    }
    
    private static void updateTopic(AdminClient admin, String topic, long retentionTime) {
    	if (topic.equals("") || retentionTime == 0) {
    		System.out.println("** The update command requires the -t topic and -r retentionTime arguments");
    		return;
    	}
    	try {
    		// The AdminClient incrementalAlterConfigs command can alter the configurations of multiple
    		// entities (brokers or topics). The entity is specified by a ConfigResource object.
    		ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
    		// The alteration is specified by an object with the configuration property to be changed,
    		// the new value (as a string), and the operation to perform. Here we are settng the
    		// retention time to the new value. Another possibly-useful operation is OpType.DELETE,
    		// which will revert the property to the default value.
    		AlterConfigOp op = new AlterConfigOp(
    				new ConfigEntry(TopicConfig.RETENTION_MS_CONFIG, Long.toString(retentionTime)), 
    				AlterConfigOp.OpType.SET);
    		
    		// As with topic creation, the interaction is asynchronous, and the result object
    		// methods return futures which will resolve when the alteration is complete.
    		AlterConfigsResult result = admin.incrementalAlterConfigs(Collections.singletonMap(configResource, Collections.singleton(op)));
    		result.values().get(configResource).get(10, TimeUnit.SECONDS);
    		System.out.printf("** Successfully altered configuration of topic %s\n", topic);
    		
    		// The alteration does not return data. Use a different asynchronous operation to 
    		// request the modified configuration and print it.
        	DescribeConfigsResult configResult = admin.describeConfigs(Collections.singleton(configResource));
        	Config config = configResult.values().get(configResource).get(10, TimeUnit.SECONDS);
        	printConfig(config);
        	System.out.println();
    	} catch (Exception e) {
        	System.err.printf("** Caught exception deleting topic %s: %s\n", topic, e.toString());
        }
    }
    
    private static void listTopics(AdminClient admin) {
        try {
    		// As with topic creation, the interaction is asynchronous, and the result object
    		// methods return futures which will resolve when the list of topics has been returned
        	// from the brokers.
        	ListTopicsResult listResult = admin.listTopics();
        	Map<String, TopicListing> topics = listResult.namesToListings().get(10, TimeUnit.SECONDS);
        	
        	System.out.printf("\n** %d topics found\n", topics.entrySet().size());
        	for (Entry<String, TopicListing> entry : topics.entrySet()) {	
        		System.out.printf("  %s\n", entry.getKey());
        	}
        	System.out.println();
        } catch (Exception e) {
        	System.err.printf("** Caught exception listing topics: %s\n", e.toString());
        }
    }
    
    private static void describeTopic(AdminClient admin, String topic) {
    	if (topic.equals("")) {
    		System.out.println("The describe command requires the -t topic argument");
    		return;
    	}
        try {
       		// The describeTopics method can describe multiple topics; we request just one.
        	// As with topic creation, the interaction is asynchronous, and the result object
    		// methods return futures which will resolve when the topic information has been
        	// returned from the brokers.
        	DescribeTopicsResult describeResult = admin.describeTopics(Collections.singleton(topic));
        	TopicDescription desc = describeResult.values().get(topic).get(10, TimeUnit.SECONDS);
        	
        	// See the README for this project
        	System.out.printf("\n** Topic %s found\n", topic);
        	System.out.printf("  name %s\n",  desc.name());
        	List<TopicPartitionInfo> partitions = desc.partitions();
        	System.out.printf("  %d partitions\n", partitions.size());
        	for (TopicPartitionInfo pinfo : partitions) {
        		// The partition information uses Node objects with information about the brokers. This
        		// sample only prints the broker IDs.
        		List<Integer> replicas = pinfo.replicas().stream().map(v -> v.id()).collect(Collectors.toList());
        		System.out.printf("    %d: leader is broker %d, replicas are %s\n", pinfo.partition(), pinfo.leader().id(), replicas.toString());
        	}
        	
        	// Make another asynchronous call to get the configuration information for the topic.
        	// The topic is specified by a ConfigResource, as in the updateTopic method.
        	ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
        	DescribeConfigsResult configResult = admin.describeConfigs(Collections.singleton(configResource));
        	Config config = configResult.values().get(configResource).get(10, TimeUnit.SECONDS);
        	printConfig(config);
        	System.out.println();
        } catch (Exception e) {
        	System.err.printf("** Caught exception describing topic %s: %s\n", topic, e.toString());
        }
    }
    
    private static void printConfig(Config config) {
		System.out.println("** Configuration:");
		for (ConfigEntry entry : config.entries()) {
			System.out.printf("  %s = %s\n", entry.name(), entry.value());
		}
    }
}
