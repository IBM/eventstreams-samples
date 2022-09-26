# eventstreams-samples/java-apache/06-pause-resume

The consumer application in this directory illustrates some techniques
to ensure that consumers make calls to the `poll` method within the required time limit,
and thus avoid unnecessary consumer rebalancing which impacts throughput.
The directory includes a simple producer which does not illustrate any new concepts.

See the [consumer](./consumer/README.md) and [producer](./producer/README.md) READMEs for instructions on building and running the applications.


## Concepts

Kafka consumers are members of _consumer groups_, which have string identifiers.
Each group represents the consumers that are jointly consuming messages from a topic.
The Kafka brokers track the consumers in each group, and assign the partitions of the topic to the active consumers.

A consumer application must poll for messages regularly;
that is, there is a (configured) limit on the amount of time that may elapse between calls to `poll`.
If the consumer does not poll in time, the Kafka brokers will assume that it has terminated and remove it from the consumer group
in what is called a _rebalance operation_.
This prevents other consumers in its group from reading messages until the brokers finish the cleanup.

The consumer's configured polling interval (`max.poll.interval.ms`) defaults to five minutes, which is often sufficient.
However in practice a consumer may not be able to guarantee all messages will be processed in time.
For example the processing of a message may perform operations on a database or other external resource,
introducing unpredictable delays.

This sample illustrates strategies for meeting the Kafka polling requirement.

- The consumer can set the maximum number of messages returned by a poll. In an application, this can be used to ensure an upper bound on the number that need to be processed before the next poll operation.

- The consumer monitors the amount of time that it has taken for processing messages. This allows it to perform another poll when nearing the polling interval limit.

- The consumer uses the `pause` and `resume` methods of the Apache client. This notifies the client that the consumer does not wish to receive more messages from a poll. Note that it does _not_ remove the consumer's obligation to poll within the polling interval.


## Example

Run the producer to send some messages into the topic:
```
java -jar target/producer06-1.0.0.jar -a APIKEY -b BOOTSTRAP -t TOPIC -n 50
```

Run the consumer to consume those messages:
```
java -jar target/consumer06-1.0.0.jar -a APIKEY -b BOOTSTRAP -t TOPIC -p 20 -d 2 -i 10
```

In the above, the arguments are:
- `-p 20`: Read at most 20 messages on each poll
- `-d 2`: Simulate (with sleep) a processing time of 2 seconds per message
- `-i 10`: Use no more than 10 seconds processing messages before performing the next poll

The 10 second processing time is unrealistically small, assuming your consumer is configured with the default 5 minute interval,
but useful for illustrating the behavior. What you will see is:

1. The consumer will read the first batch of 20 messages.
1. It will process 4 to 5 of these messages, taking 2 seconds each, before reaching the 10-second limit.
1. It will pause, so that it does not receive new messages on polls.
1. It will poll again, receiving 0 messages because it is paused.
1. It will process the next 4 to 5 messages, reaching the 10-second limit, and poll again.
1. Processing of messages in groups of 4 to 5 messages continues until all 20 messages are processed.
1. It will resume, so that it receives messages on the next poll.
1. The next poll will return another 20 messages, which are processed similarly.


