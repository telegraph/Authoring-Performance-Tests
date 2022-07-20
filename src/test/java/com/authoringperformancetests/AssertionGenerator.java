package com.authoringperformancetests;

import io.gatling.javaapi.core.Assertion;

import static com.authoringperformancetests.RequestUtils.CREATE_RESPONSE_TIME_THRESHOLD;
import static com.authoringperformancetests.RequestUtils.PERCENTILE;
import static com.authoringperformancetests.RequestUtils.PUBLISH_RESPONSE_TIME_THRESHOLD;
import static com.authoringperformancetests.RequestUtils.UPDATE_RESPONSE_TIME_THRESHOLD;
import static io.gatling.javaapi.core.CoreDsl.details;
import static io.gatling.javaapi.core.CoreDsl.global;

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

  public static Assertion globalErrorThresholds() {
    return global()
        .successfulRequests()
        .percent()
        .gt(PERCENTILE);
  }

  public static Assertion publishThresholds(String requestName) {
    return details(requestName)
        .responseTime()
        .percentile(PERCENTILE)
        .lt(PUBLISH_RESPONSE_TIME_THRESHOLD);
  }

  public static Assertion validatePublishThresholds(String requestName) {
    return details(requestName).successfulRequests().percent().gt(PERCENTILE);
  }


  private AssertionGenerator() {}
}
