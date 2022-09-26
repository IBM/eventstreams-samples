# eventstreams-samples/java-apache/04-batch

The producer application in this directory illustrates some techniques for batching messages to improve throughput.
See the [producer](./producer/README.md) README for instructions on building and running the sample.

## Concepts

The Apache Kafka producer client send operation is fundamentally asynchronous.
When the producer calls the `send` method, the client queues the message in a buffer and returns a java `Future`.
At some later time, the client will send a request containing the message to the Kafka brokers and receive a response.
Once the response is received, the `Future` can be resolved,
and either returns the metadata for the successfully-written message, or an exception for a failure.

The [02-sample](../02-sample/README.md) producer forces the asynchronous operation to behave as if it were synchronous,
by calling the `Future.get` method returned by each `send`.
This causes the Kafka client to send a request to the brokers with the queued message and wait for the response.
This technique is simple, and if an error occurs the message can easily be re-sent.
However it will reduce throughput considerably since messages are sent and added to partitions on the servers one at a time.

The Apache Kafka producer client can buffer messages and send them in batches.
This will reduce overhead and improve throughput,
as a single request containing all the messages is sent to the brokers,
and a single response with the status of each of the messages is returned.
This does mean that the responses for the messages must be processed asynchronously,
which can complicate the handling of message failures.
Another risk is that the producer application may crash when some messages have been buffered but not sent,
which will cause those messages to be lost.

There are several techniques for controlling batching, some of which are illustrated in the sample.
- Resolution of the `Future` returned by a `send` will cause any buffered messages to be sent to the brokers.
- The producer `flush` method causes any buffered messages to be sent, as does the `close` method.
- The property `linger.ms` determines how long the client will wait between sending batches of messages, and thus (to an extent) the amount of batching that is done. This parameter must be used with care, as it will introduce additional latency when the producer is generating messages at a relatively low rate.

The Apache Kafka producer client also allows a callback function to be provided to the `send` method.
When the message in that `send` has been sent and the response received,
the callback function will be invoked. 
The callback receives two arguments, a `RecordMetadata` which is non-null for a successful send,
and an `Exception` which is non-null for a failure.
(Resolving the `Future.get` produces the same result, either by returning the `RecordMetadata` or throwing the `Exception`.)
This callback can be used to record success or failure of the operation, or to attempt recovery from a failure.

The sample producer illustrates these techniques. The command-line arguments include:
- `-r futureCount`: After sending `futureCount` messages, resolve the future of the last message sent.
- `-f flushCount`: After sending `flushCount` messages, call the `flush` method.
- `-l lingerMilliseconds`: Set the `linger.ms` property to the given value.

To show the effect of the linger property, there is a delay of 100 milliseconds between sends.
The sample prints when sending a message, resolving a future, or calling the flush or close methods.
Each send includes a callback which prints the success or failure of the message send.

A run will thus print "sending" messages as `send` is called.
Eventually one of four things will happen:
- The `linger.ms` limit is reached, causing the buffered messages to be sent.
- The `futureCount` is reached and the producer resolves a future, causing the buffered messages to be sent.
- The `flushCount` is reached and the producer calls `flush`, causing the buffered messages to be sent.
- All messages have been sent and the producer calls `close`, causing the buffered messages to be sent.

When the messages are sent, all the associated callbacks are made and print the outcome.
So the output of the program will be a repeated sequence of:
- A series of "sending" messages.
- No message, if the linger time is reached; or a "future", "flushing", or "closing" message.
- A series of "successful send" or "failed send" messages from the callbacks.

For examining the behavior, it is simplest to set only one of the options at a time.
For example the resolution of futures can be examined with:
```
java -jar target/producer04-1.0.0.jar -a APIKEY -b BOOTSTRAP -t TOPIC -n 100 -r 8
```

The use of flush can be examined with:
```
java -jar target/producer04-1.0.0.jar -a APIKEY -b BOOTSTRAP -t TOPIC -n 100 -f 8
```

The effect of `linger.ms` can be examined with:
```
java -jar target/producer04-1.0.0.jar -a APIKEY -b BOOTSTRAP -t TOPIC -n 100 -l 500
```
On the last, since there is a 100 ms delay between sends, the expectation is that batches will be sent after 5 sends.

All of these will show that `close` also sends any buffered messages.

