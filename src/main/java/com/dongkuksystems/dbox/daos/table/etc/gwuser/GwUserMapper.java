package com.dongkuksystems.dbox.daos.table.etc.gwuser;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.table.etc.GwUser;
import com.dongkuksystems.dbox.models.table.etc.VUser; 

public interface GwUserMapper {
  public boolean login(@Param("userId") String userId, @Param("password") String password);
  public Optional<VUser> selectOneByUserId(@Param("userId") String userId); 
  public Optional<VUser> selectOneBySabun(@Param("sabun") String sabun); 
  public List<VUser> selectListByOrgId(@Param("orgId") String orgId, @Param("usageState") String usageState, @Param("direction") String direction);
  public List<String> selectUserIdListByTitleCodesForSpecialUser(@Param("titleCodes") List<String> titleCodes);
  public List<VUser> selectUserListByDeptCodes(@Param("deptCodes") List<String> deptCodes);
  public List<VUser> selectUserListByCabinetCodes(@Param("deptCodes") List<String> deptCodes);
  public Optional<GwUser> selectOtherGwUserOneByUserId(@Param("userId") String userId);
  public List<VUser> selectUserListByUserIds(@Param("userIds") List<String> userIds);
  public List<VUser> selectGwUserListByUserIds(@Param("userIds") List<String> userIds);
  public int updateUserPw(@Param("userId") String userId, @Param("oldPw") String oldPw, @Param("newPw") String newPw);
}
