package com.dongkuksystems.dbox.services.manager.sharegroup;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.daos.type.manager.sharegroup.ShareGroupDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.sharegroup.PatchDeptDto;
import com.dongkuksystems.dbox.models.dto.type.manager.sharegroup.PatchShareGroupDto;
import com.dongkuksystems.dbox.models.dto.type.manager.sharegroup.ShareGroupCreateDto;
import com.dongkuksystems.dbox.models.type.manager.sharegroup.ShareGroup;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class ShareGroupServiceImpl extends AbstractCommonService implements ShareGroupService {

  private final ShareGroupDao shareGroupDao;

  public ShareGroupServiceImpl(ShareGroupDao shareGroupDao) {
    this.shareGroupDao = shareGroupDao;
  }

  @Override
  public List<ShareGroup> selectShareGroups() {
    return shareGroupDao.selectAll();
  }

  @Override
  public List<ShareGroup> selectDeptInShareGroup(String rObjectId) {
    return shareGroupDao.selectDeptInShareGroup(rObjectId);
  }

  @Override
  public String patchDept(String rObjectId, UserSession userSession, PatchDeptDto dto) throws Exception {
    IDfSession idfSession = null;
    IDfSession idfAdminSession = DCTMUtils.getAdminSession(); // Superuser
    try {
      idfSession = this.getIdfSession(userSession);
      //emds_share_group_r 에 부서 추가
      IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId));
      for (String str : dto.getUDeptCode()) {
        idf_PObj.appendString("u_dept_code", str);
      }
      idf_PObj.save();
      
      List<ShareGroup> shareGroupDeptSize = shareGroupDao.selectCabinetCodeList(rObjectId, "#");

      for (int i = 0; i < shareGroupDeptSize.size(); i++) {
        // 해당 부서의 acl g_d00000_share
        List<ShareGroup> onlyOneCabinetCode = shareGroupDao.selectOnlyOneCabinetCode(rObjectId, shareGroupDeptSize.get(i).getUDeptCode());
        IDfGroup idf_Group = idfAdminSession.getGroup("g_" + onlyOneCabinetCode.get(0).getUCabinetCode() + "_share");

        // 위에 선택된 부서를 제외한 나머지 부서들의 캐비넷코드
        List<ShareGroup> selectCabinetCodeList = shareGroupDao.selectCabinetCodeList(rObjectId, onlyOneCabinetCode.get(0).getUDeptCode());
        for (int j = 0; j < selectCabinetCodeList.size(); j++) {
          idf_Group.addGroup("g_" + selectCabinetCodeList.get(j).getUCabinetCode());
          idf_Group.save();
        }
      }     
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfAdminSession != null && idfAdminSession.isConnected()) {
        idfAdminSession.disconnect();
      }
      if (idfSession != null && idfSession.isConnected()) {
        sessionRelease(userSession.getUser().getUserId(), idfSession);
      }
    }
    return userSession.getUser().getUserId();
  }

  @Override
  public String createShareGroup(UserSession userSession, ShareGroupCreateDto dto) throws Exception {
    IDfSession idfSession = null;
    try {
      idfSession = this.getIdfSession(userSession);
      IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_share_group");
      idf_PObj.setString("u_com_code", dto.getUComCode());
      idf_PObj.setString("u_share_name", dto.getUShareName());
      idf_PObj.setString("u_share_desc", dto.getUShareDesc());
      idf_PObj.setString("u_create_user", idfSession.getLoginUserName());
      idf_PObj.setString("u_create_date", (new DfTime()).toString());
      idf_PObj.save();
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return userSession.getUser().getUserId();
  }

  @Override
  public String deleteShareGroup(String rObjectId, UserSession userSession) throws Exception {
    IDfSession idfSession = null;
    IDfSession idfAdminSession = DCTMUtils.getAdminSession(); // Superuser
    try {
      idfSession = this.getIdfSession(userSession);
      
      List<String> DeleteDeptList = new ArrayList<String>();
      List<String> ExceptionDeptList = new ArrayList<String>();

      // 공유그룹에 속한 모든 부서 정보
      List<ShareGroup> shareGroupAllDept = shareGroupDao.selectCabinetCodeList(rObjectId, "#");
      
      
      for(int a = 0; a < shareGroupAllDept.size(); a++) {
        // a번째 해당 부서를 제외한 나머지 부서
        List<ShareGroup> deptList = shareGroupDao.selectCabinetCodeList(rObjectId, shareGroupAllDept.get(a).getUDeptCode());
      
        String aclGroupName = ("g_" + deptList.get(a).getUCabinetCode() + "_share");

        // dm_group acl에 속한 부서 캐비넷코드
        List<ShareGroup> aclCabinetCodes = shareGroupDao.selectAclCabinetCode(aclGroupName);
        
        for(int b = 0; b < aclCabinetCodes.size(); b++) {          
            if(shareGroupAllDept.get(a).getUCabinetCode().equals(aclCabinetCodes.get(b).getUCabinetCode())) {  
              ExceptionDeptList.add(aclCabinetCodes.get(b).getUCabinetCode());              
          }          
            
        }
        
      }

        
        //해당 부서다른 공유그룹에 속해있지 않은경우 
//        else {
//          // 공유그룹에 속한 각각 부서의 공유acl group 삭제
//          for (int i = 0; i < shareGroupAllDept.size(); i++) {
//            IDfGroup idf_Group = idfAdminSession.getGroup("g_" + shareGroupAllDept.get(i).getUCabinetCode() + "_share");
//            
//            // 위에 i번째 부서를 제외한 나머지 부서들의 캐비넷코드
//            List<ShareGroup> exceptDeptList = shareGroupDao.selectCabinetCodeList(rObjectId, shareGroupAllDept.get(i).getUDeptCode());        
//            for (int j = 0; j < exceptDeptList.size(); j++) {
//              idf_Group.removeGroup("g_" + exceptDeptList.get(j).getUCabinetCode());
//              idf_Group.save();
//            }
//          }
//        }
           

      
      
      //edms_share_group_s 데이터 삭제
      IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId));
      idf_PObj.destroy();
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfAdminSession != null && idfAdminSession.isConnected()) {
        idfAdminSession.disconnect();
      }
      if (idfSession != null && idfSession.isConnected()) {
        sessionRelease(userSession.getUser().getUserId(), idfSession);
      }
    }
    return rObjectId;
  }

  @Override
  public String deleteDept(String rObjectId, UserSession userSession, String uDeptCode) throws Exception {
    IDfSession idfSession = null;
    IDfSession idfAdminSession = DCTMUtils.getAdminSession(); // Superuser
    try {
      idfSession = this.getIdfSession(userSession);

      // 삭제 선택한 부서의 부서코드, 캐비넷코드 정보
      List<ShareGroup> deleteDeptData = shareGroupDao.selectOnlyOneCabinetCode(rObjectId, uDeptCode);
      // 공유그룹에 속한 모든 부서 정보
      List<ShareGroup> shareGroupAllDept = shareGroupDao.selectCabinetCodeList(rObjectId, "#");
      //삭제대상부서 제외 한 공유그룹의 나머지 부서 리스트
      List<ShareGroup> exceptDeptList = shareGroupDao.selectCabinetCodeList(rObjectId, uDeptCode);

      // acl 제거 
      for (int i = 0; i < shareGroupAllDept.size(); i++) {
        IDfGroup idf_Group = idfAdminSession.getGroup("g_" + shareGroupAllDept.get(i).getUCabinetCode() + "_share");

        // 삭제부서인 경우 삭제부서 제외한 공유그룹에 속한 부서 모두를 삭제부서의 share acl에서 삭제
        if (shareGroupAllDept.get(i).getUCabinetCode().equals(deleteDeptData.get(0).getUCabinetCode())) {
          // 삭제 부서를 제외한 나머지 부서들의 캐비넷코드
          for (int j = 0; j < exceptDeptList.size(); j++) {
            idf_Group.removeGroup("g_" + exceptDeptList.get(j).getUCabinetCode());
            idf_Group.save();
          }
        }
        // 삭제부서가 아닌경우 삭제부서의 캐비넷코드만 각 부서 acl에서 삭제 
        else {
          // 삭제 부서를 제외한 나머지 부서들의 캐비넷코드          
            idf_Group.removeGroup("g_" + deleteDeptData.get(0).getUCabinetCode());
            idf_Group.save();
          }
        }                            

      //edms_share_group_r 에 리피팅된 부서 삭제
      IDfPersistentObject idfPObj = idfSession.getObject(new DfId(rObjectId));

      int i_ValIdx = idfPObj.findString("u_dept_code", uDeptCode);
      idfPObj.remove("u_dept_code", i_ValIdx);
      idfPObj.save();
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfAdminSession != null && idfAdminSession.isConnected()) {
        idfAdminSession.disconnect();
      }
      if (idfSession != null && idfSession.isConnected()) {
        sessionRelease(userSession.getUser().getUserId(), idfSession);
      }
    }
    return rObjectId;
  }

  @Override
  public String patchShareGroup(String rObjectId, UserSession userSession, PatchShareGroupDto dto) throws Exception {
    IDfSession idfSession = null;
    try {
      idfSession = this.getIdfSession(userSession);
      String s_ObjId = rObjectId;
      IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));
      idf_PObj.setString("u_share_name", dto.getUShareName());
      idf_PObj.setString("u_share_desc", dto.getUShareDesc());
      idf_PObj.setString("u_update_user", idfSession.getLoginUserName());
      idf_PObj.setString("u_update_date", (new DfTime()).toString());
      idf_PObj.save();
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return userSession.getUser().getUserId();
  }
}