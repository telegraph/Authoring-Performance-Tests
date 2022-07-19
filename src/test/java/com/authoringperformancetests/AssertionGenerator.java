package com.authoringperformancetests;

import io.gatling.javaapi.core.Assertion;

import static com.authoringperformancetests.RequestUtils.CREATE_RESPONSE_TIME_THRESHOLD;
import static com.authoringperformancetests.RequestUtils.PERCENTILE;
import static com.authoringperformancetests.RequestUtils.PUBLISH_LIVE_POSTS;
import static com.authoringperformancetests.RequestUtils.PUBLISH_RESPONSE_TIME_THRESHOLD;
import static com.authoringperformancetests.RequestUtils.UPDATE_RESPONSE_TIME_THRESHOLD;
import static io.gatling.javaapi.core.CoreDsl.details;

public final class AssertionGenerator {

  public static Assertion createThresholds(String requestName) {
    return  details(requestName)
        .responseTime()
        .percentile(PERCENTILE)
        .lt(CREATE_RESPONSE_TIME_THRESHOLD);
  }

  public static Assertion updateThresholds(String requestName) {
    return details(requestName)
        .responseTime()
        .percentile(PERCENTILE)
        .lt(UPDATE_RESPONSE_TIME_THRESHOLD);
  }

  public static Assertion publishThresholds(String requestName) {
    return details(requestName)
        .responseTime()
        .percentile(PERCENTILE)
        .lt(PUBLISH_RESPONSE_TIME_THRESHOLD);
  }

  public static Assertion validatePublishThresholds(String requestName) {
    return details(requestName).successfulRequests().percent().is(100.0);
  }


  private AssertionGenerator() {}
}
