package com.dongkuksystems.dbox.models.type.docbox;

import java.time.LocalDateTime;
import java.util.List;

import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Research {
  @ApiModelProperty(value = "연구과제 id", required = true)
  private String rObjectId;
  @ApiModelProperty(value = "문서함코드", required = true)
  private String uCabinetCode;
  @ApiModelProperty(value = "연구과제코드", required = true)
  private String uRschCode;
  @ApiModelProperty(value = "연구과제명", required = true)
  private String uRschName;
  @ApiModelProperty(value = "주관부서")
  private String uOwnDept; 
  @ApiModelProperty(value = "책임자")
  private String uChief;
  @ApiModelProperty(value = "보안등급")
  private String uSecLevel;
  @ApiModelProperty(value = "완료여부")
  private String uFinishYn;
  @ApiModelProperty(value = "시행년도", example = "2021")
  private String uStartYear;
  @ApiModelProperty(value = "분류폴더 ID")
  private String uFolId;  
  @ApiModelProperty(value = "목록보기 활성화 여부")
  private String uListOpenYn;
  @ApiModelProperty(value = "생성자")
  private String uCreateUser;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" )
  @ApiModelProperty(value = "생성 일시")
  private LocalDateTime uCreateDate;
  @ApiModelProperty(value = "수정자")
  private String uUpdateUser;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" )
  @ApiModelProperty(value = "수정 일시")
  private LocalDateTime uUpdateDate;
  @ApiModelProperty(value = "하위 폴더 존재 여부")
  private Boolean hasFolderChildren;
  @ApiModelProperty(value = "하위 문서 존재 여부")
  private Boolean hasDocChildren;
  @ApiModelProperty(value = "삭제 여부")
  private String uDeleteStatus;
  
  @ApiModelProperty(value = "소유부서 상세")
  private VDept ownDeptDetail;
  @ApiModelProperty(value = "책임자 상세")
  private VUser chiefDetail;
  

  @ApiModelProperty(value = "소유부서 회사")
  private String comOrgId;
  @ApiModelProperty(value = "반복 타입")
  private List<ResearchRepeating> researchRepeatings;
}
