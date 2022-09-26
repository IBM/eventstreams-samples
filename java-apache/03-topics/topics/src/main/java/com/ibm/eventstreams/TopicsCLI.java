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
 * TopicsCLI parses the command-line arguments for the topics sample.  The parsing
 * itself is irrelevant to the configuration and operation of the Kafka admin client.
 * 
 * The first argument must be the topic command, one of "create", "delete", "describe", "update", or "list".
 * 
 * -a apikey
 * The apikey, from the credentials for the Event Streams instance. Required.
 * 
 * -b bootstrapServers
 * The kafka brokers list, from the credentials for the instance; as a string of comma-separated broker names. Required.
 * 
 * -t topicName
 * The topic name. Required for topic create, delete, describe, and update.
 * 
 * -p partitions
 * The number of partitions, for topic creation. Optional, defaults to 1.
 * 
 * -r retentionTime
 * The retention time in milliseconds, for topic creation or update. Optional, defaults to 0 (meaning to use default).
 */
class TopicsCLI {
	final String command;
	final String apikey;
	final String bootstrapServers;
	final String topic;
	final int partitions;
	final long retentionTime;
	
	private TopicsCLI(String command, String apikey, String bootstrapServers, String topic, int partitions, long retentionTime) {
		this.command = command;
		this.apikey = apikey;
		this.bootstrapServers = bootstrapServers;
		this.topic = topic;
		this.partitions = partitions;
		this.retentionTime = retentionTime;
	}
	
	static TopicsCLI parse(String[] args) {	
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
					.argName("topic")
					.desc("(optional) topic name for the create, delete, and describe commands")
					.required(false)
					.build());
		options.addOption(
				Option.builder("p")
					.hasArg(true)
					.argName("partitions")
					.desc("(optional) number of partitions for the create command")
					.required(false)
					.build());
		options.addOption(
				Option.builder("r")
					.hasArg(true)
					.argName("retentionTime")
					.desc("(optional) retention time in milliseconds for the create command")
					.required(false)
					.build());
		
		if (args.length < 1) {
			printHelpAndExit("First argument must be command", options);
		}
		String command = args[0];
		if (!isLegalCommand(command)) {
			printHelpAndExit("Unknown command " + command, options);
		}
		// shift off that argument
		String[] nargs = new String[args.length-1];
		for (int i = 1; i < args.length; i++) {
			nargs[i-1] = args[i];
		}
		args = nargs;
		
		
		CommandLine cmd = null;
		try {
			cmd = new DefaultParser().parse(options, args);
		} catch (Exception e) {
			printHelpAndExit(e.toString(), options);
		}
		
		String apikey = cmd.getOptionValue("a");
		String bootstrapServers = cmd.getOptionValue("b");
		String topic = cmd.getOptionValue("t", "");
		int partitions = 1;
		try {
			partitions = Integer.parseInt(cmd.getOptionValue("p", "1"));
		} catch (Exception e) {
			printHelpAndExit("Bad -p value: " + e.toString(), options);
		}
		if (partitions <= 0) {
			printHelpAndExit("Bad -p value: must be greater than 0", options);
		}
		long retentionTime = 0;
		try {
			retentionTime = Long.parseLong(cmd.getOptionValue("r", "0"));
		} catch (Exception e) {
			printHelpAndExit("Bad -r value: " + e.toString(), options);
		}
		if (retentionTime > 0 && retentionTime < 3600000) {
			printHelpAndExit("Bad -r value: must be at least 3600000", options);
		}

		return new TopicsCLI(command, apikey, bootstrapServers, topic, partitions, retentionTime);
	}
	
	static boolean isLegalCommand(String s) {
		return s.equals("create") || s.equals("delete") || s.equals("list") || s.equals("describe") || s.equals("update");
	}
	
	static void printHelpAndExit(String emessage, Options options) {
		System.err.printf("Incorrect command line arguments: %s\n", emessage);
	    HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp("topic [create|delete|list|describe|update] args...", options);
	    System.exit(1);
	}
}