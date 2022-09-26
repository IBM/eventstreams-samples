# eventstreams-samples/java-apache/01-kafka-intro/consumer

This directory contains a simple Kafka consumer that uses the Apache Kafka client
to read string-formatted messages from a Kafka topic. To build and run the sample:

1. Run `mvn package`. This creates the executable jar file `target/consumer01-1.0.0.jar`.
2. Run `java -jar target/consumer01-1.0.0.jar [arguments...]`

## Arguments

The program takes the following arguments:

- `-a APIKEY` (required): The "apikey" from the service credentials
- `-b BOOTSTRAP` (required): The "bootstrap_endpoints" from the service credentials
- `-t TOPIC` (required): The topic name, which must already exist
- `-g GROUP` (optional): The name of the consumer group; defaults to `consumerGroup`
- `-c AUTOCOMMIT` (optional): Whether to auto-commit after reading a message; defaults to true
- `-o AUTOOFFSET` (optional): Where to start reading messages when no offset is recorded; defaults to `latest`, the other allowed value is `earliest`
- `-p POLLRECORDS` (optional): The maximum number of records to read on each poll; defaults to 500

Sample usage:

```
java -jar target/consumer01-1.0.0.jar \
    -a APIKEY_REDACTED \
    -b "kafka-0.mh-UUID-0000.us-south.containers.appdomain.cloud:9093,kafka-1.mh-UUID-0000.us-south.containers.appdomain.cloud:9093,kafka-2.mh-UUID-0000.us-south.containers.appdomain.cloud:9093" \
    -t my-test-topic \
    -g my-consumer-group
```

## Operation

See the [ConsumerMain.java](./src/main/java/com/ibm/eventstreams/ConsumerMain.java) code.

The consumer parses the command-line arguments
(see [ConsumerCLI.java](./src/main/java/com/ibm/eventstreams/ConsumerCLI.java)).
It then creates a configuration for an Apache Kafka consumer client,
following the Event Streams best-practices documentation at [TODO].
(see [ConsumerConfiguration.java](./src/main/java/com/ibm/eventstreams/ConsumerConfiguration.java)).

The main program then overrides the default configuration options for the auto-commit, auto-offset, and max-poll
with the values set by the `-c`, `-o`, and `-p` options.

The consumer creates an Apache Kafka consumer client with the configuration,
and subscribes the client to the topic.
It then polls the Kafka brokers _once_.
The poll operation returns zero or more _records_, each representing a message
that a Kafka producer has sent to the brokers.
This simple example processes each record by printing the message information.

When using an Apache consumer client, it is important to close the client after use.
Failing to do this can cause the Kafka brokers to perform a rebalancing operation,
which will impact message throughput.
The sample ensures this by adding a shutdown hook, so the control-C causes the client to be closed.

## Important Note

This example is for illustrative purposes, and should not be used as the basis of a consumer application.
It does not follow best practices because:
- The consumer client is only used to read a single group of records. Creating a new client for each record will cause excessive overhead, as each client must connect to the kafka brokers and authenticate.
- Several configuration options, including the auto-commit, auto-offset, and max-poll values, can be set to non-default values using the command-line arguments. Applications may need to set these and other configuration parameters, but care must be taken to determine appropriate values based on application behavior.
