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
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * ProducerConfiguration creates the configuration for the Apache Kafka producer client.
 * See https://cloud.ibm.com/docs/EventStreams?topic=EventStreams-producing_messages for
 * more information on configuring producers for use with Event Streams.
 */
public class ProducerConfiguration {
	
	static Properties makeConfiguration(ProducerCLI args) {
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

        // Set up the recommended Event Streams kafka producer configuration
        // The client ID identifies your client application, and appears in the kafka server logs.
        configs.put(CommonClientConfigs.CLIENT_ID_CONFIG, "eventstreams-java-sample-producer1");
        // Setting acks to "all" means that the kafka server will not acknowledge receipt of a message until
        // it has been distributed to all the brokers that are replicating the message. This is the strongest 
        // guarantee that messages will not be lost.
        configs.put(ProducerConfig.ACKS_CONFIG, "all");
        // Setting retries to a non-zero value means that the apache client will try to resend messages
        // on transient errors. This reduces the need for retry logic in the producer code/
        configs.put(ProducerConfig.RETRIES_CONFIG, 2147483647);
        // Setting idempotence to true means that the apache client will guarantee that message send retries
        // will not result in duplication of messages.
        configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        // With idempotence set to true, we must also configure the in-flight requests per connection to 5 or less.
        configs.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        
        // The client can compress message data before it is transmitted. Typically the
        // trade-off between CPU and network bandwidth used makes this worthwhile.
        // configs.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        // Part of the producer's configuration is the classes to use for serializing
        // data into the records (message) that are sent from the client to Kafka.
        // In this sample, our keys and messages will both be strings.
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		
		return configs;
	}

}
