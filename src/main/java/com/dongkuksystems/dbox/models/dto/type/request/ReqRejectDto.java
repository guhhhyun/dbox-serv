package com.dongkuksystems.dbox.models.dto.type.request;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.models.common.UserSession;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class ReqRejectDto {
	
	

	
	
	public static IDfPersistentObject rejectReqAuth(String uReqDocId, IDfSession idfSession, String uRejectReason, UserSession userSession)
			throws Exception {
		
	
		String s_ReqId = uReqDocId;

		IDfPersistentObject idf_DOC = idfSession.getObject(new DfId(s_ReqId));

		
		idf_DOC.setString("u_reject_reason", uRejectReason);
		idf_DOC.setString("u_approver", userSession.getDUserId());
		idf_DOC.setString("u_req_status", "D");
		idf_DOC.setString("u_action_date",  (new DfTime()).toString());
		idf_DOC.save();
		
		return idf_DOC;
	}
}