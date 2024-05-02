package com.dongkuksystems.dbox.config.support;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.repositories.redis.RedisRepository;
import com.dongkuksystems.dbox.securities.JWT;
import com.dongkuksystems.dbox.services.sso.SsoManager;

/*
* preHandle() : 맵핑되기 전 처리
* postHandle() : 맵핑되고난 후 처리
* afterCompletion() : 모든 작업이 완료된 후 실행
*/
@Component
public class HttpInterceptor implements HandlerInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(HttpInterceptor.class);

  @Autowired
  private SsoManager ssoManager;
  
//  @Override
//  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//  	boolean isJava = request.getHeader("User-Agent").toUpperCase().indexOf("JAVA") > -1;
//  	String serverNm = request.getServerName();
//  	//boolean isServerCall = serverNm.contains("niris") ||serverNm.contains("172.31.")||serverNm.contains("10.90.9.106")?true:false;
//  	boolean isServerCall = serverNm.contains("niris") ||serverNm.contains("172.31.")?true:false;
//	
//    String sso_id = ssoManager.getSsoId(request);
////    logger.error("aaaaaa {}", sso_id);
//
//    // 1.SSO ID 수신
//    Object a = request.getSession().getAttribute("sysAdmin");
//    String reqUrl = request.getRequestURI();
//    boolean isSysAdmin = false;
//    if (reqUrl.contains("login") || reqUrl.contains("logout") || reqUrl.contains("/m/") || reqUrl.contains(".json")|| reqUrl.contains("swagger")) {
//      return true;
//    }
//    if (reqUrl.contains("kupload/raonkhandler") ) {
//      return true;
//    }
//    if (a != null) {
//      isSysAdmin = (Boolean) a;
//    }
//    if (isSysAdmin == true) {
//      logger.error("[STEP 0] 관리자 모드로 로그인");
//    } else {
//      sso_id = ssoManager.getSsoId(request);
//      String retCode = ssoManager.getEamSessionCheck(request, response);
//
//      if (sso_id == null) {
//        if (request.getAttribute("api_key") != null) {
//          return true;
//        }
////        logger.error("[STEP 1] 현재 접근계정 SSO ID : {}", sso_id);
////        if (!retCode.equals("0")&& !retCode.equals("1005")  && !isJava && !isServerCall) { // 허용되지 않는 서버에서 오더라도 로그인 되도록 수정
//////            ssoManager.goLoginPage(request, response); //Niris 로그인페이지로 이동
////            return false;
////        }
//      } else {
//        // 4.쿠키 유효성 확인 :0(정상)
//        if ( !retCode.equals("0") && !retCode.equals("1005") &&!isJava) { // 허용되지 않는 서버에서 오더라도 로그인 되도록 수정
//          logger.error("[STEP 2] 현재 접근계정 SSO ID : {}", sso_id);
//          logger.error("[STEP 3] RETURN CODE : {}", retCode);
////          ssoManager.goErrorPage(response, Integer.parseInt(retCode));
//          return false;
//        }
//        // 5.업무시스템에 읽을 사용자 아이디를 세션으로 생성
//        String EAM_ID = (String) request.getSession().getAttribute("SSO_ID");
//        if (EAM_ID == null || EAM_ID.equals("") || !sso_id.equals(EAM_ID)) {
//          request.getSession().setAttribute("SSO_ID", sso_id);
//        }
////              logger.debug("[SSO PROCESS #3] SSO 인증 성공!!");
////              6.업무시스템 페이지 호출(세션 페이지 또는 메인페이지 지정) --> 업무시스템에 맞게 URL 수정!
//        if (EAM_ID == null || !sso_id.equals(EAM_ID)) {
//          String dBoxToken = null;
//          if (request.getCookies() != null) {
//            for (Cookie cookie : request.getCookies()) {
//              if (tokenHeader.equals(cookie.getName())) {
//                dBoxToken = cookie.getValue();
//                break;
//              }
//            }
//          } else { 
//            UserSession userSession = (UserSession) redisRepository.getObject(sso_id, UserSession.class);
//            if (userSession != null ) {
//              dBoxToken = userSession.getToken();
//            } 
//          } 
//          if (dBoxToken == null) {
//            return false;
//          }
//          try {
//            JWT.Claims claims = verify(request, dBoxToken);
//            return true;
//          } catch (Exception e) {
//            logger.warn("Jwt processing failed: {}", e.getMessage());
//            return false;
//          } 
//        }
//      }
//    }
//    return false;
//  }
//
//  @Override
//  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
//      ModelAndView modelAndView) {
////    System.out.println("b");
//  }
//
//  @Override
//  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
////    System.out.println("c");
//  }
//
//  private JWT.Claims verify(HttpServletRequest request, String token) {
//    return jwt.verify(token);
//  }
}