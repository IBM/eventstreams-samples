# eventstreams-samples/java-apache/05-metrics/consumer

This directory contains a simple Kafka consumer that uses the Apache Kafka client
to read string-formatted messages from a Kafka topic. To build and run the sample:

1. Run `mvn package`. This creates the executable jar file `target/consumer05-1.0.0.jar`.
2. Run `java -jar target/consumer05-1.0.0.jar [arguments...]`

## Arguments

The program takes the following arguments:

- `-a APIKEY` (required): The "apikey" from the service credentials
- `-b BOOTSTRAP` (required): The "bootstrap_endpoints" from the service credentials
- `-t TOPIC` (required): The topic name, which must already exist
- `-g GROUP` (optional): The name of the consumer group; defaults to `consumerGroup`

Sample usage:

```
java -jar target/consumer05-1.0.0.jar \
    -a APIKEY_REDACTED \
    -b "kafka-0.mh-UUID-0000.us-south.containers.appdomain.cloud:9093,kafka-1.mh-UUID-0000.us-south.containers.appdomain.cloud:9093,kafka-2.mh-UUID-0000.us-south.containers.appdomain.cloud:9093" \
    -t my-test-topic \
    -g my-consumer-group
```

## Metrics

Metrics can be used to gain operational visibility into the performance and health of your applications. You can measure message delivery end to end, to assure every message is delivered from producer to consumer, measure how long messages take to be delivered, this can assist in helping any source of problems in your cluster.

The most fundamental function of the consumer is to consume/read incoming messages from kafka brokers.
There are a lot of metrics to measure for the consumer, you can check this [link](https://docs.bmc.com/docs/PATROL4Kafka/10/kafka-consumer-metrics-kfk_consumer_metrics-724762910.html) for more information about different metrics. 

In this example, we will be measuring the following metrics:

1. request-rate        : The average number of requests sent per second to the broker.
2. incoming-byte-rate  : The average number of incoming bytes received per second from all servers.

We are going to add the metrics we want to run against our application to a List in the beginning of our program:

    `final static List<String> metricsOfInterest = List.of("request-rate", "incoming-byte-rate");`

    Our application takes this list and checks to make sure that each metric is a valid metric and prints the metrics out to the console. 

