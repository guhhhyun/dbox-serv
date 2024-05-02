package com.dongkuksystems.dbox.controllers;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dongkuksystems.dbox.daos.table.log.LogDao;
import com.dongkuksystems.dbox.repositories.redis.RedisRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 컨트롤러의 공통 로직을 가진 추상 클래스
 *
 * @author 차소익
 */
@RequestMapping("/api")
public abstract class AbstractCommonController {
  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private LogDao logDao;

  @Autowired
  private RedisRepository redisRepository;

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private ObjectMapper objectMapper;

  protected LogDao getLogDao() {
    return logDao;
  }

  /**
   * redis repository
   */
  protected RedisRepository getRedisRepository() {
    return redisRepository;
  }

  /**
   * jwt service
   */
  protected AuthenticationManager getAuthenticationManager() {
    return authenticationManager;
  }

  /**
   * object mapper
   */
  protected ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  /**
   * 클라이언트 ip 조회
   */
  protected String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-FORWARDED-FOR");
    if (ip == null)
      ip = request.getRemoteAddr();
    return ip;
  }

  /**
  *  @return Mobile, PC
  */
  protected boolean chkIsMobile(HttpServletRequest request) {
    // 모바일인지 구분
    String userAgent = request.getHeader("User-Agent").toUpperCase();
    if (userAgent.indexOf("MOBILE") > -1) {
      return true;
    } else {
      return false;
    }
  }
  
  /**
  *  @return Mobile, PHONE, PC
  */
  protected String getClientType(HttpServletRequest request) {
    // 모바일인지 구분
    String userAgent = request.getHeader("User-Agent").toUpperCase();
    if (userAgent.indexOf("MOBILE") > -1) {
      if (userAgent.indexOf("PHONE") == -1)
        userAgent = "PHONE";
      else
        userAgent = "TABLET";
    } else {
      userAgent = "PC";
    }
    return userAgent;
  }
}
