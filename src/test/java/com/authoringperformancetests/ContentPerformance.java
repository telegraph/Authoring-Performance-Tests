package com.authoringperformancetests;

import io.gatling.javaapi.core.ActionBuilder;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import static com.authoringperformancetests.AssertionGenerator.createThresholds;
import static com.authoringperformancetests.AssertionGenerator.globalErrorThresholds;
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
import static com.authoringperformancetests.RequestUtils.ARTICLE_CREATE_JSON;
import static com.authoringperformancetests.RequestUtils.ARTICLE_UPDATE_JSON;
import static com.authoringperformancetests.RequestUtils.CONTENT_ENDPOINT;
import static com.authoringperformancetests.RequestUtils.CONTENT_TYPE;
import static com.authoringperformancetests.RequestUtils.CREATED;
import static com.authoringperformancetests.RequestUtils.CREATE_ARTICLE;
import static com.authoringperformancetests.RequestUtils.CREATE_GALLERY;
import static com.authoringperformancetests.RequestUtils.CREATE_LIVE_ARTICLE;
import static com.authoringperformancetests.RequestUtils.CREDENTIALS_PREPROD_PASSWORD;
import static com.authoringperformancetests.RequestUtils.CREDENTIALS_PREPROD_USERNAME;
import static com.authoringperformancetests.RequestUtils.DEFAULT_SESSION_ATTRIBUTE_VALUE;
import static com.authoringperformancetests.RequestUtils.GALLERY_CREATE_JSON;
import static com.authoringperformancetests.RequestUtils.GALLERY_UPDATE_JSON;
import static com.authoringperformancetests.RequestUtils.HEADER_JSON;
import static com.authoringperformancetests.RequestUtils.ID;
import static com.authoringperformancetests.RequestUtils.LIVE_ARTICLE_CREATE_JSON;
import static com.authoringperformancetests.RequestUtils.LIVE_ARTICLE_UPDATE_JSON;
import static com.authoringperformancetests.RequestUtils.LIVE_POSTS_ID;
import static com.authoringperformancetests.RequestUtils.LIVE_POSTS_UPDATE_JSON;
import static com.authoringperformancetests.RequestUtils.OK;
import static com.authoringperformancetests.RequestUtils.POSTS_PUBLISHED_URL;
import static com.authoringperformancetests.RequestUtils.PUBLISH_ARTICLE;
import static com.authoringperformancetests.RequestUtils.PUBLISH_ENDPOINT;
import static com.authoringperformancetests.RequestUtils.PUBLISH_GALLERY;
import static com.authoringperformancetests.RequestUtils.PUBLISH_JSON;
import static com.authoringperformancetests.RequestUtils.PUBLISH_LIVE_ARTICLE;
import static com.authoringperformancetests.RequestUtils.PUBLISH_LIVE_POSTS;
import static com.authoringperformancetests.RequestUtils.TIME;
import static com.authoringperformancetests.RequestUtils.UPDATE_ARTICLE;
import static com.authoringperformancetests.RequestUtils.UPDATE_GALLERY;
import static com.authoringperformancetests.RequestUtils.UPDATE_LIVE_ARTICLE;
import static com.authoringperformancetests.RequestUtils.UPDATE_LIVE_POSTS;
import static com.authoringperformancetests.RequestUtils.UPDATE_LIVE_POSTS_ENDPOINT;
import static com.authoringperformancetests.RequestUtils.USERS;
import static com.authoringperformancetests.RequestUtils.VALIDATE_GALLERY_PUBLISH;
import static com.authoringperformancetests.RequestUtils.VALIDATE_LIVE_ARTICLE_PUBLISH;
import static com.authoringperformancetests.RequestUtils.VALIDATE_PUBLISH;
import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.doIf;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.stressPeakUsers;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class ContentPerformance extends Simulation {

  public ContentPerformance() throws IOException {
    int articleUsers = Integer.getInteger("users", 140);
    int galleryUsers = Integer.getInteger("users", 20);
    int liveArticleUsers = Integer.getInteger("users", 30);
    long totalTime = Long.getLong("totalTime", 5);


    this.setUp(
            createArticleScenario().injectOpen(rampUsers(articleUsers).during(Duration.ofMinutes(totalTime))),
            createGalleryScenario().injectOpen(rampUsers(galleryUsers).during(Duration.ofMinutes(totalTime))),
            createLiveArticleScenario().injectOpen(rampUsers(liveArticleUsers).during(Duration.ofMinutes(totalTime)))
        )
        .assertions(
            globalErrorThresholds(),
            createThresholds(CREATE_ARTICLE),
            createThresholds(CREATE_GALLERY),
            createThresholds(CREATE_LIVE_ARTICLE),
            updateThresholds(UPDATE_ARTICLE),
            updateThresholds(UPDATE_GALLERY),
            updateThresholds(UPDATE_LIVE_ARTICLE),
            updateThresholds(UPDATE_LIVE_POSTS),
            publishThresholds(PUBLISH_ARTICLE),
            publishThresholds(PUBLISH_GALLERY),
            publishThresholds(PUBLISH_LIVE_ARTICLE),
            publishThresholds(PUBLISH_LIVE_POSTS),
            validatePublishThresholds(VALIDATE_PUBLISH),
            validatePublishThresholds(VALIDATE_GALLERY_PUBLISH),
            validatePublishThresholds(VALIDATE_LIVE_ARTICLE_PUBLISH)
        )
        .protocols(generateHttpProtocol());
  }


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

  private ScenarioBuilder createArticleScenario() throws IOException {

    return CoreDsl.scenario("Load Testing Articles")
        .exec(setDefaultSessionValues())
        .feed(generateFeeder())
        .exec(createArticleRequest(CREATE_ARTICLE, ARTICLE_CREATE_JSON))
        .pause(1)
        .exec(updateArticleRequest(UPDATE_ARTICLE, ARTICLE_UPDATE_JSON))
        .pause(1)
        .exec(publishArticleRequest(PUBLISH_ARTICLE))
        .pause(1)
        .exec(validatePublishedArticle(VALIDATE_PUBLISH));
  }

  private ScenarioBuilder createGalleryScenario() throws IOException {
    return CoreDsl.scenario("Load Testing Galleries")
        .exec(setDefaultSessionValues())
        .feed(generateFeeder())
        .exec(createArticleRequest(CREATE_GALLERY, GALLERY_CREATE_JSON))
        .pause(1)
        .exec(updateArticleRequest(UPDATE_GALLERY, GALLERY_UPDATE_JSON))
        .pause(1)
        .exec(publishArticleRequest(PUBLISH_GALLERY))
        .pause(1)
        .exec(validatePublishedArticle(VALIDATE_GALLERY_PUBLISH));
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
