package com.dongkuksystems.dbox.models.dto.mobile;

import com.dongkuksystems.dbox.models.dto.AbstractCommonDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MobileDeviceLoginDto extends AbstractCommonDto {
	private String userId;
	private String deviceUuid;
	private String deviceType;
	private String pushKey;
	private String modelNm;
	
	public void setUserId(String userId) {
		addAssignParameters("userId");
		this.userId = userId;
	}
	
	public void setDeviceUuid(String deviceUuid) {
		addAssignParameters("deviceUuid");
		this.deviceUuid = deviceUuid;
	}
	
	public void setDeviceType(String deviceType) {
		addAssignParameters("deviceType");
		this.deviceType = deviceType;
	}
	
	public void setPushKey(String pushKey) {
		addAssignParameters("pushKey");
		this.pushKey = pushKey;
	}
	
	public void setModelNm(String modelNm) {
		addAssignParameters("modelNm");
		this.modelNm = modelNm;
	}
}
