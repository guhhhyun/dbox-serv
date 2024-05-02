package com.dongkuksystems.dbox.models.dto.type.request;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ReqTakeoutRejectDto {
	


		public static IDfPersistentObject RejectTakeout(String rObjectId, IDfSession idfSession, String rejectReason)
				throws Exception {		
			String s_ObjId = rObjectId;
			IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));

			idf_PObj.setString("u_req_status", "D");
			idf_PObj.setString("u_reject_reason", rejectReason);
			idf_PObj.setString("u_action_date", (new DfTime()).toString());
			idf_PObj.setString("u_approver", idfSession.getLoginUserName());
			idf_PObj.save(); 
			return idf_PObj;
		}
		
	
	 
}