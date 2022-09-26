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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * ProducerCLI parses the command-line arguments for the simple producer client sample.  The parsing
 * itself is irrelevant to the configuration and operation of the Kafka producer client.
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
 * -k keyContent
 * The content of the message key. It may have a "%d" format string which will be replaced with the message number.
 * Optional; defaults to "key-%d".
 * 
 * -m messageContent
 * The content of each message. It may have a "%d" format string which will be replaced with the message number.
 * Optional; defaults to "sample message %d".
 *
 * -n messageCount
 * The number of messages to produce. Optional; defaults to 1.
 * 

 */
class ProducerCLI {
	final String apikey;
	final String bootstrapServers;
	final String topicName;
	final String keyContent;
	final String messageContent;
	final int messageCount;
	
	private ProducerCLI(String apikey, String bootstrapServers, String topicName, String keyContent, String messageContent, int messageCount) {
		this.apikey = apikey;
		this.bootstrapServers = bootstrapServers;
		this.topicName = topicName;
		this.keyContent = keyContent;
		this.messageContent = messageContent;
		this.messageCount = messageCount;
	}
	
	static ProducerCLI parse(String[] args) { 
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
				Option.builder("k")
					.hasArg(true)
					.argName("keyContent")
					.desc("(optional, default 'key-%d') key of each message, with optional '%d' format for message number")
					.required(false)
					.build());
		options.addOption(
				Option.builder("m")
					.hasArg(true)
					.argName("messageContent")
					.desc("(optional, default 'sample message %d') content of each message, with optional '%d' format for message number")
					.required(false)
					.build());
		options.addOption(
				Option.builder("n")
					.hasArg(true)
					.argName("messageCount")
					.desc("(optional, default 1) number of messages to produce")
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
		
		String messageContent = cmd.getOptionValue("m", "sample message %d");
		checkFormat("messageContent", messageContent, options);
		String keyContent = cmd.getOptionValue("k", "key-%d");
		checkFormat("keyContent", keyContent, options);
		
		String mc = cmd.getOptionValue("n", "1");
		int messageCount = 0;
		try {
			messageCount = Integer.parseInt(mc);
		} catch (Exception e) {
			printHelpAndExit("Bad -n value: " + e.toString(), options);
		}
		if (messageCount < 0) {
			printHelpAndExit("Bad -n value: must be 0 or more", options);
		}
		
		return new ProducerCLI(apikey, bootstrapServers, topicName, keyContent, messageContent, messageCount);
	}
	
	static void printHelpAndExit(String emessage, Options options) {
		System.err.printf("Incorrect command line arguments: %s\n", emessage);
	    HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp("producer", options);
	    System.exit(1);
	}
	
	static void checkFormat(String id, String fmt, Options options) {
		try {
			String.format(fmt, 3);
		} catch (Exception e) {
			printHelpAndExit("Invalid " + id + ": format can contain at most one '%d' format", options);
		}
	}
}