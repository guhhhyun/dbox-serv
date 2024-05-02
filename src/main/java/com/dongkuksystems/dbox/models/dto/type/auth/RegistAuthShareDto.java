package com.dongkuksystems.dbox.models.dto.type.auth;

import java.time.LocalDateTime;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfTime;
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
public class RegistAuthShareDto {
  @ApiModelProperty(value = "문서함 코드", required = true)
  private String uCabinetCode;
  
  @ApiModelProperty(value = "폴더 ID", required = true)
  private String uObjId;
  
  @ApiModelProperty(value = "권한자(부서,개인) ID")
  private String uAuthorId;
  
  @ApiModelProperty(value = "권한자타입(부서/개인)")
  private String uAuthorType;
  
  @ApiModelProperty(value = "권한구분")
  private String uPermitType;
  
  @ApiModelProperty(value = "생성자")
  private String uCreateUser;
  
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "접근 일시")
  private LocalDateTime uCreateDate;
  
  public static IDfPersistentObject toIDfPersistentObject(IDfSession idfSession, RegistAuthShareDto dto) throws Exception {
    IDfPersistentObject idfPObj = (IDfPersistentObject) idfSession.newObject("edms_auth_share");
    idfPObj.setString("u_obj_id"      , dto.getUObjId      ());
    idfPObj.setString("u_author_id"   , dto.getUAuthorId   ());
    idfPObj.setString("u_author_type" , dto.getUAuthorType ());
    idfPObj.setString("u_permit_type" , dto.getUPermitType ());
    idfPObj.setString("u_create_user" , idfSession.getLoginUserName());
    idfPObj.setString("u_create_date" , (new DfTime()).toString());
    return idfPObj;
  }
}
