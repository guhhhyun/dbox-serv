package com.dongkuksystems.dbox.daos.table.log;

import java.util.List;

import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.table.log.LogFolder;
import com.dongkuksystems.dbox.models.table.log.LogLogin;
import com.dongkuksystems.dbox.models.table.log.LogPcDocs;
import com.dongkuksystems.dbox.models.table.log.LogShare;
import com.dongkuksystems.dbox.models.table.log.LogUsb;
import com.dongkuksystems.dbox.models.table.log.LogUserLock;

public interface LogDao {
  public void insertLog(LogDoc logDoc);
  public void insertLog(LogFolder logFolder);
  public void insertLog(LogLogin logLogin);

  public void insertLog(LogUsb logUsb);
  public void insertLog(LogShare logShare);
  public void insertLog(LogPcDocs logPcDocs);
  public void insertLog(LogUserLock logUserLock);
  
  public void insertLogPcDocsList(List<LogPcDocs> logPcDocs);
  public void deleteLogPcDocsList(String userid);
}
