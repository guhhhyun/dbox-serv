package com.dongkuksystems.dbox.daos.table.log;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.table.log.LogFolder;
import com.dongkuksystems.dbox.models.table.log.LogLogin;
import com.dongkuksystems.dbox.models.table.log.LogPcDocs;
import com.dongkuksystems.dbox.models.table.log.LogShare;
import com.dongkuksystems.dbox.models.table.log.LogUsb;
import com.dongkuksystems.dbox.models.table.log.LogUserLock; 

@Primary
@Repository
public class LogDaoImpl implements LogDao {
  private LogMapper logMapper;

  public LogDaoImpl(LogMapper logMapper) {
    this.logMapper = logMapper;
  }

  @Override
  public void insertLog(LogDoc logDoc) {
    logMapper.insertLogDoc(logDoc);
  }

  @Override
  public void insertLog(LogFolder logFolder) {
    logMapper.insertLogFolder(logFolder);
  }

  @Override
  public void insertLog(LogLogin logLogin) {
    logMapper.insertLogLogin(logLogin);
  }

  @Override
  public void insertLog(LogUsb logUsb) {
    logMapper.insertLogUsb(logUsb);
  }

  @Override
  public void insertLog(LogShare logShare) {
    logMapper.insertLogShare(logShare);
  }

  @Override
  public void insertLog(LogPcDocs logPcDocs) {
    logMapper.insertLogPcDocs(logPcDocs);
  }
  
  @Override
  public void insertLogPcDocsList(List<LogPcDocs> logPcDocs) {
	  logMapper.insertLogPcDocsList(logPcDocs);
  }
  
  @Override
  public void deleteLogPcDocsList(String userid) {
	  logMapper.deleteLogPcDocsList(userid);
  }
  
  

  @Override
  public void insertLog(LogUserLock logUserLock) {
    logMapper.insertLogUserLock(logUserLock);
  }
}
