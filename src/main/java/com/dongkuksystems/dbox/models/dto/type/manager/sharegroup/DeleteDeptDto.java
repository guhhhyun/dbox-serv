package com.dongkuksystems.dbox.models.dto.type.manager.sharegroup;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteDeptDto {
	
	@ApiModelProperty(value = "ObjectId")
	private String rObjectId;
	
	@ApiModelProperty(value = "부서코드")
	private String uDeptCode;

	public static void DeleteDept(String rObjectId, IDfSession idfSession, DeleteDeptDto dto)
			throws Exception {

		IDfPersistentObject idfPObj = idfSession.getObject(new DfId(rObjectId));

		int i_ValIdx = idfPObj.findString("u_dept_code", dto.getUDeptCode());
		idfPObj.remove("u_dept_code", i_ValIdx);
		idfPObj.save();		
				
	}

}
