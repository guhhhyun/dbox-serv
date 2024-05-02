package com.dongkuksystems.dbox.models.dto.type.auth;

import static com.google.common.base.Preconditions.checkNotNull;

import com.documentum.fc.client.IDfSession;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.models.table.etc.VUser;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class CheckAuthParam {
  @ApiModelProperty(value = "요청 권한 레벨", required = true)
  private GrantedLevels grantedLevel;
  @ApiModelProperty(value = "hamType이 N 아닐경우 project code, research code, orgId", notes = "", required = true)
  private String targetObjectId;
  @ApiModelProperty(value = "상위 함 타입", example = "P:프로젝트, R:연구과제, D:부서, N:NONE(folder, doc 직접 권한체크)")
  private String hamType;
  @ApiModelProperty(value = "요청 사용자정보", required = true)
  private VUser user;
  @ApiModelProperty(value = "문서 권한 조회할때만 필수")
  private IDfSession idfSession;

  
  public CheckAuthParam(GrantedLevels grantedLevel, String targetObjectId, String hamType, 
      VUser user, IDfSession idfSession) {
    checkNotNull(grantedLevel, "grantedLevel must be provided.");
    checkNotNull(user, "user must be provided.");
//    checkNotNull(hamType, "hamType must be provided."); 
    checkNotNull(targetObjectId, "targetObjectId must be provided.");
    this.grantedLevel = grantedLevel;
    this.targetObjectId = targetObjectId;
    this.hamType = hamType;
    this.user = user;
    this.idfSession = idfSession;
  }

  public boolean hamValidation() {
    if ("P".equals(this.hamType) || "R".equals(this.hamType)) {
      return true;
    }
    return false;
  }
  
  
}
