package com.dongkuksystems.dbox.daos.table.etc.gwuser;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.table.etc.GwUser;
import com.dongkuksystems.dbox.models.table.etc.VUser;

public interface GwUserDao {
  public boolean login(String userId, String password);
  public Optional<VUser> selectOneByUserId(String userId); 
  public Optional<VUser> selectOneBySabun(String sabun); 
  public List<VUser> selectListByOrgId(String orgId, String usageState, String direction);
  public List<String> selectUserIdListByTitleCodesForSpecialUser(List<String> titleCodes);
  public List<VUser> selectUserListByDeptCodes(List<String> deptCodes);
  public List<VUser> selectUserListByCabinetCodes(List<String> deptCodes);
  public Optional<GwUser> selectOtherGwUserOneByUserId(String userId);
  public List<VUser> selectUserListByUserIds(List<String> userIds);
  public List<VUser> selectGwUserListByUserIds(List<String> userIds);
  int updateUserPw(String userId, String oldPw, String newPw);
}
