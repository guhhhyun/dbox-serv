package com.dongkuksystems.dbox.models.dto.type.docbox.project;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectUpdateDto {
  @ApiModelProperty(value = "프로젝트코드", required = true)
  private String uPjtCode;	

  @ApiModelProperty(value = "프로젝트명")
  private String uPjtName;	
  @ApiModelProperty(value = "보안등급")
  private String uSecLevel; 
  @ApiModelProperty(value = "주관부서")
  private String uOwnDept;
  @ApiModelProperty(value = "완료여부")
  private String uFinishYn;	
  @ApiModelProperty(value = "시행년도")
  private String uStartYear;	
  @ApiModelProperty(value = "책임자")
  private String uChiefId;  
  @ApiModelProperty(value = "목록보기 활성화 여부")
  private String uListOpenYn;	
  
  @ApiModelProperty(value = "변경자")
  private String uUpdateUser;

  @ApiModelProperty(value = "참여부서(조회/다운로드) 리스트")
  private List<String> uJoinDeptReads;	
  @ApiModelProperty(value = "참여부서(편집/삭제) 리스트")
  private List<String> uJoinDeptDels;	
}
