package com.dongkuksystems.dbox.services.manager.rolemanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.dongkuksystems.dbox.daos.type.manager.roleauth.RoleAuthDao;
import com.dongkuksystems.dbox.daos.type.manager.rolemanagement.RoleManagementDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.rolemanagement.RoleManagementDto;
import com.dongkuksystems.dbox.models.type.manager.roleauth.RoleAuth;
import com.dongkuksystems.dbox.models.type.manager.rolemanagement.RoleManagement;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.manager.roleauth.RoleAuthService;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class RoleManagementServiceImpl extends AbstractCommonService implements RoleManagementService {

  private final RoleManagementDao roleManagementDao;
  private final RoleAuthService roleAuthService;
  private final RoleAuthDao roleAuthDao;

  public RoleManagementServiceImpl(RoleManagementDao roleManagementDao, RoleAuthDao roleAuthDao,
      RoleAuthService roleAuthService) {
    this.roleManagementDao = roleManagementDao;
    this.roleAuthDao = roleAuthDao;
    this.roleAuthService = roleAuthService;
  }

  @Override
  public List<RoleManagement> selectRoleManagement(String uDocFlag) {
    return roleManagementDao.selectRoleManagement(uDocFlag);
  }

  @Override
  public Optional<RoleManagement> selectUnPolicyGroup(String rObjectId, String uOptionVal) {
    return roleManagementDao.selectUnPolicyGroup(rObjectId, uOptionVal);
  }

  @Override
  public void updatePolicy(UserSession userSession, String rObjectId, RoleManagementDto dto) throws Exception {
    IDfSession idf_Sess = this.getIdfSession(userSession);
    IDfSession idfAdminSession = DCTMUtils.getAdminSession(); // Superuser

    try {
      idf_Sess.beginTrans();

      // 전사관리자
      if (dto.getUAuthScope().equals("C")) {
        IDfPersistentObject idf_PObj = idfAdminSession.getObject(new DfId(rObjectId));
        Optional<RoleManagement> anotherGroup = roleManagementDao.selectUnPolicyGroup(rObjectId, dto.getUOptionVal());
        IDfPersistentObject idf_PObj2 = idfAdminSession.getObject(new DfId(anotherGroup.get().getRObjectId()));

        String list[] = { "g_dks_mgr", "g_itg_mgr", "g_unc_mgr", "g_fei_mgr" };

        for (String roleAuth : list) {
          List<RoleAuth> userList = roleAuthDao.selectRoleAuthGroupUser2(roleAuth, "1", "C", dto.getUDocFlag());

          if (userList.size() > 0) {

            IDfGroup idf_Group = (IDfGroup) idfAdminSession.getGroup(userList.get(0).getGroupName());
            IDfGroup idf_Group2 = (IDfGroup) idfAdminSession.getGroup("");

            String groupName = userList.get(0).getGroupName();
            String result = groupName.substring(groupName.length() - 1, groupName.length());

            if (result.equals("a")) {
              idf_Group2 = (IDfGroup) idfAdminSession.getGroup(groupName.substring(0, groupName.length() - 1) + "b");
            } else if (result.equals("b")) {
              idf_Group2 = (IDfGroup) idfAdminSession.getGroup(groupName.substring(0, groupName.length() - 1) + "a");
            }

            // 기존 그룹 사용자 삭제 -> 정책 변경 그룹에 사용자 추가
            for (int i = 0; i < userList.size(); i++) {
              idf_Group.removeUser(userList.get(i).getUserId());
              idf_Group2.addUser(userList.get(i).getUserId());
            }
            idf_Group.save();
            idf_Group2.save();
          }

        }

        // 정책 변경 update + 1->0 0->1 로 동시에
        // selectUnPolicyGroup 에서 조회해온 selected값으로 수정
        idf_PObj.setString("u_selected", anotherGroup.get().getUSelected());
        // 화면에서 던져준 값으로 수정
        idf_PObj2.setString("u_selected", dto.getUSelected());

        idf_PObj.save();
        idf_PObj2.save();

      }

      // 감사
      else if (dto.getUAuthScope().equals("G")) {
        IDfPersistentObject idf_PObj = idfAdminSession.getObject(new DfId(rObjectId));
        Optional<RoleManagement> anotherGroup = roleManagementDao.selectUnPolicyGroup(rObjectId, dto.getUOptionVal());
        IDfPersistentObject idf_PObj2 = idfAdminSession.getObject(new DfId(anotherGroup.get().getRObjectId()));
        List<RoleAuth> userList = roleAuthService.selectRoleAuthGroupUsers("g_audit_wf", "1", "G");

        if (userList.size() > 0) {

          IDfGroup idf_Group = (IDfGroup) idfAdminSession.getGroup(userList.get(0).getGroupName());
          IDfGroup idf_Group2 = (IDfGroup) idfAdminSession.getGroup("");

          String groupName = userList.get(0).getGroupName();
          String result = groupName.substring(groupName.length() - 1, groupName.length());

          if (result.equals("a")) {
            idf_Group2 = (IDfGroup) idfAdminSession.getGroup(groupName.substring(0, groupName.length() - 1) + "b");
          } else if (result.equals("b")) {
            idf_Group2 = (IDfGroup) idfAdminSession.getGroup(groupName.substring(0, groupName.length() - 1) + "a");
          }

          // 기존 그룹 사용자 삭제 -> 정책 변경 그룹에 사용자 추가
          for (int i = 0; i < userList.size(); i++) {
            idf_Group.removeUser(userList.get(i).getUserId());
            idf_Group2.addUser(userList.get(i).getUserId());
          }
          idf_Group.save();
          idf_Group2.save();
        }

        idf_PObj.setString("u_selected", anotherGroup.get().getUSelected());
        // selectUnPolicyGroup에서 조회해온 rObjectId
        // 화면에서 던져준 값으로 수정
        idf_PObj2.setString("u_selected", dto.getUSelected());

        idf_PObj.save();
        idf_PObj2.save();
      }

      // 부서관리자
      else if (dto.getUAuthScope().equals("D")) {
        IDfPersistentObject idf_PObj = idfAdminSession.getObject(new DfId(rObjectId));
        Optional<RoleManagement> anotherGroup = roleManagementDao.selectUnPolicyGroup(rObjectId, dto.getUnUOptionVal());
        IDfPersistentObject idf_PObj2 = idfAdminSession.getObject(new DfId(anotherGroup.get().getRObjectId()));

        List<RoleAuth> groupList = roleAuthDao.selectDeptMgrGroup(dto.getUDocFlag(), dto.getUOptionVal());

        for (int i = 0; i < groupList.size(); i++) {
          List<RoleAuth> userList = roleAuthDao.selectDeptMgrUser(groupList.get(i).getGroupName());

          if (userList.get(0).getUsersNames() != null) {

            IDfGroup idf_Group = (IDfGroup) idfAdminSession.getGroup(groupList.get(i).getGroupName());
            IDfGroup idf_Group2 = (IDfGroup) idfAdminSession.getGroup("");

            String groupName = userList.get(0).getGroupName();
            String result = groupName.substring(groupName.length() - 1, groupName.length());

            if (result.equals("a")) {
              idf_Group2 = (IDfGroup) idfAdminSession.getGroup(groupName.substring(0, groupName.length() - 1) + "b");
            } else if (result.equals("b")) {
              idf_Group2 = (IDfGroup) idfAdminSession.getGroup(groupName.substring(0, groupName.length() - 1) + "a");
            }

            // 기존 그룹 사용자 삭제 -> 정책 변경 그룹에 사용자 추가
            for (int j = 0; j < userList.size(); j++) {
              idf_Group.removeUser(userList.get(j).getUsersNames());
              idf_Group2.addUser(userList.get(j).getUsersNames());
            }

            idf_Group.save();
            idf_Group2.save();
          }

        }
        // 정책 변경 update + 1->0 0->1 로 동시에
        // selectUnPolicyGroup 에서 조회해온 selected값으로 수정
        idf_PObj.setString("u_selected", anotherGroup.get().getUSelected());
        // 화면에서 던져준 값으로 수정
        idf_PObj2.setString("u_selected", dto.getUSelected());

        idf_PObj.save();
        idf_PObj2.save();
      }

    } catch (Exception e) {
      throw e;
    } finally {
      if (idf_Sess != null) {
        if (idf_Sess.isTransactionActive()) {
          idf_Sess.abortTrans();
        }
        idfAdminSession.disconnect();
        sessionRelease(userSession.getUser().getUserId(), idf_Sess);
      }
    }
  }

}