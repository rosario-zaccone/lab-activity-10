#### Software Architecture and Platforms - a.y. 2025-2026

## Lab Activity #10 - 20251128 

v1.1.0-20251130

**Event-Driven Architectures and Microservices**
- Event store technology example: [**Apache Kafka**](https://kafka.apache.org/) 
  - "An open-source distributed event streaming platform" - [watching together this video intro](https://kafka.apache.org/intro)
  - [Background and Context](https://developer.confluent.io/faq/apache-kafka/architecture-and-terminology/)
  - [Kafka Documentation](https://kafka.apache.org/documentation/)
    - [Architecture](https://kafka.apache.org/39/documentation/streams/architecture)
  - Kafka [Quick start](https://kafka.apache.org/quickstart)
    - [Setting up Kafka Using Docker](https://docs.google.com/document/d/1sGcs2UHeAx8lrca5PuMeGTZVGq7NIBm_oFyQhe5jFuc/edit?usp=sharing)
      - using Docker Compose with `kafka-deplo.yaml` config file
      - user guides: [Kafka docs](https://developer.confluent.io/confluent-tutorials/kafka-on-docker/), [Docker docs](https://docs.docker.com/guides/kafka/) 
  - Working with Kafka - Kafka clients
    - [Kafka clients in Java](https://docs.confluent.io/kafka-clients/java/current/overview.html)
    - Simple examples in repo - `sap.kafka` package
  - Dashboard Tools for Kafka: [Offset Explorer](https://www.kafkatool.com/) (formerly Kafka Tool)
    - Using Offset Explorer GUI tool as a simple dashboard for Kafka
      - setting the  Bootstrap servers property to connect to the Kafka server to listen at port 9092 or 29092 (if using Docker) for the host machine
- Documenting API in Event-Driven Architectures: [**AsyncAPI**](https://www.asyncapi.com/) initiative
  - Kafka case
    - [tutorial from AsyncAPI](https://www.asyncapi.com/docs/tutorials/kafka)
    - [tutoria  from Kafka](https://www.confluent.io/de-de/events/kafka-summit-europe-2021/getting-started-with-asyncapi-how-to-describe-your-kafka-cluster/)
    - [Terminology mapping](https://dalelane.co.uk/blog/?p=4219)
- **TTT Game System case study**
  - Focus on `ttt-game-service` microservice: making it event-driven, using Kafka 
    - designing event channels - both static and dynamic, inspired by REST principles
      - static event channels, functioning as a single entry point (à-la-REST)
        - `create-game-requests` (input)
        - `create-game-requests-approved` (output)
        - `create-game-requests-rejected` (output)  
        - `new-game-created` (output) 
      - dynamic event channels
        - to join a game identified by `<gameId>`
          - `game-<gameId>-join-requests` (input)
          - `game-<gameId>-join-requests-approved` (output)
          - `game-<gameId>-join-requests-rejected` (output)
        - to make a move by a player session identified by `<playerSessionId>`
          - `session-<playerSessionId>-move-requests` (input)
          - `session-<playerSessionId>-move-requests-approved` (output)
          - `session-<playerSessionId>-move-requests-rejected` (output)
        - about the events of game identified by `<gameId>`
          - `game-<gameId>-events` (output)
    - adding an event-driven controller
    - implementing event sourcing in `ttt-game-service` with an in-memory event store
  - [TTT Game Service AsyncAPI spec](./ttt-game-service/doc/async-api-spec.yaml)
    - including some main fields
    - verified with [AsyncAPI Studio](https://studio.asyncapi.com/)
  - Creating new proxies in other services interacting with `ttt-game-service`
    - `ttt-lobby-service`
    - `ttt-api-gateway`
  - Running
    - simple interaction example with the single service:
      - running the broker `docker run -p 9092:9092 apache/kafka:4.1.1`
      - running the service: `ttt_game_service_infrastructure.GameServiceNoDockerMain`
      - running  `ttt_game_service_infrastructure.SimpleInteractionExample`
    - full TTT Game System
      - setting up all services and broker with `docker compose up`
      - interacting with the API gateway as seen in previous labs
  - TTT Game


       
   
