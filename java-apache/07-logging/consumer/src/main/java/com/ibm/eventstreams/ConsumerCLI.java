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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ConsumerCLI parses the command-line arguments for the simple consumer client sample.  The parsing
 * itself is irrelevant to the configuration and operation of the Kafka consumer client.
 * 
 * -a apikey
 * The apikey, from the credentials for the Event Streams instance. Required.
 * 
 * -b bootstrapServers
 * The kafka brokers list, from the credentials for the instance; as a string of comma-separated broker names. Required.
 * 
 * -t topicName
 * The topic name; the topic must have been created. Required.
 * 
 * -g consumerGroup
 * The consumer group identifier.
 */
public class ConsumerCLI {
	final String apikey;
	final String bootstrapServers;
	final String topicName;
	final String consumerGroup;
	
	private ConsumerCLI(String apikey, String bootstrapServers, String topicName, String consumerGroup) {
		this.apikey = apikey;
		this.bootstrapServers = bootstrapServers;
		this.topicName = topicName;
		this.consumerGroup = consumerGroup;
	}
	
	static ConsumerCLI parse(String[] args) { 
		Options options = new Options();
		
		options.addOption(
				Option.builder("a")
					.hasArg(true)
					.argName("apikey")
					.desc("(required) apikey from the credentials for your Event Streams instance")
					.required()
					.build());
		options.addOption(
				Option.builder("b")
					.hasArg(true)
					.argName("bootstrapServers")
					.desc("(required) bootstrap servers from the credentials for your Event Streams instance, as a comma-separated list")
					.required()
					.build());
		options.addOption(
				Option.builder("t")
					.hasArg(true)
					.argName("topicName")
					.desc("(required) name of the Kafka topic, which you must have already created")
					.required()
					.build());
		options.addOption(
				Option.builder("g")
					.hasArg(true)
					.argName("consumerGroup")
					.desc("(optional, default 'consumerGroup') consumer group identifier")
					.required(false)
					.build());
		
		CommandLine cmd = null;
		try {
			cmd = new DefaultParser().parse(options, args);
		} catch (Exception e) {
			printHelpAndExit(e.toString(), options);
		}
		
		String apikey = cmd.getOptionValue("a");
		String bootstrapServers = cmd.getOptionValue("b");
		String topicName = cmd.getOptionValue("t");
		String consumerGroup = cmd.getOptionValue("g", "consumerGroup");
		
		return new ConsumerCLI(apikey, bootstrapServers, topicName, consumerGroup);
	}
	
	static void printHelpAndExit(String emessage, Options options) {
		// Log the error, using the same configuration and files as ConsumerMain
		Logger logger = LogManager.getLogger(ConsumerCLI.class);
		logger.error(String.format("Incorrect command line arguments: %s\n", emessage));
		
		// Send the output of the HelpFormatter to a string, so we can log it
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
	    HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp(pw, 80, "consumer", "", options, 0, 0, "");
	    logger.error(sw.getBuffer().toString());
	    System.exit(1);
	}
}
