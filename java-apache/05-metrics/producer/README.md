# eventstreams-samples/java-apache/05-metrics/producer

This directory contains a simple Kafka producer that uses the Apache Kafka client
to write a series of string-format messages to a Kafka topic. To build and run the sample:

1. Run `mvn package`. This creates the executable jar file `target/producer05-1.0.0.jar`.
2. Run `java -jar target/producer05-1.0.0.jar [arguments...]`

## Arguments

The program takes the following arguments:

- `-a APIKEY` (required): The "apikey" from the service credentials
- `-b BOOTSTRAP` (required): The "bootstrap_endpoints" from the service credentials
- `-t TOPIC` (required): The topic name, which must already exist
- `-k KEY` (optional): The message key; defaults to `key-%d`
- `-m MESSAGE` (optional): The message content; defaults to `sample message %d`
- `-n COUNT` (optional): The number of messages to generate; defaults to 1

Sample usage:

```
java -jar target/producer05-1.0.0.jar \
    -a APIKEY_REDACTED \
    -b "kafka-0.mh-UUID-0000.us-south.containers.appdomain.cloud:9093,kafka-1.mh-UUID-0000.us-south.containers.appdomain.cloud:9093,kafka-2.mh-UUID-0000.us-south.containers.appdomain.cloud:9093" \
    -t my-test-topic \
    -k "my-key-%d"
    -m "my sample message %d"
    -n 100
```

## Metrics

Metrics can be used to gain operational visibility into the performance and health of your applications. You can measure message delivery end to end, to assure every message is delivered from producer to consumer, measure how long messages take to be delivered, this can assist in helping any source of problems in your cluster.

The most fundamental function of the producer is to push messages to Kafka brokers.
There are a lot of metrics to measure for the producer, you can check this [link](https://docs.bmc.com/docs/PATROL4Kafka/10/kafka-producer-metrics-kfk_producer_metrics-724762953.html) for more information about different metrics. 

In this example, we will be measuring the following metrics:

1. batch-size-avg     : The average number of bytes sent per partition per request
2. outgoing-byte-rate : The average number of bytes sent per second to the broker.

We are going to add the metrics we want to run against our application to a List in the beginning of our program:

    private final static List<String> metricsOfInterest = List.of("batch-size-avg","outgoing-byte-rate");

    Our application takes this list and checks to make sure that each metric is a valid metric and prints the metrics out to the console. 

