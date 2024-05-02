package com.dongkuksystems.dbox.daos.table.etc.gwdept;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.table.gwdept.GwDeptListManagerDto;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwDeptPathDto;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwManagerListDto;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwSelectDeptManagerDto;
import com.dongkuksystems.dbox.models.table.etc.GwDept;
import com.dongkuksystems.dbox.models.table.etc.VDept; 

public interface GwDeptMapper {
  public List<VDept> selectAll(); 
  public Optional<VDept> selectOneByOrgId(@Param("orgId") String orgId);
  public Optional<GwDept> selectGwOneByOrgId(@Param("orgId") String orgId);
  public Optional<VDept> selectOneByCabinetCode(@Param("cabinetCode") String cabinetCode);
  public VDept selectOneByOrgIdDefault(@Param("orgId") String orgId); 
  public List<VDept> selectListByUpOrgId(@Param("upOrgId") String upOrgId, @Param("usageState") String usageState, @Param("direction") String direction); 
  public GwDeptPathDto selectDeptPath(@Param("orgId") String orgId); 
  public String selectOrgIdByCabinetcode(@Param("cabinetCode") String cabinetCode); 
  public List<VDept> selectDeptChildrenByOrgId(@Param("orgId") String orgId); 
  public String selectComCodeByCabinetCode(@Param("cabinetCode") String cabinetCode);
  public List<String> selectOrgIdRecursiveUsable(@Param("orgId") String orgId);
  public List<String> selectUserListOfPart(@Param("gwOrgId") String gwOrgId);//jjg,2021.10.19 Part사용자들 조회용

  public List<GwSelectDeptManagerDto> selectDeptManager(@Param("deptId")String deptId, @Param("userId")String userId);
  public List<GwManagerListDto> ManagerList(@Param("managerId")String managerId);
  public List<GwDeptListManagerDto> selectCheckdeptManagerList(@Param("deptId")String deptId, @Param("newUserId")String newUserId);
  public List<GwDept> selectDeptMng(String managerPerId);
  public List<GwDept> selectDeptsInGwDept();

  List<GwDeptListManagerDto> selectDeptMemberList(@Param("deptId") String deptId);
  List<GwDeptListManagerDto> selectDeptManagerList(@Param("deptId") String deptId);
  public String selectDsearchDeptPath(@Param("dataCabinetCode") String dataCabinetCode);
  
}
