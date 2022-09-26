# eventstreams-samples/java-apache/08-commit/consumer

This directory contains a simple Kafka consumer that uses the Apache Kafka client
to read string-formatted messages from a Kafka topic. To build and run the sample:

1. Run `mvn package`. This creates the executable jar file `target/consumer08-1.0.0.jar`.
2. Run `java -jar target/consumer08-1.0.0.jar [arguments...]`

## Arguments

The program takes the following arguments:

- `-a APIKEY` (required): The "apikey" from the service credentials
- `-b BOOTSTRAP` (required): The "bootstrap_endpoints" from the service credentials
- `-t TOPIC` (required): The topic name, which must already exist
- `-g GROUP` (optional): The name of the consumer group; defaults to `consumerGroup`
- `-c COMMITCOUNT` (optional): The number of records to process before committing the offset; defaults to 10
- `-f FAILURECOUNT` (optional): The number of records to process before simulating a failure; defaults to 0 (no failure).

Sample usage:

```
java -jar target/consumer08-1.0.0.jar \
    -a APIKEY_REDACTED \
    -b "kafka-0.mh-UUID-0000.us-south.containers.appdomain.cloud:9093,kafka-1.mh-UUID-0000.us-south.containers.appdomain.cloud:9093,kafka-2.mh-UUID-0000.us-south.containers.appdomain.cloud:9093" \
    -t my-test-topic \
    -g my-consumer-group
    -p 10
    -d 5
```

