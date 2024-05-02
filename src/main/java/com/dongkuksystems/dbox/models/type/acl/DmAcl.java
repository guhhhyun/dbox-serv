package com.dongkuksystems.dbox.models.type.acl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class DmAcl {
	private String rObjectId;
	private String objectName;
	private String ownerName;
}
