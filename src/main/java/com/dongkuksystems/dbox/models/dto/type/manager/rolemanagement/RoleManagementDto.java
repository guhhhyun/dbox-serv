package com.dongkuksystems.dbox.models.dto.type.manager.rolemanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleManagementDto {
	private String groupName;
	private String userId;
	private String uSelected;
	private String uAuthGroup;
	private String uConfigFlag;
	private String uAuthScope;
	private String uGroupScope;
	private String rObjectId;
	private String uOptionVal;
	private String uDocFlag;
	
	private String unUOptionVal;
	
}

