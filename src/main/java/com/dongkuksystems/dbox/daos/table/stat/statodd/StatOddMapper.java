package com.dongkuksystems.dbox.daos.table.stat.statodd;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.table.statodd.StatOddFilterDto;
import com.dongkuksystems.dbox.models.table.stat.StatOdd; 

public interface StatOddMapper { 
  public List<StatOdd> selectAll(@Param("statOdd") StatOddFilterDto dto);
}
