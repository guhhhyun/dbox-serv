package com.dongkuksystems.dbox.models.dto.type.manager.authrequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class AuthRequestUserDto {
	  private String uOwnDeptCode;	  
	  private String displayName;
	  private String objectName;
	  private String overStartDate;
	  private String overEndDate;
}

