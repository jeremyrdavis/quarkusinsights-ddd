# QuarkusInsights — Episodes Subdomain Specification

You are building the **Episodes subdomain** of a podcast / live-stream management system called QuarkusInsights, as a single Quarkus monolith using Domain-Driven Design and Hexagonal Architecture. This file is the contract. It describes the subdomain and the architectural rules; it does not prescribe class names, file layout idioms, or implementation details. You pick those.

The bar: **every business rule is enforced by structure, not by convention.** A maintainer should be able to delete the comments and still not be able to violate the rules. An adapter swap (REST → CLI, Panache → jOOQ) should require touching only the adapter, never the domain.

This subdomain is one slice of a larger application. Three other bounded contexts (People, Engagement, Catalog) exist conceptually but are **out of scope here** — you build only Episodes. Where Episodes references those contexts, it does so by opaque ID only (see "Cross-context references" below).

## Stack

- Quarkus 3.x (latest stable), Java 21+ — single Maven module
- Hibernate ORM with Panache (repository style, not active record)
- PostgreSQL via Quarkus Dev Services (no manual datasource config required in dev/test)
- Quarkus REST + Jackson for JSON
- JUnit 5, REST Assured

## The subdomain

QuarkusInsights publishes one show at a time. Each show has a sequential number, a title, an air date, an abstract, one or more presenters (hosts), and one or more speakers (guests). Shows progress through this lifecycle:

```
   SCHEDULED ──goLive──> LIVE ──publish──> PUBLISHED
       │
       └─ cancel ──> CANCELED   (terminal)
```

PUBLISHED and CANCELED are terminal — there is no "unpublish" or "uncancel". An episode in those states stays there.

This subdomain owns the Episode aggregate, its lifecycle, and its presenter/speaker assignments. It does **not** own people (presenters and speakers are referenced by ID only — there is no Person aggregate in this codebase yet), comments, ratings, or any public catalog/UI surface.

## Architectural rules

These are non-negotiable. Each rule states *what* and *why*.

- **Domain layer is pure POJOs.** No imports from `jakarta.persistence`, `jakarta.ws.rs`, `io.quarkus.*`, or any framework. *Why:* the domain is the part that must not change when the framework changes. If it imports the framework, the framework is now load-bearing for the business rules.

- **Aggregates enforce their own invariants in their own methods.** Validation does not live in REST controllers, application services, or database constraints alone. A behavior method either accepts the call and records the outcome, or refuses it with a typed exception. *Why:* if the rule lives elsewhere, it can be bypassed by any new entry point (CLI, message consumer, admin tool).

- **Persistence is an adapter.** The aggregate (a POJO) and the row-shaped persistence model are different objects, translated by an explicit mapper. The aggregate must not extend any framework base class or carry any persistence annotations. *Why:* the aggregate's lifecycle is owned by the domain, not by Hibernate. JPA's reflection-based field access and lazy-loading proxies routinely break invariants the aggregate is trying to protect.

- **REST is an adapter.** Resources translate JSON ↔ commands and translate domain exceptions to HTTP codes. They contain no branching on business state. Validation happens in value-object constructors at the boundary, not in the resource. *Why:* the same domain operation must be callable from anywhere — CLI, message consumer, scheduled job — without re-implementing rules.

- **Cross-context references are by ID only.** When the Episode aggregate refers to a Person who is a presenter, it holds that Person's ID, never a direct Person reference or a Person class from this codebase. The `PersonId` type lives in this subdomain as an opaque UUID-wrapping value object; no Person aggregate exists here. *Why:* direct references defeat aggregate boundaries — a mutation through one aggregate would silently change what another aggregate sees, and consistency invariants stop being enforceable. The People context will own the Person aggregate when it's added later; until then, this subdomain trusts the IDs it is given.

- **Domain events are recorded by the aggregate, dispatched by the application service.** Behavior methods on the aggregate append events to an internal buffer; the application service reads the buffer after `save(...)` and dispatches them via a publisher port. There are no event subscribers in this subdomain — events go to a publisher port that, in this scope, may dispatch them via CDI `Event<>` for future contexts to subscribe to, or simply log them. *Why:* keeping the buffer/publish pattern in place means adding a subscriber later (e.g., a Catalog projection) needs no changes to the Episode aggregate or service.

- **When a rule cannot be enforced by any single aggregate, enforce it in three places.** The episode number uniqueness rule ("no two episodes share the same number") is the canonical example: a single Episode aggregate cannot check it. Enforce in (1) the application service via a repository query before scheduling, (2) a database UNIQUE constraint as a race-protecting safety net, (3) the persistence adapter, which catches the constraint-violation exception and re-throws the domain exception so callers never see infrastructure errors. *Why:* the service check is fast and gives clean error messages; the DB constraint catches concurrent races; the adapter translation keeps the domain's exception vocabulary consistent. Skipping any of the three creates a real hole.

- **Domain events are facts, immutable, named in past tense.** Their payloads carry IDs and primitives, never full aggregate references. Their timestamp is for audit and ordering — never for business decisions (clock skew makes wall-time unreliable for that).

- **Documentation is part of the deliverable.** Every public class has class-level Javadoc explaining its role in the architecture (which layer) and its responsibility in domain terms. Every public method has Javadoc with a one-sentence summary in domain language, plus `@param`, `@return` (when non-void), and `@throws` for each exception thrown by intent. Aggregate behavior methods additionally document pre-state, postcondition, and emitted events. Trivial accessors (record components, generated getters) do not need their own Javadoc. Use `/** */` blocks; single-line `//` is not Javadoc. Commented-out code is forbidden — delete it; git keeps history.

## Subdomain layout

The single bounded context here is **Programming** (alternative name: Episodes). It lives in its own package under a single Maven module. It follows this layering pattern: domain (POJOs, ports), application (use cases, commands), infrastructure (persistence adapters), and interfaces (REST adapters).

A small **shared kernel** package holds cross-context primitives: `PersonId` (opaque UUID wrapper, the only cross-context reference Episode needs), a `DomainEvent` marker interface, and a `DomainEventPublisher` port. When other contexts arrive later, they will share this kernel.

### The Episode aggregate

The source of truth for shows. Episodes are scheduled, filled with content, taken live, and published or canceled.

The Episode aggregate has one inside-aggregate entity, **Abstract** (the show's synopsis), with its own ID. Abstract is replaced — not edited in place — on resubmission.

Invariant table:

| Operation | Pre-state | Postcondition | Domain event |
|---|---|---|---|
| `schedule(number, title, airDate)` | factory (no aggregate exists) | status = SCHEDULED; airDate ≥ today | `EpisodeScheduled` |
| `submitAbstract(text)` | status = SCHEDULED | abstract is set; previous abstract (if any) is replaced | `AbstractSubmitted` |
| `assignPresenter(personId)` | status ∈ {SCHEDULED, LIVE} | presenters contains personId (idempotent) | `PresenterAssigned` only if newly added |
| `assignSpeaker(personId)` | status ∈ {SCHEDULED, LIVE} | speakers contains personId (idempotent) | `SpeakerAssigned` only if newly added |
| `goLive()` | status = SCHEDULED ∧ airDate ≤ today | status = LIVE | `EpisodeWentLive` |
| `publish()` | status = LIVE ∧ abstract ≠ null ∧ \|presenters\| ≥ 1 ∧ \|speakers\| ≥ 1 | status = PUBLISHED | `EpisodePublished` |
| `cancel(reason)` | status = SCHEDULED ∧ reason is non-blank | status = CANCELED | `EpisodeCanceled` |

Value objects:

- **Episode ID** — opaque UUID-wrapping value object identifying the aggregate.
- **Episode number** — ≥ 1, unique across all episodes (the cross-aggregate rule).
- **Episode title** — non-blank, 1–200 chars.
- **Air date** — a calendar date. Accepts any date including past dates (rehydration from storage requires this); the "must not be in the past" rule is checked in `schedule(...)` against today, not in the value object itself.
- **Abstract text** — non-blank, 100–5000 chars.
- **Abstract ID** — opaque UUID-wrapping value object identifying the inside-aggregate Abstract entity.
- **Episode status** — enumeration: SCHEDULED, LIVE, PUBLISHED, CANCELED.
- **Person ID** (shared kernel) — opaque UUID-wrapping value object referencing a Person owned by another bounded context not built here. Treated as data, never dereferenced.

The Abstract entity has its own ID (separate from the Episode ID) and a submission timestamp; it is replaced wholesale on each `submitAbstract` call.

### Domain events

Seven events, one per state-changing operation: `EpisodeScheduled`, `AbstractSubmitted`, `PresenterAssigned`, `SpeakerAssigned`, `EpisodeWentLive`, `EpisodePublished`, `EpisodeCanceled`. Each is an immutable record carrying IDs/primitives plus an `occurredAt` timestamp and implements the shared `DomainEvent` marker.

Idempotent operations (presenter and speaker assignment) emit no event on a no-op.

### Application service

A single service exposing one method per operation above (seven write methods plus a load-by-id query if needed by adapters). Each method:

1. Loads the aggregate (or, for `schedule`, performs the cross-aggregate uniqueness pre-check and constructs a new one).
2. Invokes the aggregate behavior.
3. Persists via the repository port.
4. Drains the aggregate's recorded events and publishes them through the publisher port.

Service methods are `@Transactional`. The service contains no business logic — load, call, save, publish.

### REST surface

A resource exposing one endpoint per operation under a stable URL prefix (e.g., `POST /api/episodes`, `POST /api/episodes/{id}/abstract`, `POST /api/episodes/{id}/presenters`, `POST /api/episodes/{id}/go-live`, `POST /api/episodes/{id}/publish`, `POST /api/episodes/{id}/cancel`) plus `GET /api/episodes/{id}` for loading. Map domain exceptions to HTTP status codes per the table below; the mapping is one exception type to one status, implemented in dedicated exception-mapper classes (or equivalent), not in the resource methods.

| Exception family | HTTP status |
|---|---|
| Episode not found | 404 |
| State-machine conflict (e.g., publishing what's not live) | 409 |
| Missing precondition (no abstract, no presenter, no speaker on publish) | 409 |
| Duplicate episode number | 409 |
| Air date in the past (on schedule) or not yet reached (on go-live) | 400 |
| Value-object validation failure (out-of-range, blank, null) | 400 |

## Testing approach

The test pyramid reflects the architectural layers. Aim for fast tests at the bottom, broader tests as you climb.

- **Pure-domain tests** — JUnit only, no `@QuarkusTest`. Cover every value-object validation rule and every aggregate behavior method including each precondition exception. These run in milliseconds and need no infrastructure.
- **Application-service tests** — Plain JUnit with an in-memory repository implementation (a `HashMap`-backed implementation of the repository port, used only in tests) and a recording event publisher (an implementation of the publisher port that appends events to a list for assertion). These exercise the full service-to-aggregate path without booting Quarkus. They also verify the cross-aggregate uniqueness pre-check.
- **Persistence tests** — `@QuarkusTest` with `@TestTransaction` (so each test rolls back). Cover the mapper in both directions, the repository's load and save paths, and the database constraint enforcement (specifically, that a duplicate episode number raises the domain exception via the adapter's translation, not a raw `PersistenceException`).
- **REST tests** — `@QuarkusTest` with REST Assured. Cover the happy path of each endpoint plus at least one error case per HTTP status.

Quarkus Dev Services brings up PostgreSQL automatically for `@QuarkusTest` runs. No manual datasource configuration is needed in `application.properties`; leave it empty.

## What "done" looks like

The build is complete when:

1. `./mvnw test` is green — pure-domain JUnit tests (no Quarkus boot) plus `@QuarkusTest` integration tests for adapters.
2. `./mvnw quarkus:dev` boots without errors. `curl /q/health/ready` returns 200.
3. The end-to-end happy path works via `curl`: schedule an episode → submit an abstract → assign a presenter → assign a speaker → go live → publish. Each step returns 200/201 with a body reflecting the updated state, and a final `GET /api/episodes/{id}` shows status PUBLISHED with the abstract, presenters, and speakers populated.
4. Submitting a second episode with a duplicate number returns 409 with a domain "episode number already exists" error, not a `PSQLException` or a 500. Same for publishing without an abstract, going live before the air date, etc. — every domain refusal returns the correct status with a structured error body.
5. Grepping the domain packages for `jakarta.persistence`, `jakarta.ws.rs`, or `io.quarkus.` returns zero hits.
6. Every domain reference to a Person is a `PersonId`, not a Person class. There is no Person aggregate in this codebase.
7. The aggregate's recorded events are drained and dispatched via the publisher port on every successful service call — verified by the application-service test's recording publisher.

## Out of scope

- Other bounded contexts: People (Person aggregate), Engagement (comments, ratings), Catalog (public read model + UI). These exist in the broader application but are not part of this subdomain.
- A landing page, episode history page, or any HTML / Qute / HTMX surface. The deliverable here is REST only.
- Authentication, authorization, account management.
- Production deployment manifests beyond what Quarkus generates by default.
- Custom observability (no OpenTelemetry/Prometheus config beyond defaults).
- Cloud-specific persistence configuration. Dev Services handles dev/test.
- Real message brokers. CDI events (or simple in-process dispatch) are sufficient.
- Internationalization, full-text search, pagination.
- Email or notification side-effects.
- Background scheduling (no quartz, no `@Scheduled`).
- Subscribers for the domain events the aggregate emits. The publisher port must exist and must be called, but no `@Observes` consumers are required in this scope — events go nowhere yet (or to a logger). When People/Engagement/Catalog arrive, they will subscribe; the Episodes subdomain stays untouched.
