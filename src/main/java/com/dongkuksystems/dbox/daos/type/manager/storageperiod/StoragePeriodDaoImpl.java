package com.dongkuksystems.dbox.daos.type.manager.storageperiod;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.manager.storageperiod.StoragePeriodLogListDto;
import com.dongkuksystems.dbox.models.type.manager.storageperiod.StoragePeriod;
import com.dongkuksystems.dbox.models.type.manager.storageperiod.StoragePeriodLogList;
@Primary
@Repository
public class StoragePeriodDaoImpl implements StoragePeriodDao {
	private StoragePeriodMapper storagePeriodMapper;
	
	public StoragePeriodDaoImpl(StoragePeriodMapper storagePeriodMapper) {
		this.storagePeriodMapper = storagePeriodMapper; 
	}
	
	  @Override
	  public List<StoragePeriod> selectStoragePeriod(String uCodeVal1){
		  return storagePeriodMapper.selectStoragePeriod(uCodeVal1);
	  }
	  
	   @Override
	    public List<StoragePeriod> selectDeleteSchedule(String uCodeVal1){
	      return storagePeriodMapper.selectDeleteSchedule(uCodeVal1);
	  }
	   
	   @Override
     public List<StoragePeriodLogList> selectRecycleLog(StoragePeriodLogListDto dto){
       return storagePeriodMapper.selectRecycleLog(dto);
   }
	   
	   @Override
     public List<StoragePeriodLogList> selectDeleteLog(StoragePeriodLogListDto dto){
       return storagePeriodMapper.selectDeleteLog(dto);
   }
}
