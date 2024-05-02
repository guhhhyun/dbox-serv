package com.dongkuksystems.dbox.models.type.history;

import java.time.LocalDateTime;

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
public class HistoryAttach {
	
	  @ApiModelProperty(value = "작업 Code", required = true)
	  private String uJobCode;
	  @ApiModelProperty(value = "문서 ID", required = true)
	  private String uDocId;
	  @ApiModelProperty(value = "문서 키", required = true)
	  private String uDocKey;
	  @ApiModelProperty(value = "문서명", required = true)
	  private String uDocName;
	  @ApiModelProperty(value = "버전", required = true)
	  private String uDocVersion;
	  @ApiModelProperty(value = "소유부서", required = true)
	  private String uOwnDeptcode;
	  @ApiModelProperty(value = "실행부서", required = true)
	  private String uActDeptCode;
	  @ApiModelProperty(value = "작업자", required = true)
	  private String uJobUser;
	  @ApiModelProperty(value = "작업자구분, P:개인, D:부서관리자, E:사별관리자, G:그룹관리자, S:시스템", required = true)
	  private String uJobUserType; 
	  // @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	  @JsonFormat(pattern = "yyyy-MM-dd")
	  @JsonSerialize(using = LocalDateTimeSerializer.class)
	  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	  @ApiModelProperty(value = "작업 일시", required = true)
	  private LocalDateTime uJobDate;
	  @ApiModelProperty(value = "문서 상태")
	  private String uDocStatus;
	  @ApiModelProperty(value = "문서 보안등급")
	  private String uSecLevel;
	  @ApiModelProperty(value = "문서함코드", required = true)
	  private String uCabinetCode; 
	  @ApiModelProperty(value = "작업구분, 등록=[D:Dbox, P:PC], 수정=[W:덮어쓰기, V:버전생성], 조회=[D:Dbox, L:링크], 첨부=[O:원문, H:HTML], 보안등급 변경=[U:상향, D:하향], 복호화 반출=[S:")
	  private String uJobGubun; 
	  @ApiModelProperty(value = "이전정보")
	  private String uBeforeChangeVal;
	  @ApiModelProperty(value = "이후정보")
	  private String uAfterChangeVal;
	  @ApiModelProperty(value = "IP")
	  private String uUserIp;
	  @ApiModelProperty(value = "첨부 시스템")
	  private String uAttachSystem;
	  
	  @ApiModelProperty(value = "시스템명")
	  private String uSystemName;
	  
	  @ApiModelProperty(value = "작성자")
	  private String displayName;
	  
	  @ApiModelProperty(value = "rContentSize")
	  private String rContentSize;
	  @ApiModelProperty(value = "rFullContentSize")
	  private String rFullContentSize;
	  @ApiModelProperty(value = "파일 사이즈")
	  private String contentsize;

	  @ApiModelProperty(value = "사용자 회사명")
	  private String companyName;
	  @ApiModelProperty(value = "사용자 부서명")
	  private String deptName;
	  
	  
	  // 변경정보
	  @ApiModelProperty(value = "분류 명")
	  private String logJobCodeName;
	  @ApiModelProperty(value = "문서상태 명 'L', 'Live','C','Closed' ")
	  private String logDocStatusName;
	  @ApiModelProperty(value = "보안등급 명 'S', '제한','T','팀내','C','사내','G','그룹사내' ")
	  private String logSecLevelName;
	  @ApiModelProperty(value = "이정정보 명")
	  private String logBeforeChangeName;
	  @ApiModelProperty(value = "이후정보 명")
	  private String logAfterChangeName;
	  @ApiModelProperty(value = "자료 소유부서 명")
	  private String logOwnDeptName;
	  @ApiModelProperty(value = "실행 부서 명")
	  private String logActDeptName;	  
	  @ApiModelProperty(value = "파일 size format 변경")
	  private String logFileSizeName;
	  
	  // 첨부이력 ( 외부 사이트 파일 반출 이력 )
	  @ApiModelProperty(value = "자료 List")
	  private String attachListName;
	  @ApiModelProperty(value = "반출일자 Format")
	  private String attachJobDateFormat;
	  @ApiModelProperty(value = "작성자 명")
	  private String attachReqUserName;
	  @ApiModelProperty(value = "부서 명")
	  private String attachCabinetCodeName;
	  @ApiModelProperty(value = "회사 명")
	  private String attachComCodeName;
	  
	  
}
