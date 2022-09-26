# eventstreams-samples/java-apache/01-kafka-intro/TwoPartition.md

These examples require a topic with at least two partitions.
These cannot be run with a Lite plan, which limits you to a single topic and partition.
You can use a Standard plan to run these.

You will need to follow the [instructions](../../Instructions.md) to create an Event Streams instance,
service credentials, and a topic with two partitions.

Build both the producer and the consumer following the instructions in their README files.
The programs are run using `java -jar <jarfile> [arguments...]`. Note that both require these arguments:

- `-a APIKEY`: the "apikey" from the service credentials
- `-b BOOTSTRAP`: the "bootstrap_endpoints" from the service credentials
- `-t TOPIC`: the name of the topic you created


### Produce Multiple Messages into Partitions

Run the producer application with the default arguments and create 10 messages, i.e.

```
java -jar target/producer0-0.1.1.jar -a APIKEY -b BOOTSTRAP -t TOPIC
```

The application will send ten messages, with keys `key-0`, `key-1`, ... `key-9` and content `sample message 0`, ... `sample message 9`.
Note the information printed for each message, which will look something like:
```
** Successfully sent message 0, topicPartition=TOPIC-1, offset 0
** Successfully sent message 1, topicPartition=TOPIC-0, offset 0
** Successfully sent message 2, topicPartition=TOPIC-0, offset 1
** Successfully sent message 3, topicPartition=TOPIC-1, offset 1
** Successfully sent message 4, topicPartition=TOPIC-1, offset 2
** Successfully sent message 5, topicPartition=TOPIC-0, offset 2
** Successfully sent message 6, topicPartition=TOPIC-0, offset 3
** Successfully sent message 7, topicPartition=TOPIC-1, offset 3
** Successfully sent message 8, topicPartition=TOPIC-1, offset 4
** Successfully sent message 9, topicPartition=TOPIC-1, offset 5
```

The topic has two partitions, meaning that it is split into two ordered lists of messages on the Kafka brokers.
As each message is sent, it is assigned to one of the two partitions.
This assignment can be controlled, but with the default configuration we are using,
the message is assigned using a hash of the message key.
For example in the above, the key `key-0` was hashed, and that hash caused the message
to be put in partition 1 of the topic, shown as `TOPIC-1`.
Similarly the hash of `key-1` put that message in partition 0 of the topic, shown as `TOPIC-0`.

Messages with the same key will always be assigned to the same partition,
so it is important when using the default hash-based partition assignment
to  make sure the messages have different keys.
If not, they will all end up in the same partition, which is not what we normally want when using more than one partition.
(You can see this by running the producer several times, using `-n 1` to create messages.
The message key will always be `key-0`, and they will all be assigned to the same partition.)

The hashing will distribute messages roughly equally between the partitions, though not exactly as we can see in the above example
where four messages were assigned to TOPIC-0 and six messages to TOPIC-1.
Also note that each partition has its own offsets, indexing from the beginning of the partition.

**On the Kafka brokers, assuming the above distribution:**
- The topic partition 0 has four messages, at offsets 0 through 3. These are message numbers 1, 2, 5, and 6.
- The topic partition 1 has six messages, at offsets 0 through 5. These are message numbers 0, 3, 4, 7, 8, and 9.
- There are no commit offsets.


### Consume the Messages with a Single Consumer

(This assumes you have run the previous step, and your topic has the ten messages in two partitions.)

Run the consumer application with the default arguments, but using the earliest commit offset option
(as shown in the [one-partition examples](./OnePartition.md), this means it will start reading at the beginning of the partition), i.e.

```
java -jar target/consumer0-0.1.1.jar -a APIKEY -b BOOTSTRAP -t TOPIC -o earliest
```

Kafka assigns a topic's partitions to the consumers that subscribe to the topic.
Since there is only one consumer at this point, Kafka will assign both partitions to the consumer,
meaning that it will be able to read from both of them.
If you check the logging, you will see a message showing this, similar to:
```
ConsumerCoordinator - [Consumer clientId=eventstreams-java-sample-consumer0, groupId=consumerGroup] Adding newly assigned partitions: TOPIC-1, TOPIC-0
```

When the consumer polls, it will receive all the messages from _one_ of the two partitions.
It will print these, and also auto-commit that partition to show all the messages have been read.
For example, it might receive from partition 1 first, and print
```
** 6 records read
** read record key='key-0', value='sample message 0', topicPartition=TOPIC-1 offset=0
** read record key='key-3', value='sample message 3', topicPartition=TOPIC-1 offset=1
** read record key='key-4', value='sample message 4', topicPartition=TOPIC-1 offset=2
** read record key='key-7', value='sample message 7', topicPartition=TOPIC-1 offset=3
** read record key='key-8', value='sample message 8', topicPartition=TOPIC-1 offset=4
** read record key='key-9', value='sample message 9', topicPartition=TOPIC-1 offset=5
```

**On the Kafka brokers:**
- The topic partition 0 still has four messages, at offsets 0 through 3. These are message numbers 1, 2, 5, and 6.
- The topic partition 1 still has six messages, at offsets 0 through 5. These are message numbers 0, 3, 4, 7, 8, and 9.
- There is one _commit offset_ for the topic partition 1, recording that consumer group `consumerGroup` will next read offset 6. (This was set because of the auto-commit option.)


If you run the consumer again, still using the `-o earliest` flag, the poll will return the messages from the other partition 0.
(Rarely, it may check partition 1 again, and return 0 messages).  The application will print
```
** 4 records read
** read record key='key-1', value='sample message 1', topicPartition=TOPIC-0 offset=0
** read record key='key-2', value='sample message 2', topicPartition=TOPIC-0 offset=1
** read record key='key-5', value='sample message 5', topicPartition=TOPIC-0 offset=2
** read record key='key-6', value='sample message 6', topicPartition=TOPIC-0 offset=3
```

**On the Kafka brokers:**
- The topic partition 0 still has four messages, at offsets 0 through 3. These are message numbers 1, 2, 5, and 6.
- The topic partition 1 still has six messages, at offsets 0 through 5. These are message numbers 0, 3, 4, 7, 8, and 9.
- There is one _commit offset_ for the topic partition 0, recording that consumer group `consumerGroup` will next read offset 4. (This was set by the auto-commit option.)
- There is one _commit offset_ for the topic partition 1, recording that consumer group `consumerGroup` will next read offset 6. (This was set in the previous run of the consumer, and is not changed when messages are consumed from the other partition.)


### Running One Consumer

As with the one-partition examples, the above examples are neither very realistic nor best practices.
Kafka producers and consumers are intended to be long-running processes, with producers generating messages
and consumers repeatedly polling them. The samples have options to allow this.
You will need to open multiple shell windows to run all the applications.

First let us see what happens if we run one consumer in the multiple-partition case.
Start the consumer first, using the `-l true` option so it continues polling:
```
java -jar target/consumer0-0.1.1.jar -a APIKEY -b BOOTSTRAP -t TOPIC -l true
```

You will see the consumer start up, and you will again see the ConsumerCoordinator assign it both partitions.
Assuming you haven't written any new messages into the topic, the consumer will start polling
and printing at 10-second intervals that 0 records were read.

Now start the producer in a separate shell, sending a large number of messages and with a 1-second delay between each message:
```
java -jar target/producer0-0.1.1.jar -a APIKEY -b BOOTSTRAP -t TOPIC -n 1000 -d 1
```

You will see the producer start to generate messages, and write them into the two partitions.
You will also see the consumer start to read and print the messages.
It reads messages from both partitions, since both partitions have been assigned to it.

You will almost certainly see the messages are read and printed in the same order that they were produced,
as indicated by the message number.
Kafka does not guarantee this when multiple partitions are being read by a single consumer;
it can only guarantee the ordering within a partition.
We saw this in the previous example, where the messages were consumed in two batches,
first all the messages from partition 1 (message numbers 0,3,4,7,8,9), then those of partition 0 (message numbers 1,2,5,6).
However in this case with the one-second delay between message produce operations,
the consumer will have time to poll and consume each message before the next arrives,
making the partitioning somewhat irrelevant.

When you've seen enough, use control-C to stop both the producer and consumer.
(See the discussion of clean shutdown of Apache Kafka clients in the one-partition examples.)


### Running Two Consumers

Each Kafka partition can be read by only one consumer in any given consumer group.
The usual reason for having multiple partitions is so that you can run one consumer per partition,
thus processing the messages concurrently.
To see how this works, we will run two consumer applications.

Start the first consumer with the loop option:
```
java -jar target/consumer0-0.1.1.jar -a APIKEY -b BOOTSTRAP -t TOPIC -l true
```

Note in its logging that it has been assigned the two partitions. It will start polling and printing 0 messages.

Now start the second consumer in a new shell:
```
java -jar target/consumer0-0.1.1.jar -a APIKEY -b BOOTSTRAP -t TOPIC -l true
```

You will see some logging in _both_ shells, as the ConsumerCoordinator takes one of the two partitions
away from the first consumer, and assigns it to this new consumer.

(At this point, if you control-C one of the consumers, you will see logging in the other consumer
as the killed consumer's partition is re-assigned to the active partition. Kafka does this rebalancing
as necessary, so that each partition is assigned to some active consumer.)

With the two consumers running, start the producer:
```
java -jar target/producer0-0.1.1.jar -a APIKEY -b BOOTSTRAP -t TOPIC -n 1000 -d 1
```

You will see it produce messages, and you will see the two consumers consuming the messages,
each from their assigned partition.
The messages processed by either consumer from its single partition are consumed in the order produced,
but there is no necessary ordering between the partitions.
For example if one of the consumers always took a little longer to process a message than the other,
we would eventually see its message number lagging behind the other's, and there would be a growing backlog
of unprocessed messages in its partition.
This is one reason that applications following Kafka best practices monitor their performance metrics,
and provide operators with warnings when such performance issues arise.

### Three Consumers?

If you start a third consumer, you will again see some logging for partition assignment.
The end result will be two consumers each assigned one partition, with the third consumer having no partition.
When the producer is run, only two of the consumers will receive messages and do work.
So it seems that there is no reason to have more consumers than partitions.

However, there are reasons where this is sometimes useful, which you can see with these samples.
Start up the three consumers and the producer. Two consumers will start processing messages
while the third polls unproductively.

Now, control-C one of the two active consumers. You will see some logging as the partitions are reassigned,
and then the formerly-inactive consumer will start processing messsages; it has been assigned the partition
of the consumer that was killed.
Having such a backup ready to take over if an active consumer dies can often be useful for ensuring overall system performance.
If the backup consumer were _not_ running, the freed partition would be assigned to one of the already-busy consumers,
which would then be consuming from two partitions increasing its load and impacting its performance.

