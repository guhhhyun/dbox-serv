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
public class AuthBase {
  @ApiModelProperty(value = "authbase 키")
  private String rObjectId;
  @ApiModelProperty(value = "문서 키 / 폴더 ID")
  private String uObjId;
  @ApiModelProperty(value = "문서/폴더 구분','D:문서, F:폴더")
  private String uObjType;
  @ApiModelProperty(value = "문서상태', 'L:Live, C:Closed")
  private String uDocStatus;
  @ApiModelProperty(value = "권한구분(조회/삭제)', 'R:조회, D:삭제")
  private String uPermitType;
  @ApiModelProperty(value = "소유부서여부")
  private String uOwnDeptYn;
  @ApiModelProperty(value = "권한자(부서,사용자) ID")
  private String uAuthorId;
  @ApiModelProperty(value = "권한자타입(부서/사용자)', 'D:부서, U:사용자, S:기본권한그룹'")
  private String uAuthorType;
  @ApiModelProperty(value = "생성자")
  private String uCreateUser;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", timezone="Asia/Seoul")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "생성 일시")
  private LocalDateTime uCreateDate;
  @ApiModelProperty(value = "외부 키")
  private String uExtKey;
  @ApiModelProperty(value = "추가 소스 구분")
  private String uAddGubun;
  
  @ApiModelProperty(value = "권한 사용자 상세")
  private VUser user;
  @ApiModelProperty(value = "권한 부서 상세")
  private VDept dept;
}
