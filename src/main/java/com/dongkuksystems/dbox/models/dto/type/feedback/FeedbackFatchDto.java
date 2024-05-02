package com.dongkuksystems.dbox.models.dto.type.feedback;


import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.errors.ForbiddenException;
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
public class FeedbackFatchDto {
	 @ApiModelProperty(value = "피드백 내용")
	 private String uFeedback;
	 @ApiModelProperty(value = "비공개 여부")
	 private String uOpenFlag;
	 
	 public static IDfPersistentObject PatchFeedback(UserSession userSession, IDfSession idfSession, FeedbackFatchDto dto, String feedbackId) throws Exception {

			IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.getObject(new DfId(feedbackId));
			
			idf_PObj.setString("u_feedback", dto.getUFeedback());
			idf_PObj.setString("u_open_flag", dto.getUOpenFlag());
			idf_PObj.save();
			
			if(!(userSession.getDUserId().equals(idf_PObj.getString("u_create_user")))) {
				throw new ForbiddenException("권한이 없습니다.");
			}
			if(idf_PObj.getString("u_feedback").length() > 1000) {
				throw new BadRequestException("내용은 1000자 이상 불가능합니다.");
			}
			
			return idf_PObj;
			
		}
}
