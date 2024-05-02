package com.dongkuksystems.dbox.models.dto.type.folder;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfTime;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistFolderDto {
  @ApiModelProperty(value = "문서함 코드", required = true)
  private String uCabinetCode;
  @ApiModelProperty(value = "폴더구분", required = true)
  private String uFolType;
  @ApiModelProperty(value = "폴더명")
  private String uFolName;
  @ApiModelProperty(value = "상위폴더 ID")
  private String uUpFolId;
  @ApiModelProperty(value = "보안등급")
  private String uSecLevel;
  @ApiModelProperty(value = "폴더상태 O:일반, C:잠금")
  private String uFolStatus;
  @ApiModelProperty(value = "태그")
  private String uFolTag;
  @ApiModelProperty(value = "분류")
  private String uFolClass;
  @ApiModelProperty(value = "수정가능여부 default:1")
  @Builder.Default
  private String uEditableFlag = "1";
  @ApiModelProperty(value = "삭제가능여부")
  private String uDeleteStatus;
  @ApiModelProperty(value = "생성자")
  private String uCreateUser;

  @ApiModelProperty(value = "프로젝트/연구과제 코드", required = true)
  private String uPrCode;
  @ApiModelProperty(value = "프로젝트/연구과제 타입", required = true)
  private String uPrType;
  
  public static IDfPersistentObject toIDfPersistentObject(IDfSession idfSession, RegistFolderDto dto) throws Exception {

    // tmp
//    String s_DCTMFolderId = DCTMUtils.makeEDMFolder(idfSession);

    IDfPersistentObject idfPObj = (IDfPersistentObject) idfSession.newObject("edms_folder");
    idfPObj.setString("u_cabinet_code", dto.getUCabinetCode());
    idfPObj.setString("u_fol_type", dto.getUFolType());
    idfPObj.setString("u_fol_name", dto.getUFolName());
    idfPObj.setString("u_up_fol_id", dto.getUUpFolId());
    idfPObj.setString("u_sec_level", dto.getUSecLevel());
    idfPObj.setString("u_fol_status", dto.getUFolStatus());
    idfPObj.setString("u_fol_tag", dto.getUFolTag());
    idfPObj.setString("u_fol_class", dto.getUFolClass());
    idfPObj.setString("u_editable_flag", dto.getUEditableFlag());
    idfPObj.setString("u_delete_status", dto.getUDeleteStatus());
    idfPObj.setString("u_create_user", dto.getUCreateUser());
    idfPObj.setString("u_create_date", (new DfTime()).toString());
    idfPObj.setString("u_update_user", dto.getUCreateUser());
    idfPObj.setString("u_update_date", (new DfTime()).toString());
    idfPObj.setString("u_pr_code", dto.getUPrCode());
    idfPObj.setString("u_pr_type", dto.getUPrType());
    return idfPObj;
  }
}
