# eventstreams-samples/java-apache/01-kafka-intro/producer

This directory contains a simple Kafka producer that uses the Apache Kafka client
to write string-format messages to a Kafka topic. To build and run the sample:

1. Run `mvn package`. This creates the executable jar file `target/producer01-1.0.0.jar`.
2. Run `java -jar target/producer01-1.0.0.jar [arguments...]`

## Arguments

The program takes the following arguments:

- `-a APIKEY` (required): The "apikey" from the service credentials
- `-b BOOTSTRAP` (required): The "bootstrap_endpoints" from the service credentials
- `-t TOPIC` (required): The topic name, which must already exist
- `-k KEY` (optional): The message key; defaults to `key-%d`
- `-m MESSAGE` (optional): The message content; defaults to `sample message %d`
- `-n COUNT` (optional): The number of messages to produce; defaults to 1
- `-d DELAY` (optional): The delay in seconds between messages; defaults to 0

Sample usage:

```
java -jar target/producer01-1.0.0.jar \
    -a APIKEY_REDACTED \
    -b "kafka-0.mh-UUID-0000.us-south.containers.appdomain.cloud:9093,kafka-1.mh-UUID-0000.us-south.containers.appdomain.cloud:9093,kafka-2.mh-UUID-0000.us-south.containers.appdomain.cloud:9093" \
    -t my-test-topic \
    -k "my-key-%d" \
    -m "my sample message-%d" \
    -n 1000 \
    -d 1
```

## Operation

See the [ProducerMain.java](./src/main/java/com/ibm/eventstreams/ProducerMain.java) code.

The producer parses the command-line arguments
(see [ProducerCLI.java](./src/main/java/com/ibm/eventstreams/ProducerCLI.java)).
It then creates a configuration for an Apache Kafka producer client,
following the Event Streams best-practices documentation at [TODO].
(see [ProducerConfiguration.java](./src/main/java/com/ibm/eventstreams/ProducerConfiguration.java)).

The producer creates an Apache Kafka producer client with the configuration.
It then sends COUNT records with the given key and message to the topic.
The key and message may have a `%d` format, which is replaced with the message number (0 through COUNT-1).
Each send operation returns a Java Future, and the producer gets the result of the future;
this means that it will wait until the Kafka brokers have confirmed receipt of the message.
The message information is printed.

## Important Note

This example is for illustrative purposes, and should not be used as the basis of a producer application.
It does not follow best practices because:
- Creating a new producer client for each batch of records will cause excessive overhead, as each client must connect to the kafka brokers and authenticate.
- The produce operation is synchronous. This causes overhead as the client can send only a single record at a time, instead of batching record.
