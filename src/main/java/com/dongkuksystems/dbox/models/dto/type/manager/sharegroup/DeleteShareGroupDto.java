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
public class DeleteShareGroupDto {
	
	@ApiModelProperty(value = "ObjectId")
	private String rObjectId;

	public static void DeleteShareGroup(String rObjectId, IDfSession idfSession, DeleteShareGroupDto dto)
			throws Exception {

		String s_ObjId = rObjectId;

		IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));

		idf_PObj.destroy();
	}

}
