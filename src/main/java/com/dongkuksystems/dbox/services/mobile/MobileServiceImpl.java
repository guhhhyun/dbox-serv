package com.dongkuksystems.dbox.services.mobile;

import org.springframework.stereotype.Service;

import com.dongkuksystems.dbox.daos.table.mobile.device.MobileDeviceDao;
import com.dongkuksystems.dbox.models.dto.mobile.MobileDeviceLoginDto;
import com.dongkuksystems.dbox.services.AbstractCommonService;

@Service
public class MobileServiceImpl extends AbstractCommonService implements MobileService {
	private final MobileDeviceDao mobileDeviceDao;

	public MobileServiceImpl(MobileDeviceDao mobileDeviceDao) {
		this.mobileDeviceDao = mobileDeviceDao;
	}

	@Override
	public void updateMobileDevice(String userId, MobileDeviceLoginDto mobileDevice) throws Exception {
		mobileDeviceDao.mergeOne(userId, mobileDevice);
	}
}
