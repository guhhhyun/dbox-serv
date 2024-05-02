package com.dongkuksystems.dbox.models.dto.type.manager.sharegroup;

import java.util.List;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatchDeptDto {
	
	@ApiModelProperty(value = "list")
	List<String> uDeptCode;
}
