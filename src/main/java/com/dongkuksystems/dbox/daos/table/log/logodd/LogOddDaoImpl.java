package com.dongkuksystems.dbox.daos.table.log.logodd;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.table.logodd.LogOddFilterDto;
import com.dongkuksystems.dbox.models.table.log.LogOdd; 

@Primary
@Repository
public class LogOddDaoImpl implements LogOddDao {
  private LogOddMapper logOddMapper;

  public LogOddDaoImpl(LogOddMapper logOddMapper) {
    this.logOddMapper = logOddMapper;
  }
	
	@Override
	public List<LogOdd> selectAll(LogOddFilterDto filter) {
		// TODO Auto-generated method stub
		return logOddMapper.selectAll(filter);
	}
  
  
}
