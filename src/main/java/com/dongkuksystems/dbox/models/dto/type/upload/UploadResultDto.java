package com.dongkuksystems.dbox.models.dto.type.upload;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadResultDto {
  @Default
	@ApiModelProperty(value = "업로드 갯수")
	private int uploadCnt = 0;
  @Default
	@ApiModelProperty(value = "실패 갯수")
	private int successCnt = 0;
  @Default
	@ApiModelProperty(value = "실패 갯수")
	private int failedCnt = 0;

  @Default
  @ApiModelProperty(value = "1실패 갯수")
  private int authCnt = 0;
  @Default
  @ApiModelProperty(value = "2실패 갯수")
  private int lockCnt = 0;
  @Default
  @ApiModelProperty(value = "3실패 갯수")
  private int extCnt = 0;
  @Default
  @ApiModelProperty(value = "4실패 갯수")
  private int nameCnt = 0;
  @Default
  @ApiModelProperty(value = "5실패 갯수")
  private int errorCnt = 0; 
  @ApiModelProperty(value = "실패메세지")
  List<String> errorMsg;
}
