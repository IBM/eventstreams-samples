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
import org.apache.kafka.common.config.SaslConfigs;

/**
 * TopicsConfiguration creates the configuration for the Apache Kafka admin
 * client.
 * The only required information are the bootstrap servers and the SASL
 * authentication
 * with the apikey. See
 * https://cloud.ibm.com/docs/EventStreams?topic=EventStreams-kafka_java_api
 * for additional information on use of the Kafka administration interface.
 */
public class TopicsConfiguration {

	static Properties makeConfiguration(TopicsCLI args) {
		// The configuration for the apache admin client is a Properties, with keys
		// defined in the apache client libraries.
		final Properties configs = new Properties();

		// To access the kafka servers, we need to authenticate using SASL_SSL and the
		// apikey, and provide the bootstrap server list

		// SASL_MECHANISM=PLAIN via API key is deprecated, use OAUTHBEARER instead
		// configs.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
		// configs.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
		// configs.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(
		// "org.apache.kafka.common.security.plain.PlainLoginModule required
		// username=\"token\" password=\"%s\";", args.apikey));
		configs.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
		configs.put(SaslConfigs.SASL_MECHANISM, "OAUTHBEARER");
		configs.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(
				"org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required grant_type=\"urn:ibm:params:oauth:grant-type:apikey\" apikey=%s\";",
				args.apikey));
		configs.put(SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS,
				"com.ibm.eventstreams.oauth.client.IAMOAuthBearerLoginCallbackHandler");
		configs.put(SaslConfigs.SASL_OAUTHBEARER_TOKEN_ENDPOINT_URL, "https://iam.cloud.ibm.com/identity/token");
		configs.put(SaslConfigs.SASL_OAUTHBEARER_JWKS_ENDPOINT_URL, "https://iam.cloud.ibm.com/identity/keys");
		configs.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, args.bootstrapServers);

		return configs;
	}

}
