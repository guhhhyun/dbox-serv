package com.dongkuksystems.dbox.daos.type.sysobject;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository; 

@Primary
@Repository
public class SysObjectDaoImpl implements SysObjectDao {
  private SysObjectMapper sysObjectMapper;

  public SysObjectDaoImpl(SysObjectMapper sysObjectMapper) {
    this.sysObjectMapper = sysObjectMapper;
  }
  
  @Override
  public String selectObjectTypeOne(String rObjectId) {
  	return sysObjectMapper.selectObjectTypeOne(rObjectId);
  }
}
