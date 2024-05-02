package com.dongkuksystems.dbox.models.dto.type.doc;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.dongkuksystems.dbox.constants.Commons;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocLinkListDto {
	@ApiModelProperty(value = "문서 아이디")
	private String rObjectId;
	@ApiModelProperty(value = "작성자")
	private String uCreateuser;
	@ApiModelProperty(value = "작성자 직급")
	private String uCreateTitleName;
	@ApiModelProperty(value = "작성자 소속부서")
	private String uCreateOrgNm;
	@ApiModelProperty(value = "시스템명")
	private String uWfSystem;
	@ApiModelProperty(value = "결제양식")
	private String uWfForm;
	@ApiModelProperty(value = "제목")
	private String uWfTitle;
	@ApiModelProperty(value = "결재자")
	private String uWfApprover;
	@ApiModelProperty(value = "결재일시")
	private LocalDateTime uWfApproverDate;

  public void setUWfApproverDate(LocalDateTime uWfApproverDate) {
    if (uWfApproverDate == null) this.uWfApproverDate = null;
    else {
      ZonedDateTime zdt = uWfApproverDate.atZone(ZoneId.of("Asia/Seoul"));
      long milli = zdt.toInstant().toEpochMilli();
      this.uWfApproverDate = Commons.NULL_DATE == milli  ? null : uWfApproverDate;
    }
  }
}

