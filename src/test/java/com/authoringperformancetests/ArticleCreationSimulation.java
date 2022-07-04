package com.authoringperformancetests;

import io.gatling.javaapi.core.ActionBuilder;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Random;

import static com.authoringperformancetests.RequestUtils.*;
import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.asLongAs;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.details;
import static io.gatling.javaapi.core.CoreDsl.doIf;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class ArticleCreationSimulation extends Simulation {

  HttpProtocolBuilder httpProtocol =
      http.baseUrl(BASE_URL)
          .acceptHeader(HEADER_JSON)
          .userAgentHeader(AGENT_HEADER)
          .disableFollowRedirect();

  ScenarioBuilder scn =
      CoreDsl.scenario("Load Test Creating Article")
          .exec(
              session ->
                  session
                      .set(ID, DEFAULT_SESSION_ATTRIBUTE_VALUE)
                      .set(PUBLISH_URL, DEFAULT_SESSION_ATTRIBUTE_VALUE)
                      .set(RETRY_CODE, NOT_FOUND))
          .exec(createArticleRequest())
          .pause(1)
          .exec(updateArticleRequest())
          .pause(1)
          .exec(publishArticleRequest())
          .pause(1)
          //.exec(validatePublishedArticle())
          .pause(1);

  public ArticleCreationSimulation() throws IOException {
    this.setUp(scn.injectOpen(rampUsers(500).during(Duration.ofMinutes(5))))
        .assertions(
            global().successfulRequests().percent().gt(85.0),
            details(CREATE_ARTICLE)
                .responseTime()
                .percentile(PERCENTILE)
                .lt(CREATE_RESPONSE_TIME_THRESHOLD),
            details(UPDATE_ARTICLE)
                .responseTime()
                .percentile(PERCENTILE)
                .lt(UPDATE_RESPONSE_TIME_THRESHOLD),
            details(PUBLISH_ARTICLE)
                .responseTime()
                .percentile(PERCENTILE)
                .lt(PUBLISH_RESPONSE_TIME_THRESHOLD))
        .protocols(httpProtocol);
  }

  private ActionBuilder createArticleRequest() throws IOException {

    Random random = new Random();

    return http(CREATE_ARTICLE)
        .post(CONTENT_ENDPOINT)
        .header(CONTENT_TYPE, HEADER_JSON)
        .basicAuth("Telegraph", "VO9?~A2BC*VtqG")
        .body(StringBody(
            "{\n" +
                "  \"headline\": \"new perf tests " + random.nextLong() + "\",\n" +
                "  \"commentingStatus\": false,\n" +
                "  \"contentType\": \"article\",\n" +
                "  \"kicker\": \"test\",\n" +
                "  \"evergreen\": true,\n" +
                "  \"section\": \"/content/telegraph/news\",\n" +
                "  \"storyType\": \"standard\"\n" +
                "}"
        ))
        .check(status().is(CREATED))
        .check(jsonPath("$..id").exists().saveAs(ID));
  }

  private ChainBuilder updateArticleRequest() throws IOException {

    String body = new String(Files.readAllBytes(Paths.get(ARTICLE_UPDATE_JSON)));

    return doIf(session -> (!session.get(ID).equals("_")))
        .then(
            exec(
                http(UPDATE_ARTICLE)
                    .put(UPDATE_ENDPOINT)
                    .header(CONTENT_TYPE, HEADER_JSON)
                    .basicAuth("Telegraph", "VO9?~A2BC*VtqG")
                    .body(StringBody(body))
                    .check(status().is(OK))
                    .check(jsonPath("$..externalPath").exists().saveAs(PUBLISH_URL))));
  }

  private ChainBuilder publishArticleRequest() throws IOException {

    String body = new String(Files.readAllBytes(Paths.get(PUBLISH_JSON)));

    return doIf(session -> (!session.get(PUBLISH_URL).equals(DEFAULT_SESSION_ATTRIBUTE_VALUE)))
        .then(
            exec(
                http(PUBLISH_ARTICLE)
                    .post(PUBLISH_ENDPOINT)
                    .header(CONTENT_TYPE, HEADER_JSON)
                    .basicAuth("Telegraph", "VO9?~A2BC*VtqG")
                    .body(StringBody(body))
                    .check(status().is(CREATED))));
  }

  private ChainBuilder validatePublishedArticle() {
    return doIf(session -> (!session.get(PUBLISH_URL).equals(DEFAULT_SESSION_ATTRIBUTE_VALUE)))
        .then(
            asLongAs(session -> session.get(RETRY_CODE).equals(NOT_FOUND))
                .on(
                    exec(
                        http(VALIDATE_PUBLISH)
                            .get(PUBLISHER_URL)
                            .check(status().not(NOT_FOUND).saveAs(RETRY_CODE)))));
  }
}
