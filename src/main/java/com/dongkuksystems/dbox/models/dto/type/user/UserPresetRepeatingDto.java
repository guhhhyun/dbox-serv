package com.dongkuksystems.dbox.models.dto.type.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class UserPresetRepeatingDto {
	@ApiModelProperty(value = "프리셋 id", required = true)
  private String rObjectId;
  @ApiModelProperty(value = "Live-읽기 권한자")
  private String uLiveReadAuthor;
  @ApiModelProperty(value = "Live-삭제 권한자")
  private String uLiveDeleteAuthor;
  @ApiModelProperty(value = "Closed 읽기 권한자")
  private String uClosedReadAuthor;
  @ApiModelProperty(value = "Live-읽기 권한자 명")
  private String uLiveReadAuthorName;
  @ApiModelProperty(value = "Live-삭제 권한자 명")
  private String uLiveDeleteAuthorName;
  @ApiModelProperty(value = "Closed 읽기 권한자 명")
  private String uClosedReadAuthorName;
  
  @ApiModelProperty(value = "아이디")
  private String id;
  @ApiModelProperty(value = "성명")
  private String name;
  @ApiModelProperty(value = "타입 GROUP, USER")
  private String type;
 
}
