package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.constants.Commons;
import com.dongkuksystems.dbox.daos.table.log.LogDao;
import com.dongkuksystems.dbox.errors.UnauthorizedException;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.mobile.MobileDeviceLoginDto;
import com.dongkuksystems.dbox.models.table.log.LogLogin;
import com.dongkuksystems.dbox.repositories.redis.RedisRepository;
import com.dongkuksystems.dbox.securities.AuthenticationRequest;
import com.dongkuksystems.dbox.securities.AuthenticationResult;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.securities.JwtAuthenticationToken;
import com.dongkuksystems.dbox.services.mobile.MobileService;
import com.dongkuksystems.dbox.services.sso.SsoManager;
import com.dongkuksystems.dbox.utils.CommonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(tags = "로그인 APIs")
public class LoginController extends AbstractCommonController {
  @Value("${auth.ignore-ip}")
  private String[] authIgnoreIps;
  
  private final RedisRepository redisRepository;
  private final MobileService mobileService;
  private SsoManager ssoManager;
  private LogDao logDao;

  public LoginController(RedisRepository redisRepository, MobileService mobileService, LogDao logDao) {
    this.redisRepository = redisRepository;
    this.mobileService = mobileService;
    this.logDao = logDao;
  }

  @PostMapping("/loginSSO")
  @ApiOperation(value = "sso로그인)")
	public ApiResult<AuthenticationResult> loginSSO(@RequestBody AuthenticationRequest authRequest, HttpServletRequest request, HttpServletResponse response) throws Exception{
		String reqUrl = request.getParameter("reqUrl");
		try {
			String sso_id = ssoManager.getSsoId(request);
      boolean isMobile = chkIsMobile(request);  // TODO flutter에서도 모바일 여부 확인 가능한지
			
      JwtAuthenticationToken authToken = new JwtAuthenticationToken(ssoManager.getSsoId(request),"");
      Authentication authentication = getAuthenticationManager().authenticate(authToken);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      
      ObjectMapper mapper = getObjectMapper();
      Map<String, Object> map = mapper.convertValue(authentication.getDetails(), HashMap.class);
      Cookie cookie = new Cookie("api_key", (String) map.get("apiToken"));
      cookie.setMaxAge(Commons.REDIS_TIMEOUT_SEC.intValue());
      cookie.setHttpOnly(false);
      cookie.setPath("/");
      
      // 모바일에서 요청되었고 모바일 정보가 포함되었을 경우 업데이트
      if (isMobile && authRequest.getMobileDevice() != null) {
      	authRequest.getMobileDevice().setUserId(sso_id);
      	mobileService.updateMobileDevice(sso_id, authRequest.getMobileDevice());
      }
      response.addCookie(cookie);
      
      logDao.insertLog(LogLogin.builder()
              .uLoginSource("W")
              .uUserId(sso_id) 
              .uUserIp(getClientIp(request))
              .build());
      
      return OK((AuthenticationResult) authentication.getDetails());
    } catch (AuthenticationException e) {
      throw new UnauthorizedException(e.getMessage());
    }
  }
	
  @PostMapping("/login")
  @ApiOperation(value = "사용자 로그인 (API 토큰 필요없음)")
  public ApiResult<AuthenticationResult> authentication(@RequestBody AuthenticationRequest authRequest,
      HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    try {
      String ip = request.getRemoteAddr();

      boolean isAuthIgnore = Arrays.asList(authIgnoreIps).stream().anyMatch(item -> StringUtils.equals(item, ip));
      
//      boolean isMobile = chkIsMobile(request);  // TODO flutter에서도 모바일 여부 확인 가능한지
      JwtAuthenticationToken authToken = new JwtAuthenticationToken(authRequest.getUserId(),
          authRequest.getPassword(), authRequest.getIsMobile(), isAuthIgnore);
      Authentication authentication = getAuthenticationManager().authenticate(authToken);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      
      ObjectMapper mapper = getObjectMapper();
      Map<String, Object> map = mapper.convertValue(authentication.getDetails(), HashMap.class);
      Cookie cookie = new Cookie("api_key", (String) map.get("apiToken"));
      cookie.setMaxAge(Commons.REDIS_TIMEOUT_SEC.intValue());
      cookie.setHttpOnly(false);
      cookie.setPath("/");
      
      // 모바일에서 요청되었고 모바일 정보가 포함되었을 경우 업데이트
      if (authRequest.getIsMobile() && authRequest.getMobileDevice() != null) {
      	authRequest.getMobileDevice().setUserId(authRequest.getUserId());
      	mobileService.updateMobileDevice(authRequest.getUserId(), authRequest.getMobileDevice());
      }

      //TODO: (유두연) login log 주석 
      logDao.insertLog(LogLogin.builder()
          .uLoginSource("W")
          .uUserId(authRequest.getUserId()) 
          .uUserIp(getClientIp(request))
          .build());
      
      response.addCookie(cookie);
      return OK((AuthenticationResult) authentication.getDetails());
    } catch (AuthenticationException e) {
      throw new UnauthorizedException(e.getMessage());
    }
  }

  @PostMapping("/logout")
  @ApiOperation(value = "사용자 로그인 (API 토큰 필요없음)")
  public ApiResult<Boolean> logout(@AuthenticationPrincipal JwtAuthentication authentication,
      HttpServletResponse response) throws UnauthorizedException {
    try {
      if (authentication == null) {
        return OK(true);
      }
      UserSession userSession = (UserSession) redisRepository.getObject(authentication.loginId.toString(), UserSession.class);
      if (userSession != null) {
        redisRepository.delete(authentication.loginId.toString());
        redisRepository.put(Commons.LOGOUT_PREFIX + userSession.getToken(), "LOGOUT TOKEN", Commons.REDIS_TIMEOUT_SEC); 
      }
      Cookie cookie = new Cookie("api_key", null);
      cookie.setMaxAge(0);
      cookie.setHttpOnly(false);
      cookie.setPath("/"); 
      response.addCookie(cookie);
      return OK(true);
    } catch (AuthenticationException e) {
      throw new UnauthorizedException(e.getMessage());
    }
  }

  @GetMapping("/session")
  @ApiOperation(value = "session확인")
  public ApiResult<UserSession> getSession(@AuthenticationPrincipal JwtAuthentication authentication) {
    UserSession userSession = (UserSession) redisRepository.getObject(authentication.loginId, UserSession.class);
    return OK(userSession);
  }
  
  @GetMapping("/checkSession/{loginId}")
  @ApiOperation(value = "session확인 (개발용)")
  public ApiResult<UserSession> checkSessionForTest(@AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "doc obj Id", example = "213211") @PathVariable String loginId) {
    UserSession userSession = (UserSession) redisRepository.getObject(loginId, UserSession.class);
    return OK(userSession);
  }
  
  @GetMapping("/removeSession/{loginId}")
  @ApiOperation(value = "session삭제 (개발용)")
  public ApiResult<String> removeSessionForTest(@AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "doc obj Id", example = "213211") @PathVariable String loginId) {
    redisRepository.delete(Commons.SESSION_PREFIX + loginId);
    return OK("OK");
  }
  
  
  
}
