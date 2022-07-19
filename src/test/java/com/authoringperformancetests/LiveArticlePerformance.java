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

import static com.authoringperformancetests.AssertionGenerator.createThresholds;
import static com.authoringperformancetests.AssertionGenerator.publishThresholds;
import static com.authoringperformancetests.AssertionGenerator.updateThresholds;
import static com.authoringperformancetests.AssertionGenerator.validatePublishThresholds;
import static com.authoringperformancetests.PerformanceGenerator.createArticleRequest;
import static com.authoringperformancetests.PerformanceGenerator.generateFeeder;
import static com.authoringperformancetests.PerformanceGenerator.generateHttpProtocol;
import static com.authoringperformancetests.PerformanceGenerator.publishArticleRequest;
import static com.authoringperformancetests.PerformanceGenerator.setDefaultSessionValues;
import static com.authoringperformancetests.PerformanceGenerator.updateArticleRequest;
import static com.authoringperformancetests.PerformanceGenerator.validatePublishedArticle;
import static com.authoringperformancetests.RequestUtils.AGENT_HEADER;
import static com.authoringperformancetests.RequestUtils.BASE_URL;
import static com.authoringperformancetests.RequestUtils.CONTENT_ENDPOINT;
import static com.authoringperformancetests.RequestUtils.CONTENT_TYPE;
import static com.authoringperformancetests.RequestUtils.CREATED;
import static com.authoringperformancetests.RequestUtils.CREATE_LIVE_ARTICLE;
import static com.authoringperformancetests.RequestUtils.CREATE_RESPONSE_TIME_THRESHOLD;
import static com.authoringperformancetests.RequestUtils.CREDENTIALS_PREPROD_PASSWORD;
import static com.authoringperformancetests.RequestUtils.CREDENTIALS_PREPROD_USERNAME;
import static com.authoringperformancetests.RequestUtils.DEFAULT_SESSION_ATTRIBUTE_VALUE;
import static com.authoringperformancetests.RequestUtils.HEADER_JSON;
import static com.authoringperformancetests.RequestUtils.ID;
import static com.authoringperformancetests.RequestUtils.LIVE_ARTICLE_CREATE_JSON;
import static com.authoringperformancetests.RequestUtils.LIVE_ARTICLE_UPDATE_JSON;
import static com.authoringperformancetests.RequestUtils.LIVE_POSTS_ID;
import static com.authoringperformancetests.RequestUtils.LIVE_POSTS_UPDATE_JSON;
import static com.authoringperformancetests.RequestUtils.NOT_FOUND;
import static com.authoringperformancetests.RequestUtils.OK;
import static com.authoringperformancetests.RequestUtils.PERCENTILE;
import static com.authoringperformancetests.RequestUtils.POSTS_PUBLISHED_URL;
import static com.authoringperformancetests.RequestUtils.PUBLISHED_URL;
import static com.authoringperformancetests.RequestUtils.PUBLISHER_URL;
import static com.authoringperformancetests.RequestUtils.PUBLISH_ENDPOINT;
import static com.authoringperformancetests.RequestUtils.PUBLISH_JSON;
import static com.authoringperformancetests.RequestUtils.PUBLISH_LIVE_ARTICLE;
import static com.authoringperformancetests.RequestUtils.PUBLISH_LIVE_POSTS;
import static com.authoringperformancetests.RequestUtils.PUBLISH_RESPONSE_TIME_THRESHOLD;
import static com.authoringperformancetests.RequestUtils.RETRY_CODE;
import static com.authoringperformancetests.RequestUtils.TIME;
import static com.authoringperformancetests.RequestUtils.UPDATE_ENDPOINT;
import static com.authoringperformancetests.RequestUtils.UPDATE_LIVE_ARTICLE;
import static com.authoringperformancetests.RequestUtils.UPDATE_LIVE_POSTS;
import static com.authoringperformancetests.RequestUtils.UPDATE_LIVE_POSTS_ENDPOINT;
import static com.authoringperformancetests.RequestUtils.UPDATE_RESPONSE_TIME_THRESHOLD;
import static com.authoringperformancetests.RequestUtils.USERS;
import static com.authoringperformancetests.RequestUtils.VALIDATE_LIVE_ARTICLE_PUBLISH;
import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.asLongAsDuring;
import static io.gatling.javaapi.core.CoreDsl.details;
import static io.gatling.javaapi.core.CoreDsl.doIf;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class LiveArticlePerformance extends Simulation {

  private ScenarioBuilder createLiveArticleScenario() throws IOException {
    return CoreDsl.scenario("Load Testing Live Articles")
        .exec(setDefaultSessionValues())
        .feed(generateFeeder())
        .exec(createLiveArticleRequest())
        .pause(1)
        .exec(updateLivePostsRequest())
        .pause(1)
        .exec(updateArticleRequest(UPDATE_LIVE_ARTICLE, LIVE_ARTICLE_UPDATE_JSON))
        .pause(1)
        .exec(publishLivePosts())
        .pause(1)
        .exec(publishArticleRequest(PUBLISH_LIVE_ARTICLE))
        .pause(1)
        .exec(validatePublishedArticle(VALIDATE_LIVE_ARTICLE_PUBLISH));
  }

  public LiveArticlePerformance() throws IOException {
    this.setUp(
        createLiveArticleScenario()
            .injectOpen(
                rampUsers(USERS)
                    .during(Duration.ofMinutes(TIME))
            )
        )
        .assertions(
            createThresholds(CREATE_LIVE_ARTICLE),
            updateThresholds(UPDATE_LIVE_ARTICLE),
            updateThresholds(UPDATE_LIVE_POSTS),
            publishThresholds(PUBLISH_LIVE_ARTICLE),
            publishThresholds(PUBLISH_LIVE_POSTS),
            validatePublishThresholds(VALIDATE_LIVE_ARTICLE_PUBLISH)
        )
        .protocols(generateHttpProtocol());
  }

  private ActionBuilder createLiveArticleRequest() throws IOException {

    String body = new String(Files.readAllBytes(Paths.get(LIVE_ARTICLE_CREATE_JSON)));

    return http(CREATE_LIVE_ARTICLE)
        .post(CONTENT_ENDPOINT)
        .header(CONTENT_TYPE, HEADER_JSON)
        .basicAuth(CREDENTIALS_PREPROD_USERNAME, CREDENTIALS_PREPROD_PASSWORD)
        .body(StringBody(body))
        .check(status().is(CREATED))
        .check(jsonPath("$..id").exists().saveAs(ID))
        .check(jsonPath("$..components[*].livePosts").exists().saveAs(LIVE_POSTS_ID));
  }


  private ChainBuilder updateLivePostsRequest() throws IOException {

    String body = new String(Files.readAllBytes(Paths.get(LIVE_POSTS_UPDATE_JSON)));

    return doIf(session -> (!session.get(LIVE_POSTS_ID).equals(DEFAULT_SESSION_ATTRIBUTE_VALUE)))
        .then(
            exec(
                http(UPDATE_LIVE_POSTS)
                    .put(UPDATE_LIVE_POSTS_ENDPOINT)
                    .header(CONTENT_TYPE, HEADER_JSON)
                    .basicAuth(CREDENTIALS_PREPROD_USERNAME, CREDENTIALS_PREPROD_PASSWORD)
                    .body(StringBody(body))
                    .check(status().is(OK))
                    .check(jsonPath("$..externalPath").exists().saveAs(POSTS_PUBLISHED_URL))));
  }

  private ChainBuilder publishLivePosts() throws IOException {

    String body = new String(Files.readAllBytes(Paths.get(PUBLISH_JSON)));

    return doIf(session -> (!session.get(POSTS_PUBLISHED_URL).equals(DEFAULT_SESSION_ATTRIBUTE_VALUE)))
        .then(
            exec(
                http(PUBLISH_LIVE_POSTS)
                    .post(PUBLISH_ENDPOINT)
                    .header(CONTENT_TYPE, HEADER_JSON)
                    .basicAuth(CREDENTIALS_PREPROD_USERNAME, CREDENTIALS_PREPROD_PASSWORD)
                    .body(StringBody(body))
                    .check(status().is(CREATED))));
  }
}
