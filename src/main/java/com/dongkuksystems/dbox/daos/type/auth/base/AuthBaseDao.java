package com.dongkuksystems.dbox.daos.type.auth.base;

import java.util.List;

import com.dongkuksystems.dbox.models.type.auth.AuthBase;

public interface AuthBaseDao {
  public List<AuthBase> selectList(String objectId, String authType); 
  public List<AuthBase> selectDetailList(String objectId);
  public List<AuthBase> selectLiveCloseList(String dataId, String docStatus);
}
