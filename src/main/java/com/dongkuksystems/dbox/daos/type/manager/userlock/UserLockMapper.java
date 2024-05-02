package com.dongkuksystems.dbox.daos.type.manager.userlock;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.user.UserLockFilterDto;
import com.dongkuksystems.dbox.models.type.user.UserLock; 

public interface UserLockMapper { 
  public List<UserLock> selectAll(@Param("userLock") UserLockFilterDto userLockFilterDto);

  public UserLock selectOneByObjId(@Param("rObjectId") String rObjectId);
}
