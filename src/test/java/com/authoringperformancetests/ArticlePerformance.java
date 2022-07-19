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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.authoringperformancetests.RequestUtils.*;
import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.asLongAsDuring;
import static io.gatling.javaapi.core.CoreDsl.details;
import static io.gatling.javaapi.core.CoreDsl.doIf;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class ArticlePerformance extends Simulation {

  HttpProtocolBuilder httpProtocol =
      http.baseUrl(BASE_URL)
          .acceptHeader(HEADER_JSON)
          .userAgentHeader(AGENT_HEADER)
          .disableFollowRedirect();

  Iterator<Map<String, Object>> feeder =
      Stream.generate((Supplier<Map<String, Object>>) () -> {
            String uuid = UUID.randomUUID().toString();
            return Collections.singletonMap("uuid", uuid);
          }
      ).iterator();

  ScenarioBuilder scn =
      CoreDsl.scenario("Load Testing Articles")
          .exec(
              session -> {
                session
                    .set(ARTICLE_ID, DEFAULT_SESSION_ATTRIBUTE_VALUE)
                    .set(ARTICLE_PUBLISH_URL, DEFAULT_SESSION_ATTRIBUTE_VALUE)
                    .set(RETRY_CODE, NOT_FOUND);
                return session;
              })
          .feed(feeder)
          .exec(createArticleRequest())
          .pause(1)
          .exec(updateArticleRequest())
          .pause(1)
          .exec(publishArticleRequest())
          .pause(1)
          .exec(validatePublishedArticle());

  public ArticlePerformance() throws IOException {
    this.setUp(scn.injectOpen(rampUsers(USERS).during(Duration.ofMinutes(TIME))))
        .assertions(
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
                .lt(PUBLISH_RESPONSE_TIME_THRESHOLD),
            details(VALIDATE_PUBLISH).successfulRequests().percent().is(100.0)
        )
        .protocols(httpProtocol);
  }

  private ActionBuilder createArticleRequest() throws IOException {

    String body = new String(Files.readAllBytes(Paths.get(ARTICLE_CREATE_JSON)));

    return http(CREATE_ARTICLE)
        .post(CONTENT_ENDPOINT)
        .header(CONTENT_TYPE, HEADER_JSON)
        .basicAuth("Telegraph", "VO9?~A2BC*VtqG")
        .body(StringBody(body))
        .check(status().is(CREATED))
        .check(jsonPath("$..id").exists().saveAs(ARTICLE_ID));
  }

  private ChainBuilder updateArticleRequest() throws IOException {

    String body = new String(Files.readAllBytes(Paths.get(ARTICLE_UPDATE_JSON)));

    return doIf(session -> (!session.get(ARTICLE_ID).equals(DEFAULT_SESSION_ATTRIBUTE_VALUE)))
        .then(
            exec(
                http(UPDATE_ARTICLE)
                    .put(UPDATE_ENDPOINT)
                    .header(CONTENT_TYPE, HEADER_JSON)
                    .basicAuth("Telegraph", "VO9?~A2BC*VtqG")
                    .body(StringBody(body))
                    .check(status().is(OK))
                    .check(jsonPath("$..externalPath").exists().saveAs(ARTICLE_PUBLISH_URL))));
  }

  private ChainBuilder publishArticleRequest() throws IOException {

    String body = new String(Files.readAllBytes(Paths.get(PUBLISH_JSON)));

    return doIf(session -> (!session.get(ARTICLE_PUBLISH_URL).equals(DEFAULT_SESSION_ATTRIBUTE_VALUE)))
        .then(
            exec(
                http(PUBLISH_ARTICLE)
                    .post(PUBLISH_ENDPOINT)
                    .header(CONTENT_TYPE, HEADER_JSON)
                    .basicAuth("Telegraph", "VO9?~A2BC*VtqG")
                    .body(StringBody(body))
                    .check(status().is(CREATED).saveAs(RETRY_CODE))));
  }

  private ChainBuilder validatePublishedArticle() {

    return asLongAsDuring(session -> !session.get(RETRY_CODE).equals(OK), Duration.ofMillis(PUBLISH_RESPONSE_TIME_THRESHOLD)).on(
        exec(
            http(VALIDATE_PUBLISH)
                .get(PUBLISHER_URL)
                .basicAuth("Telegraph", "VO9?~A2BC*VtqG")
                .check(status().saveAs(RETRY_CODE)))
            .pause(1)
    ).exec(
        http(VALIDATE_PUBLISH)
            .get(PUBLISHER_URL)
            .basicAuth("Telegraph", "VO9?~A2BC*VtqG")
            .check(status().is(OK).saveAs(RETRY_CODE)));
  }
}
