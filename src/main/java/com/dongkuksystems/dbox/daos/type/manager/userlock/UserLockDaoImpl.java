package com.dongkuksystems.dbox.daos.type.manager.userlock;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.user.UserLockFilterDto;
import com.dongkuksystems.dbox.models.type.user.UserLock; 

@Primary
@Repository
public class UserLockDaoImpl implements UserLockDao {
  private UserLockMapper userLockMapper;

  public UserLockDaoImpl(UserLockMapper userLockMapper) {
    this.userLockMapper = userLockMapper;
  }

  @Override
  public List<UserLock> selectAll(UserLockFilterDto userLockFilterDto) {
    return userLockMapper.selectAll(userLockFilterDto);
  }

  @Override
  public UserLock selectOneByObjId(String rObjectId) {
    
    return userLockMapper.selectOneByObjId(rObjectId);
  }
  
  
}
