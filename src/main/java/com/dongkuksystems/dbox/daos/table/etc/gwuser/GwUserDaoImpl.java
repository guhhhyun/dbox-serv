package com.dongkuksystems.dbox.daos.table.etc.gwuser;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.table.etc.GwUser;
import com.dongkuksystems.dbox.models.table.etc.VUser; 

@Primary
@Repository
public class GwUserDaoImpl implements GwUserDao {
  private GwUserMapper userMapper;

  public GwUserDaoImpl(GwUserMapper userMapper) {
    this.userMapper = userMapper;
  }
  
  @Override
  public boolean login(String userId, String password) {
    return userMapper.login(userId, password);
  }

  @Override
  public Optional<VUser> selectOneByUserId(String userId) {
    return userMapper.selectOneByUserId(userId);
  }

  @Override
  public Optional<VUser> selectOneBySabun(String sabun) {
    return userMapper.selectOneBySabun(sabun);
  }

  @Override
  public List<VUser> selectListByOrgId(String orgId, String usageState, String direction) {
    return userMapper.selectListByOrgId(orgId, usageState, direction);
  }
  
  @Override
  public List<String> selectUserIdListByTitleCodesForSpecialUser(List<String> titleCodes) {
  	return userMapper.selectUserIdListByTitleCodesForSpecialUser(titleCodes);
  }
  
  @Override
  public List<VUser> selectUserListByDeptCodes(List<String> deptCodes) {
	  return userMapper.selectUserListByDeptCodes(deptCodes);
  }
  @Override
  public List<VUser> selectUserListByCabinetCodes(List<String> deptCodes) {
    return userMapper.selectUserListByCabinetCodes(deptCodes);
  }

  @Override
  public Optional<GwUser> selectOtherGwUserOneByUserId(String userId) {
    return userMapper.selectOtherGwUserOneByUserId(userId);
  }

  @Override
  public List<VUser> selectUserListByUserIds(List<String> userIds) {
  	  return userMapper.selectUserListByUserIds(userIds);
  }

  @Override
  public List<VUser> selectGwUserListByUserIds(List<String> userIds) {
      return userMapper.selectGwUserListByUserIds(userIds);
  }

  @Override
  public int updateUserPw(String userId, String oldPw, String newPw) {
    return userMapper.updateUserPw(userId, oldPw, newPw);
  }
  
//  @Override
//  public List<UserBefore> selectListByGroupId(String groupId, long offset, int limit) {
//    return userMapper.selectListByGroupId(groupId.value(), offset, limit);
//  } 
}
