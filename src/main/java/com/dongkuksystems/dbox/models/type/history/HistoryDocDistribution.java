package com.dongkuksystems.dbox.models.type.history;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class HistoryDocDistribution {
	
	@ApiModelProperty(value = "문서 KEY")
	private String uDocKey;
	@ApiModelProperty(value = "문서명")
	private String uDocName;
	@ApiModelProperty(value = "문서함CODE")
	private String uCabinetCode;
	
	// 조직별 LifeCycle 정보
	@ApiModelProperty(value = "사용자")
	private String uJobUser;
	@ApiModelProperty(value = "사용자명")
	private String jobUserName;
	
	@ApiModelProperty(value = "권한신청")
	private String cntPermitReq;
	@ApiModelProperty(value = "권한부여")
	private String cntPermitApprove;
	@ApiModelProperty(value = "보안등급변경")
	private String cntSecChange;
	@ApiModelProperty(value = "보안등급변경")
	private String cntSecChangeUp;
	@ApiModelProperty(value = "보안등급변경")
	private String cntSecChangeDown;
	@ApiModelProperty(value = "복호화반출요청")
	private String cntTakeReq;
	@ApiModelProperty(value = "복호화반출부여")
	private String cntTakeApprove;
	@ApiModelProperty(value = "첨부")
	private String cntAttach;
	@ApiModelProperty(value = "인쇄")
	private String cntPrint;
	 
	
	
}
