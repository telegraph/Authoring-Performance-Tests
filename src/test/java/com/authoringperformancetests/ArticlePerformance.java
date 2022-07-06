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
import static io.gatling.javaapi.core.CoreDsl.asLongAs;
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
          .exec(publishArticleRequest());

  public ArticlePerformance() throws IOException {
    this.setUp(scn.injectOpen(rampUsers(70).during(Duration.ofMinutes(5))))
        .protocols(httpProtocol);
  }

  private ActionBuilder createArticleRequest() throws IOException {

    return http(CREATE_ARTICLE)
        .post(CONTENT_ENDPOINT)
        .header(CONTENT_TYPE, HEADER_JSON)
        .basicAuth("Telegraph", "VO9?~A2BC*VtqG")
        .body(StringBody(
            "{\n" +
                "  \"headline\": \"Article Load Testing ${uuid}\",\n" +
                "  \"commentingStatus\": false,\n" +
                "  \"contentType\": \"article\",\n" +
                "  \"kicker\": \"test\",\n" +
                "  \"evergreen\": true,\n" +
                "  \"section\": \"/content/telegraph/performance/0/gatling\",\n" +
                "  \"storyType\": \"standard\"\n" +
                "}"
        ))
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
                    .check(status().is(CREATED))));
  }


  private ChainBuilder validatePublishedArticle() {
    return doIf(session -> (!session.get(ARTICLE_PUBLISH_URL).equals(DEFAULT_SESSION_ATTRIBUTE_VALUE)))
        .then(
            asLongAs(session -> session.get(RETRY_CODE).equals(NOT_FOUND))
                .on(
                    exec(
                        http(VALIDATE_PUBLISH)
                            .get(PUBLISHER_URL)
                            .check(status().not(NOT_FOUND).saveAs(RETRY_CODE)))));
  }
}
