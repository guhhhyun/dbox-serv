package com.dongkuksystems.dbox.models.dto.type.manager.roleauth;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleAuthDto {
	private String groupName;
	private String userId;
	private String uComCode;
	private String uGroupScope;
	private String uConfigFlag;
	private String rObjectId;	
}

