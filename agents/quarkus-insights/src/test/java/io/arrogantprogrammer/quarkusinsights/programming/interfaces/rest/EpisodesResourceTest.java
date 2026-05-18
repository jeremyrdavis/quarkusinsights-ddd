package io.arrogantprogrammer.quarkusinsights.programming.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class EpisodesResourceTest {

    private static final AtomicInteger NUMBERS = new AtomicInteger(5_000);

    private static int nextNumber() {
        return NUMBERS.getAndIncrement();
    }

    private static String today() {
        return LocalDate.now().toString();
    }

    private static String future() {
        return LocalDate.now().plusDays(7).toString();
    }

    private static String validAbstract() {
        return "x".repeat(150);
    }

    private String scheduleEpisode(int number, String airDate) {
        return given()
                .contentType("application/json")
                .body(Map.of(
                        "number", number,
                        "title", "Episode " + number,
                        "airDate", airDate))
                .when().post("/api/episodes")
                .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .body("id", notNullValue())
                .body("status", equalTo("SCHEDULED"))
                .extract().path("id");
    }

    @Test
    void fullLifecycleHappyPath() {
        int number = nextNumber();
        String id = scheduleEpisode(number, today());

        given().contentType("application/json")
                .body(Map.of("text", validAbstract()))
                .when().post("/api/episodes/{id}/abstract", id)
                .then().statusCode(200)
                .body("synopsis.text", equalTo(validAbstract()));

        given().contentType("application/json")
                .body(Map.of("personId", UUID.randomUUID().toString()))
                .when().post("/api/episodes/{id}/presenters", id)
                .then().statusCode(200)
                .body("presenters", hasSize(1));

        given().contentType("application/json")
                .body(Map.of("personId", UUID.randomUUID().toString()))
                .when().post("/api/episodes/{id}/speakers", id)
                .then().statusCode(200)
                .body("speakers", hasSize(1));

        given().when().post("/api/episodes/{id}/go-live", id)
                .then().statusCode(200)
                .body("status", equalTo("LIVE"));

        given().when().post("/api/episodes/{id}/publish", id)
                .then().statusCode(200)
                .body("status", equalTo("PUBLISHED"));

        given().when().get("/api/episodes/{id}", id)
                .then().statusCode(200)
                .body("status", equalTo("PUBLISHED"))
                .body("synopsis.text", equalTo(validAbstract()))
                .body("presenters", hasSize(1))
                .body("speakers", hasSize(1));
    }

    @Test
    void getReturns404ForUnknownEpisode() {
        given().when().get("/api/episodes/{id}", UUID.randomUUID().toString())
                .then().statusCode(404)
                .body("code", equalTo("episode_not_found"));
    }

    @Test
    void duplicateNumberReturns409Domain() {
        int number = nextNumber();
        scheduleEpisode(number, future());

        given().contentType("application/json")
                .body(Map.of(
                        "number", number,
                        "title", "duplicate",
                        "airDate", future()))
                .when().post("/api/episodes")
                .then()
                .statusCode(409)
                .body("code", equalTo("duplicate_episode_number"));
    }

    @Test
    void pastAirDateReturns400() {
        given().contentType("application/json")
                .body(Map.of(
                        "number", nextNumber(),
                        "title", "ancient",
                        "airDate", LocalDate.now().minusDays(1).toString()))
                .when().post("/api/episodes")
                .then().statusCode(400)
                .body("code", equalTo("invalid_air_date"));
    }

    @Test
    void goLiveBeforeAirDateReturns400() {
        int number = nextNumber();
        String id = scheduleEpisode(number, future());

        given().when().post("/api/episodes/{id}/go-live", id)
                .then().statusCode(400)
                .body("code", equalTo("invalid_air_date"));
    }

    @Test
    void publishBeforeLiveReturns409StateConflict() {
        int number = nextNumber();
        String id = scheduleEpisode(number, today());

        given().when().post("/api/episodes/{id}/publish", id)
                .then().statusCode(409)
                .body("code", equalTo("illegal_episode_state"));
    }

    @Test
    void publishWithoutAbstractReturns409MissingPrecondition() {
        int number = nextNumber();
        String id = scheduleEpisode(number, today());

        given().contentType("application/json")
                .body(Map.of("personId", UUID.randomUUID().toString()))
                .when().post("/api/episodes/{id}/presenters", id)
                .then().statusCode(200);
        given().contentType("application/json")
                .body(Map.of("personId", UUID.randomUUID().toString()))
                .when().post("/api/episodes/{id}/speakers", id)
                .then().statusCode(200);
        given().when().post("/api/episodes/{id}/go-live", id)
                .then().statusCode(200);

        given().when().post("/api/episodes/{id}/publish", id)
                .then().statusCode(409)
                .body("code", equalTo("missing_publish_precondition"));
    }

    @Test
    void blankTitleReturns400FromBeanValidation() {
        given().contentType("application/json")
                .body(Map.of(
                        "number", nextNumber(),
                        "title", "",
                        "airDate", future()))
                .when().post("/api/episodes")
                .then().statusCode(400);
    }

    @Test
    void shortAbstractReturns400FromValueObject() {
        int number = nextNumber();
        String id = scheduleEpisode(number, future());

        given().contentType("application/json")
                .body(Map.of("text", "too short"))
                .when().post("/api/episodes/{id}/abstract", id)
                .then().statusCode(400)
                .body("code", is(notNullValue()));
    }

    @Test
    void cancelMakesEpisodeTerminal() {
        int number = nextNumber();
        String id = scheduleEpisode(number, future());

        given().contentType("application/json")
                .body(Map.of("reason", "guest dropped out"))
                .when().post("/api/episodes/{id}/cancel", id)
                .then().statusCode(200)
                .body("status", equalTo("CANCELED"))
                .body("cancellationReason", equalTo("guest dropped out"));
    }
}
