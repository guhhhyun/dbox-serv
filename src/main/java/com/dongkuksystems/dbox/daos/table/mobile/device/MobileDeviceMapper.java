package com.dongkuksystems.dbox.daos.table.mobile.device;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.mobile.MobileDeviceLoginDto;
import com.dongkuksystems.dbox.models.table.mobile.MobileDevice; 

public interface MobileDeviceMapper {
  public List<MobileDevice> selectList(@Param("userIds") List<String> userIds);
  public int updateOne(@Param("userId") String userId, @Param("mobileDevice") MobileDeviceLoginDto mobileDevice); 
  public int mergeOne(@Param("userId") String userId, @Param("mobileDevice") MobileDeviceLoginDto mobileDevice); 
}
