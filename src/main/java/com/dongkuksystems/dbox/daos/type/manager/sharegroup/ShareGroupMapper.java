package com.dongkuksystems.dbox.daos.type.manager.sharegroup;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.type.manager.sharegroup.ShareGroup; 

public interface ShareGroupMapper { 
  public List<ShareGroup> selectAll();
  public List<ShareGroup> selectDeptInShareGroup(@Param("rObjectId") String rObjectId); 
  public Optional<ShareGroup> selectOneByUDeptCode(@Param("uDeptCode") String uDeptCode);
  public List<ShareGroup> selectOnlyOneCabinetCode(@Param("rObjectId") String rObjectId, @Param("uDeptCode") String uDeptCode);
  public List<ShareGroup> selectCabinetCodeList(@Param("rObjectId") String rObjectId, @Param("uDeptCode") String uDeptCode);
  public List<ShareGroup> selectDuplicationDept(@Param("rObjectId") String rObjectId, @Param("uDeptCode") String uDeptCode);
  public List<ShareGroup> selectAclCabinetCode(@Param("aclGroupName") String aclGroupName);

}