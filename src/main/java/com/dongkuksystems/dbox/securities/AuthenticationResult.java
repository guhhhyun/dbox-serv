package com.dongkuksystems.dbox.securities;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dongkuksystems.dbox.models.table.etc.VUser;

import io.swagger.annotations.ApiModelProperty;

public class AuthenticationResult {

  @ApiModelProperty(value = "API 토큰", required = true)
  private final String apiToken;

  @ApiModelProperty(value = "사용자 정보", required = true)
  private final VUser user;

  public AuthenticationResult(String apiToken, VUser user) {
    checkNotNull(apiToken, "apiToken must be provided.");
    checkNotNull(user, "user must be provided.");

    this.apiToken = apiToken;
    this.user = user;
  }

  public String getApiToken() {
    return apiToken;
  }

  public VUser getUser() {
    return user;
  }
}
