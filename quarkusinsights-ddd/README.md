# quarkusinsights-rest-03

## Subdomains

- Programming - manages submission of abstracts and scheduling episodes
- People - manages profiles of hosts and speakers
- Engagement - manages viewer interaction, email subscriptions, comments, and ratings

## Events
Events are the things the business cares about.
- EpisodeScheduled
- EpisodeUpdated
- EpisodeLive
- EpisodeEnded
- EpisodeOnDemand

## Adapters
Adapters connect the application to external systems and services, such as databases, message queues, and third-party APIs.
- REST
- Database
- Kafka

## Data Transfer Objects (DTOs)
DTOs are used to transfer data between different parts of the application, such as between the application and between the application and external services.

## Commands
Commands are requests to perform an action in the system. They are used to encapsulate the intent to perform an action and are typically sent to the application services.

## Value Objects
Value objects encapsulate data that has no identity, such as dates, times, and monetary amounts. They are used to represent concepts that are meaningful to the business domain, such as an episode's duration or a speaker's biography.

## Application Services
Application services orchestrate the business logic and coordinate the interactions between different parts of the application.

## Domain Services
Domain services encapsulate business logic that does not have another logical location.

## Aggregates
Aggregates are a group of domain objects that are treated as a single unit. They are used to encapsulate the business logic and ensure that the invariants of the aggregate are maintained.

## Repositories
Repositories are responsible for persisting and retrieving domain objects from a data store. They provide a consistent interface for accessing and manipulating data, and are used by the application services to interact with the domain objects.

### Entities
Persisted data.

### Mappers
Convert between domain objects and Entities.
