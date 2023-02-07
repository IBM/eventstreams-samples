# eventstreams-samples/java-apache/oauth-client

This directory contains a supporting library when Java client is configuring SASL OAUTHBEARER as the authentication method and wants to obtain token from IBM Cloud IAM service via an IAM API key.

### Build locally

```sh
mvn clean package
```

### Build through Maven in client application

adding below in `pom.xml`'s dependencies

```xml
<dependency>
    <groupId>com.ibm.eventstreams</groupId>
    <artifactId>oauth-client</artifactId>
    <version>0.1.2</version>
</dependency>
```

### Build through Gradle in client application

adding below in `build.gradle`'s dependencies

```gradle
implementation com.ibm.eventstreams:oauth-client:0.1.2
```
