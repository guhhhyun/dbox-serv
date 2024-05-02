package com.dongkuksystems.dbox.securities;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.ERROR;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.GenericFilterBean;

import com.dongkuksystems.dbox.constants.Commons;
import com.dongkuksystems.dbox.constants.DCTMConstants;
import com.dongkuksystems.dbox.daos.table.log.LogDao;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.Role;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.table.log.LogLogin;
import com.dongkuksystems.dbox.repositories.redis.RedisRepository;
import com.dongkuksystems.dbox.services.login.LoginService;
import com.dongkuksystems.dbox.services.sso.SsoManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

public class JwtAuthenticationTokenFilter extends GenericFilterBean {

//    private static final Pattern BEARER = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);

  Logger loggger = LoggerFactory.getLogger(getClass());

  @Value("${jwt.token.header}")
  private String tokenHeader;
  
  @Value("${jwt.token.refesh-hour}")
  private int refreshHour;

  @Autowired
  private JWT jwt;

  @Autowired
  private RedisRepository redisRepository;

  @Autowired
  private LoginService loginService;

  @Autowired
  private SsoManager ssoManager;

  @Autowired
  private LogDao logDao;
  
  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;
    
    String newLoginId = null;
    String ssoLoginId = ssoManager.getSsoId(request);
    String loginId = null;
    String authorizationToken = null;
    JWT.Claims claims = null;
    int lockChk = 0;
    boolean none = false;
    boolean isLock = false;
//    boolean isBann = false;
    boolean validateToken = false;
    boolean newTokenFlag = false;
    boolean chkRedisFlag = false;
    boolean saveRedisFlag = false;
    UserSession userSession = null;
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      authorizationToken = obtainAuthorizationToken(request);
      if (authorizationToken != null) {
        try {
          String isLogout = redisRepository.get(Commons.LOGOUT_PREFIX + authorizationToken, String.class);
          if (isLogout != null) {
            loginId = null;
            authorizationToken = null;
          } else {
            claims = verify(authorizationToken);
            loginId = claims.loginId;
          }
        } catch (Exception e) {
          // TODO: token is not valid
          loggger.info("Verifying token failed (1)");
        }
      }
    }
    
    if (ssoLoginId != null && loginId != null) {
      //sso, token 둘다 있을 경우
      if (!ssoLoginId.equals(loginId)) {
        chkRedisFlag = true;
        newLoginId = ssoLoginId;
      } else {
        newLoginId = ssoLoginId;
        validateToken = true;
      }
      lockChk = checkLockedUser(ssoLoginId, request);
    } else if (ssoLoginId == null && loginId != null) {
      //토큰만 있을경우
//      claims = verify(authorizationToken);
      lockChk = checkLockedUser(loginId, request);
      newLoginId = loginId;
      chkRedisFlag = true;
//      validateToken = true;
    } else if (ssoLoginId != null && loginId == null) {
      //sso만 있을 경우 ->레디스 userSession 확인
      lockChk = checkLockedUser(ssoLoginId, request);
      newLoginId = ssoLoginId;
      chkRedisFlag = true;
    } else {
      none = true;
    }
    
    if (none) {
      chain.doFilter(request, response);
    } else {
      if (chkRedisFlag) {
        userSession = (UserSession) redisRepository.getObject(newLoginId, UserSession.class);
        if (userSession == null) {
          newTokenFlag = true;
        } else {
          try {
            validateToken = true;
            authorizationToken = userSession.getToken();

            String isLogout = redisRepository.get(Commons.LOGOUT_PREFIX + authorizationToken, String.class);
            if (isLogout != null) {
              loggger.info("Verifying token failed -> logout token");
              newTokenFlag = true;
              validateToken = false;
            } else {
              claims = verify(authorizationToken); 
              //다큐멘텀 아이디 없는 경우가 있어서 임의로 추가..
              if (userSession.getDUserId() == null) {
                userSession.setDocbase(DCTMConstants.DOCBASE);
                userSession.setDUserId(userSession.getUser().getUserId());
                userSession.setDPw(DCTMConstants.DCTM_GLOBAL_PW); 
                saveRedisFlag = true;
              }
            }
            
          } catch (Exception e) {
            loggger.info("Verifying token failed (2)");
            newTokenFlag = true;
            validateToken = false;
          }
        }
      }
      
//      if (validateToken) {
//        if (lockChk == 0 || lockChk == 3) {
//          lockChk = checkLockedUser(null, authorizationToken, request);
//        }
//      }
//      if (lockChk == 1) isBann = true;
      
      if (lockChk == 2) isLock = true;
      else {
        //진행
        if (newTokenFlag) {
          try {
            userSession = loginService.loginWithoutPassword(newLoginId);
            if (userSession.getUser().getUserState() != null ) {
              //[ search D'Box 사용/미사용 (0:사용, 1:미사용) ]
              //and edms_usr.user_state <> '0'
              if ("1".equals(userSession.getUser().getUserState())) {
                if (lockChk != 3) isLock = true;
              }
            }
            if (!isLock) {
              authorizationToken = jwt.newToken(JWT.Claims.of(userSession.getUser().getUserId(), new String[] { Role.USER.value() }));
              userSession.setToken(authorizationToken);
              userSession.setDocbase(DCTMConstants.DOCBASE);
              userSession.setDUserId(userSession.getUser().getUserId());
              userSession.setDPw(DCTMConstants.DCTM_GLOBAL_PW);
              saveRedisFlag = true;
              this.addTokenToCookie(tokenHeader, authorizationToken, response);
              claims = verify(authorizationToken);
            }
          } catch (Exception e) {
            loggger.info("Verifying token failed (3)");
//            loggger.warn("Login processing failed: {}", e.getMessage());
          }
        } else if (validateToken) {
          // 만료 60분 전 x 3h
          boolean isError = false;
          if (canRefresh(claims, 60000 * 60 * refreshHour)) {
            authorizationToken = jwt.refreshToken(claims);
            if (userSession == null) {
              userSession = (UserSession) redisRepository.getObject(newLoginId, UserSession.class);
              if (userSession == null) {
                try {
                  userSession = loginService.loginWithoutPassword(newLoginId);
                  logDao.insertLog(LogLogin.builder()
                      .uLoginSource("W")
                      .uUserId(newLoginId) 
                      .uUserIp(getClientIp(request))
                      .build());
                } catch (Exception e) {
                  isError = true;
                  loggger.info("Filter Login failed ");
                }
              }
            } 
            if (!isError) {
//            authorizationToken = jwt.refreshToken(authorizationToken);
              userSession.setToken(authorizationToken);
              userSession.setDocbase(DCTMConstants.DOCBASE);
              userSession.setDUserId(userSession.getUser().getUserId());
              userSession.setDPw(DCTMConstants.DCTM_GLOBAL_PW); 
              saveRedisFlag = true;
            }
          }
          if (!isError) {
            this.addTokenToCookie(tokenHeader, authorizationToken, response);
          }
        } 
      }
      if ((!isLock)) {
        if (saveRedisFlag) redisRepository.put(Commons.SESSION_PREFIX + newLoginId, userSession, Commons.REDIS_TIMEOUT_SEC);
        List<GrantedAuthority> authorities = obtainAuthorities(claims);
        if (isNotEmpty(newLoginId) && authorities.size() > 0) {
          JwtAuthenticationToken authentication = new JwtAuthenticationToken(
              new JwtAuthentication(newLoginId), authorizationToken, authorities);
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
      } else {
        //banned
        loggger.info("====Banned User Request====");
        boolean isMobile = chkIsMobile(request);
        if (!isMobile) {
          Map<String, Object> result = new HashMap<>();
          result.put("success", false);
          Map<String, Object> error = new HashMap<>();
          error.put("message", "LOCKED");
          error.put("type", "L");
          result.put("error", error);
          String json = new Gson().toJson(result);
          response.setStatus(HttpStatus.FORBIDDEN.value());
          response.setHeader("content-type", "application/json");
          response.getWriter().write(json);
          response.getWriter().flush();
          response.getWriter().close();
        } else {
          Map<String, Object> result = new HashMap<>();
          result.put("success", false);
          Map<String, Object> error = new HashMap<>();
          error.put("message", "User is Locked or Banned");
          error.put("status", HttpStatus.FORBIDDEN.value());
          result.put("response", error);
          String json = new Gson().toJson(result);
          response.setStatus(HttpStatus.OK.value());
          response.setHeader("content-type", "application/json");
          response.getWriter().write(json);
          response.getWriter().flush();
          response.getWriter().close();
        }
      }
    }
  }

  private boolean canRefresh(JWT.Claims claims, long refreshRangeMillis) {
    long exp = claims.exp();
    if (exp > 0) {
      long remain = exp - System.currentTimeMillis();
      return remain < refreshRangeMillis;
    }
    return false;
  }

  private List<GrantedAuthority> obtainAuthorities(JWT.Claims claims) {
    String[] roles = claims != null ? claims.roles : null;
    return roles == null || roles.length == 0 ? Collections.emptyList()
        : Arrays.stream(roles).map(SimpleGrantedAuthority::new).collect(toList());
  }

  private String obtainAuthorizationToken(HttpServletRequest request) {
    String token = request.getHeader(tokenHeader);
    if (token == null) token = request.getParameter(tokenHeader);
    
    String rst = null;
    if (token != null) {
      if (loggger.isDebugEnabled())
        loggger.debug("Jwt authorization api detected: {}", token);
      try {
        token = URLDecoder.decode(token, "UTF-8");
        String[] parts = token.split(" ");
        if (parts.length == 1) {
          rst = parts[0];
        }
//                if (parts.length == 2) {
//                    String scheme = parts[0];
//                    String credentials = parts[1];
//                    return BEARER.matcher(scheme).matches() ? credentials : null;
//                }
      } catch (UnsupportedEncodingException e) {
        loggger.error(e.getMessage(), e);
      }
    } else {
      if (request.getCookies() != null) {
        for (Cookie cookie : request.getCookies()) {
          if (tokenHeader.equals(cookie.getName())) {
            rst = cookie.getValue();
          }
        }
      }
    }
    return rst;              
  }

  private int checkLockedUser(String userId, HttpServletRequest request) {
    if ("/kupload/raonkhandler".equals(request.getServletPath())) return 3;
//    if (token != null) {
//      String isBanned = redisRepository.get(Commons.LOGOUT_PREFIX + token, String.class);
//      if (isBanned != null) return 1;
//    }
    if (userId != null) {
      String isLocked = redisRepository.get(Commons.LOCK_PREFIX + userId, String.class);
      if (isLocked != null) return 2;
    }
    return 0;
  }
  
  private void addTokenToCookie(String key, String value, HttpServletResponse response) {
    Cookie cookie = new Cookie(key, value);
    cookie.setMaxAge(Commons.REDIS_TIMEOUT_SEC.intValue());
    cookie.setPath("/");
    cookie.setHttpOnly(false);
    response.addCookie(cookie);
  }
  
  private String getTokenFromCookie(HttpServletRequest request, HttpServletResponse response, String key) {
    String token = null;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (key.equals(cookie.getName())) {  
          token = cookie.getValue();
          break;
        }
      }
    }
    return token;
  }
  private JWT.Claims verify(String token) {
    return jwt.verify(token);
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
   * 클라이언트 ip 조회
   */
  protected String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-FORWARDED-FOR");
    if (ip == null)
      ip = request.getRemoteAddr();
    return ip;
  }
}