package com.dongkuksystems.dbox.models.dto.type.request;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.models.common.UserSession;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqTakeoutCreateFreeDto {

	 @ApiModelProperty(value = "요청문서 ID")
	 private String uReqDocId;
	 @ApiModelProperty(value = "반출종류선택")
	 private String uApprType;

	 
	 public static IDfPersistentObject CreateReqTakeoutFree(IDfSession idfSession, ReqTakeoutCreateFreeDto dto, UserSession userSession) throws Exception {
		 	
			IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_req_takeout");
				
			idf_PObj.setString("u_req_user", userSession.getDUserId());
			idf_PObj.setString("u_req_date", (new DfTime()).toString());
			idf_PObj.setString("u_action_date", (new DfTime()).toString());
			idf_PObj.setString("u_appr_type", dto.getUApprType());
			
			
			idf_PObj.save();
			

			return idf_PObj;

		}
	
}
