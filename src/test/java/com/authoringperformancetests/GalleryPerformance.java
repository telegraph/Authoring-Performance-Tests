package com.authoringperformancetests;

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
import static com.authoringperformancetests.RequestUtils.CREATE_GALLERY;
import static com.authoringperformancetests.RequestUtils.GALLERY_CREATE_JSON;
import static com.authoringperformancetests.RequestUtils.GALLERY_UPDATE_JSON;
import static com.authoringperformancetests.RequestUtils.PUBLISH_GALLERY;
import static com.authoringperformancetests.RequestUtils.TIME;
import static com.authoringperformancetests.RequestUtils.UPDATE_GALLERY;
import static com.authoringperformancetests.RequestUtils.USERS;
import static com.authoringperformancetests.RequestUtils.VALIDATE_GALLERY_PUBLISH;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;

import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import java.io.IOException;
import java.time.Duration;

public class GalleryPerformance extends Simulation {

  public GalleryPerformance() throws IOException {
    this.setUp(
        createGalleryScenario()
            .injectOpen(
                rampUsers(USERS)
                    .during(Duration.ofMinutes(TIME))
            )
        ).assertions(
            globalErrorThresholds(),
            createThresholds(CREATE_GALLERY),
            updateThresholds(UPDATE_GALLERY),
            publishThresholds(PUBLISH_GALLERY),
            validatePublishThresholds(VALIDATE_GALLERY_PUBLISH))
        .protocols(generateHttpProtocol());
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
}
