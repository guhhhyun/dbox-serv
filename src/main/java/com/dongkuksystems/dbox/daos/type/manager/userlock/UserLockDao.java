package com.dongkuksystems.dbox.daos.type.manager.userlock;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.user.UserLockFilterDto;
import com.dongkuksystems.dbox.models.type.user.UserLock; 


public interface UserLockDao {
  public List<UserLock> selectAll(UserLockFilterDto userLockFilterDto); 
  public UserLock selectOneByObjId(String rObjectId);
}
