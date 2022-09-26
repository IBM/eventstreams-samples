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
 * -a apikey, -apikey apikey
 * The apikey, from the credentials for the Event Streams instance. Required.
 * 
 * -b bootstrapServers, -bootstrap boostrapServers
 * The kafka brokers list, from the credentials for the instance; as a string of comma-separated broker names. Required.
 * 
 * -t topicName, -topic topicName
 * The topic name; the topic must have been created. Required.
 * 
 * -k keyContent, -key keyContent
 * The content of the message key. It may have a "%d" format string which will be replaced with the message number.
 * Optional; defaults to "key-%d".
 * 
 * -m messageContent, -m messageContent
 * The content of each message. It may have a "%d" format string which will be replaced with the message number.
 * Optional; defaults to "sample message %d".
 *
 * -n messageCount
 * The number of messages to produce. Optional; defaults to 1.
 *
 * -d messageDelay, -delay messageDelay
 * The delay between sending each message, in seconds. Optional; defaults to 0.
 */
class ProducerCLI {
	final String apikey;
	final String bootstrapServers;
	final String topicName;
	final String keyContent;
	final String messageContent;
	final int messageCount;
	final int messageDelay;
	
	private ProducerCLI(String apikey, String bootstrapServers, String topicName, String keyContent, String messageContent, int messageCount, int messageDelay) {
		this.apikey = apikey;
		this.bootstrapServers = bootstrapServers;
		this.topicName = topicName;
		this.keyContent = keyContent;
		this.messageContent = messageContent;
		this.messageCount = messageCount;
		this.messageDelay = messageDelay;
	}
	
	static ProducerCLI parse(String[] args) { 
		Options options = new Options();
		
		options.addOption(
				Option.builder("a")
					.longOpt("apikey")
					.hasArg(true)
					.argName("apikey")
					.desc("(required) apikey from the credentials for your Event Streams instance")
					.required()
					.build());
		options.addOption(
				Option.builder("b")
					.longOpt("bootstrap")
					.hasArg(true)
					.argName("bootstrapServers")
					.desc("(required) bootstrap servers from the credentials for your Event Streams instance, as a comma-separated list")
					.required()
					.build());
		options.addOption(
				Option.builder("t")
					.longOpt("topic")
					.hasArg(true)
					.argName("topicName")
					.desc("(required) name of the Kafka topic, which you must have already created")
					.required()
					.build());
		options.addOption(
				Option.builder("k")
					.longOpt("key")
					.hasArg(true)
					.argName("keyContent")
					.desc("(optional, default 'key-%d') key of each message, with optional '%d' format for message number")
					.required(false)
					.build());
		options.addOption(
				Option.builder("m")
					.longOpt("message")
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
		options.addOption(
				Option.builder("d")
					.longOpt("delay")
					.hasArg(true)
					.argName("delay")
					.desc("(optional, default 0) delay in seconds between messages")
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
		
		int messageCount = 1;
		try {
			messageCount = Integer.parseInt(cmd.getOptionValue("n", "1"));
			if (messageCount <= 0) {
				printHelpAndExit("Invalid messageCount value, must be greater than 0", options);
			}
		} catch (Exception e) {
			printHelpAndExit("Invalid messageCount value: " + e.toString(), options);
		}
		int messageDelay = 0;
		try {
			messageDelay = Integer.parseInt(cmd.getOptionValue("d", "0"));
			if (messageCount <= 0) {
				printHelpAndExit("Invalid messageDelay value, must be greater than or equal to 0", options);
			}
		} catch (Exception e) {
			printHelpAndExit("Invalid messageDelay value: " + e.toString(), options);
		}
		
		return new ProducerCLI(apikey, bootstrapServers, topicName, keyContent, messageContent, messageCount, messageDelay);
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