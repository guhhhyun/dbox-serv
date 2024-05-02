package com.dongkuksystems.dbox.daos.table.log.logodd;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.table.logodd.LogOddFilterDto;
import com.dongkuksystems.dbox.models.table.log.LogOdd; 

public interface LogOddMapper { 
  public List<LogOdd> selectAll(@Param("logOdd") LogOddFilterDto dto);
}
