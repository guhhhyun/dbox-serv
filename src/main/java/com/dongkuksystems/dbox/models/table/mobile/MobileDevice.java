package com.dongkuksystems.dbox.models.table.mobile;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MobileDevice {
	private String userId;
	private String deviceUuid;
	private String deviceType;
	private String pushKey;
	private String modelNm;
	private LocalDateTime createDate;
	private LocalDateTime updateDate;
}
