package com.dongkuksystems.dbox.daos.table.log.logodd;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.table.logodd.LogOddFilterDto;
import com.dongkuksystems.dbox.models.table.log.LogOdd;


public interface LogOddDao {
  public List<LogOdd> selectAll(LogOddFilterDto filter);
  
}
