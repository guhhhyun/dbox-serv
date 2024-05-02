package com.dongkuksystems.dbox.services;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.documentum.fc.client.IDfSession;
import com.dongkuksystems.dbox.daos.table.log.LogDao;
import com.dongkuksystems.dbox.models.common.Documentum;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.etc.LogDebugDto;
import com.dongkuksystems.dbox.models.table.log.LogAgent;
import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.table.log.LogLogin;
import com.dongkuksystems.dbox.models.table.log.LogPcDocs;
import com.dongkuksystems.dbox.models.table.log.LogShare;
import com.dongkuksystems.dbox.models.table.log.LogUsb;
import com.dongkuksystems.dbox.models.table.log.LogUserLock;
import com.dongkuksystems.dbox.utils.RestTemplateUtils;

/**
 * 서비스의 공통 로직을 가진 추상 클래스
 *
 * @author 차소익, 유두연
 */
public abstract class AbstractCommonService {
  protected final Logger logger = LoggerFactory.getLogger(this.getClass());
  
  private IDfSession idfSession;
  @Autowired
  private LogDao logDao;
  @Autowired
  private Documentum documentum;
  @Autowired
  private ModelMapper modelMapper;
  @Autowired
  private Environment env;
  @Autowired
  private RestTemplateUtils restTemplateUtils;

  protected IDfSession getIdfSession(UserSession userSession) throws Exception {
    this.idfSession = this.documentum.getSession(userSession);
    return this.idfSession;
  }

  protected IDfSession getIdfSession(String userId, String docBase) throws Exception {
    this.idfSession = this.documentum.getSession(userId, docBase);
    return this.idfSession;
  }
  
  protected void sessionRelease(String dUserId, IDfSession idfSession) {
    this.documentum.release(dUserId, idfSession);
  }

  protected ModelMapper getModelMapper() throws Exception {
    return modelMapper;
  }

	/**
	 * 환경변수
	 */
	protected Environment getEnv() {
		return env;
	}
	 /**
	  * 외부 API 호출 유틸
	  */
	 protected RestTemplateUtils getRestTemplateUtils() {
	  return restTemplateUtils;
	 }

  protected void insertLog(LogLogin log) {
    logDao.insertLog(log);
  }

  protected void insertLog(LogDoc log) {
    logDao.insertLog(log);
  }

//  protected void insertLog(LogFolder log) {
//    logDao.insertLog(log);
//  }

  protected void insertLog(LogUsb log) {
    logDao.insertLog(log);
  }

  protected void insertLog(LogShare log) {
    logDao.insertLog(log);
  }

  protected void insertLog(LogPcDocs log) {
    logDao.insertLog(log);
  }
  
  // 자산 로그 ( 리스트입력. )
  protected void insertLogPcDocsList(List<LogPcDocs> log) {
	logDao.insertLogPcDocsList(log);
  }
  
  protected void deleteLogPcDocsList(String userid) {
	logDao.deleteLogPcDocsList(userid);
  }

  protected void insertLog(LogUserLock log) {
    logDao.insertLog(log);
  }
  
  protected void insertDebugLog(LogDebugDto logDebugDto) {
    logDao.insertDebugLog(logDebugDto);
  }
  

  protected void insertLog(LogAgent log) {
    logDao.insertLog(log);
  }
}
