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

import static com.authoringperformancetests.RequestUtils.AGENT_HEADER;
import static com.authoringperformancetests.RequestUtils.ARTICLE_PUBLISH_URL;
import static com.authoringperformancetests.RequestUtils.BASE_URL;
import static com.authoringperformancetests.RequestUtils.CONTENT_ENDPOINT;
import static com.authoringperformancetests.RequestUtils.CONTENT_TYPE;
import static com.authoringperformancetests.RequestUtils.CREATED;
import static com.authoringperformancetests.RequestUtils.CREATE_GALLERY;
import static com.authoringperformancetests.RequestUtils.DEFAULT_SESSION_ATTRIBUTE_VALUE;
import static com.authoringperformancetests.RequestUtils.GALLERY_ID;
import static com.authoringperformancetests.RequestUtils.GALLERY_PUBLISH_URL;
import static com.authoringperformancetests.RequestUtils.GALLERY_UPDATE_JSON;
import static com.authoringperformancetests.RequestUtils.HEADER_JSON;
import static com.authoringperformancetests.RequestUtils.NOT_FOUND;
import static com.authoringperformancetests.RequestUtils.OK;
import static com.authoringperformancetests.RequestUtils.PUBLISHER_URL;
import static com.authoringperformancetests.RequestUtils.PUBLISH_ENDPOINT;
import static com.authoringperformancetests.RequestUtils.PUBLISH_GALLERY;
import static com.authoringperformancetests.RequestUtils.PUBLISH_JSON;
import static com.authoringperformancetests.RequestUtils.RETRY_CODE;
import static com.authoringperformancetests.RequestUtils.UPDATE_ENDPOINT;
import static com.authoringperformancetests.RequestUtils.UPDATE_GALLERY;
import static com.authoringperformancetests.RequestUtils.VALIDATE_PUBLISH;
import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.asLongAs;
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
          .exec(createGalleryRequest())
          .pause(1)
          .exec(updateGalleryRequest())
          .pause(1)
          .exec(publishGalleryRequest());

  public GalleryPerformance() throws IOException {
    this.setUp(scn.injectOpen(rampUsers(70).during(Duration.ofMinutes(5))))
        .protocols(httpProtocol);
  }

  private ActionBuilder createGalleryRequest() throws IOException {

    return http(CREATE_GALLERY)
        .post(CONTENT_ENDPOINT)
        .header(CONTENT_TYPE, HEADER_JSON)
        .basicAuth("Telegraph", "VO9?~A2BC*VtqG")
        .body(StringBody(
            "{\n" +
                "  \"headline\": \"Gallery Load Testing " + System.currentTimeMillis() + "\",\n" +
                "  \"commentingStatus\": false,\n" +
                "  \"contentType\": \"gallery\",\n" +
                "  \"kicker\": \"test\",\n" +
                "  \"evergreen\": true,\n" +
                "  \"section\": \"/content/telegraph/performance/0/gatling\",\n" +
                "  \"storyType\": \"standard\"\n" +
                "}"
        ))
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
