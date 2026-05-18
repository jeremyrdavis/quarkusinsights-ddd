# CLAUDE_EPISODES.md

This file provides guidance to Claude Code (claude.ai/code) when working in this repository to build the **Episodes subdomain** of QuarkusInsights.

## Read the spec first

`SPEC_EPISODES.md` at the repo root is the single source of truth for what to build. It defines:

- the Episode aggregate and its lifecycle (the invariant table is the contract)
- the architectural rules (domain purity, ports & adapters, the three-place uniqueness rule, etc.)
- the required value objects, domain events, and REST surface
- the "done" criteria

Read it before doing anything. If `SPEC_EPISODES.md` is silent on a question, prefer the simplest idiomatic Quarkus answer over inventing requirements. Don't add scope it doesn't ask for.

## Tech stack

Versions live in `SPEC_EPISODES.md` under "Stack" — defer to it. The working notes that matter when you're in the code:

- Single Maven module. `cd` to the Maven project root (look for `pom.xml`) before running any `mvn` / `mvnw` commands.
- Hibernate ORM with Panache in **repository style, not active record** — JPA entities live in the persistence-adapter package, never on aggregates.
- Quarkus Dev Services spins up PostgreSQL automatically in dev/test; `application.properties` should stay empty for datasource config. Docker must be running.
- Quarkus REST + Jackson for JSON adapters.

## Common commands

From the Maven project root:

| Task | Command |
|---|---|
| Dev mode (live reload, Dev UI at `/q/dev/`) | `./mvnw quarkus:dev` |
| Run unit + `@QuarkusTest` tests | `./mvnw test` |
| Run a single test | `./mvnw test -Dtest=ClassName` (or `-Dtest=ClassName#methodName`) |
| Continuous testing in dev mode | press `r` in the `quarkus:dev` console |
| Run failsafe integration tests | `./mvnw verify -DskipITs=false` |
| Package (thin jar in `target/quarkus-app/`) | `./mvnw package` |
| Native build | `./mvnw package -Dnative` |

## Testing conventions

`SPEC_EPISODES.md` "Testing approach" defines the four-layer pyramid (pure-domain JUnit, application-service with in-memory adapters, persistence with `@TestTransaction`, REST with REST Assured). Follow it.

Optional pair pattern: a `*IT.java` companion extending a `*Test.java` under `@QuarkusIntegrationTest` re-runs the suite against the packaged artifact. Gate it with `<skipITs>true</skipITs>` in the pom so it's only triggered by `mvn verify -DskipITs=false` or the `native` profile. The spec doesn't require this pattern — add it only if you want native verification coverage.

## Layout

The spec calls for hexagonal layering. Use this package shape under `<group>.<artifact>.programming/` (or whatever you call the single bounded context):

- `domain/` — pure POJO aggregate, value objects, domain events, repository ports. **No imports from `jakarta.persistence`, `jakarta.ws.rs`, `io.quarkus.*`.**
- `application/` — application service(s), command records.
- `infrastructure/persistence/` — JPA entities, mapper, repository implementation.
- `interfaces/` — JAX-RS resource, request/response DTOs, exception mappers.

A small sibling `shared/` package holds the cross-context primitives the spec lists: `PersonId`, `DomainEvent`, `DomainEventPublisher`. `PersonId` is an opaque UUID wrapper — **no Person aggregate exists in this codebase**.

## What this subdomain is not

The broader QuarkusInsights application has three other bounded contexts (People, Engagement, Catalog). They are explicitly **out of scope here**. Don't:

- Add a Person aggregate, even if the Episode references `PersonId`s.
- Add Comments, Ratings, or any engagement features.
- Add a public catalog, projections, Qute templates, or any HTML/UI.
- Add subscribers (`@Observes`) to the domain events the Episode emits. The publisher port must exist and must be called, but events going nowhere yet is fine — when other contexts arrive later, they subscribe; the Episode aggregate stays untouched.

The deliverable is REST + Postgres only.

## Definition of done

Every criterion in `SPEC_EPISODES.md` "What 'done' looks like" should pass. Walk through the list before declaring complete — especially the curl-driven lifecycle flow (schedule → submit abstract → assign presenter → assign speaker → go-live → publish) and the duplicate-episode-number 409 path that verifies the three-place uniqueness rule.

`.gitignore` typically excludes `CLAUDE_EPISODES.md` and `.claude/` — they're local-only working context for a Claude Code session and don't need to be committed.
