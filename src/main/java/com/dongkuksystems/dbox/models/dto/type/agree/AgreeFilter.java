package com.dongkuksystems.dbox.models.dto.type.agree;

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
public class AgreeFilter {
	@ApiModelProperty(value = "서약/동의자")
	private String uUserId;
	@ApiModelProperty(value = "T:부서장동의(자동승인), U:사용자동의(프리패스)")
	private String uAgreeType;
	@ApiModelProperty(value = "회사코드")
	private String uComeCode;
	@ApiModelProperty(value = "부서코드")
	private String uDeptCode;
	@ApiModelProperty(value = "서약/동의명")
	private String uAgreeName;
  @ApiModelProperty(value = "동의여부")
  private String uAgreeYn;

}
