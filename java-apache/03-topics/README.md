# eventstreams-samples/java-apache/03-topics

This directory contains a sample application written in java, using the Apache Kafka clients,
illustrating how to create, delete, and inspect Kafka topics programmatically.
Note that most of the operations demonstrated by this sample can be done using
the Event Streams UI or CLI.
See the [topics](./topics/README.md) README for instructions on building and running the application.

Some of the features of this application, such as creating topics with multiple partitions,
require an Event Streams Enterprise or Standard plan.

## Concepts

For best practice, the creation and deletion of topics should be rare events.
Topics should be treated as long-lived data storage, similar to database tables,
which are created once when developing your overall application architecture.
Producer and consumer applications should write and read topic data,
but in general should not create or delete topics.

Kafka can allow automatic creation of topics by producers and consumers;
that is, if a producer sends to, or consumer subscribes to, an unknown topic, Kafka will create it.
Event Streams **disables** this capability and does not permit users to enable it,
since topic-creation incurs (billable) costs to the user.
Topics can still be created programmatically as this sample illustrates.

The Apache Kafka libraries include an AdminClient which provides administrative operations
on the resources maintained by the Kafka brokers, including topics, consumer groups, and access control lists (among others).
The admin client is created and configured similarly to the producer and consumer clients in other samples;
in particular for Event Streams, the API key and Kafka broker list are used for authentication and operations.
The client should also be closed when the application no longer needs it.

The methods of the admin client are used to request operations on the resources.
These operations are asynchronous; the client sends a request to the broker, which will send back a response when the operation is complete.
In the Java implementation, this is represented by a Future which when resolved returns the operation result,
or (usually) throws an exception if the operation failed.

For example, the `createTopics` method takes a collection of topics which are to be created and returns a `CreateTopicsResult` object.
That object has several methods that return futures, including:
- `all()` returns a future that either resolves successfully if all topics were created, or throws an exception if creation of any topic failed.
- `values()` returns a map from topic name to future, such that the future for a given topic resolves successfully if that topic was created, or throws an exception if creation of that topic failed.
- `config(String topic)` returns a future that either resolves successfully and returns the configuration of the created topic, or throws an exception if creation of that topic failed.


## Topics Sample

See the [TopicsMain.java](./topics/src/main/java/com/ibm/eventstreams/TopicsMain.java) code.

The application parses the command-line arguments
(see [TopicsCLI.java](./topics/src/main/java/com/ibm/eventstreams/TopicsCLI.java)).
It then creates a configuration for an Apache Kafka admin client
(see [TopicsConfiguration.java](./topics/src/main/java/com/ibm/eventstreams/TopicsConfiguration.java)).
As in the producer and consumer samples shown elsewhere, the client uses the API key with SASL authentication to access the brokers.

The application creates an Apache Kafka admin client with the configuration.
It then performs the topic operation specified by the command line arguments.

The first argument to the application must be the topic operation to perform:
- `create` to create a topic.
- `delete` to delete a topic.
- `update` to change the configuration of a topic.
- `describe` to give information about a topic.
- `list` to list all topic names.

The following arguments are required for all commands:

- `-a APIKEY` (required): The "apikey" from the service credentials
- `-b BOOTSTRAP` (required): The "bootstrap_endpoints" from the service credentials

The following arguments are used by some commands:
- `-t TOPIC`: The topic name; required for create, delete, and describe.
- `-p PARTITIONS`: The number of partitions; used by create; defaults to 1.
- `-r RETENTION_TIME`: The configured retention time in milliseconds; used by create and update; defaults to 0, which uses the Kafka system's default.

In all cases, the operations are handled synchronously; the application calls the AdminClient method,
then resolves the future to find the result of the operation.
For example the topic creation operation in method `createTopic` does the following:

1. Create the data structure describing the topic to be created.
1. Call the admin client's `createTopic` method with that data structure (a collection of topic specifications), receiving a `CreateTopicsResult` which provides methods to get futures for the operation results.
1. Resolve the future returned by the `all()`, which either succeeds (with no return value) or throws an exception.
1. If the creation succeeded, resolve the future returned by the `config(topic)` method, which returns the configuration of the created topic (in general it could also throw an exception, but will not in this application because the `all` method succeeded).
1. Print that configuration.

The other commands are structured similarly. In a few cases, such as `updateTopic`, two operations are performed
(to modify the configuration, and to get the modified configuration) and their futures are resolved.

## Commands with Examples

### Create

The `create` command creates a new topic.
- The `-t TOPICNAME` argument is required.
- The `-p PARTITIONS` argument is optional, and defaults to 1 partition.
- The `-r RETENTIONTIME` argument is optional, and defaults to 0, meaning the Kafka default is used.

If the operation is successful, a success message is printed,
followed by the configuration of the newly-created topic.
If the operation fails, the exception is printed.

In this sample, the retention time is used to illustrate how topics can be configured at creation time
using the Kafka admin client. There are many topic configuration parameters which can be set.
Care must be taken when choosing these values,
since some configurations can negatively impact performance.

Sample topic creation, to create topic named `my-topic` with 3 partitions and a retention time of 7200000 milliseconds:
```
java -jar target/topics03-1.0.0.jar \
    create -t my-topic -p 3 -r 7200000 \
    -a APIKEY \
    -b BOOTSTRAP_SERVERS
```

The Event Streams CLI plugin can also be used to create topics. The above command is equivalent to
```
ibmcloud es topic-create --name my-topic --partitions e --config retention.ms=7200000
```


### Delete

The `delete` command deletes an existing topic. It does not request confirmation before deletion!
- The `-t TOPICNAME` argument is required.

If the operation is successful, a success message is printed.
If the operation fails, the exception is printed.

Sample deletion, to delete topic named `my-topic`:
```
java -jar target/topics03-1.0.0.jar \
    delete -t my-topic \
    -a APIKEY \
    -b BOOTSTRAP_SERVERS
```

The Event Streams CLI plugin can also be used to delete topics. The above command is equivalent to
```
ibmcloud es topic-delete --name my-topic
```


### Update

The `update` command changes the configuration of a new topic.
- The `-t TOPICNAME` argument is required.
- The `-r RETENTIONTIME` argument is required.

If the operation is successful, a success message is printed,
followed by the modified configuration of the topic.
If the operation fails, the exception is printed.

In this sample, the retention time is used to illustrate how topics configurations can be changed
using the Kafka admin client. There are many topic configuration parameters which can be set.
Care must be taken when choosing these values,
since some configurations can negatively impact performance.

Sample topic update, to update topic named `my-topic` to have a retention time of 14400000 milliseconds:
```
java -jar target/topics03-1.0.0.jar \
    update -t my-topic -r 14400000 \
    -a APIKEY \
    -b BOOTSTRAP_SERVERS
```

The Event Streams CLI plugin can also be used to update topics. The above command is equivalent to:
```
ibmcloud es topic-update --name my-topic --config retention.ms=14400000
```

### Describe

The `describe` command shows information about an existing topic.
- The `-t TOPICNAME` argument is required.

If the operation is successful, a success message is printed, followed by the topic information.
If the operation fails, the exception is printed.

The topic information includes:
- The topic name
- The number of partitions
  - For each partition, the broker which is the leader, and the brokers which are replicating the partition
- The topic configuration

The partition leader and replicas are Kafka concepts, and describe how the Kafka brokers
are managing the information in the partitions.  For example, the output:
```
    0: leader is broker 1, replicas are [1, 2, 0]
```
shows the leader for partition 0 is broker 1.
This means that when the Apache Kafka clients produce or consume messages of partition 0,
the requests will be routed to broker 1.
The brokers are also exchanging information, and all the information about partition 0
is being replicated to brokers 2 and 0, so if broker 1 is not available for any reason,
the producers and consumers can continue operations by sending requests to either of those brokers.

Sample topic description, of topic named `my-topic`:
```
java -jar target/topics03-1.0.0.jar \
    describe -t my-topic \
    -a APIKEY \
    -b BOOTSTRAP_SERVERS
```

The Event Streams CLI plugin can also be used to describe topics. The above command is equivalent to:
```
ibmcloud es topic my-topic
```
although the information printed is somewhat different.


### List

The `list` command lists all the topic names.

Sample topic list, to list all topics:
```
java -jar target/topics03-1.0.0.jar \
    list \
    -a APIKEY \
    -b BOOTSTRAP_SERVERS
```

The Event Streams CLI plugin can also be used to list topics. The above command is equivalent to:
```
ibmcloud es topics
```

