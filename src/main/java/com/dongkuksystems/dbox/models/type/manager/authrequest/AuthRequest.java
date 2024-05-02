package com.dongkuksystems.dbox.models.type.manager.authrequest;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthRequest {
	@ApiModelProperty(value = "ObjecId")
	private String rObjectId;
	@ApiModelProperty(value = "부서코드")
	private String uReqDeptCode;
	@ApiModelProperty(value = "부서명")
	private String orgNm;
	@ApiModelProperty(value = "회사코드")
	private String comOrgId;
	@ApiModelProperty(value = "요청자id")
	private String uReqUser;
	@ApiModelProperty(value = "요청 상태")
	private String uReqStatus;
	@ApiModelProperty(value = "요청자 이름")
	private String displayName;
	@ApiModelProperty(value = "요청문서 key")
	private String uReqDocKey;
	@ApiModelProperty(value = "요청문서 id")
	private String uReqDocId;
	@ApiModelProperty(value = "요청문서 이름")
	private String uReqDocName;
	@ApiModelProperty(value = "보안등급")
	private String uSecLevel;
	@ApiModelProperty(value = "요청사유")
	private String uReqReason;
	@ApiModelProperty(value = "요청일")
	private String uReqDate;
	@ApiModelProperty(value = "권한기간")
	private String uOpenFlag;
	@ApiModelProperty(value = "승인자")
	private String uApprover;
	@ApiModelProperty(value = "반려사유")
	private String uRejectReason;
	@ApiModelProperty(value = "소유부서코드")
	private String uOwnDeptCode;
	@ApiModelProperty(value = "요청자 email")
	private String email;

}
