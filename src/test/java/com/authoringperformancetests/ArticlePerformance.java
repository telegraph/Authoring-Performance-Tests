package com.authoringperformancetests;
/*
import static com.authoringperformancetests.PerformanceGenerator.*;
import static com.authoringperformancetests.AssertionGenerator.*;
import static com.authoringperformancetests.RequestUtils.ARTICLE_CREATE_JSON;
import static com.authoringperformancetests.RequestUtils.ARTICLE_UPDATE_JSON;
import static com.authoringperformancetests.RequestUtils.CREATE_ARTICLE;
import static com.authoringperformancetests.RequestUtils.PUBLISH_ARTICLE;
import static com.authoringperformancetests.RequestUtils.TIME;
import static com.authoringperformancetests.RequestUtils.UPDATE_ARTICLE;
import static com.authoringperformancetests.RequestUtils.USERS;
import static com.authoringperformancetests.RequestUtils.VALIDATE_PUBLISH;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;

import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import java.io.IOException;
import java.time.Duration;

public class ArticlePerformance extends Simulation {

  public ArticlePerformance() throws IOException {
    this.setUp(
            createArticleScenario()
                .injectOpen(
                    rampUsers(USERS)
                        .during(Duration.ofMinutes(TIME))
                )
        )
        .assertions(
            globalErrorThresholds(),
            createThresholds(CREATE_ARTICLE),
            updateThresholds(UPDATE_ARTICLE),
            publishThresholds(PUBLISH_ARTICLE),
            validatePublishThresholds(VALIDATE_PUBLISH)
        )
        .protocols(generateHttpProtocol());
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
}
*/