package com.authoringperformancetests;

public class RequestUtils {

  public static final String BASE_URL = "https://chase-preprod-ams.aws-preprod.telegraph.co.uk/bin/telegraph/author/v2";
  public static final String BASE_URL_CS = "https://chase-preprod-ams.aws-preprod.telegraph.co.uk/bin/telegraph/author/v2";
  public static final String PUBLISHER_URL = "https://preprod-ams.telegraph.co.uk/${publishedUrl}/";
  public static final String PUBLISHER_URL_CS = "https://preprod-ams.telegraph.co.uk/${publishedUrl}/";
  public static final String CONTENT_ENDPOINT = "/content";
  public static final String UPDATE_ENDPOINT = CONTENT_ENDPOINT + "?id=#{id}";
  public static final String UPDATE_LIVE_POSTS_ENDPOINT = CONTENT_ENDPOINT + "?id=#{postsId}";
  public static final String PUBLISH_ENDPOINT = CONTENT_ENDPOINT + "/workflow";

  public static final String HEADER_JSON = "application/json";
  public static final String AGENT_HEADER = "Performance Testing";
  public static final String ID = "id";
  public static final String LIVE_POSTS_ID = "postsId";
  public static final String PUBLISHED_URL = "publishedUrl";
  public static final String POSTS_PUBLISHED_URL = "postsPublishedUrl";
  public static final String RETRY_CODE = "retryCode";
  public static final String DEFAULT_SESSION_ATTRIBUTE_VALUE = "_";
  public static final String CONTENT_TYPE = "Content-Type";

  public static final String CREATE_ARTICLE = "create-article-request";
  public static final String UPDATE_ARTICLE = "update-article-request";
  public static final String PUBLISH_ARTICLE = "publish-article-workflow-request";
  public static final String VALIDATE_PUBLISH = "validate-article-published";

  public static final String CREATE_GALLERY = "create-gallery-request";
  public static final String UPDATE_GALLERY = "update-gallery-request";
  public static final String PUBLISH_GALLERY = "publish-gallery-workflow-request";
  public static final String VALIDATE_GALLERY_PUBLISH = "validate-gallery-published";

  public static final String CREATE_LIVE_ARTICLE = "create-live-article-request";
  public static final String UPDATE_LIVE_ARTICLE = "update-live-article-request";
  public static final String UPDATE_LIVE_POSTS = "update-live-posts-request";
  public static final String PUBLISH_LIVE_POSTS = "publish-live-posts-workflow-request";
  public static final String PUBLISH_LIVE_ARTICLE = "publish-live-article-workflow-request";
  public static final String VALIDATE_LIVE_ARTICLE_PUBLISH = "validate-gallery-published";

  public static final Integer USERS = 70;
  public static final Integer TIME = 5;
  public static final double PERCENTILE = 95.0;
  public static final Integer CREATE_RESPONSE_TIME_THRESHOLD = 3500;
  public static final Integer UPDATE_RESPONSE_TIME_THRESHOLD = 2500;
  public static final Integer PUBLISH_RESPONSE_TIME_THRESHOLD = 60000;
  public static final Integer NOT_FOUND = 404;
  public static final Integer OK = 200;
  public static final Integer CREATED = 201;

  public static final String ARTICLE_UPDATE_JSON = "src/main/resources/ArticleUpdate.json";
  public static final String ARTICLE_CREATE_JSON = "src/main/resources/ArticleCreate.json";
  public static final String GALLERY_UPDATE_JSON = "src/main/resources/GalleryUpdate.json";
  public static final String GALLERY_CREATE_JSON = "src/main/resources/GalleryCreate.json";
  public static final String LIVE_POSTS_UPDATE_JSON = "src/main/resources/LivePostsUpdate.json";
  public static final String LIVE_ARTICLE_UPDATE_JSON = "src/main/resources/LiveArticleUpdate.json";
  public static final String LIVE_ARTICLE_CREATE_JSON = "src/main/resources/LiveArticleCreate.json";
  public static final String PUBLISH_JSON = "src/main/resources/Publish.json";

  public static final String CREDENTIALS_PREPROD_USERNAME = "Telegraph";
  public static final String CREDENTIALS_PREPROD_PASSWORD = "VO9?~A2BC*VtqG";
  public static final String CREDENTIALS_CS_USERNAME = "Telegraph";
  public static final String CREDENTIALS_CS_PASSWORD = "VO9?~A2BC*VtqG";

}
