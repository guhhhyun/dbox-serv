package com.dongkuksystems.dbox.services.sso;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.initech.eam.api.NXContext;
import com.initech.eam.api.NXNLSAPI;
import com.initech.eam.nls.CookieManager;
import com.initech.eam.smartenforcer.SECode;

@Service
@SuppressWarnings("all")
public class SsoManager {
  private static final Logger logger = LoggerFactory.getLogger(SsoManager.class);
  private static String SERVICE_NAME = "";
  private static String SERVER_URL = "";
  private static String SERVER_PORT = "";
  private static String ASCP_URL = "";
  private static String custom_url = "";
  private static String NLS_URL = "";
  private static String NLS_PORT = "";
  private static String NLS_LOGIN_URL = "";
  private static String NLS_LOGOUT_URL = "";
  private static String NLS_ERROR_URL = "";
  private static String ND_URL1 = "";
  private static String ND_URL2 = "";
  private static Vector PROVIDER_LIST = new Vector();
  private static final int COOKIE_SESSTION_TIME_OUT = 43200; // 43200 = 12*60*60; // 초단위 ( 현재 12시간을 적용함. )
  private static String TOA = "1";
  private static String SSO_DOMAIN = ".dongkuk.com";
  private static final int timeout = 15000;
  private static NXContext context = null;
  private static boolean isDevServer = true;
  static {
    try {
      InetAddress inet = InetAddress.getLocalHost();
      String serverIp = inet.getHostAddress();
      
      System.out.println("sso server ip : " + serverIp);
      SERVICE_NAME = "Dbox";
      SERVER_URL = "https://dbox.dongkuk.com";
      //SERVER_URL = "https://dbox-dev.dongkuk.com";
      //SERVER_PORT = "8080";
      ASCP_URL = SERVER_URL + ":" + SERVER_PORT;
      custom_url = "";
      NLS_URL = "http://ssodev.dongkuk.com";
      NLS_PORT = "8080";
      NLS_LOGIN_URL = NLS_URL + ":" + NLS_PORT + "/nls3/cookieLogin.jsp";
      NLS_LOGOUT_URL = NLS_URL + ":" + NLS_PORT + "/nls3/NCLogout.jsp";
      NLS_ERROR_URL = NLS_URL + ":" + NLS_PORT + "/nls3/error.jsp";
      ND_URL1 = "http://ssodev.dongkuk.com:5480";
      List<String> serverurlList = new ArrayList<String>();
      serverurlList.add(ND_URL1);
      context = new NXContext(serverurlList, 15000);
      CookieManager.setEncStatus(true);
      PROVIDER_LIST.add("ssodev.dongkuk.com");
      isDevServer = true;       
      
      //TODO : // 2022.01.17 - 
//      if (serverIp != null && ("172.31.1.82".equals(serverIp) || "172.31.1.83".equals(serverIp))) {// 운영서버 IP일때
//          SERVICE_NAME = "ASSET";
//          SERVER_URL = "http://dbox.dongkuk.com";
//          SERVER_PORT = "80";
//          ASCP_URL = SERVER_URL + ":" + SERVER_PORT;
//          custom_url = "";
//          NLS_URL = "http://sso.dongkuk.com";
//          NLS_PORT = "80";
//          NLS_LOGIN_URL = NLS_URL + ":" + NLS_PORT + "/nls3/cookieLogin.jsp";
//          NLS_LOGOUT_URL = NLS_URL + ":" + NLS_PORT + "/nls3/NCLogout.jsp";
//          NLS_ERROR_URL = NLS_URL + ":" + NLS_PORT + "/nls3/error.jsp";
//          ND_URL1 = "http://nd1.dongkuk.com:5480";
//          ND_URL2 = "http://nd2.dongkuk.com:5480";
//          List<String> serverurlList = new ArrayList<String>();
//          serverurlList.add(ND_URL1);
//          serverurlList.add(ND_URL2);
//          context = new NXContext(serverurlList, 15000);
//          CookieManager.setEncStatus(true);
//          PROVIDER_LIST.add("sso.dongkuk.com");
//          isDevServer = false;
//      } else { // 개발서버일때, if ("172.31.1.116".equals(serverIp) || serverIp.contains("10.90.") ||
//          SERVICE_NAME = "Dbox";
//          SERVER_URL = "https://dbox-test.dongkuk.com";
//          //SERVER_URL = "https://dbox-dev.dongkuk.com";
//          //SERVER_PORT = "8080";
//          ASCP_URL = SERVER_URL + ":" + SERVER_PORT;
//          custom_url = "";
//          NLS_URL = "http://ssodev.dongkuk.com";
//          NLS_PORT = "8080";
//          NLS_LOGIN_URL = NLS_URL + ":" + NLS_PORT + "/nls3/cookieLogin.jsp";
//          NLS_LOGOUT_URL = NLS_URL + ":" + NLS_PORT + "/nls3/NCLogout.jsp";
//          NLS_ERROR_URL = NLS_URL + ":" + NLS_PORT + "/nls3/error.jsp";
//          ND_URL1 = "http://ssodev.dongkuk.com:5480";
//          List<String> serverurlList = new ArrayList<String>();
//          serverurlList.add(ND_URL1);
//          context = new NXContext(serverurlList, 15000);
//          CookieManager.setEncStatus(true);
//          PROVIDER_LIST.add("ssodev.dongkuk.com");
//          isDevServer = true;    	  
//      }
      
      SECode.setCookiePadding("_V42");
      logger.info("[SsoManager configuration] SERVICE_NAME             : {}             ", SERVICE_NAME);
      logger.info("[SsoManager configuration] SERVER_URL               : {}             ", SERVER_URL);
      logger.info("[SsoManager configuration] SERVER_PORT              : {}             ", SERVER_PORT);
      logger.info("[SsoManager configuration] ASCP_URL                 : {}             ", ASCP_URL);
      logger.info("[SsoManager configuration] custom_url               : {}             ", custom_url);
      logger.info("[SsoManager configuration] NLS_URL                  : {}             ", NLS_URL);
      logger.info("[SsoManager configuration] NLS_PORT                 : {}             ", NLS_PORT);
      logger.info("[SsoManager configuration] NLS_LOGIN_URL            : {}             ", NLS_LOGIN_URL);
      logger.info("[SsoManager configuration] NLS_LOGOUT_URL           : {}             ", NLS_LOGOUT_URL);
      logger.info("[SsoManager configuration] NLS_ERROR_URL            : {}             ", NLS_ERROR_URL);
      logger.info("[SsoManager configuration] ND_URL1                  : {}             ", ND_URL1);
      logger.info("[SsoManager configuration] ND_URL2                  : {}             ", ND_URL2);
      logger.info("[SsoManager configuration] PROVIDER_LIST            : {}             ", PROVIDER_LIST);
      logger.info("[SsoManager configuration] COOKIE_SESSTION_TIME_OUT : {}             ", COOKIE_SESSTION_TIME_OUT);
      logger.info("[SsoManager configuration] TOA                      : {}             ", TOA);
      logger.info("[SsoManager configuration] SSO_DOMAIN               : {}             ", SSO_DOMAIN);
      logger.info("[SsoManager configuration] timeout                  : {}             ", timeout);
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
  }

  public static boolean isDevServer() {
    return isDevServer;
  }

  public String getSsoId(HttpServletRequest request) {
    String sso_id = null;
    try {
      sso_id = CookieManager.getCookieValue(SECode.USER_ID, request);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return sso_id;
  }
  
  public String getDBoxToken(HttpServletRequest request, String  tokenHeader) {
    String sso_id = null;
    sso_id = CookieManager.getCookieValue(tokenHeader, request);
    return sso_id;
  }

  public void goLoginPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String reqUrl = ASCP_URL + request.getRequestURI();
    String param = "";
    Enumeration<String> it = request.getParameterNames();
    while (it.hasMoreElements()) {
      String value = (String) it.nextElement();
      param = "?" + value + "=" + request.getParameter(value);
    }
    reqUrl += param;
    CookieManager.addCookie(SECode.USER_URL, reqUrl, SSO_DOMAIN, response);
    CookieManager.addCookie(SECode.R_TOA, TOA, SSO_DOMAIN, response);
    if (custom_url.equals("")) {
      CookieManager.addCookie("CLP", "", SSO_DOMAIN, response);
    } else {
      CookieManager.addCookie("CLP", custom_url, SSO_DOMAIN, response);
    }
    response.sendRedirect(NLS_LOGIN_URL);
  }

  public String getEamSessionCheckAndAgentVaild(HttpServletRequest request, HttpServletResponse response) {
    String retCode = "";
    try {
      retCode = CookieManager.verifyNexessCookieAndAgentVaild(request, response, 0, 43200, PROVIDER_LIST, SERVER_URL,
          context);
    } catch (Exception npe) {
      npe.printStackTrace();
    }
    return retCode;
  }

  public String getEamSessionCheck(HttpServletRequest request, HttpServletResponse response) {
    String retCode = "";
    try {
      retCode = CookieManager.verifyNexessCookie(request, response, 0, 43200, PROVIDER_LIST);
    } catch (Exception npe) {
      npe.printStackTrace();
    }
    return retCode;
  }

  public String getEamSessionCheck2(HttpServletRequest request, HttpServletResponse response) {
    String retCode = "";
    try {
      NXNLSAPI nxNLSAPI = new NXNLSAPI(context);
      retCode = nxNLSAPI.readNexessCookie(request, response, 0, 0);
    } catch (Exception npe) {
      npe.printStackTrace();
    }
    return retCode;
  }

  public void goErrorPage(HttpServletResponse response, int error_code) throws Exception {
    CookieManager.removeNexessCookie(SSO_DOMAIN, response);
    CookieManager.addCookie(SECode.USER_URL, ASCP_URL, SSO_DOMAIN, response);
    response.sendRedirect(NLS_ERROR_URL + "?errorCode=" + error_code);
  }

  public int readNexessCookie(HttpServletRequest request, HttpServletResponse response) {
    int retCode = 0;
    try {
      retCode = CookieManager.readNexessCookie(request, response, SSO_DOMAIN, 0, 43200);
    } catch (Exception npe) {
      npe.printStackTrace();
    }
    return retCode;
  }

  public boolean checkSession(HttpServletRequest request, HttpServletResponse response) {
    try {
      String sso_id = getSsoId(request);
      logger.info("[STEP 1] SSO ID : {}," + sso_id);
      if (sso_id == null) {
        if (request.getRequestURI().indexOf(".json") == -1) {
          goLoginPage(request, response);
        }
        return false;
      }
      String retCode = getEamSessionCheckAndAgentVaild(request, response);
      logger.info("[STEP 2] RETURN CODE : {}," + retCode);
      if (!retCode.equals("0")) {
        if (request.getRequestURI().indexOf(".json") == -1) {
          goErrorPage(response, Integer.parseInt(retCode));
        }
        return false;
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

}
