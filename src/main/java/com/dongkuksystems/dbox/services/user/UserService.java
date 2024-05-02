package com.dongkuksystems.dbox.services.user;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.table.etc.GwUser;
import com.dongkuksystems.dbox.models.table.etc.VUser;


public interface UserService {
  VUser login(String socialPerId, String password) throws Exception;

  Optional<VUser> selectOneBySabun(String sabun) throws Exception;

  Optional<VUser> selectOneByUserId(String userId) throws Exception;
  
  Optional<GwUser> selectOtherGwUserOneByUserId(String userId) throws Exception;

  List<VUser> selectUserListByUserIds(List<String> userIds) throws Exception;

  List<VUser> selectGwUserListByUserIds(List<String> userIds) throws Exception;
  
  int updateUserPw(String userId, String oldPw, String newPw) throws Exception;
}
