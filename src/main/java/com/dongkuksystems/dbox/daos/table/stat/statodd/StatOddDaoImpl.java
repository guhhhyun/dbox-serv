package com.dongkuksystems.dbox.daos.table.stat.statodd;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.table.statodd.StatOddFilterDto;
import com.dongkuksystems.dbox.models.table.stat.StatOdd; 

@Primary
@Repository
public class StatOddDaoImpl implements StatOddDao {
  private StatOddMapper statOddMapper;

  public StatOddDaoImpl(StatOddMapper statOddMapper) {
    this.statOddMapper = statOddMapper;
  }

  @Override
  public List<StatOdd> selectAll(StatOddFilterDto filter) {
    return statOddMapper.selectAll(filter);
  }
  
  
}
