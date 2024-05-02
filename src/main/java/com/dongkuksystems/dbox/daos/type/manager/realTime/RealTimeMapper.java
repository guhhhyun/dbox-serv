package com.dongkuksystems.dbox.daos.type.manager.realTime;

import java.util.List;

import com.dongkuksystems.dbox.models.type.manager.realTime.RealTime;


public interface RealTimeMapper {
	
	public List<RealTime> selectRealTime(String uComCode);

}
