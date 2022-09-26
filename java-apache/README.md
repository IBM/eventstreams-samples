# eventstreams-samples/java-apache

This directory contains samples written in java, using the Apache Kafka clients.
Each sample is in a separate directory, and may contain several applications, each in a sub-directory.

## Building and Running the Samples

To build and run the samples you will need:

- Apache maven (`mvn`)
- JDK 11 or later (`java`)
- An instance of Event Streams, with service credentials and topic already created

Each sample application is in a separate directory, and when built creates an executable jar file.

1. Change to the directory of the sample application.
2. Run `mvn package`. This creates the executable jar file in the `target` directory.
3. Run `java -jar target/<jarfile.jar> [arguments...]`

The README for each sample has additional information about the purpose of the sample,
and the README for each application has further instructions for building and running the application
including command-line arguments.

## Common Arguments

All samples require the following command-line arguments, which are used to access the Event Streams instance and topic.

- `-a APIKEY`: The "apikey" from the service credentials
- `-b BOOTSTRAP`: The "bootstrap_endpoints" from the service credentials
- `-t TOPIC`: The topic name

Producer samples additionally have the following optional arguments.

- `-k KEY`: The key of the Kafka record to be produced
- `-m MESSAGE`: The message of the Kafka record to be produced

Consumer samples additionally have the following optional arguments.

- `-g GROUP`: The name of the consumer group

See the README for each sample for additional arguments and the use of each argument.s

## Samples

- [01-kafka-intro](./01-kafka-intro/README.md): A sample for experimenting with Kafka and observing the behavior of producers and consumers. This sample is not an example of best practices!

- [02-sample](./02-sample): A basic producer and consumer that follow best practices.

- [03-topics](./03-topics): Shows how to create, delete, and inspect topics programmatically.

- [04-batch](./04-batch): Shows strategies for producer message batching.

- [05-metrics](./05-metrics): Shows how to collect useful metrics for producer and consumer.

- [06-pause-resume](./06-pause-resume): Show how a consumer can pause and resume polling to match its ability to process messages

- [07-logging](./07-logging): Shows how to use and configure log4j logging.

- [08-commit](./08-commit): Shows strategies for consumer message commit.

