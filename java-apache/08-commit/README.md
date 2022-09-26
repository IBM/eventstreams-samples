# eventstreams-samples/java-apache/08-commit

The consumer example in this directory illustrates strategies for committing message offsets.
Having consumers perform message commits instead of using auto-commit gives better control over message consumption
and can also improve throughput.
The directory includes a simple producer which does not illustrate any new concepts.

See the [consumer](./consumer/README.md) and [producer](./producer/README.md) READMEs for instructions on building and running the applications.

## Concepts

The messages in a Kafka partition can be thought of as an indexed array,
with messages written by the producers appended to the end of the array.
The Kafka servers maintain a _consumer offset_ for each combination of consumer group and partition.
The offset is the index of the next message that will be delivered when `poll`
is called by the consumer of that consumer group which is assigned that partition.
When the poll returns multiple messages, the messages are a contiguous block starting at the consumer offset,
with consecutive offsets in the partition.

A _commit_ changes the consumer offset (by analogy with a database commit).
Earlier samples used the Kafka _auto-commit_ feature, which automatically commits messages returned by a poll.
This commit is done in the background at regular intervals.
For example if the current offset is 30, and the consumer polls and receives ten messages 30, 31, ... 39,
the offset will (at some point) be committed to position 40.

One problem with using auto-commit arises if the consumer process crashes before it has processed all the messages
received from the poll.
This can result in some messages never being processed.
In the above example, if the auto-commit has committed to offset 40,
but the consumer fails while processing message 34, no consumer will ever process messages 34 to 39,
since when a new consumer polls from the partition it will receive messages starting at the commit offset of 40.

Rather than using auto-commit, consumers can control the update of the consumer offset
by performing commit operations.
In the above example, a consumer could commit after it finishes processing each message returned from the poll;
thus after processing message 30 it will commit the consumer offset to 31,
after processing message 31 it will commit the consumer offset to 32, and so on.
If it crashes while processing message 34, the new consumer's poll will return messages starting with the unprocessed message 34.

The commit operation is not without cost, since it involves a request/response to the Kafka brokers
as well as activity on the brokers to update and replicate the new commit offset.
Committing after processing some number of messages or at regular time intervals will reduce this overhead.
However, this does mean there is a possibility that some messages will be processed twice,
if a consumer fails after processing some messages but before committing.

In the earlier example if we commit after every five messages,
the consumer might process messages 30 to 34 successfully and commit to offset 35;
then it might process messages 35 and 36 but fail during message 37.
The new consumer process would then receive messages starting at the commited offset 35,
so messages 35 and 36 would be processed a second time.
The overall application would need to be designed to handle this possibility that messages will be processed twice.

This sample illustrates the consumer commit operation,
using the strategy of committing after processing a certain number of messages (set by a command line argument).
The consumer can also simulate a failure after some number of messages (also a command line argument).


## Example

Run the producer to send some messages into the topic:
```
java -jar target/producer08-1.0.0.jar -a APIKEY -b BOOTSTRAP -t TOPIC -n 30
```

Run the consumer to consume those messages:
```
java -jar target/consumer08-1.0.0.jar -a APIKEY -b BOOTSTRAP -t TOPIC -c 5 -f 13
```
In the above, the arguments are:
- `-c 5`: Commit after every 5 messages
- `-f 18`: Simulate a failure after 13 messages

What you will see is:

1. The consumer will poll and receive all 30 messages, offsets 0 to 29.
1. It will process the 5 messages at offsets 0 to 4, then commit to offset 5.
1. It will process the 5 messages at offsets 5 to 9, then commit to offset 10.
1. It will process the 3 messages at offsets 10 to 12, then simulate a failure and exit.

If you now run the consumer again with the same arguments, you will see:

1. The consumer will poll and receive 20 messages, offsets 10 to 29 -- since the previous consumer committed to offset 10.
1. It will process the 5 messages at offsets 10 to 14, so messages 10 to 12 will have been processed twice by the two consumers; then commit to offset 15.
1. It will process the 5 messages at offsets 15 to 19, then commit to offset 20.
1. It will process the 3 messages at offsets 20 to 22, then simulate a failure and exit.

If you run the consumer a third time, you will see:

1. The consumer will poll and receive 10 messages, offsets 20 to 29 -- since the previous consumer committed to offset 20.
1. It will process the 5 messages at offsets 20 to 24, so messages 20 to 22 will have been processed twice by the two consumers; then commit to offset 25.
1. It will process the 5 messages at offsets 25 to 29, then commit to offset 30.
1. It will continue to poll every 10 seconds, receiving no new messages.

