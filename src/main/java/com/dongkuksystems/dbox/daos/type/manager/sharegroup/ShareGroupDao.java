package com.dongkuksystems.dbox.daos.type.manager.sharegroup;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.type.manager.sharegroup.ShareGroup; 


public interface ShareGroupDao {
  public List<ShareGroup> selectAll(); 
  public List<ShareGroup> selectDeptInShareGroup(String rObjectId);
  public Optional<ShareGroup> selectOneByUDeptCode(String uDeptCode); 
  public List<ShareGroup> selectOnlyOneCabinetCode(String rObjectId, String uDeptCode);
  public List<ShareGroup> selectCabinetCodeList(String rObjectId, String uDeptCode);
  public List<ShareGroup> selectDuplicationDept(String rObjectId, String uDeptCode);
  public List<ShareGroup> selectAclCabinetCode(String aclGroupName);
}
