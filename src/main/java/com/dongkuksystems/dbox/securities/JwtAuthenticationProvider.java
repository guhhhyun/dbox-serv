package com.dongkuksystems.dbox.securities;

import static org.apache.commons.lang3.ClassUtils.isAssignable;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.dongkuksystems.dbox.constants.Commons;
import com.dongkuksystems.dbox.constants.DCTMConstants;
import com.dongkuksystems.dbox.errors.LockUnauthorizedException;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.errors.UnauthorizedException;
import com.dongkuksystems.dbox.models.common.Role;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.repositories.redis.RedisRepository;
import com.dongkuksystems.dbox.services.login.LoginService;

public class JwtAuthenticationProvider implements AuthenticationProvider {

  private final JWT jwt;
  private final LoginService loginService;
  private final RedisRepository redisRepository;
  public JwtAuthenticationProvider(JWT jwt, LoginService loginService, RedisRepository redisRepository) {
    this.jwt = jwt;
    this.loginService = loginService;
    this.redisRepository = redisRepository;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) authentication;
    return processUserAuthentication(authenticationToken.authenticationRequest());
  }

  private Authentication processUserAuthentication(AuthenticationRequest request) {
    try {
      boolean isAuthIgnore = request.isAuthIgnore();
    	UserSession userSession = isAuthIgnore
    	    ? loginService.loginWithoutPassword(request.getUserId())
    	    : loginService.login(request.getUserId(), request.getPassword());
    	boolean isLocked = false;
    	if (userSession.getUser().getUserState() != null ) {
    	  if ("1".equals(userSession.getUser().getUserState())) {
    	    isLocked = true;
    	  } else {
    	    String locked = redisRepository.get(Commons.LOCK_PREFIX + request.getUserId(), String.class);
    	    if (locked != null) isLocked = true;
    	  }
    	}
    	if (isLocked) {
    	  if (request.getIsMobile() != null && request.getIsMobile())
    	    throw new LockUnauthorizedException("LOCKED", true);
    	  else 
    	    throw new LockUnauthorizedException("LOCKED", false);
    	}
    	JwtAuthenticationToken authenticated = new JwtAuthenticationToken(userSession.getUser().getUserId(), null, createAuthorityList(Role.USER.value()));
      String apiToken = jwt.newToken(JWT.Claims.of(userSession.getUser().getUserId(), new String[] { Role.USER.value() }));
      userSession.setToken(apiToken);
      userSession.setDocbase(DCTMConstants.DOCBASE);
      userSession.setDUserId(userSession.getUser().getUserId());
      userSession.setDPw(DCTMConstants.DCTM_GLOBAL_PW);
      authenticated.setDetails(new AuthenticationResult(apiToken, userSession.getUser()));
      redisRepository.put(Commons.SESSION_PREFIX + request.getUserId(), userSession, Commons.REDIS_TIMEOUT_SEC);
      
      return authenticated;
    } catch (UnauthorizedException e) {
      if (request.getIsMobile() != null && request.getIsMobile()) {
        throw new LockUnauthorizedException("USER NOT FOUND", true);
      } else {
        throw e;
      }
    } catch (LockUnauthorizedException e) {
      throw e;
    } catch (NotFoundException e) {
      if (request.getIsMobile() != null && request.getIsMobile()) {
        throw new LockUnauthorizedException("USER NOT FOUND", true);
      } else {
        throw new UsernameNotFoundException(e.getMessage());
      }
    } catch (IllegalArgumentException e) {
      throw new BadCredentialsException(e.getMessage());
    } catch (DataAccessException e) {
      throw new AuthenticationServiceException(e.getMessage(), e);
    } catch (Exception e) {
      //TODO: exception
      throw new BadCredentialsException(e.getMessage(), e);
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return isAssignable(JwtAuthenticationToken.class, authentication);
  }
}
