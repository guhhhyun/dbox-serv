package com.dongkuksystems.dbox.models.dto.type.manager.manageid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class ManageIdCreateDto {
	  private String socialPerId;
	  private String email;
	  private String uCabinetCode;
	  private String orgId;
	  private String userState;
	

}
