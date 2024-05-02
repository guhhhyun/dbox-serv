package com.dongkuksystems.dbox.models.dto.type.request;

import java.time.LocalDateTime;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.sharegroup.PatchShareGroupDto;
import com.dongkuksystems.dbox.models.type.request.ReqAuth;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



public class ReqApproveDto {
	

	public static IDfPersistentObject approveReqAuth(String uReqDocId, IDfSession idfSession, UserSession userSession)
			throws Exception {
		
	
		String s_ReqId = uReqDocId;

		IDfPersistentObject idf_DOC = idfSession.getObject(new DfId(s_ReqId));

		idf_DOC.setString("u_req_status", "A");
		idf_DOC.setString("u_approver", userSession.getDUserId());
		idf_DOC.setString("u_action_date",  (new DfTime()).toString());
		
		idf_DOC.save();
		
	
		return idf_DOC;

	}
}
