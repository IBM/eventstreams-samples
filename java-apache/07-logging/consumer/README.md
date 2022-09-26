# eventstreams-samples/java-apache/07-logging/consumer

This directory contains a simple Kafka consumer that uses the Apache Kafka client
to read string-formatted messages from a Kafka topic. To build and run the sample:

1. Run `mvn package`. This creates the executable jar file `target/consumer07-1.0.0.jar`.
2. Run `java -jar target/consumer07-1.0.0.jar [arguments...]`

## Arguments

The program takes the following arguments:

- `-a APIKEY` (required): The "apikey" from the service credentials
- `-b BOOTSTRAP` (required): The "bootstrap_endpoints" from the service credentials
- `-t TOPIC` (required): The topic name, which must already exist
- `-g GROUP` (optional): The name of the consumer group; defaults to `consumerGroup`

Sample usage:

```
java -jar target/consumer07-1.0.0.jar \
    -a APIKEY_REDACTED \
    -b "kafka-0.mh-UUID-0000.us-south.containers.appdomain.cloud:9093,kafka-1.mh-UUID-0000.us-south.containers.appdomain.cloud:9093,kafka-2.mh-UUID-0000.us-south.containers.appdomain.cloud:9093" \
    -t my-test-topic \
    -g my-consumer-group
```



