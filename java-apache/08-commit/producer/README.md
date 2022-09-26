# eventstreams-samples/java-apache/08-commit/producer

This directory contains a simple Kafka producer that uses the Apache Kafka client
to write string-format messages to a Kafka topic. To build and run the sample:

1. Run `mvn package`. This creates the executable jar file `target/producer08-1.0.0.jar`.
2. Run `java -jar target/producer08-1.0.0.jar [arguments...]`

## Arguments

The program takes the following arguments:

- `-a APIKEY` (required): The "apikey" from the service credentials
- `-b BOOTSTRAP` (required): The "bootstrap_endpoints" from the service credentials
- `-t TOPIC` (required): The topic name, which must already exist
- `-k KEY` (optional): The message key; defaults to `key-%d`
- `-m MESSAGE` (optional): The message content; defaults to `sample message %d`
- `-n COUNT` (optional): The number of messages to produce; defaults to 1
- `-d DELAY` (optional): The delay in seconds between messages; defaults to 0

Sample usage:

```
java -jar target/producer08-1.0.0.jar \
    -a APIKEY_REDACTED \
    -b "kafka-0.mh-UUID-0000.us-south.containers.appdomain.cloud:9093,kafka-1.mh-UUID-0000.us-south.containers.appdomain.cloud:9093,kafka-2.mh-UUID-0000.us-south.containers.appdomain.cloud:9093" \
    -t my-test-topic \
    -k "my-key-%d" \
    -m "my sample message-%d" \
    -n 1000 \
    -d 1
```

