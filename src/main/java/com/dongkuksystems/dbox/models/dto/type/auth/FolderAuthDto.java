package com.dongkuksystems.dbox.models.dto.type.auth;

import java.util.List;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.AuthorType;
import com.dongkuksystems.dbox.constants.DboxObjectType;
import com.dongkuksystems.dbox.constants.DocStatus;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.constants.SysObjectType;
import com.dongkuksystems.dbox.models.type.auth.AuthBase;
import com.dongkuksystems.dbox.models.type.auth.AuthShare;
import com.dongkuksystems.dbox.models.type.user.UserPresetDetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FolderAuthDto {
  private List<AuthBase> authBaseList;
  private List<AuthShare> authShareList;
  private UserPresetDetail userPreset;
  
  public static IDfPersistentObject setAuthBaseObj(IDfSession idfSession, AuthBase authBase) throws DfException {
    IDfPersistentObject obj = (IDfPersistentObject) idfSession.newObject(SysObjectType.AUTH_BASE.getValue());
    obj.setString("u_obj_id", authBase.getUObjId());
    obj.setString("u_obj_type", authBase.getUObjType());
//    if (DboxObjectType.DOCUMENT.getValue().equals(authBase.getUObjType()))
    obj.setString("u_doc_status", authBase.getUDocStatus());
    obj.setString("u_permit_type", authBase.getUPermitType());
    obj.setString("u_own_dept_yn", authBase.getUOwnDeptYn());
    obj.setString("u_author_id", authBase.getUAuthorId());
    obj.setString("u_author_type", authBase.getUAuthorType());
    obj.setString("u_add_gubun", authBase.getUAddGubun());
    obj.setString("u_ext_key", authBase.getUExtKey());
    obj.setString("u_create_user", authBase.getUCreateUser());
    obj.setString("u_create_date", (new DfTime()).toString());
    return obj;
  } 

  public static IDfPersistentObject setAuthBaseObjByAuthShare(IDfSession idfSession, AuthShare authShare) throws DfException {
    IDfPersistentObject obj = (IDfPersistentObject) idfSession.newObject(SysObjectType.AUTH_BASE.getValue());
    obj.setString("u_obj_id", authShare.getUObjId());
    obj.setString("u_obj_type", DboxObjectType.DOCUMENT.getValue());
    obj.setString("u_doc_status", authShare.getDocStatus());
    obj.setString("u_permit_type", authShare.getUPermitType()!=null?authShare.getUPermitType():GrantedLevels.READ.getLabel());
    obj.setString("u_add_gubun", "S"); // 공유협업
    obj.setString("u_own_dept_yn", "N");
    obj.setString("u_author_id", AuthorType.TEAM.getValue().equals(authShare.getUAuthorType())? "g_".concat(authShare.getUAuthorId()):authShare.getUAuthorId());
    obj.setString("u_author_type", authShare.getUAuthorType());
    obj.setString("u_create_user", authShare.getUCreateUser());
    obj.setString("u_create_date", (new DfTime()).toString());
    return obj;
  } 
  
  public static IDfPersistentObject setAuthShareObj(IDfSession idfSession, AuthShare authShare) throws DfException {
    IDfPersistentObject obj = (IDfPersistentObject) idfSession.newObject(SysObjectType.AUTH_SHARE.getValue());
    obj.setString("u_obj_id", authShare.getUObjId());
    obj.setString("u_author_id", authShare.getUAuthorId());
    obj.setString("u_author_type", authShare.getUAuthorType());
    obj.setString("u_permit_type", authShare.getUPermitType());
    obj.setString("u_create_user", authShare.getUCreateUser());
    obj.setString("u_create_date", (new DfTime()).toString()); 
    return obj;
  }
}
