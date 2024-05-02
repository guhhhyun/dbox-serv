package com.dongkuksystems.dbox.models.dto.type.upload;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckinDocDto {
 
	@ApiModelProperty(value = "문서 R_OBJECT_ID")
	private String sObjectId;
	
	@ApiModelProperty(value = "문서 U_DOC_KEY")
	private String sDocKey;
	
	@ApiModelProperty(value = "Agent File Upload Path")
	private String sFilePath;
	
	@ApiModelProperty(value = "Agent File Name")
	private String sFileName;
	
	// user_id, r_object_id, file, file_path, file_name
	// return_code, return_msg
	
	
	/**
	 * 편집완료
	 * 
	 * @param idfSession : 세션
	 * @param dto        : 편집완료 정보
	 * @return
	 * @throws Exception
	 */
	public static IDfSysObject checkinIDfDocument(IDfSession idfSession, CheckinDocDto dto) throws Exception {

	    IDfSysObject idfDoc = (IDfSysObject) idfSession.getObject(new DfId(dto.getSObjectId()));
	    
	    // 파일 set
	    idfDoc.setFile(dto.getSFilePath());
	    
	    return idfDoc;
	}
	
}
