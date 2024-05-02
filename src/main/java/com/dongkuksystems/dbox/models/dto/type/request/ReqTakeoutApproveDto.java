package com.dongkuksystems.dbox.models.dto.type.request;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.FutureOrPresent;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.models.common.UserSession;
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


public class ReqTakeoutApproveDto {
	
	

		public static IDfPersistentObject ApproveTakeout(String rObjectId, IDfSession idfSession, UserSession userSession)
				throws Exception {		
			String s_ObjId = rObjectId;
			IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));

			idf_PObj.setString("u_req_status", "A");
			idf_PObj.setString("u_action_date", (new DfTime()).toString());
			idf_PObj.setString("u_approver", userSession.getDUserId());
			idf_PObj.save(); 
			return idf_PObj;
		}
		
	
	 
}
