package com.dongkuksystems.dbox.models.type.acl;

import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class DmAclRepeating {
	private String rObjectId;
	private String rAccessorName;
	private String rAccessorPermit;

	private VUser user;
	private VDept dept;
}
