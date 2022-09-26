# eventstreams-samples/java-apache/03-topics/topics

This directory contains a simple application that uses the Apache Kafka admin client
to create, delete, update, and inspect topics.
To build and run the sample:

1. Run `mvn package`. This creates the executable jar file `target/topics03-1.0.0.jar`.
2. Run `java -jar target/topics03-1.0.0.jar [arguments...]`

## Arguments

The first argument to the application must be the command to run:
- `create` to create a topic.
- `delete` to delete a topic.
- `describe` to give information about a topic.
- `update` to change the configuration of a topic.
- `list` to list all topic names.

The following arguments are required for all commands:

- `-a APIKEY` (required): The "apikey" from the service credentials
- `-b BOOTSTRAP` (required): The "bootstrap_endpoints" from the service credentials

The following arguments are used by some commands:
- `-t TOPIC`: The topic name; required for create, delete, and describe.
- `-p PARTITIONS`: The number of partitions; used by create; defaults to 1.
- `-r RETENTION_TIME`: The configured retention time in milliseconds; used by create and update; defaults to 0, which uses the Kafka system's default.

