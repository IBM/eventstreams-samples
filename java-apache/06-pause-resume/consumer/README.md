# eventstreams-samples/java-apache/06-pause-resume/consumer

This directory contains a simple Kafka consumer that uses the Apache Kafka client
to read string-formatted messages from a Kafka topic. To build and run the sample:

1. Run `mvn package`. This creates the executable jar file `target/consumer06-1.0.0.jar`.
2. Run `java -jar target/consumer06-1.0.0.jar [arguments...]`

## Arguments

The program takes the following arguments:

- `-a APIKEY` (required): The "apikey" from the service credentials
- `-b BOOTSTRAP` (required): The "bootstrap_endpoints" from the service credentials
- `-t TOPIC` (required): The topic name, which must already exist
- `-g GROUP` (optional): The name of the consumer group; defaults to `consumerGroup`
- `-p POLLRECORDS` (optional): The maximum number of records to read on each poll; defaults to 500
- `-d PROCESSING_SECONDS` (optional): The (simulated) time required to process each message; defaults to 1 second
- `-i INTERVAL_SECONDS` (optional): The time allowed for processing messages; defaults to 10 second

Sample usage:

```
java -jar target/consumer06-1.0.0.jar \
    -a APIKEY_REDACTED \
    -b "kafka-0.mh-UUID-0000.us-south.containers.appdomain.cloud:9093,kafka-1.mh-UUID-0000.us-south.containers.appdomain.cloud:9093,kafka-2.mh-UUID-0000.us-south.containers.appdomain.cloud:9093" \
    -t my-test-topic \
    -g my-consumer-group
    -p 10
    -d 5
    -i 10
```

