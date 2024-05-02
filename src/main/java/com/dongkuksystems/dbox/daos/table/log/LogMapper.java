package com.dongkuksystems.dbox.daos.table.log;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.etc.LogDebugDto;
import com.dongkuksystems.dbox.models.table.log.LogAgent;
import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.table.log.LogFolder;
import com.dongkuksystems.dbox.models.table.log.LogLogin;
import com.dongkuksystems.dbox.models.table.log.LogPcDocs;
import com.dongkuksystems.dbox.models.table.log.LogShare;
import com.dongkuksystems.dbox.models.table.log.LogUsb;
import com.dongkuksystems.dbox.models.table.log.LogUserLock; 

public interface LogMapper {
  public void insertLogDoc(@Param("logDoc") LogDoc logDoc);
  public void insertLogFolder(@Param("logFolder") LogFolder logFolder);
  public void insertLogLogin(@Param("logLogin") LogLogin logLogin);
  
  public void insertLogUsb(@Param("logUsb") LogUsb logUsb);
  public void insertLogShare(@Param("logShare") LogShare logShare);
  public void insertLogPcDocs(@Param("logPcDocs") LogPcDocs logPcDocs);
  public void insertLogUserLock(@Param("logUserLock") LogUserLock logUserLock);
  public void insertLogAgent(@Param("logAgent") LogAgent logAgent);
  
  public void insertLogPcDocsList(List<LogPcDocs> logPcDocs);
  public void deleteLogPcDocsList(@Param("userid") String userid);
  
  public void insertDebugLog(@Param("logDebug") LogDebugDto logDebugDto);
}
