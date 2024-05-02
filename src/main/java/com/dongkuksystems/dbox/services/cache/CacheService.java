package com.dongkuksystems.dbox.services.cache;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.custom.dmformat.DmFormat;

public interface CacheService {
  List<DmFormat> selectDmFormats();

  void clearCaches();

  void initSelectDmFormats();

  void initSelectDepts();
  
  void initSelectDeptChildren();

  void initSelectDeptTree();
  
  void initSelectMangeIdTree();

  void initSelectDeptPath();

  void initSelectDeptByOrgId();
  
  void initSelectGwDeptByOrgId();
  
  void initSelectDeptCodeByCabinetcode();

  void initSelectDeptChildrenByOrgId();
  
  void initSelectComCodeByCabinetCode();
  
  void initSelectOrgIdByCabinetcode();
  
  void initSelectUserListOfPart();
  
  void initSelectTemplates();
}
