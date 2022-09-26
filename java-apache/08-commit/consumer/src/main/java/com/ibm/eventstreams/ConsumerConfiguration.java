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

import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * ConsumerConfiguration creates the configuration for the Apache Kafka consumer client.
 * See https://cloud.ibm.com/docs/EventStreams?topic=EventStreams-consuming_messages for
 * more information on configuring consumers for use with Event Streams.
 */
public class ConsumerConfiguration {

	public static Properties makeConfiguration(ConsumerCLI args) {
		// The configuration for the apache client is a Properties, with keys defined in the apache client libraries.
		final Properties configs = new Properties();

		// To access the kafka servers, we need to authenticate using SASL_SSL and the apikey,
		// and provide the bootstrap server list
        configs.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        configs.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        configs.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"token\" password=\"%s\";",
                args.apikey));
        configs.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, args.bootstrapServers);

        // Set up the recommended Event Streams kafka consumer configuration
        // The client ID identifies your client application, and appears in the kafka server logs.
        configs.put(CommonClientConfigs.CLIENT_ID_CONFIG, "eventstreams-java-sample-consumer1");
        
        // Each consumer is a member of a consumer group, identified by a string (command-line argument
        // in this sample).
	    configs.put(ConsumerConfig.GROUP_ID_CONFIG, args.consumerGroup);
	    
        // For this sample, new consumer groups will start reading messages at the earliest available offset.
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // Do not auto-commit
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
	    
        // Part of the consumer's configuration is the classes to use for deserializing
        // data from the records (message) that are received by this consumer.
        // In this sample, we'll assume that the records contain strings.
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
	        
	    return configs;
	}
}
