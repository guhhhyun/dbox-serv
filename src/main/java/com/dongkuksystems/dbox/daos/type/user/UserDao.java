package com.dongkuksystems.dbox.daos.type.user;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.user.UserFilterDto;
import com.dongkuksystems.dbox.models.type.user.User; 


public interface UserDao {
  public List<User> selectAll(UserFilterDto userFilterDto); 
  
}
