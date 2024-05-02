package com.dongkuksystems.dbox.models.dto.type.manager.duplication;

import java.time.LocalDateTime;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatchDuplicationDto {
	
//	@ApiModelProperty(value = "공유그룹명")
//	private String uShareName;
//	
//	@ApiModelProperty(value = "공유설명")
//	private String uShareDesc;
//	
//	@ApiModelProperty(value = "수정자")
//	private String uUpdateUser;
//	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//	@JsonSerialize(using = LocalDateTimeSerializer.class)
//	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
//	@ApiModelProperty(value = "수정 일시")
//	private LocalDateTime uUpdateDate;
	
	@ApiModelProperty(value = "삭제상태")
	private String uDeleteStatus;
	@ApiModelProperty(value = "Closed 처리일시")
	private String uClosedDate;
	@ApiModelProperty(value = "Closed 처리자")
	private String uCloser;
	@ApiModelProperty(value = "휴지통으로 삭제일")
	private String uRecycleDate;
	@ApiModelProperty(value = "마지막 파일 편집자")
	private String uLastEditor;

	public static IDfPersistentObject PatchDuplication(String rObjectId, IDfSession idfSession, PatchDuplicationDto dto)
			throws Exception {		
		System.out.println("PatchDuplicationDto-PatchDuplication : " + dto);		
		
		String s_ObjId = rObjectId;
		IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));
		
		System.out.println("PatchDuplicationDto-dto.getUSystemName() : " + dto.getUDeleteStatus());
		idf_PObj.setString("u_delete_status", dto.getUDeleteStatus());
		
		String uDeleteStatus = dto.getUDeleteStatus();
		
		//R : 폐기요청 , D : 휴지통
		if("R".equals(uDeleteStatus)) {
			idf_PObj.setString("u_closed_date", (new DfTime()).toString());
			idf_PObj.setString("u_closer", idfSession.getLoginUserName());		
		}
		
		if("D".equals(uDeleteStatus)) {
			idf_PObj.setString("u_recycle_date", (new DfTime()).toString());
		}
		
		//todo check - 추가해야 할듯.
		//u_closed_date               TIME (SET LABEL_TEXT='Closed 처리일시'),
		//u_closer                    CHAR(100) (SET LABEL_TEXT='Closed 처리자'),
		
		//u_recycle_date              TIME (SET LABEL_TEXT='휴지통으로 삭제일'),
	    //u_last_editor               CHAR(100) (SET LABEL_TEXT='마지막 파일 편집자')
	    
	    idf_PObj.save();    

		return idf_PObj;

	}	

}
