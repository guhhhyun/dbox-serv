package com.dongkuksystems.dbox.daos.type.auth.share;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.auth.AuthShare; 

@Primary
@Repository
public class AuthShareDaoImpl implements AuthShareDao {
  private AuthShareMapper authShareMapper;

  public AuthShareDaoImpl(AuthShareMapper authShareMapper) {
    this.authShareMapper = authShareMapper;
  }

  @Override
  public List<AuthShare> selectList(String objectId) {
    return authShareMapper.selectList(objectId);
  }

  @Override
  public List<AuthShare> selectDetailList(String objectId) {
  	return authShareMapper.selectDetailList(objectId);
  }
}
