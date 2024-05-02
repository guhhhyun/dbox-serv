package com.dongkuksystems.dbox.securities;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;


public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private Object principal;

  private String credentials;
  
  private Boolean isMobile;
  
  private boolean isAuthIgnore;

  public JwtAuthenticationToken(String principal, String credentials) {
      super(null);
      super.setAuthenticated(false);

      this.principal = principal;
      this.credentials = credentials;
  }
  
  public JwtAuthenticationToken(String principal, String credentials, Boolean isMobile, boolean isAuthIgnore) {
    super(null);
    super.setAuthenticated(false);

    this.principal = principal;
    this.credentials = credentials;
    this.isMobile = isMobile;
    this.isAuthIgnore = isAuthIgnore;
  }

  JwtAuthenticationToken(Object principal, String credentials, Collection<? extends GrantedAuthority> authorities) {
      super(authorities);
      super.setAuthenticated(true);

      this.principal = principal;
      this.credentials = credentials;
  }

  AuthenticationRequest authenticationRequest() {
      return new AuthenticationRequest(String.valueOf(principal), credentials, isMobile, isAuthIgnore);
  }

  @Override
  public Object getPrincipal() {
      return principal;
  }

  @Override
  public String getCredentials() {
      return credentials;
  }

  public void setIsMobile(Boolean isMobile) {
    this.isMobile = isMobile;
  }
  
  public Boolean isMobile() {
    return this.isMobile;
  }
  
  public Boolean getIsAuthIgnore() {
    return isAuthIgnore;
  }

  public void setIsAuthIgnore(Boolean isAuthIgnore) {
    this.isAuthIgnore = isAuthIgnore;
  }

  public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
      if (isAuthenticated)
          throw new IllegalArgumentException("Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");

      super.setAuthenticated(false);
  }

  @Override
  public void eraseCredentials() {
      super.eraseCredentials();
      credentials = null;
  }

}