package com.dongkuksystems.dbox.models.dto.type.feedback;


import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.errors.BadRequestException;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class FeedbackCreateDto {
	
	  @ApiModelProperty(value = "피드백 내용")
	  private String uFeedback;
	  @ApiModelProperty(value = "비공개 여부 | O:공개, S:비공개")
	  private String uOpenFlag;
	 
	  
	  public static IDfPersistentObject CreateFeedback(IDfSession idfSession, FeedbackCreateDto dto) throws Exception {

			IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_feedback");
			
			idf_PObj.setString("u_open_flag", dto.getUOpenFlag());
			idf_PObj.setString("u_feedback", dto.getUFeedback());
			idf_PObj.setString("u_create_user", idfSession.getLoginUserName());
			idf_PObj.setString("u_create_date", (new DfTime()).toString());
	
			idf_PObj.save();
			
			if(idf_PObj.getString("u_feedback").length() > 1000) {
				throw new BadRequestException("내용은 1000자 이상 불가능합니다.");
			}

			return idf_PObj;

		}
	
}
