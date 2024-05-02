package com.dongkuksystems.dbox.daos.type.manager.storageperiod;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.manager.storageperiod.StoragePeriodLogListDto;
import com.dongkuksystems.dbox.models.type.manager.storageperiod.StoragePeriod;
import com.dongkuksystems.dbox.models.type.manager.storageperiod.StoragePeriodLogList;

public interface StoragePeriodDao {
	public List<StoragePeriod> selectStoragePeriod(String uCodeVal1);
  public List<StoragePeriod> selectDeleteSchedule(String uCodeVal1);
  public List<StoragePeriodLogList> selectRecycleLog(StoragePeriodLogListDto dto);  
  public List<StoragePeriodLogList> selectDeleteLog(StoragePeriodLogListDto dto);  
}
