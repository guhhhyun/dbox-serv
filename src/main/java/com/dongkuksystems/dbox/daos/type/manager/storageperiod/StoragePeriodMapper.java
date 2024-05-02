package com.dongkuksystems.dbox.daos.type.manager.storageperiod;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.manager.storageperiod.StoragePeriodLogListDto;
import com.dongkuksystems.dbox.models.type.manager.storageperiod.StoragePeriod;
import com.dongkuksystems.dbox.models.type.manager.storageperiod.StoragePeriodLogList;

public interface StoragePeriodMapper {
  public List<StoragePeriod> selectStoragePeriod(@Param("uCodeVal1") String uCodeVal1);
  public List<StoragePeriod> selectDeleteSchedule(@Param("uCodeVal1") String uCodeVal1);
  public List<StoragePeriodLogList> selectRecycleLog(@Param("storagePeriodLogList") StoragePeriodLogListDto dto);
  public List<StoragePeriodLogList> selectDeleteLog(@Param("storagePeriodLogList") StoragePeriodLogListDto dto);
}
