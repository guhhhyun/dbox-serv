package com.dongkuksystems.dbox.models.common;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.documentum.com.DfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfLoginInfo;
import com.dongkuksystems.dbox.constants.Commons;
import com.dongkuksystems.dbox.constants.DCTMConstants;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Component
public class Documentum {
  private Logger logger = LoggerFactory.getLogger(Documentum.class);
//  private final HashMap<String, IDfSessionManager> sessionMap = new HashMap<>();
  LoadingCache<String, IDfSessionManager> sessionCache = CacheBuilder.newBuilder()
      .maximumSize(Commons.DCTM_SESSION_MAXIMUM).expireAfterWrite(Commons.DCTM_SESSION_DURATION_MIN, TimeUnit.MINUTES)
      // 필요시 개발해야함
//      .removalListener(MY_LISTENER)
      .build(new CacheLoader<String, IDfSessionManager>() {
        @Override
        public IDfSessionManager load(String key) throws Exception {
          return null;
        }
      });

  private IDfSessionManager createSessionManager(String userName, String userPassword, String docbrokerHost,
      String docbrokerPort) throws DfException {
    DfClientX clientX = new DfClientX();
    IDfClient client = clientX.getLocalClient();
    IDfTypedObject config = client.getClientConfig();

    if (docbrokerHost != null && docbrokerPort != null) {
      config.setString("primary_host", docbrokerHost);
      config.setInt("primary_port", Integer.valueOf(docbrokerPort));
    }
    IDfSessionManager sessionManager = null;
    sessionManager = client.newSessionManager();
    IDfLoginInfo loginInfo = clientX.getLoginInfo();
    loginInfo.setUser(userName);
    loginInfo.setPassword(userPassword);
    sessionManager.setIdentity(IDfSessionManager.ALL_DOCBASES, loginInfo);
    logger.info("Session Manager Created");
    return sessionManager;
  }

  public void makeSessionManager(UserSession userSession) throws Exception {
    String userId = null; 
    if (userSession.getDUserId() == null) {
      userId = userSession.getUser().getUserId();
      userSession.setDocbase(DCTMConstants.DOCBASE);
      userSession.setDUserId(userSession.getUser().getUserId());
      userSession.setDPw(DCTMConstants.DCTM_GLOBAL_PW);
    } else {
      userId = userSession.getDUserId();
    }
    IDfSessionManager sessionManager = sessionCache.getIfPresent(userId);
    if (sessionManager == null) {
      logger.info("Creating session manager");
      sessionCache.put(userSession.getDUserId(),
          createSessionManager(userSession.getDUserId(), DCTMConstants.DCTM_GLOBAL_PW, null, null));
      logger.info("Created session manager");
    }
  }

  public IDfSession getSession(UserSession userSession) throws Exception {
    IDfSessionManager sessionManager = null;
    String userId = null; 
    if (userSession.getDUserId() == null) {
      userId = userSession.getUser().getUserId();
      userSession.setDocbase(DCTMConstants.DOCBASE);
      userSession.setDUserId(userSession.getUser().getUserId());
      userSession.setDPw(DCTMConstants.DCTM_GLOBAL_PW);
    } else {
      userId = userSession.getDUserId();
    }
    sessionManager = sessionCache.getIfPresent(userId);
    if (sessionManager == null) {
      logger.info("Creating session manager");
      sessionManager = createSessionManager(userSession.getDUserId(), DCTMConstants.DCTM_GLOBAL_PW, null, null);
      sessionCache.put(userSession.getDUserId(), sessionManager);
      logger.info("Created session manager");
    } else {
      sessionManager = sessionCache.get(userSession.getDUserId());
    }

    try {
      return sessionManager.getSession(userSession.getDocbase());
    } catch (Exception e) {
      String msg = String.format("Session cannot be instantiated for user %s for docBase %s. " + "Exception: %s, %s.",
          userSession.getDUserId(), userSession.getDocbase(), e.getClass(), e.getMessage());
      logger.error(msg);
      throw new DfException(msg, e);
    }
  }

  public IDfSession getSession(String userId, String docBase) throws Exception {
    IDfSessionManager sessionManager = sessionCache.getIfPresent(userId);
    if (sessionManager == null) {
      logger.info("Creating session manager");
      sessionManager = createSessionManager(userId, DCTMConstants.DCTM_GLOBAL_PW, null, null);
      sessionCache.put(userId, sessionManager);
      logger.info("Created session manager");
    } else {
      sessionManager = sessionCache.get(userId);
    }

    try {
      return sessionManager.getSession(docBase);
    } catch (Exception e) {
      String msg = String.format("Session cannot be instantiated for user %s for docBase %s. " + "Exception: %s, %s.",
          userId, docBase, e.getClass(), e.getMessage());
      logger.error(msg);
      throw new DfException(msg, e);
    }
  }

  public void release(String dUserId, IDfSession session) {
    IDfSessionManager sessionManager = sessionCache.getIfPresent(dUserId);
    if (sessionManager != null && session != null && session.isConnected()) {
      sessionManager.release(session);
    }
  }
}