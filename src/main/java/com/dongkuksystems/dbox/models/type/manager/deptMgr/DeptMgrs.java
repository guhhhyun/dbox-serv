package com.dongkuksystems.dbox.models.type.manager.deptMgr;

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
public class DeptMgrs {
	  @ApiModelProperty(value = "r")
	  private String rObjectId;
	  @ApiModelProperty(value = "부서코드")
	  private String uDeptCode;
	  @ApiModelProperty(value = "사용자 ID")
	  private String uUserId;
	  @ApiModelProperty(value = "관리자 타입")
	  private String uMgrType;
	  @ApiModelProperty(value = "지정자")
	  private String uAssignUser;
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
	  @JsonSerialize(using = LocalDateTimeSerializer.class)
	  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	  @ApiModelProperty(value = "지정일")
	  private LocalDateTime uAssignDate;
	  @ApiModelProperty(value = "지정자 유형")
	  private String uAssignUserType;
	  
	
}
