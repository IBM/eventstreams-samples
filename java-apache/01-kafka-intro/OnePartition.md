# eventstreams-samples/java-apache/01-kafka-intro/OnePartition.md

All of these examples can be run using a lite plan, which provides a single topic and partition.
You will need to follow the [instructions](../../Instructions.md) to create an Event Streams instance,
service credentials, and a topic with one partition.

Build both the producer and the consumer following the instructions in their README files.
The programs are run using `java -jar <jarfile> [arguments...]`. Note that both require these arguments:

- `-a APIKEY`: the "apikey" from the service credentials
- `-b BOOTSTRAP`: the "bootstrap_endpoints" from the service credentials
- `-t TOPIC`: the name of the topic you created


### Produce a Message

Run the producer application with the default arguments, i.e.

```
java -jar target/producer01-0.1.1.jar -a APIKEY -b BOOTSTRAP -t TOPIC
```

The application logs the configuration and creation of the Apache Kafka producer client.
If something goes wrong at this stage, it will log the issue and exit.
The most likely problems are incorrect values of the APIKEY or BOOTSTRAP.
Your environment must also allow traffic to the Kafka bootstrap servers and to port 9093.

The application then sends one message, with key `key-0' and content 'sample message 0'.
If something goes wrong at this stage, it will log the issue and exit.
The most likely problem is an incorrect value of the TOPIC, that is, the topic does not exist.

After sending the message and receiving confirmation from the Kafka brokers of the receipt,
the application will print:
```
** Successfully sent message 0, topicPartition=TOPIC-0 offset 0
```

(The message offset is the unique index within the topic and partition. Since this is the first message sent,
it is at offset 0.)

The application closes the Apache Kafka producer client, which generates more logging.

**On the Kafka brokers:**
- Your topic has one message, at offset 0
- There are no _commit offsets_ for the topic, that is, no record that any consumers have read the topic.


### Consume a Message

(This assumes you have run the previous step, and your topic has one message.)

Run the consumer application with the default arguments, i.e.

```
java -jar target/consumer01-0.1.1.jar -a APIKEY -b BOOTSTRAP -t TOPIC
```

The application logs the configuration and creation of the Apache Kafka consumer client.
If something goes wrong at this stage, it will log the issue and exit
(this is unlikely to happen if the producer worked).

The application _subscribes_ to the topic (again, this should not have any errors if the producer worked).
The logging will show the consumer joining the _consumer group_ `consumerGroup`,
which is the default value of the consumer group property.

The application then _polls_, requesting that the kafka brokers send any available messages.
The brokers return the single message in the topic
(rarely, the request may not be fulfilled in time, and zero messages will be returned;
if this happens run the consumer again).

The consumer is using the default _auto-commit_ option,
meaning that when the Apache Kafka client polls and reads a message,
it tells the brokers that the message has been successfully received and should not be sent again.

The application will print information about the message:
```
read record key='key-0', value='sample message 0', topicPartition=TOPIC-0 offset=0
```

The application closes the Apache Kafka consumer client, which generates more logging.

**On the Kafka brokers:**
- Your topic still has only one message, at offset 0.
- There is one _commit offset_ for the topic, recording that consumer group `consumerGroup` will next read offset 1. (This was set because of the auto-commit option.)

Because there is only one message, if you run the consumer again, the poll will not receive any messages.
That is, the brokers have recorded that the next message to be sent to this consumer group is at offset 1,
and there is no such message, so nothing is sent.


### Add a Consumer Group

(This assumes you have run the previous steps, and your topic has one message waiting to be consumed.)

Each consumer belongs to a _consumer group_, identified by a string.
Multiple consumer groups can read from a topic, with each group getting all the messages in the topic.
The Kafka brokers keep a separate commit offset for each consumer group.

To illustrate this, run the consumer again, using a group name different from the default `consumerGroup`, e.g.

```
java -jar target/consumer01-0.1.1.jar -a APIKEY -b BOOTSTRAP -t TOPIC -g theNewGroup
```

The application will read 0 messages.
This is because this is the first time that the Kafka brokers have received a poll from this consumer group,
so they do not have a consumer offset for the group.
The default behavior in this case is to assign the group the _latest_ offset,
as if it had already read all the messages.
A later example will show how to change this behavior.

**On the Kafka brokers:**
- Your topic still has one message.
- The commit offset for `consumerGroup` is 1 (because a message was read and auto-commited).
- The commit offset for `theNewGroup` is 1 (because it was assigned the latest offset).

Now add three more messages to the topic, changing the message content:

```
java -jar target/producer01-0.1.1.jar -a APIKEY -b BOOTSTRAP -t TOPIC -n 3 -m "next batch %d"
```

The producer will print:

```
** Successfully sent message 0, topicPartition=TOPIC-0 offset 1
** Successfully sent message 1, topicPartition=TOPIC-0 offset 2
** Successfully sent message 2, topicPartition=TOPIC-0 offset 3
```

**On the Kafka brokers:**
- Your topic has four messages at offsets 0 through 4.
- The commit offset for `consumerGroup` is still 1.
- The commit offset for `theNewGroup` is still 1.

If you now run the consumer with the new group,
```
java -jar target/consumer01-0.1.1.jar -a APIKEY -b BOOTSTRAP -t TOPIC -g theNewGroup
```

It will read and print these three messages:
```
read record key='key-0', value='next batch 0', topicPartition=TOPIC-0 offset=1
read record key='key-1', value='next batch 1', topicPartition=TOPIC-0 offset=2
read record key='key-2', value='next batch 2', topicPartition=TOPIC-0 offset=3
```

**On the Kafka brokers:**
- Your topic has four messages at offsets 0 through 4.
- The commit offset for `consumerGroup` is still 1.
- The commit offset for `theNewGroup` is 4.


### Reading All Messages with Earliest Offset

Some applications may want a new consumer group to read all available messages.
This can be done by setting a configuration parameter, available in this sample consumer with the `-o earliest` option.
For example if we run the consumer with a third group (assuming the state at the end of the previous example):

```
java -jar target/consumer01-0.1.1.jar -a APIKEY -b BOOTSTRAP -t TOPIC -g thirdGroup -o earliest
```

The consumer will start reading at the earliest offset 0, and will print
```
read record key='key-0', value='sample message 0', topicPartition=TOPIC-0 offset=0
read record key='key-0', value='next batch 0', topicPartition=TOPIC-0 offset=1
read record key='key-1', value='next batch 1', topicPartition=TOPIC-0 offset=2
read record key='key-2', value='next batch 2', topicPartition=TOPIC-0 offset=3
```

**On the Kafka brokers:**
- Your topic has four messages at offsets 0 through 4.
- The commit offset for `consumerGroup` is 1.
- The commit offset for `theNewGroup` is 4.
- The commit offset for `thirdGroup` is 4 (it started at 0, and has read all four messages).


### Limiting the Number of Messages Read

The consumer's poll operation returns zero or more messages.
The number of messages is limited by several configuration properties.
For example there is a configurable limit on the size in bytes of the data returned from the servers,
and the number of messages will be limited to the number that can fit within that limit.
There is also a configuration to limit the number of messages read which is available in this sample consumer
using the `-p NUMBER` option.

There are several reasons an application might need to impose such a limit.
The most important is that a consumer must poll frequently
or the Kafka brokers will assume that it has terminated and remove it.
This prevents other consumers in its group from reading messages until the brokers finish the cleanup.
Thus, a consumer must ensure that it can process all received messages within the configured polling time.
Later samples will show techniques for ensuring this behavior.

To see the behavior when limiting the number of messages received,
poll with a new consumer group, starting at the earliest offset, but only reading one message at a time:
```
java -jar target/consumer01-0.1.1.jar -a APIKEY -b BOOTSTRAP -t TOPIC -g thirdGroup -o earliest -p 1
```

Each time this is run, the consumer will print one message until all the messages have been read.

### Message Commit

In the previous examples the consumer was running with auto-commit,
meaning that the Apache Kafka client updates the commit offset when a message is received.
The sample consumer has an option to disable auto-commit.

Poll with yet another consumer group, starting with the earliest offset, and disable auto-commit with `-c`:
```
java -jar target/consumer01-0.1.1.jar -a APIKEY -b BOOTSTRAP -t TOPIC -g fourthGroup -o earliest -c false
```

The consumer will print all the messages starting at offset 0.

If you run the above again, you will see that the consumer again prints all the messages.
Auto-commit is disabled, but this consumer has no other mechanism for committing messages;
this is definitely _not_ best practice in Kafka!
Applications may want to disable auto-commit, but only because they have their own commit strategy.
Typically this means committing an offset after the message has been processed,
thereby ensuring each message is consumed and processed at least once.
Later samples will show this behavior.


### Long-Running Producers and Consumers

None of the above examples have been particularly realistic, and certainly not Kafka best practices,
because the producer has been run briefly to add messages to the topic,
then the consumer has been run to poll once and process the messages returned.
It is more normal for both producers and consumers to be long-lived processes,
with the producers generating a stream of messages into the Kafka topic
and the consumers polling repeatedly and processing the messages.
The sample applications have options to do this. You will need to open two shell windows to run both applications at once.

Start the consumer first, using the `-l true` option so it continues polling:
```
java -jar target/consumer01-0.1.1.jar -a APIKEY -b BOOTSTRAP -t TOPIC -l true
```

You will see the polling occurring at 10-second intervals, printing that 0 records were read.

Now start the producer in a separate shell, sending a large number of messages and with a 1-second delay between each message:
```
java -jar target/producer01-0.1.1.jar -a APIKEY -b BOOTSTRAP -t TOPIC -n 1000 -d 1
```

You will see the producer generating messages, and the consumer will start to read and print these.
Note that when a message is available, the poll returns the message quickly;
it does not wait the full 10 seconds (which is a parameter to the poll request).

When you've seen enough, use control-C to stop both the producer and consumer.
Note that there is some logging after you hit control-C, showing that the Apache Kafka producer and consumer clients
have been closed; this is because both applications use a shutdown hook to ensure the client is closed.
Applications should make every effort to close clients, since failing to do so will cause operations on the Kafka brokers
which can negatively impact performance.
