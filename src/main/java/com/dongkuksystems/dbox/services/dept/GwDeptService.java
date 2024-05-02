package com.dongkuksystems.dbox.services.dept;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwDeptChildrenDto;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwDeptFilterDto;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwDeptListManagerDto;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwDeptPathDto;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwDeptTreeDto;
import com.dongkuksystems.dbox.models.table.etc.GwDept;
import com.dongkuksystems.dbox.models.table.etc.VDept;

public interface GwDeptService {
  List<VDept> selectDepts(String comOrgId, String mobileYn);
  List<GwDept> selectDeptsInGwDept(String comOrgId);
  GwDept selectGwDeptByOrgId(String orgId) throws Exception;
  VDept selectDeptByOrgId(String orgId) throws Exception;
  String selectDeptCodeByCabinetcode(String cabinetcode) throws Exception;
  GwDeptChildrenDto selectDeptChildren(String orgId, boolean userYn, boolean addJobYn) throws Exception;
  GwDeptTreeDto selectDeptTree(GwDeptFilterDto dto) throws Exception;
  GwDeptPathDto selectDeptPath(String orgId) throws Exception;
  List<VDept> selectDeptChildrenByOrgId(String orgId) throws Exception;
  String selectComCodeByCabinetCode(String cabinetcode) throws Exception;
  List<String> selectUserListOfPart(String gwDeptCode) throws Exception;//jjg,2021.10.19 Part사용자들 조회용
  List<GwDeptListManagerDto> selectDeptMemberList(String deptId);
  void postDeptManager(String deptId, UserSession userSession, List<GwDeptListManagerDto> members) throws Exception;
	 
  String selectOrgIdByCabinetcode(String cabinetcode) throws Exception;
  List<GwDept> selectDeptMng(String managerPerId);
}
