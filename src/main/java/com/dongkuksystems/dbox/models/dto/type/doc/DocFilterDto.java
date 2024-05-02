package com.dongkuksystems.dbox.models.dto.type.doc;

import java.util.List;

import com.dongkuksystems.dbox.constants.DCTMConstants;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class DocFilterDto {
  @ApiModelProperty(value = "캐비넷코드", required = true)
	private String uCabinetCode;
  @ApiModelProperty(value = "상위 함 타입", example = "P:프로젝트, R:연구과제, D:부서, F:folder", required = true)
  private String hamType;
  @ApiModelProperty(value = "중복체크 파일경로리스트(중복체크에서만사용)", example = "/테스트폴더 1레벨/IIDS샘플/[경영전략]DRM 보안등급 정리_v1.1_210503.pptx")
  List<String> docPaths;
  
  @ApiModelProperty(value = "상위 함 아이디 ", hidden = true)
  private String hamId; 
  @ApiModelProperty(value = "상위 폴더 아이디 ", hidden = true)
  private String uFolId;
  @ApiModelProperty(value = "파일명 ", hidden = true)
  private String objectName;
  @ApiModelProperty(value = "파일확장자")
  private String uFileExt;
  @Builder.Default
  @ApiModelProperty(value = "최신버전 ", hidden = true)
  private String iHasFolder = "1";
  @ApiModelProperty(value = "프로젝트 코드", hidden = true)
  private String uPrCode;
  @ApiModelProperty(value = "프로젝트 타입", hidden = true)
  private String uPrType;
  @ApiModelProperty(value = "문서삭제상태", hidden = true)
  private String uDeleteStatus;
  @ApiModelProperty(value = "폴더함상태", hidden = true)
  private String uFolType;
  @ApiModelProperty(value = "문서 키값", hidden = true)
  private String uDocKey;
  
  public void addInfo(String dataId) {
    if ("P".equals(this.hamType) || "R".equals(this.hamType)) {
      this.hamId = dataId;
      this.uFolId = DCTMConstants.DCTM_BLANK;
    } else if ("D".equals(this.hamType)) {
      this.uFolId = DCTMConstants.DCTM_BLANK;
    } else {
      this.uFolId = dataId;
    }
  }
}
