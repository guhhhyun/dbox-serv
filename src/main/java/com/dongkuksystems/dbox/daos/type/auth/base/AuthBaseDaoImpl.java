package com.dongkuksystems.dbox.daos.type.auth.base;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.auth.AuthBase; 

@Primary
@Repository
public class AuthBaseDaoImpl implements AuthBaseDao {
  private AuthBaseMapper authBaseMapper;

  public AuthBaseDaoImpl(AuthBaseMapper authBaseMapper) {
    this.authBaseMapper = authBaseMapper;
  }

  @Override
  public List<AuthBase> selectList(String objectId, String authType) {
    return authBaseMapper.selectList(objectId, authType);
  }

  @Override
  public List<AuthBase> selectDetailList(String objectId) {
    return authBaseMapper.selectDetailList(objectId);
  }

  @Override
  public List<AuthBase> selectLiveCloseList(String dataId, String docStatus) {
  	return authBaseMapper.selectLiveCloseList(dataId, docStatus);
  }
}
