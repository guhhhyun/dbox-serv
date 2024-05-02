package com.dongkuksystems.dbox.daos.table.etc.gwdept;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.table.gwdept.GwDeptListManagerDto;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwDeptPathDto;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwManagerListDto;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwSelectDeptManagerDto;
import com.dongkuksystems.dbox.models.table.etc.GwDept;
import com.dongkuksystems.dbox.models.table.etc.VDept; 

@Primary
@Repository
public class GwDeptDaoImpl implements GwDeptDao {
  private GwDeptMapper gwDeptMapper;

  public GwDeptDaoImpl(GwDeptMapper gwDeptMapper) {
    this.gwDeptMapper = gwDeptMapper;
  }


  @Override
  public List<VDept> selectAll() {
    return gwDeptMapper.selectAll();
  }

  @Override
  public Optional<VDept> selectOneByOrgId(String orgId) {
    return gwDeptMapper.selectOneByOrgId(orgId);
  }

  @Override
  public Optional<GwDept> selectGwOneByOrgId(String orgId) {
    return gwDeptMapper.selectGwOneByOrgId(orgId);
  }
  
  @Override
  public Optional<VDept> selectOneByCabinetCode(String cabinetCode) {
	  return gwDeptMapper.selectOneByCabinetCode(cabinetCode);
  }

  @Override
//  @Cacheable(value = "selectDeptByUpOrgId", key = "#upOrgId")
  public List<VDept> selectListByUpOrgId(String upOrgId, String usageState, String direction) {
    return gwDeptMapper.selectListByUpOrgId(upOrgId, usageState, direction);
  }


  @Override
  public VDept selectOneByOrgIdDefault(String orgId) {
    return gwDeptMapper.selectOneByOrgIdDefault(orgId);
  }


  @Override
  public GwDeptPathDto selectDeptPath(String orgId) {
    return gwDeptMapper.selectDeptPath(orgId);
  }


  @Override
  public String selectOrgIdByCabinetcode(String cabinetCode) {
    return gwDeptMapper.selectOrgIdByCabinetcode(cabinetCode);
  }


  @Override
  public List<VDept> selectDeptChildrenByOrgId(String orgId) {
    return gwDeptMapper.selectDeptChildrenByOrgId(orgId);
  }


  @Override
  public String selectComCodeByCabinetCode(String cabinetCode) {
    return gwDeptMapper.selectComCodeByCabinetCode(cabinetCode);
  }
  
  @Override
  public List<String> selectOrgIdRecursiveUsable(String orgId) {
  	return gwDeptMapper.selectOrgIdRecursiveUsable(orgId);
  }

//  @Override
//  public List<UserBefore> selectListByGroupId(String groupId, long offset, int limit) {
//    return userMapper.selectListByGroupId(groupId.value(), offset, limit);
//  }
  @Override
  public List<String> selectUserListOfPart(String gwOrgId){ //jjg,2021.10.19 Part사용자들 조회용
  	return gwDeptMapper.selectUserListOfPart(gwOrgId);
  }

  @Override
  public List<GwDeptListManagerDto> selectDeptMemberList(String deptId) {
    return gwDeptMapper.selectDeptMemberList(deptId);
  }

  @Override
  public List<GwSelectDeptManagerDto> selectDeptManager(String deptId, String userId) {
    // TODO Auto-generated method stub
    return gwDeptMapper.selectDeptManager(deptId, userId);
  }


  @Override
  public List<GwManagerListDto> ManagerList(String managerId) {
    // TODO Auto-generated method stub
    return gwDeptMapper.ManagerList(managerId);
  }


  @Override
  public List<GwDeptListManagerDto> selectCheckdeptManagerList(String deptId, String newUserId) {
    // TODO Auto-generated method stub
    return gwDeptMapper.selectCheckdeptManagerList(deptId, newUserId);
  }

  @Override
  public List<GwDept> selectDeptMng(String managerPerId) {
    return gwDeptMapper.selectDeptMng(managerPerId);
  }
  
  @Override
  public List<GwDept> selectDeptsInGwDept() {
    return gwDeptMapper.selectDeptsInGwDept();
  }

  @Override
  public List<GwDeptListManagerDto> selectDeptManagerList(String deptId) {
    return gwDeptMapper.selectDeptMemberList(deptId);
  }



}
