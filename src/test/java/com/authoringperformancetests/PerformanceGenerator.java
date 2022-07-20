package com.authoringperformancetests;

import static com.authoringperformancetests.RequestUtils.AGENT_HEADER;
import static com.authoringperformancetests.RequestUtils.BASE_URL;
import static com.authoringperformancetests.RequestUtils.CONTENT_ENDPOINT;
import static com.authoringperformancetests.RequestUtils.CONTENT_TYPE;
import static com.authoringperformancetests.RequestUtils.CREATED;
import static com.authoringperformancetests.RequestUtils.CREDENTIALS_PREPROD_PASSWORD;
import static com.authoringperformancetests.RequestUtils.CREDENTIALS_PREPROD_USERNAME;
import static com.authoringperformancetests.RequestUtils.DEFAULT_SESSION_ATTRIBUTE_VALUE;
import static com.authoringperformancetests.RequestUtils.HEADER_JSON;
import static com.authoringperformancetests.RequestUtils.ID;
import static com.authoringperformancetests.RequestUtils.LIVE_POSTS_ID;
import static com.authoringperformancetests.RequestUtils.NOT_FOUND;
import static com.authoringperformancetests.RequestUtils.OK;
import static com.authoringperformancetests.RequestUtils.PUBLISHED_URL;
import static com.authoringperformancetests.RequestUtils.PUBLISHER_URL;
import static com.authoringperformancetests.RequestUtils.PUBLISH_ENDPOINT;
import static com.authoringperformancetests.RequestUtils.PUBLISH_JSON;
import static com.authoringperformancetests.RequestUtils.PUBLISH_RESPONSE_TIME_THRESHOLD;
import static com.authoringperformancetests.RequestUtils.RETRY_CODE;
import static com.authoringperformancetests.RequestUtils.UPDATE_ENDPOINT;
import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.asLongAsDuring;
import static io.gatling.javaapi.core.CoreDsl.doIf;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ActionBuilder;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class PerformanceGenerator {

  public static Function<Session, Session> setDefaultSessionValues() {
    return session -> {
      session
          .set(ID, DEFAULT_SESSION_ATTRIBUTE_VALUE)
          .set(LIVE_POSTS_ID, DEFAULT_SESSION_ATTRIBUTE_VALUE)
          .set(PUBLISHED_URL, DEFAULT_SESSION_ATTRIBUTE_VALUE)
          .set(RETRY_CODE, NOT_FOUND);
      return session;
    };
  }

  public static Iterator<Map<String, Object>> generateFeeder() {
    return Stream.generate((Supplier<Map<String, Object>>) () -> {
          String uuid = UUID.randomUUID().toString();
          return Collections.singletonMap("uuid", uuid);
        }
    ).iterator();
  }

  public static HttpProtocolBuilder generateHttpProtocol() {
    return http.baseUrl(BASE_URL)
        .acceptHeader(HEADER_JSON)
        .header(CONTENT_TYPE, HEADER_JSON)
        .basicAuth(CREDENTIALS_PREPROD_USERNAME, CREDENTIALS_PREPROD_PASSWORD)
        .userAgentHeader(AGENT_HEADER)
        .disableFollowRedirect();
  }

  public static ActionBuilder createArticleRequest(String requestName, String jsonFile) throws IOException {

    String body = new String(Files.readAllBytes(Paths.get(jsonFile)));

    return http(requestName)
        .post(CONTENT_ENDPOINT)
        .body(StringBody(body))
        .check(status().is(CREATED))
        .check(jsonPath("$..id").exists().saveAs(ID));
  }

  public static ChainBuilder updateArticleRequest(String requestName, String jsonFile) throws IOException {

    String body = new String(Files.readAllBytes(Paths.get(jsonFile)));

    return doIf(session -> (!session.get(ID).equals(DEFAULT_SESSION_ATTRIBUTE_VALUE)))
        .then(
            exec(
                http(requestName)
                    .put(UPDATE_ENDPOINT)
                    .body(StringBody(body))
                    .check(status().is(OK))
                    .check(jsonPath("$..externalPath").exists().saveAs(PUBLISHED_URL))));
  }

  public static ChainBuilder publishArticleRequest(String requestName) throws IOException {

    String body = new String(Files.readAllBytes(Paths.get(PUBLISH_JSON)));

    return doIf(session -> (!session.get(PUBLISHED_URL).equals(DEFAULT_SESSION_ATTRIBUTE_VALUE)))
        .then(
            exec(
                http(requestName)
                    .post(PUBLISH_ENDPOINT)
                    .body(StringBody(body))
                    .check(status().is(CREATED).saveAs(RETRY_CODE))));
  }

  public static ChainBuilder validatePublishedArticle(String requestName) {

    return asLongAsDuring(session -> !session.get(RETRY_CODE).equals(OK), Duration.ofMillis(PUBLISH_RESPONSE_TIME_THRESHOLD)).on(
        exec(
            http(requestName)
                .get(PUBLISHER_URL)
                .check(status().saveAs(RETRY_CODE)))
            .pause(1)
    ).exec(
        http(requestName)
            .get(PUBLISHER_URL)
            .check(status().is(OK).saveAs(RETRY_CODE)));
  }

  private PerformanceGenerator() {}
}
