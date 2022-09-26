# eventstreams-samples/java-apache/04-batch/producer

This directory contains a Kafka producer that uses the Apache Kafka client
to write a series of messages to a Kafka topic. To build and run the sample:

1. Run `mvn package`. This creates the executable jar file `target/producer04-1.0.0.jar`.
2. Run `java -jar target/producer04-1.0.0.jar [arguments...]`

## Arguments

The program takes the following arguments:

- `-a APIKEY` (required): The "apikey" from the service credentials
- `-b BOOTSTRAP` (required): The "bootstrap_endpoints" from the service credentials
- `-t TOPIC` (required): The topic name, which must already exist
- `-k KEY` (optional): The message key; defaults to `key-%d`
- `-m MESSAGE` (optional): The message content; defaults to `sample message %d`
- `-n COUNT` (optional): The number of messages to generate; defaults to 1
- `-r futureCount`: After sending `futureCount` messages, resolve the future of the last message sent.
- `-f flushCount`: After sending `flushCount` messages, call the `flush` method.
- `-l lingerMilliseconds`: Set the `linger.ms` property to the given value.


Sample usage:

```
java -jar target/producer04-1.0.0.jar \
    -a APIKEY_REDACTED \
    -b "kafka-0.mh-UUID-0000.us-south.containers.appdomain.cloud:9093,kafka-1.mh-UUID-0000.us-south.containers.appdomain.cloud:9093,kafka-2.mh-UUID-0000.us-south.containers.appdomain.cloud:9093" \
    -t my-test-topic \
    -k "my-key-%d" \
    -m "my sample message %d" \
    -n 100 \
    -f 15
```

