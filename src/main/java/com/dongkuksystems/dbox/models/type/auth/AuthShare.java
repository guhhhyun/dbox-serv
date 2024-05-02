package com.dongkuksystems.dbox.models.type.auth;

import java.time.LocalDateTime;

import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
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
public class AuthShare {
  @ApiModelProperty(value = "authbase 키")
  private String rObjectId;
  @ApiModelProperty(value = "폴더 ID")
  private String uObjId;
  @ApiModelProperty(value = "권한자(부서,개인) ID")
  private String uAuthorId;
  @ApiModelProperty(value = "권한자타입(부서/사용자)', 'D:부서, P:개인")
  private String uAuthorType;
  @ApiModelProperty(value = "권한구분(조회/삭제)', 'R:조회, D:삭제")
  private String uPermitType;
  @ApiModelProperty(value = "생성자")
  private String uCreateUser;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", timezone="Asia/Seoul")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "생성 일시")
  private LocalDateTime uCreateDate;

  @ApiModelProperty(value = "authBAse 변환용")
  private String docStatus;

  @ApiModelProperty(value = "권한자(부서,개인) 이름")
  private String uAuthorName; 
  @ApiModelProperty(value = "(부서일경우만) 부서 코드")
  private String authorOrgId; 
  @ApiModelProperty(value = "(부서일경우만) 부서 캐비넷코드")
  private String authorCabinetCode; 
  @ApiModelProperty(value = "권한구분(조회/삭제)', 'R:조회 3, D:삭제 7")
  private String uPermitNum;
  
  @ApiModelProperty(value = "공유/협업 사용자 상세")
  private VUser user;
  @ApiModelProperty(value = "공유/협업 부서 상세")
  private VDept dept;
}
