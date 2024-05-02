package com.dongkuksystems.dbox.models.type.manager.sharegroup;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ShareGroup {	
	@ApiModelProperty(value = "ObjecId")
	private String rObjectId;	
	@ApiModelProperty(value = "회사코드")
	private String uComCode;	
	@ApiModelProperty(value = "회사코드(gw_dept)")
	private String comOrgId;
	@ApiModelProperty(value = "공유 그룹명")
	private String uShareName;
	@ApiModelProperty(value = "공유설명")
	private String uShareDesc;
	@ApiModelProperty(value = "부서코드")
	private String uDeptCode;

	@ApiModelProperty(value = "생성자")
	private String uCreateUser;
	
	@JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", timezone="Asia/Seoul")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@ApiModelProperty(value = "생성 일시")
	private LocalDateTime uCreateDate;	
	
	@ApiModelProperty(value = "수정자")
	private String uUpdateUser;
	
	@JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@ApiModelProperty(value = "수정 일시")
	private LocalDateTime uUpdaDate;

	@ApiModelProperty(value = "부서이름", notes = "부서이름 같이 조회하기 위해 추가")
	private String orgNm;
	@ApiModelProperty(value = "회사이름", notes = "회사이름 같이 조회하기 위해 추가")
	private String uCodeName1;
	@ApiModelProperty(value = "부서함코드")
  private String uCabinetCode;
  @ApiModelProperty(value = "acl groups_names")
  private String groupsNames;  
}
