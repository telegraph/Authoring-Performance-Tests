package com.authoringperformancetests;

import lombok.Data;

@Data
public class InjectionStepUsers {

  private int articleUsers;

  private int liveArticleUsers;

  private int galleryUsers;

  public InjectionStepUsers(int articleUsers, int liveArticleUsers, int galleryUsers) {
    this.articleUsers = articleUsers;
    this.liveArticleUsers = liveArticleUsers;
    this.galleryUsers = galleryUsers;
  }

}
