package com.dongkuksystems.dbox.daos.type.user;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.user.UserFilterDto;
import com.dongkuksystems.dbox.models.type.user.User; 

@Primary
@Repository
public class UserDaoImpl implements UserDao {
  private UserMapper UserMapper;

  public UserDaoImpl(UserMapper UserMapper) {
    this.UserMapper = UserMapper;
  }

  @Override
  public List<User> selectAll(UserFilterDto userFilterDto) {
    return UserMapper.selectAll(userFilterDto);
  }
    
}
