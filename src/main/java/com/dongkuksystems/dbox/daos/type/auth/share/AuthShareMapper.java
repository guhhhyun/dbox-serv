package com.dongkuksystems.dbox.daos.type.auth.share;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.type.auth.AuthShare; 

public interface AuthShareMapper {
  public List<AuthShare> selectList(@Param("objectId") String objectId);
  public List<AuthShare> selectDetailList(@Param("objectId") String objectId);
}
