package com.dongkuksystems.dbox.daos.type.manager.preservationperiod;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.manager.preservationperiod.PreservationPeriodDto;

@Primary
@Repository
public class PreservationPeriodDaoImpl implements PreservationPeriodDao {
  private final PreservationPeriodMapper preservationPeriodMapper;
  
  public PreservationPeriodDaoImpl(PreservationPeriodMapper preservationPeriodMapper) {
    this.preservationPeriodMapper = preservationPeriodMapper;
  }

  @Override
  public PreservationPeriodDto selectOneByComCode(String comCode) {
  
    return preservationPeriodMapper.selectOneByComCode(comCode);
  }
  
}
