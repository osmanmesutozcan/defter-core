Getting
=========================

Where to find more information:
-------------------------------

* The [Axon Reference Guide](https://docs.axoniq.io/reference-guide/) is definitive guide on the Axon Framework and Axon Server.
* Visit [www.axoniq.io](https://www.axoniq.io) to find out about AxonIQ, the team behind the Axon Framework and Server.
* Subscribe to the [AxonIQ Youtube channel](https://www.youtube.com/AxonIQ) to get the latest Webinars, announcements, and customer stories.
* The latest version of the Giftcard App can be found [on GitHub](https://github.com/AxonIQ/giftcard-demo).
* Docker images for Axon Server are pushed to [Docker Hub](https://hub.docker.com/u/axoniq).

Core App
----------------

### Structure of the App
The Core application is split into four parts, using four sub-packages of `io.defter.core.app`:
* The `api` package contains the ([Kotlin](https://kotlinlang.org/)) sourcecode of the messages and entity. They form the API (sic) of the application.
* The `command` package contains the GiftCard Aggregate class, with all command- and associated eventsourcing handlers.
* The `query` package provides the query handlers, with their associated event handlers.
* The `core` package provides library code
* The `client` package provides graphql client layer

### Building the Giftcard app from the sources
To build the demo app, simply run the provided [Maven wrapper](https://www.baeldung.com/maven-wrapper):

```
./mvnw clean package
```

Running the Giftcard app
------------------------

The simplest way to run the app is by using the Spring-boot maven plugin:

```
docker-compose up -d
./mvnw spring-boot:run
```

The Web GUI can be found at [`http://localhost:8024`](http://localhost:8024).
The Graphql endpoint can be found at [`http://localhost:8080`](http://localhost:8080).
