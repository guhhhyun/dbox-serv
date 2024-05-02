package com.dongkuksystems.dbox.models.type.user;

import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class UserPresetRepeating {
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

  @ApiModelProperty(value = "Live-읽기 권한 사용자 상세")
  private VUser liveReadUserDetail;
  @ApiModelProperty(value = "Live-읽기 권한 부서 상세")
  private VDept liveReadDeptDetail;
  @ApiModelProperty(value = "Live-삭제 권한 사용자 상세")
  private VUser liveDeleteUserDetail;
  @ApiModelProperty(value = "Live-삭제 권한 사용자 상세")
  private VDept liveDeleteDeptDetail;
  @ApiModelProperty(value = "Closed-읽기 권한 사용자 상세")
  private VUser closedReadUserDetail;
  @ApiModelProperty(value = "Closed-읽기 권한 사용자 상세")
  private VDept closedReadDeptDetail;
}
