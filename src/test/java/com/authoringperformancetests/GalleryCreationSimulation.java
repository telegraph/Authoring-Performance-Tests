package com.authoringperformancetests;

public class GalleryCreationSimulation {

  /*HttpProtocolBuilder httpProtocol =
      http.baseUrl(BASE_URL)
          .acceptHeader(HEADER_JSON)
          .userAgentHeader(AGENT_HEADER)
          .disableFollowRedirect();

  ScenarioBuilder scn =
      CoreDsl.scenario("Load Test Creating Article")
          .exec(
              session ->
                  session
                      .set(ID, DEFAULT_SESSION_ATTRIBUTE_VALUE)
                      .set(PUBLISH_URL, DEFAULT_SESSION_ATTRIBUTE_VALUE)
                      .set(RETRY_CODE, NOT_FOUND))
          .exec(createGalleryRequest())
          .pause(1)
          .exec(updateGalleryRequest())
          .pause(1)
          .exec(publishGalleryRequest())
          .pause(1)
          //.exec(validatePublishedArticle())
          .pause(1);

  public GalleryCreationSimulation() throws IOException {

  }

  private ActionBuilder createGalleryRequest() throws IOException {

    Random random = new Random();

    return http(CREATE_GALLERY)
        .post(CONTENT_ENDPOINT)
        .header(CONTENT_TYPE, HEADER_JSON)
        .basicAuth("Telegraph", "VO9?~A2BC*VtqG")
        .body(StringBody(
            "{\n" +
                "  \"headline\": \"new gallery perf tests " + random.nextLong() + "\",\n" +
                "  \"commentingStatus\": false,\n" +
                "  \"contentType\": \"gallery\",\n" +
                "  \"kicker\": \"test\",\n" +
                "  \"evergreen\": true,\n" +
                "  \"section\": \"/content/telegraph/news\",\n" +
                "  \"storyType\": \"standard\"\n" +
                "}"
        ))
        .check(status().is(CREATED))
        .check(jsonPath("$..id").exists().saveAs(ID));
  }

  private ChainBuilder updateGalleryRequest() throws IOException {

    String body = new String(Files.readAllBytes(Paths.get(GALLERY_UPDATE_JSON)));

    return doIf(session -> (!session.get(ID).equals("_")))
        .then(
            exec(
                http(UPDATE_GALLERY)
                    .put(UPDATE_ENDPOINT)
                    .header(CONTENT_TYPE, HEADER_JSON)
                    .basicAuth("Telegraph", "VO9?~A2BC*VtqG")
                    .body(StringBody(body))
                    .check(status().is(OK))
                    .check(jsonPath("$..externalPath").exists().saveAs(PUBLISH_URL))));
  }

  private ChainBuilder publishGalleryRequest() throws IOException {

    String body = new String(Files.readAllBytes(Paths.get(PUBLISH_JSON)));

    return doIf(session -> (!session.get(PUBLISH_URL).equals(DEFAULT_SESSION_ATTRIBUTE_VALUE)))
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
    return doIf(session -> (!session.get(PUBLISH_URL).equals(DEFAULT_SESSION_ATTRIBUTE_VALUE)))
        .then(
            asLongAs(session -> session.get(RETRY_CODE).equals(NOT_FOUND))
                .on(
                    exec(
                        http(VALIDATE_PUBLISH)
                            .get(PUBLISHER_URL)
                            .check(status().not(NOT_FOUND).saveAs(RETRY_CODE)))));
  }*/
}
