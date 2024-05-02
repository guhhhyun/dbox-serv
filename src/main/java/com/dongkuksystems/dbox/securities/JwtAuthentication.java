package com.dongkuksystems.dbox.securities;

import static com.google.common.base.Preconditions.checkNotNull;


public class JwtAuthentication {

  public final String loginId;
  
  JwtAuthentication(String loginId) {
      checkNotNull(loginId, "id must be provided.");
      this.loginId = loginId;
  }
}