package com.dongkuksystems.dbox.daos.type.manager.sharegroup;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.manager.sharegroup.ShareGroup;

@Primary
@Repository
public class ShareGroupDaoImpl implements ShareGroupDao {
  private ShareGroupMapper shareGroupMapper;

  public ShareGroupDaoImpl(ShareGroupMapper shareGroupMapper) {
    this.shareGroupMapper = shareGroupMapper;
  }

  @Override
  public List<ShareGroup> selectAll() {
    return shareGroupMapper.selectAll();
  }

  @Override
  public List<ShareGroup> selectDeptInShareGroup(String rObjectId) {
    return shareGroupMapper.selectDeptInShareGroup(rObjectId);
  }

  @Override
//@Cacheable(value = "selectOneByuDeptCode", key = "#uDeptCode")
  public Optional<ShareGroup> selectOneByUDeptCode(String uDeptCode) {
    return shareGroupMapper.selectOneByUDeptCode(uDeptCode);
  }
    
  @Override
  public List<ShareGroup> selectOnlyOneCabinetCode(String rObjectId, String uDeptCode) {
    return shareGroupMapper.selectOnlyOneCabinetCode(rObjectId, uDeptCode);
  } 
  
  @Override
  public List<ShareGroup> selectCabinetCodeList(String rObjectId, String uDeptCode) {
    return shareGroupMapper.selectCabinetCodeList(rObjectId, uDeptCode);
  } 
  
  @Override
  public List<ShareGroup> selectDuplicationDept(String rObjectId, String uDeptCode) {
    return shareGroupMapper.selectDuplicationDept(rObjectId, uDeptCode);
  } 
  
  @Override
  public List<ShareGroup> selectAclCabinetCode(String aclGroupName) {
    return shareGroupMapper.selectAclCabinetCode(aclGroupName);
  } 

}
