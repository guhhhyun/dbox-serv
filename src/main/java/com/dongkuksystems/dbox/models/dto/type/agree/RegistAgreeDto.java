package com.dongkuksystems.dbox.models.dto.type.agree;

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
public class RegistAgreeDto {

    @ApiModelProperty(value = "Object ID", required = false)
    private String rObjectId             ;
    @ApiModelProperty(value = "서약/동의자", required = true)
    private String uUserId             ;
    @ApiModelProperty(value = "동의 구분", required = true)
    private String uAgreeType          ;
    @ApiModelProperty(value = "회사코드", required = true)
    private String uComCode            ;
    @ApiModelProperty(value = "부서코드", required = true)
    private String uDeptCode           ;
    @ApiModelProperty(value = "서약/동의명", required = true)
    private String uAgreeName          ;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss" )
    @ApiModelProperty(value = "등록일")
    private LocalDateTime uRegDate;

    @ApiModelProperty(value = "동의여부", required = true)
    private String uAgreeYn             ;
    
    @ApiModelProperty(value = "제개정사유", required = true)
    private String uReason             ;
    
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss" )
    @ApiModelProperty(value = "서명일")
    private LocalDateTime uSignDate;
  

    public static IDfPersistentObject register(IDfSession idfSession, RegistAgreeDto dto)
			throws Exception {

		IDfPersistentObject idf_PObj = null;
		
		String s_ObjId = dto.getRObjectId();
		String status = dto.getUAgreeType();
		if( null != s_ObjId && !s_ObjId.equals("") ) {
		    if(dto.getUAgreeType().equals("U")) {
		      idf_PObj = idfSession.getObject(new DfId(s_ObjId));
		      idf_PObj.setString("u_sign_date",   (new DfTime()).toString());
		    }else {
		      idf_PObj = idfSession.getObject(new DfId(s_ObjId));
		    }
		} else {
		if(idf_PObj == null) 
		idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_agree");
		idf_PObj.setString("u_user_id",     dto.getUUserId()    );
		idf_PObj.setString("u_agree_type",  dto.getUAgreeType() );
		idf_PObj.setString("u_com_code",    dto.getUComCode()   );
		idf_PObj.setString("u_dept_code",   dto.getUDeptCode()  );
		idf_PObj.setString("u_agree_name",  dto.getUAgreeName() );
		idf_PObj.setString("u_reason",      dto.getUReason()    );
		idf_PObj.setString("u_agree_yn",    dto.getUAgreeYn()   );
		if( null == s_ObjId  || s_ObjId.equals("") ) //신규생성일 때만 등록일이 변경됨
		    idf_PObj.setString("u_reg_date",    (new DfTime()).toString());
		idf_PObj.setString("u_sign_date",   (new DfTime()).toString());
		}
		
		return idf_PObj;

	}
    
 
}
