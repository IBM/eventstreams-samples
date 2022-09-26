# eventstreams-samples/java-apache/02-sample

The applications in this directory are a basic producer and consumer using the Apache Kafka clients.
See the [producer](./producer/README.md) and [consumer](./consumer/README.md) READMEs for instructions on building and running the applications.

To run these samples, follow the [instructions](../../Instructions.md) to create an Event Streams instance,
service credentials, and topic. The topic may have one or more partitions.

## Concepts

The applications are a very simple producer and consumer, which are configured using Event Streams recommendations.
The application code follows some, but not all, best practices, as discussed below.

When using the Apache Kafka clients, applications should `close` the client when exiting (where possible).
For example if an exception occurs from which the application cannot recover,
the client should be closed as part of the exception handling.
In Java, a `try/catch/finally` block is often the best way to ensure this.
In these applications, a simple shutdown hook is used to intercept any termination signals (such as a control-C)
and close the clients.


### Producer

See the [ProducerMain.java](./producer/src/main/java/com/ibm/eventstreams/ProducerMain.java) code.

The producer parses the command-line arguments
(see [ProducerCLI.java](./producer/src/main/java/com/ibm/eventstreams/ProducerCLI.java)).
It then creates a configuration for an Apache Kafka producer client
(see [ProducerConfiguration.java](./producer/src/main/java/com/ibm/eventstreams/ProducerConfiguration.java)).
The configuration includes the setup for SASL authentication to the Kafka brokers, using the API key in your credentials.
It also sets several producer properties to the values recommended in
the [Event Streams documentation](https://cloud.ibm.com/docs/EventStreams?topic=EventStreams-producing_messages).

The producer creates an Apache Kafka producer client with the configuration.
It then generates and sends _records_ to the topic.
The key and message content of these records are generated from the `-k` and `-m` command-line options,
by replacing the `%d` format with the record number, which ranges from 0 to the `-n` count.

Records are sent one at a time with the client `send` method.
Each record send returns a Java Future, and the producer calls `get` to resolve the future.
This causes the Apache Kafka client to send the message to the Kafka brokers,
and to wait until the brokers have confirmed receipt of the message,
before the next message is sent. The message information is printed.

Note that this is usually not best practice for producers,
since it causes messages to be sent one at a time, synchronously.
The Apache Kafka client can buffer messages and send them in batches, which will result in better throughput.
This is illustrated in a later sample.

The shutdown hook for the producer sets a boolean which terminates the message-producing loop.
After the message-producing loop exits, the producer closes the Apache Kafka client.


### Consumer

See the [ConsumerMain.java](./consumer/src/main/java/com/ibm/eventstreams/ConsumerMain.java) code.

The consumer parses the command-line arguments
(see [ConsumerCLI.java](./consumer/src/main/java/com/ibm/eventstreams/ConsumerCLI.java)).
It then creates a configuration for an Apache Kafka consumer client
(see [ConsumerConfiguration.java](./consumer/src/main/java/com/ibm/eventstreams/ConsumerConfiguration.java)).
The configuration includes the setup for SASL authentication to the Kafka brokers, using the API key in your credentials.
It also sets several consumer properties to the values recommended in
the [Event Streams documentation](https://cloud.ibm.com/docs/EventStreams?topic=EventStreams-consuming_messages).

The consumer creates an Apache Kafka consumer client with the configuration,
and subscribes the client to the topic.
It then repeatedly polls the Kafka brokers.
The poll operation returns zero or more _records_, each representing a message
that a Kafka producer has sent to the brokers.

This simple example "processes" each record by printing the message information.
Note that this is not best practice for consumers, since the processing of the received messages can take an arbitrarily long time.
There is a limit on the amount of time that consumers may take between calls to `poll`.
If a consumer does not poll within that limit, the brokers will assume that it has terminated and will perform a rebalance operation,
which will impact the throughput of the other consumers.
Strategies for avoiding this are illustrated in another sample.

This consumer runs until the process is stopped with a control-C, which triggers the shutdown hook.
This is a little more complex than in the producer sample because of the `poll` operation,
which causes the main thread to block waiting for a response from the Kafka brokers.
The `shutdown` method calls the Apache client's `wakeup` method to cause the `poll` to return. 
The Apache client is closed in the `finally` block.
With the structure of this application it could instead be closed at the end of the main method,
as is done in the producer.


## Examples of Use

Start the consumer in one shell:

```
java -jar target/consumer02-1.0.0.jar \
    -a APIKEY \
    -b BOOTSTRAP_SERVERS \
    -t TOPIC
```

The logging will show the consumer polling at 10-second intervals, receiving 0 messages each time.

Start the producer in another shell:

```
java -jar target/producer02-1.0.0.jar \
    -a APIKEY \
    -b BOOTSTRAP_SERVERS \
    -t TOPIC \
    -n 1000
```

The producer's logging will show it sending messages, and the consumer's will show it receiving the messages.
Since neither is doing any extensive processing, the two will usually run at approximately the same speed,
so each consumer poll will generally receive a single message.

If the topic has multiple partitions, a single consumer will read from all the partitions.
This can sometimes cause messages to be processed out of order.
You can also create extra consumers, which will cause the partitions to be rebalanced and assigned to the available consumers.

