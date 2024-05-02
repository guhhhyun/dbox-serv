
 package com.dongkuksystems.dbox.daos.type.manager.managerconfig;
 
import java.util.List;
//import java.util.Optional;

//import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.type.manager.Mgr;

 public interface ManagerConfigMapper {
  public List<Mgr> selectManagerConfig(@Param("uUserId") String uUserId);
  public List<Mgr> selectDeptManagerConfig(@Param("uUserId") String uUserId);
  public List<Mgr> selectMgrList(@Param("uMgrType") String uMgrType, @Param("uComCode") String uComCode);
  
 }
