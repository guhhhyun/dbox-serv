package com.dongkuksystems.dbox.daos.type.auth.base;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.type.auth.AuthBase; 

public interface AuthBaseMapper {
  public List<AuthBase> selectList(@Param("objectId") String objectId, @Param("authType") String authType); 
  public List<AuthBase> selectDetailList(@Param("objectId") String objectId); 
  public List<AuthBase> selectLiveCloseList(@Param("dataId") String dataId, @Param("docStatus") String docStatus);
}
