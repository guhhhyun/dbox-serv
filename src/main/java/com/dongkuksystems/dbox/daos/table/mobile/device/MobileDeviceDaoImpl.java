package com.dongkuksystems.dbox.daos.table.mobile.device;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.mobile.MobileDeviceLoginDto;
import com.dongkuksystems.dbox.models.table.mobile.MobileDevice; 

@Primary
@Repository
public class MobileDeviceDaoImpl implements MobileDeviceDao {
  private MobileDeviceMapper mobileDeviceMapper;

  public MobileDeviceDaoImpl(MobileDeviceMapper mobileDeviceMapper) {
    this.mobileDeviceMapper = mobileDeviceMapper;
  }

  @Override
  public List<MobileDevice> selectList(List<String> userIds) {
  	List<MobileDevice> result = mobileDeviceMapper.selectList(userIds);
  	return result;
  }
  
  @Override
  public int updateOne(String userId, MobileDeviceLoginDto mobileDevice) {
  	int result = mobileDeviceMapper.updateOne(userId, mobileDevice);
  	return result;
  }
  
  @Override
  public int mergeOne(String userId, MobileDeviceLoginDto mobileDevice) {
  	int result = mobileDeviceMapper.mergeOne(userId, mobileDevice);
  	return result;
  }
}
