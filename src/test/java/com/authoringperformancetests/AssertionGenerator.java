package com.authoringperformancetests;

import io.gatling.javaapi.core.Assertion;

import static com.authoringperformancetests.RequestUtils.CREATE_RESPONSE_TIME_THRESHOLD;
import static com.authoringperformancetests.RequestUtils.PERCENTILE;
import static com.authoringperformancetests.RequestUtils.PUBLISH_RESPONSE_TIME_THRESHOLD;
import static com.authoringperformancetests.RequestUtils.UPDATE_RESPONSE_TIME_THRESHOLD;
import static io.gatling.javaapi.core.CoreDsl.details;
import static io.gatling.javaapi.core.CoreDsl.global;

public final class AssertionGenerator {

  public static Assertion createThresholds(String group, String requestName) {
    return  details(group, requestName)
        .responseTime()
        .percentile(PERCENTILE)
        .lt(CREATE_RESPONSE_TIME_THRESHOLD);
  }

  public static Assertion updateThresholds(String group, String requestName) {
    return details(group, requestName)
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

  public static Assertion publishThresholds(String group, String requestName) {
    return details(group, requestName)
        .responseTime()
        .percentile(PERCENTILE)
        .lt(PUBLISH_RESPONSE_TIME_THRESHOLD);
  }

  public static Assertion validatePublishThresholds(String group, String requestName) {
    return details(group, requestName).successfulRequests().percent().gt(PERCENTILE);
  }


  private AssertionGenerator() {}
}
