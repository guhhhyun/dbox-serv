package com.dongkuksystems.dbox.daos.table.mobile.device;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.mobile.MobileDeviceLoginDto;
import com.dongkuksystems.dbox.models.table.mobile.MobileDevice;

public interface MobileDeviceDao {
  public List<MobileDevice> selectList(List<String> userIds);
  public int updateOne(String userId, MobileDeviceLoginDto mobileDevice); 
  public int mergeOne(String userId, MobileDeviceLoginDto mobileDevice); 
}
