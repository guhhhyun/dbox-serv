package com.dongkuksystems.dbox.models.dto.type.request;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfTime;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class ReqCreateDto {
	 @ApiModelProperty(value = "요청문서 ID", required = true)
	 private String uReqDocId;
	 @ApiModelProperty(value = "요청사유", required = true)
	 private String uReqReason;
	 @ApiModelProperty(value = "요청권한", required = true)
	 private int uReqPermit;
	
	 
	 public static IDfPersistentObject CreateReqAuth(IDfSession idfSession, ReqCreateDto dto) throws Exception {

			IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_req_auth");
			
			idf_PObj.setString("u_req_doc_id", dto.getUReqDocId());
			idf_PObj.setString("u_req_reason", dto.getUReqReason());
			idf_PObj.setString("u_req_user", idfSession.getLoginUserName());
			idf_PObj.setString("u_req_date", (new DfTime()).toString());
			idf_PObj.setString("u_req_status", "R");
			idf_PObj.setInt("u_req_permit", dto.uReqPermit);
			
			idf_PObj.save();

			return idf_PObj;

		}
}