package com.dongkuksystems.dbox.daos.type.manager.hisviewuser;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.manager.hisviewuser.HisViewUserFilterDto;
import com.dongkuksystems.dbox.models.type.manager.hisviewuser.HisViewUser; 

public interface HisViewUserMapper { 
  public List<HisViewUser> selectAll(@Param("hisViewUser") HisViewUserFilterDto hisViewUserFilterDto);
}
