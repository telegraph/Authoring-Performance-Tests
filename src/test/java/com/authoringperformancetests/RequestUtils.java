package com.authoringperformancetests;

public class RequestUtils {

  public static final String BASE_URL = "https://chase-preprod-ams.aws-preprod.telegraph.co.uk/bin/telegraph/author/v2";
  public static final String PUBLISHER_URL = "https://chase-preprod-ams.aws-preprod.telegraph.co.uk${publishedUrl}/";
  public static final String CONTENT_ENDPOINT = "/content";
  public static final String UPDATE_ENDPOINT = CONTENT_ENDPOINT + "?id=#{id}";
  public static final String PUBLISH_ENDPOINT = CONTENT_ENDPOINT + "/workflow";

  public static final String HEADER_JSON = "application/json";
  public static final String AGENT_HEADER = "Performance Testing";
  public static final String ID = "id";
  public static final String PUBLISH_URL = "publishedUrl";
  public static final String RETRY_CODE = "retryCode";
  public static final String DEFAULT_SESSION_ATTRIBUTE_VALUE = "_";
  public static final String CONTENT_TYPE = "Content-Type";

  public static final String CREATE_ARTICLE = "create-article-request";
  public static final String UPDATE_ARTICLE = "update-article-request";
  public static final String PUBLISH_ARTICLE = "publish-article-workflow-request";
  public static final String VALIDATE_PUBLISH = "validate-article-published";


  public static final double PERCENTILE = 95.0;
  public static final Integer CREATE_RESPONSE_TIME_THRESHOLD = 1750;
  public static final Integer UPDATE_RESPONSE_TIME_THRESHOLD = 1250;
  public static final Integer PUBLISH_RESPONSE_TIME_THRESHOLD = 60000;
  public static final Integer NOT_FOUND = 404;
  public static final Integer OK = 200;
  public static final Integer CREATED = 201;

  public static final String ARTICLE_UPDATE_JSON = "src/main/resources/ArticleUpdate.json";
  public static final String PUBLISH_JSON = "src/main/resources/Publish.json";

}
