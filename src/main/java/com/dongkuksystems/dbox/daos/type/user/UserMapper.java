package com.dongkuksystems.dbox.daos.type.user;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.user.UserFilterDto;
import com.dongkuksystems.dbox.models.type.user.User; 

public interface UserMapper { 
  public List<User> selectAll(@Param("user") UserFilterDto userFilterDto);
}
