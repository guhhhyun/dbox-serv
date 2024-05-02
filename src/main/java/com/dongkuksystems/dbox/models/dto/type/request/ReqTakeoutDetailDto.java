package com.dongkuksystems.dbox.models.dto.type.request;

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
public class ReqTakeoutDetailDto {
	@ApiModelProperty(value = "Object 키")
	  private String rObjectId;
	  
	  @ApiModelProperty(value = "")
	  private String uReqTitle;
	  @ApiModelProperty(value = "요청자")
	  private String uReqUser;
	  @ApiModelProperty(value = "요청자부서코드")
	  private String uReqDeptCode;
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
	  @JsonSerialize(using = LocalDateTimeSerializer.class)
	  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	  @ApiModelProperty(value = "요청일")
	  private LocalDateTime uReqDate; 
	  @ApiModelProperty(value = "요청상태")
	  private String uReqStatus;
	  @ApiModelProperty(value = "반출사유")
	  private String uReqReason;
	  @ApiModelProperty(value = "소유부서코드")
	  private String uOwnDeptCode;
	  @ApiModelProperty(value = "'반출만료일', SET COMMENT_TEXT='Flag 값이 (M)이면 메일발송 시 발송일시, (D)이면 등록 시 지정, U이면 NULL'")
	  private String uLimitFlag;
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
	  @JsonSerialize(using = LocalDateTimeSerializer.class)
	  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	  @ApiModelProperty(value = "요청일")
	  private LocalDateTime uLimitDate; 
	  @ApiModelProperty(value = "승인자")
	  private String uApprover;
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
	  @JsonSerialize(using = LocalDateTimeSerializer.class)
	  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	  @ApiModelProperty(value = "처리일")
	  private LocalDateTime uActionDate; 
	  @ApiModelProperty(value = "반려사유")
	  private String uRejectReason;
	  @ApiModelProperty(value = "승인방식")
	  private String uApprType;

	  @ApiModelProperty(value = "요청자 이름")
	  private String uReqUserName;
	  @ApiModelProperty(value = "요청자 ")
	  private String uReqUserJobPstnName;
	  @ApiModelProperty(value = "요청자 ")
	  private String uReqUserJobPstnCode;
	  @ApiModelProperty(value = "요청자 직")
	  private String uReqUserJobLevelName;
	  @ApiModelProperty(value = "요청자 직")
	  private String uReqUserJobLevelCode;
	  @ApiModelProperty(value = "요청자 직위")
	  private String uReqUserJobTitleCode;
	  @ApiModelProperty(value = "요청자 직위")
	  private String uReqUserJobTitleName;
	  @ApiModelProperty(value = "요청자 부서아이디")
	  private String uReqUserOrgId;
	  @ApiModelProperty(value = "요청자 부서명")
	  private String uReqUserOrgName;
	  @ApiModelProperty(value = "요청자 회사아이디")
	  private String uReqUserComId;
	  @ApiModelProperty(value = "요청자 회사명")
	  private String uReqUserComName;
	  
	  @ApiModelProperty(value = "승인자 아이디")
	  private String uApproverId;
	  @ApiModelProperty(value = "승인자 이름")
	  private String uApproverName;
	  @ApiModelProperty(value = "승인자 부서 코드")
	  private String uApproverOrgId;
	  @ApiModelProperty(value = "승인자 부서이름")
	  private String uApproverOrgName;
	  @ApiModelProperty(value = "승인자 회사 코드")
	  private String uApproverComId;
	  @ApiModelProperty(value = "승인자 회사명")
	  private String uApproverComName;
	  @ApiModelProperty(value = "승인자 ")
	  private String uApproverJobPstnName;
	  @ApiModelProperty(value = "승인자 ")
	  private String uApproverJobPstnCode;
	  @ApiModelProperty(value = "승인자 직")
	  private String uApproverJobLevelCode;
	  @ApiModelProperty(value = "승인자 직")
	  private String uApproverJobLevelName;
	  @ApiModelProperty(value = "승인자 직위")
	  private String uApproverJobTitleCode;
	  @ApiModelProperty(value = "승인자 직위")
	  private String uApproverJobTitleName;
	  
	  @ApiModelProperty(value = "요청문서 ID")
	  private String uReqDocId;
	  @ApiModelProperty(value = "요청문서 키")
	  private String uReqDocKey;
	  @ApiModelProperty(value = "요청문서 이름")
	  private String uReqDocName;
	  @ApiModelProperty(value = "문서 이름 + 확장자")
	  private String uReqDocTitle;
	  @ApiModelProperty(value = "요청문서 확장자")
	  private String uReqDocExt;
	  @ApiModelProperty(value = "문서 사이즈")
	  private String uDocSize;
	  @ApiModelProperty(value = "문서 경로")
	  private String uFolPath;
	  @ApiModelProperty(value = "문서 경로")
	  private String uSecLevel;
	  @ApiModelProperty(value = "문서 경로")
	  private String uVersionLabel;
	  @ApiModelProperty(value = "")
    private String uPrCode;
    @ApiModelProperty(value = "")
    private String uPrType;
    @ApiModelProperty(value = "")
    private String prOwnDeptCode;
    @ApiModelProperty(value = "")
    private String hamType;
    @ApiModelProperty(value = "")
    private String hamName;

    @ApiModelProperty(value = "")
    private String docEditorName;
    @ApiModelProperty(value = "")
    private String docEditorId;
    @ApiModelProperty(value = "")
    private String docCabinetCode;
    @ApiModelProperty(value = "")
    private String docOrgNm;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @ApiModelProperty(value = "최종수정일")
    private LocalDateTime docUpdateDate; 
}