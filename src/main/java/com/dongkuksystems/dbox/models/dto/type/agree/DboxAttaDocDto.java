package com.dongkuksystems.dbox.models.dto.type.agree;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.AclTemplate;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.constants.SecLevelCode;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedFile;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedKUploadFile;
import com.dongkuksystems.dbox.models.dto.type.upload.UploadDocDto;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;
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
public class DboxAttaDocDto {

	  @ApiModelProperty(value = "부서 Cabinet코드")
	  private String cabinetCode;
	
	  @ApiModelProperty(value = "Dbox 파일 ID")
	  private String dboxId;
	  @ApiModelProperty(value = "요청 시스템의 Contents key id")
	  private String keyId;
      @ApiModelProperty(value = "등록자")
	  private String bandWriter;
	  @ApiModelProperty(value = "요청시스템 Contents")
	  private String bandSubject;
	  @ApiModelProperty(value = "삭제여부(Y/N)")
	  private String fileDeleteYn;

	  @ApiModelProperty(value = "권한자 리스트 문자열")
	  private String bandRefLine ;
	  
	  @ApiModelProperty(value = "권한자 리스트")
	  private Map<String, String[]> bandRefLineM = new HashMap<String, String[]>();
	  
	  @ApiModelProperty(value = "요청 시스템의 Contents Link)")
	  private String bandLink;
	  
	  @ApiModelProperty(value = "현재 작업자의 작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)")
	  private String uJobUserType;
	  
	  private String uRequestIp;
	  
	  @JsonSerialize(using = LocalDateTimeSerializer.class)
	  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	  @ApiModelProperty(value = "생성 일시")
	  private LocalDateTime uCreateDate;
	  @ApiModelProperty(value = "수정자")
	  private String uModifyUser;
	  @JsonSerialize(using = LocalDateTimeSerializer.class)
	  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	  @ApiModelProperty(value = "수정 일시")
	  private LocalDateTime uModifyDate; 
	  
}
