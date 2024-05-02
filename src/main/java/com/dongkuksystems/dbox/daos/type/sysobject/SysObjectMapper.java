package com.dongkuksystems.dbox.daos.type.sysobject;

import org.apache.ibatis.annotations.Param; 

public interface SysObjectMapper {
  public String selectObjectTypeOne(@Param("rObjectId") String rObjectId);
}
