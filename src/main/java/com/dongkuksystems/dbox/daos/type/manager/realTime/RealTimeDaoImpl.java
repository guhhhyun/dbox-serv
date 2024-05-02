package com.dongkuksystems.dbox.daos.type.manager.realTime;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.manager.realTime.RealTime;


@Primary
@Repository
public class RealTimeDaoImpl implements RealTimeDao {
	private RealTimeMapper realTimeMapper;

	public RealTimeDaoImpl(RealTimeMapper realTimeMapper) {					
	    this.realTimeMapper = realTimeMapper;
	  }

	@Override
	public List<RealTime> selectRealTime(String uComCode) {   
		return realTimeMapper.selectRealTime(uComCode);
	}


}


