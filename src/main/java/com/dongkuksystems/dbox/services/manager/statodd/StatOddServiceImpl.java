package com.dongkuksystems.dbox.services.manager.statodd;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dongkuksystems.dbox.daos.table.log.logodd.LogOddDao;
import com.dongkuksystems.dbox.daos.table.stat.statodd.StatOddDao;
import com.dongkuksystems.dbox.models.dto.table.logodd.LogOddFilterDto;
import com.dongkuksystems.dbox.models.dto.table.statodd.StatOddFilterDto;
import com.dongkuksystems.dbox.models.table.log.LogOdd;
import com.dongkuksystems.dbox.models.table.stat.StatOdd;
import com.dongkuksystems.dbox.services.AbstractCommonService;

@Service
public class StatOddServiceImpl extends AbstractCommonService implements StatOddService {

	private final StatOddDao statOddDao;
	private final LogOddDao logOddDao;

	public StatOddServiceImpl(StatOddDao statOddDao, LogOddDao logOddDao) {
		this.statOddDao = statOddDao;
		this.logOddDao = logOddDao;
	}

	@Override
	public List<StatOdd> selectStatOdd(StatOddFilterDto dto) {
		// TODO Auto-generated method stub
		return statOddDao.selectAll(dto);
	}

	@Override
	public List<LogOdd> selectLogOdd(LogOddFilterDto dto) {
		// TODO Auto-generated method stub
		return logOddDao.selectAll(dto);
	}

	
}
