# eventstreams-samples/java-apache/07-logging

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
java -jar target/consumer07-1.0.0.jar \
    -a APIKEY \
    -b BOOTSTRAP_SERVERS \
    -t TOPIC
```

The logging will show the consumer polling at 10-second intervals, receiving 0 messages each time.

Start the producer in another shell:

```
java -jar target/producer07-1.0.0.jar \
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

## Logging

Logging is an important component of software development. In this sample we will configure logging using log4j framework. It can be used for debugging, auditing, and structured storage of an application's runtime. 

In this sample we are going to be using log4j for logging. Log4j allows for logging in terms of priorities and offers mechanisms to direct logging information to a great variety of destinations, such as database, file, console, etc. 

Log4j has three main components: 

1. loggers:  Responsible for capturing logging information.
2. appenders: Responsible for publishing logging information to various preferred destinations.
3. layouts: Responsible for formatting logging information in different styles.

To configure logging, first you need to add the Log4j dependency to our pom.xml file:
```
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-api</artifactId>
            <version>2.18.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <version>2.18.0</version>
        </dependency>  
```

This ensures the dependency is declared and its required components are installed. 

Next, we must create the log4j2.xml file. This configuration file has all the information regarding what level of logs we want to store, where we want our logs stored and in what format. 

In this sample, the log4j2.xml file is stored under src/main/resources

Now lets look at a basic configuration:

```
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="INFO">
    <Appenders>
        <Console name="ConsoleAppender" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n" />
        </Console>
    </Appenders>
    <Loggers>
        <Root level="debug">
            <AppenderRef ref="ConsoleAppender" />
        </Root>
    </Loggers>
</Configuration>
```

This configuration has one appender, console(stdout). This means that the logs will be printed out on the console. When running the sample using the instructions, you'll see the logs printed out in the console.


This next configuration has two appenders, File and console(stdout): 

```
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="INFO">
    <Appenders>
        <Console name="ConsoleAppender" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n" />
        </Console>
        <File name="FileAppender" fileName="logging-${date:yyyyMMdd}.log" immediateFlush="false" append="true">
            <PatternLayout pattern="%d{yyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </File>
    </Appenders>
    <Loggers>
        <Root level="debug">
            <AppenderRef ref="ConsoleAppender" />
            <AppenderRef ref="FileAppender"/>
        </Root>
    </Loggers>
</Configuration>
```


Adding the file appender now gives you an option to store the logs in the 'logging-{date:yyyyMMdd}.log' file, along with printing out logs on the console. This is the configuration that this sample comes with.

There are different levels of logs that you can log. They are defined as follows:

```
ALL 	All levels including custom levels.
DEBUG 	Designates fine-grained informational events that are most useful to debug an application.
INFO 	Designates informational messages that highlight the progress of the application at coarse-grained level.
WARN 	Designates potentially harmful situations.
ERROR 	Designates error events that might still allow the application to continue running.
FATAL 	Designates very severe error events that will presumably lead the application to abort.
OFF 	The highest possible rank and is intended to turn off logging.
TRACE 	Designates finer-grained informational events than the DEBUG.
```

The levels are ordered in importance, that is ALL < DEBUG < INFO < WARN < ERROR < FATAL < OFF. 

In our log4j2.xml file earlier, the level of the root logger is set to DEBUG, meaning all logs defined at a higher severity than debug and debug will be printed/stored. 

You can change this value to FATAL or ERROR and check your log file to see that the INFO and WARN logs will not be logged:

    `<Root level="ERROR">`

In this sample, we have most of our logs at the INFO level when we are logging information about our topic and partitions:

    `logger.info(String.format("Successfully sent message %s, offset %s\n", messageNum,metadata.offset()))`

We log at an ERROR level when we are running into exceptions while starting our consumer/producer.

    `logger.error(String.format("Caught exception creating producer : ", e))`
