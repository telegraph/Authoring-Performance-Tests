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

import static com.authoringperformancetests.RequestUtils.AGENT_HEADER;
import static com.authoringperformancetests.RequestUtils.BASE_URL;
import static com.authoringperformancetests.RequestUtils.CONTENT_ENDPOINT;
import static com.authoringperformancetests.RequestUtils.CONTENT_TYPE;
import static com.authoringperformancetests.RequestUtils.CREATED;
import static com.authoringperformancetests.RequestUtils.CREATE_ARTICLE;
import static com.authoringperformancetests.RequestUtils.CREATE_GALLERY;
import static com.authoringperformancetests.RequestUtils.CREATE_RESPONSE_TIME_THRESHOLD;
import static com.authoringperformancetests.RequestUtils.DEFAULT_SESSION_ATTRIBUTE_VALUE;
import static com.authoringperformancetests.RequestUtils.GALLERY_CREATE_JSON;
import static com.authoringperformancetests.RequestUtils.GALLERY_ID;
import static com.authoringperformancetests.RequestUtils.GALLERY_PUBLISH_URL;
import static com.authoringperformancetests.RequestUtils.GALLERY_UPDATE_JSON;
import static com.authoringperformancetests.RequestUtils.HEADER_JSON;
import static com.authoringperformancetests.RequestUtils.NOT_FOUND;
import static com.authoringperformancetests.RequestUtils.OK;
import static com.authoringperformancetests.RequestUtils.PERCENTILE;
import static com.authoringperformancetests.RequestUtils.PUBLISHER_URL;
import static com.authoringperformancetests.RequestUtils.PUBLISH_ARTICLE;
import static com.authoringperformancetests.RequestUtils.PUBLISH_ENDPOINT;
import static com.authoringperformancetests.RequestUtils.PUBLISH_GALLERY;
import static com.authoringperformancetests.RequestUtils.PUBLISH_JSON;
import static com.authoringperformancetests.RequestUtils.PUBLISH_RESPONSE_TIME_THRESHOLD;
import static com.authoringperformancetests.RequestUtils.RETRY_CODE;
import static com.authoringperformancetests.RequestUtils.TIME;
import static com.authoringperformancetests.RequestUtils.UPDATE_ARTICLE;
import static com.authoringperformancetests.RequestUtils.UPDATE_ENDPOINT;
import static com.authoringperformancetests.RequestUtils.UPDATE_GALLERY;
import static com.authoringperformancetests.RequestUtils.UPDATE_RESPONSE_TIME_THRESHOLD;
import static com.authoringperformancetests.RequestUtils.USERS;
import static com.authoringperformancetests.RequestUtils.VALIDATE_GALLERY_PUBLISH;
import static com.authoringperformancetests.RequestUtils.VALIDATE_PUBLISH;
import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.asLongAsDuring;
import static io.gatling.javaapi.core.CoreDsl.details;
import static io.gatling.javaapi.core.CoreDsl.doIf;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class GalleryPerformance extends Simulation {

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
      CoreDsl.scenario("Load Testing Galleries")
          .exec(
              session -> {
                session
                    .set(GALLERY_ID, DEFAULT_SESSION_ATTRIBUTE_VALUE)
                    .set(GALLERY_PUBLISH_URL, DEFAULT_SESSION_ATTRIBUTE_VALUE)
                    .set(RETRY_CODE, NOT_FOUND);
                return session;
              })
          .feed(feeder)
          .exec(createGalleryRequest())
          .pause(1)
          .exec(updateGalleryRequest())
          .pause(1)
          .exec(publishGalleryRequest())
          .pause(1)
          .exec(validatePublishedGallery());

  public GalleryPerformance() throws IOException {
    this.setUp(scn.injectOpen(rampUsers(USERS).during(Duration.ofMinutes(TIME))))
        .assertions(
            details(CREATE_GALLERY)
                .responseTime()
                .percentile(PERCENTILE)
                .lt(CREATE_RESPONSE_TIME_THRESHOLD),
            details(UPDATE_GALLERY)
                .responseTime()
                .percentile(PERCENTILE)
                .lt(UPDATE_RESPONSE_TIME_THRESHOLD),
            details(PUBLISH_GALLERY)
                .responseTime()
                .percentile(PERCENTILE)
                .lt(PUBLISH_RESPONSE_TIME_THRESHOLD),
            details(VALIDATE_GALLERY_PUBLISH).successfulRequests().percent().is(100.0)
        )
        .protocols(httpProtocol);
  }

  private ActionBuilder createGalleryRequest() throws IOException {

    String body = new String(Files.readAllBytes(Paths.get(GALLERY_CREATE_JSON)));

    return http(CREATE_GALLERY)
        .post(CONTENT_ENDPOINT)
        .header(CONTENT_TYPE, HEADER_JSON)
        .basicAuth("Telegraph", "VO9?~A2BC*VtqG")
        .body(StringBody(body))
        .check(status().is(CREATED))
        .check(jsonPath("$..id").exists().saveAs(GALLERY_ID));
  }

  private ChainBuilder updateGalleryRequest() throws IOException {

    String body = new String(Files.readAllBytes(Paths.get(GALLERY_UPDATE_JSON)));

    return doIf(session -> (!session.get(GALLERY_ID).equals(DEFAULT_SESSION_ATTRIBUTE_VALUE)))
        .then(
            exec(
                http(UPDATE_GALLERY)
                    .put(UPDATE_ENDPOINT)
                    .header(CONTENT_TYPE, HEADER_JSON)
                    .basicAuth("Telegraph", "VO9?~A2BC*VtqG")
                    .body(StringBody(body))
                    .check(status().is(OK))
                    .check(jsonPath("$..externalPath").exists().saveAs(GALLERY_PUBLISH_URL))));
  }

  private ChainBuilder publishGalleryRequest() throws IOException {

    String body = new String(Files.readAllBytes(Paths.get(PUBLISH_JSON)));

    return doIf(session -> (!session.get(GALLERY_PUBLISH_URL).equals(DEFAULT_SESSION_ATTRIBUTE_VALUE)))
        .then(
            exec(
                http(PUBLISH_GALLERY)
                    .post(PUBLISH_ENDPOINT)
                    .header(CONTENT_TYPE, HEADER_JSON)
                    .basicAuth("Telegraph", "VO9?~A2BC*VtqG")
                    .body(StringBody(body))
                    .check(status().is(CREATED))));
  }

  private ChainBuilder validatePublishedGallery() {

    return asLongAsDuring(session -> !session.get(RETRY_CODE).equals(OK), Duration.ofMillis(PUBLISH_RESPONSE_TIME_THRESHOLD)).on(
        exec(
            http(VALIDATE_GALLERY_PUBLISH)
                .get(PUBLISHER_URL)
                .basicAuth("Telegraph", "VO9?~A2BC*VtqG")
                .check(status().saveAs(RETRY_CODE)))
            .pause(1)
    ).exec(
        http(VALIDATE_GALLERY_PUBLISH)
            .get(PUBLISHER_URL)
            .basicAuth("Telegraph", "VO9?~A2BC*VtqG")
            .check(status().is(OK).saveAs(RETRY_CODE)));
  }
}
