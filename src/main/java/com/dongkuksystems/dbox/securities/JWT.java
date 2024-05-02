package com.dongkuksystems.dbox.securities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

public final class JWT {

  private final String issuer;

  private final String clientSecret;

  private final int expirySeconds;

  private final Algorithm algorithm;

  private final JWTVerifier jwtVerifier;

  public JWT(String issuer, String clientSecret, int expirySeconds) {
    this.issuer = issuer;
    this.clientSecret = clientSecret;
    this.expirySeconds = expirySeconds;
    this.algorithm = Algorithm.HMAC512(clientSecret);
    this.jwtVerifier = com.auth0.jwt.JWT.require(algorithm).withIssuer(issuer).build();
  }

  public String newToken(Claims claims) {
    Date now = new Date();
    LocalDateTime now2 = LocalDateTime.now();
    Calendar c = Calendar.getInstance();
    c.set(now2.getYear(), now2.getMonthValue()-1, now2.getDayOfYear(), 1, 0);
    Date one = c.getTime();
    if (now.after(one)) {
      c.add(Calendar.DATE, 1);
      one = c.getTime();
    }
    
    JWTCreator.Builder builder = com.auth0.jwt.JWT.create();
    builder.withIssuer(issuer);
    builder.withIssuedAt(now);
    if (expirySeconds > 0) {
      builder.withExpiresAt(one);
//      builder.withExpiresAt(new Date(now.getTime() + expirySeconds * 1_000L));
    }
    builder.withClaim("loginId", claims.loginId);
    builder.withArrayClaim("roles", claims.roles);
    return builder.sign(algorithm);
  }
  
  public String refreshNewToken(Claims claims) {
    Long iat = claims.iat();
    Long exp = claims.exp();
    claims.eraseIat();
    claims.eraseExp();
    Date now = new Date(iat);
    JWTCreator.Builder builder = com.auth0.jwt.JWT.create();
    builder.withIssuer(issuer);
    builder.withIssuedAt(now);
    if (expirySeconds > 0) {
      builder.withExpiresAt(new Date(exp + expirySeconds * 1_000L));
    }
    builder.withClaim("loginId", claims.loginId);
    builder.withArrayClaim("roles", claims.roles);
    return builder.sign(algorithm);
  }

  public String refreshToken(String token) throws JWTVerificationException {
    Claims claims = verify(token);
//    claims.eraseIat();
//    claims.eraseExp();
//    return newToken(claims);
    return refreshNewToken(claims);
  }
  
  public String refreshToken(Claims claims) throws JWTVerificationException {
    return refreshNewToken(claims);
//    claims.eraseIat();
//    claims.eraseExp();
//    return newToken(claims);
  }

  public String getLoginId(String token) {
    return jwtVerifier.verify(token).getClaim("loginId").asString();
  }

  public Claims verify(String token) throws JWTVerificationException {
    return new Claims(jwtVerifier.verify(token));
  }

  public String getIssuer() {
    return issuer;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public int getExpirySeconds() {
    return expirySeconds;
  }

  public Algorithm getAlgorithm() {
    return algorithm;
  }

  public JWTVerifier getJwtVerifier() {
    return jwtVerifier;
  }

  static public class Claims {
    String loginId;
    String[] roles;
    Date iat;
    Date exp;

    private Claims() {
    }

    Claims(DecodedJWT decodedJWT) {
      Claim loginId = decodedJWT.getClaim("loginId");
      if (!loginId.isNull())
        this.loginId = loginId.asString();
      Claim roles = decodedJWT.getClaim("roles");
      if (!roles.isNull())
        this.roles = roles.asArray(String.class);
      this.iat = decodedJWT.getIssuedAt();
      this.exp = decodedJWT.getExpiresAt();
    }

    public static Claims of(String loginId, String[] roles) {
      Claims claims = new Claims();
      claims.loginId = loginId;
      claims.roles = roles;
      return claims;
    }

    long iat() {
      return iat != null ? iat.getTime() : -1;
    }

    long exp() {
      return exp != null ? exp.getTime() : -1;
    }

    void eraseIat() {
      iat = null;
    }

    void eraseExp() {
      exp = null;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
          .append("loginId", loginId).append("roles", Arrays.toString(roles)).append("iat", iat)
          .append("exp", exp).toString();
    }
  }

}