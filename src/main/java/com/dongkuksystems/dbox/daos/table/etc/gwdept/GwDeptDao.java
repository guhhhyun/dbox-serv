package com.dongkuksystems.dbox.daos.table.etc.gwdept;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.dto.table.gwdept.GwDeptListManagerDto;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwDeptPathDto;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwManagerListDto;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwSelectDeptManagerDto;
import com.dongkuksystems.dbox.models.table.etc.GwDept;
import com.dongkuksystems.dbox.models.table.etc.VDept;

public interface GwDeptDao {
  public List<VDept> selectAll(); 
  public Optional<VDept> selectOneByOrgId(String orgId);
  public Optional<GwDept> selectGwOneByOrgId(String orgId);
  public Optional<VDept> selectOneByCabinetCode(String cabinetCode); 
  public String selectComCodeByCabinetCode(String cabinetCode); 
  public String selectOrgIdByCabinetcode(String cabinetCode); 
  public List<VDept> selectListByUpOrgId(String upOrgId, String usageState, String direction); 
  public VDept selectOneByOrgIdDefault(String orgId); 
  public GwDeptPathDto selectDeptPath(String orgId); 
  public List<VDept> selectDeptChildrenByOrgId(String orgId); 
  public List<String> selectOrgIdRecursiveUsable(String orgId);
//  public List<User> selectListByOrgId(String groupId, long offset, int limit); 
  
  public List<String> selectUserListOfPart(String gwOrgId); //jjg,2021.10.19 Part사용자들 조회용

  public List<GwSelectDeptManagerDto> selectDeptManager(String deptId, String userId);
  public List<GwManagerListDto> ManagerList(String managerId);
  public List<GwDeptListManagerDto> selectCheckdeptManagerList(String deptId, String newUserId);
  public List<GwDept> selectDeptMng(String managerPerId);
  public List<GwDept> selectDeptsInGwDept();
  
  List<GwDeptListManagerDto> selectDeptMemberList(String deptId);
  List<GwDeptListManagerDto> selectDeptManagerList(String deptId);

}
