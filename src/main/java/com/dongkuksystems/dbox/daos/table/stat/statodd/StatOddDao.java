package com.dongkuksystems.dbox.daos.table.stat.statodd;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.table.statodd.StatOddFilterDto;
import com.dongkuksystems.dbox.models.table.stat.StatOdd;


public interface StatOddDao {
  public List<StatOdd> selectAll(StatOddFilterDto filter); 
  
}
