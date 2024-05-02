package com.dongkuksystems.dbox.models.dto.type.request;
 

import java.util.List;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.TakeoutType;
import com.dongkuksystems.dbox.models.common.UserSession;

import io.swagger.annotations.ApiModelProperty;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

@Data
@NoArgsConstructor
@Builder
public class ReqTakeoutCreateDto {

  @ApiModelProperty(value = "P:사전승인, A:자동승인, F:프리패스")
  private String uApprType;
  @ApiModelProperty(value = "반출사유")
  private String uReqReason;

  public ReqTakeoutCreateDto(String uApprType, String uReqReason) {
    this.uReqReason = uReqReason;
    this.uApprType = uApprType;

  }
  
  public void validation() {
   
    if (TakeoutType.PRE.getValue().equals(this.uApprType)) {
      Preconditions.checkArgument((!Objects.equal(this.uReqReason, null) || !Objects.equal(this.uReqReason, "")), "사전승인일 경우 사유를 입력해야 합니다.");
    }
  }
  
  public static IDfPersistentObject CreateReqTakeout(IDfSession idfSession, ReqTakeoutCreateDto dto) throws Exception {

    IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_req_takeout");
    
      idf_PObj.setString("u_req_reason", dto.getUReqReason());
      idf_PObj.setString("u_req_user", idfSession.getLoginUserName());
      idf_PObj.setString("u_req_date", (new DfTime()).toString());
      idf_PObj.setString("u_req_status", "R");
      idf_PObj.setString("u_appr_type", "P");

      idf_PObj.save();
    
    return idf_PObj;

  }

  public static IDfPersistentObject CreateReqTakeoutFree(IDfSession idfSession, ReqTakeoutCreateDto dto,
      UserSession userSession) throws Exception {

    IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_req_takeout");

    idf_PObj.setString("u_req_user", userSession.getDUserId());
    idf_PObj.setString("u_req_date", (new DfTime()).toString());
    idf_PObj.setString("u_action_date", (new DfTime()).toString());

    idf_PObj.save();

    return idf_PObj;

  }

}
