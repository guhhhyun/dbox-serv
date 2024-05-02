package com.dongkuksystems.dbox.services.manager.statodd;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.table.logodd.LogOddFilterDto;
import com.dongkuksystems.dbox.models.dto.table.statodd.StatOddFilterDto;
import com.dongkuksystems.dbox.models.table.log.LogOdd;
import com.dongkuksystems.dbox.models.table.stat.StatOdd;

public interface StatOddService {
	List<StatOdd> selectStatOdd(StatOddFilterDto dto);
	List<LogOdd> selectLogOdd(LogOddFilterDto dto);

}
