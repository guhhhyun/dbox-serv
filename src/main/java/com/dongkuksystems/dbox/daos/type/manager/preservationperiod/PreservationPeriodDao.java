package com.dongkuksystems.dbox.daos.type.manager.preservationperiod;

import com.dongkuksystems.dbox.models.dto.type.manager.preservationperiod.PreservationPeriodDto;

public interface PreservationPeriodDao {
  public PreservationPeriodDto selectOneByComCode(String comCode);
}
