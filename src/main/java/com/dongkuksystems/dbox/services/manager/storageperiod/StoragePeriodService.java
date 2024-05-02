package com.dongkuksystems.dbox.services.manager.storageperiod;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.storageperiod.PatchDeleteScheduleDto;
import com.dongkuksystems.dbox.models.dto.type.manager.storageperiod.StoragePeriodLogListDto;
import com.dongkuksystems.dbox.models.type.manager.storageperiod.StoragePeriod;
import com.dongkuksystems.dbox.models.type.manager.storageperiod.StoragePeriodLogList;

public interface StoragePeriodService {
	
	List<StoragePeriod> selectStoragePeriod(String uCodeVal1);
	
	List<StoragePeriod> selectDeleteSchedule(String uCodeVal1);	
	
	String patchStoragePeriod(String rObjectId, String uCodeVal3, UserSession userSession) throws Exception;
	
	void patchDeleteSchedule(UserSession userSession, PatchDeleteScheduleDto dto) throws Exception;

	List<StoragePeriodLogList> selectRecycleLog(StoragePeriodLogListDto dto);  
	
	List<StoragePeriodLogList> selectDeleteLog(StoragePeriodLogListDto dto);  

}
