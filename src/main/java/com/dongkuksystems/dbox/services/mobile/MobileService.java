package com.dongkuksystems.dbox.services.mobile;

import com.dongkuksystems.dbox.models.dto.mobile.MobileDeviceLoginDto;

public interface MobileService {
	public void updateMobileDevice(String userId, MobileDeviceLoginDto mobileDevice) throws Exception;
}
