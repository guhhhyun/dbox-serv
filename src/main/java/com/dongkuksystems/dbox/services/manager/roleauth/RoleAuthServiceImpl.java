package com.dongkuksystems.dbox.services.manager.roleauth;

import java.util.List;

import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.Commons;
import com.dongkuksystems.dbox.daos.type.manager.roleauth.RoleAuthDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.roleauth.RoleAuthDto;
import com.dongkuksystems.dbox.models.dto.type.manager.rolemanagement.RoleManagementDto;
import com.dongkuksystems.dbox.models.type.manager.roleauth.RoleAuth;
import com.dongkuksystems.dbox.repositories.redis.RedisRepository;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class RoleAuthServiceImpl extends AbstractCommonService implements RoleAuthService {

  private final RoleAuthDao roleAuthDao;
  private final RedisRepository redisRepository;

  public RoleAuthServiceImpl(RoleAuthDao roleAuthDao, RedisRepository redisRepository) {
    this.roleAuthDao = roleAuthDao;
    this.redisRepository = redisRepository;
  }

  @Override
  public List<RoleAuth> selectRoleAuthGroups(String type) {
    return roleAuthDao.selectRoleAuthGroups(type);
  }

  @Override
  public List<RoleAuth> selectMgrGroups(String type, RoleManagementDto roleManagementDto) {
    return roleAuthDao.selectMgrGroups(type, roleManagementDto);
  }

  @Override
  public List<RoleAuth> selectRoleAuthGroupUsers(String uAuthGroup, String uConfigFlag, String uGroupScope) {
    // 그룹 문서 관리자 경우 dm_group에서 각 사별 전사관리자에 들어감
    if (uAuthGroup.equals("g_ent_mgr")) {
      return roleAuthDao.selectEntMgrUsers();
    } else {
      return roleAuthDao.selectRoleAuthGroupUsers(uAuthGroup, uConfigFlag, uGroupScope);
    }
  }

  @Override
  public List<RoleAuth> selectMgrUsers(String uComCode, String uGroupScope) {
    return roleAuthDao.selectMgrUsers(uComCode, uGroupScope);
  }

  @Override
  public List<RoleAuth> selectDeptMgrGroup(String uDocFlag, String uOptionVal) {
    return roleAuthDao.selectDeptMgrGroup(uDocFlag, uOptionVal);
  }

  @Override
  public void createRoleAuthUser(RoleAuthDto dto, UserSession userSession) throws Exception {
    IDfSession idf_Sess = this.getIdfSession(userSession);
    IDfSession idfAdminSession = DCTMUtils.getAdminSession(); // Superuser

    UserSession userSess = (UserSession) redisRepository.getObject(dto.getUserId(), UserSession.class);
    IDfPersistentObject idf_PObj = (IDfPersistentObject) idf_Sess.newObject("edms_mgr");

    try {
      IDfGroup idf_Group = (IDfGroup) idfAdminSession.getGroup(dto.getGroupName());

      if (idf_Group == null) {
        // 그룹문서관리자
        if (dto.getGroupName().equals("g_ent_mgr")) {
          List<RoleAuth> companyAclNameList = roleAuthDao.selectCompanyAclName(null);

          idf_PObj.setString("u_user_id", dto.getUserId());
          idf_PObj.setString("u_com_code", dto.getUComCode());
          idf_PObj.setString("u_mgr_type", dto.getUGroupScope());
          idf_PObj.setString("u_create_user", idf_Sess.getLoginUserName());
          idf_PObj.setString("u_create_date", (new DfTime()).toString());
          idf_PObj.setString("u_update_user", idf_Sess.getLoginUserName());
          idf_PObj.setString("u_update_date", (new DfTime()).toString());
          idf_PObj.save();

          // 전사관리자 dm_group에 각각 추가
          for (int i = 0; i < companyAclNameList.size(); i++) {
            IDfGroup idf_Group_Company = (IDfGroup) idfAdminSession.getGroup(companyAclNameList.get(i).getGroupName());
            IDfGroup idf_Group_Company2 = (IDfGroup) idfAdminSession
                .getGroup(companyAclNameList.get(i).getGroupName2());
            idf_Group_Company.addUser(dto.getUserId());
            idf_Group_Company2.addUser(dto.getUserId());
            idf_Group_Company.save();
            idf_Group_Company2.save();
          }

          // redis 세션 값 변경          
          if (userSess != null) {
            if (userSess.getUser().getMgr().getGroupComCode() == null) {
              userSess.getUser().getMgr().setGroupComCode("DKG");
              redisRepository.put(Commons.SESSION_PREFIX + dto.getUserId(), userSess, Commons.REDIS_TIMEOUT_SEC);
            }
          }
        }
      } else {
        // 전사문서자일경우
        if (dto.getUGroupScope().equals("C") && dto.getUConfigFlag().equals("1")) {
          idf_PObj.setString("u_user_id", dto.getUserId());
          idf_PObj.setString("u_com_code", dto.getUComCode());
          idf_PObj.setString("u_mgr_type", dto.getUGroupScope());
          idf_PObj.setString("u_create_user", idf_Sess.getLoginUserName());
          idf_PObj.setString("u_create_date", (new DfTime()).toString());
          idf_PObj.setString("u_update_user", idf_Sess.getLoginUserName());
          idf_PObj.setString("u_update_date", (new DfTime()).toString());
          idf_PObj.save();

          // redis 세션 값 변경   
          if (userSess != null) {
            if (userSess.getUser().getMgr().getCompanyComCode() == null) {
              userSess.getUser().getMgr().setCompanyComCode(dto.getUComCode());
              redisRepository.put(Commons.SESSION_PREFIX + dto.getUserId(), userSess, Commons.REDIS_TIMEOUT_SEC);
            }
          }
        }
        // 전사 프로젝트, 연구과제, 감사, Chairman도 여기서 acl등록
        idf_Group.addUser(dto.getUserId());
        idf_Group.save();
      }
    } catch (Exception e) {
      throw e;
    } finally {
      idfAdminSession.disconnect();
      sessionRelease(userSession.getUser().getUserId(), idf_Sess);
    }
  }

  @Override
  public void deleteRoleAuthUser(RoleAuthDto dto, UserSession userSession) throws Exception {
    IDfSession idf_Sess = this.getIdfSession(userSession);
    IDfSession idfAdminSession = DCTMUtils.getAdminSession(); // Superuser
    UserSession userSess = (UserSession) redisRepository.getObject(dto.getUserId(), UserSession.class);
    try {
      IDfGroup idf_Group = (IDfGroup) idfAdminSession.getGroup(dto.getGroupName());

      // 그룹문서관리자
      if (idf_Group == null) {
        if (dto.getGroupName().equals("g_ent_mgr")) {

          // 그룹, 전사 둘다 관리자인경우 그룹관리자 삭제시 edms_mgr에서 그룹관리자만 삭제
          List<RoleAuth> DeleteMgrUserList = roleAuthDao.selectDeleteMgrUser("C", dto.getUserId());

          // 동시에 전사관리자 mgr에 속해있을경우
          if (DeleteMgrUserList.size() > 0) {
            // 사별 dm_group acl list(선택한 사용자가 전사문서관리자인 경우 해당 전사 acl 제외한 list)
            List<RoleAuth> companyAclNameList = roleAuthDao
                .selectCompanyAclName(DeleteMgrUserList.get(0).getUComCode());

            // 전사관리자 dm_group에서 각각 삭제
            for (int i = 0; i < companyAclNameList.size(); i++) {
              IDfGroup idf_Group_Company = (IDfGroup) idfAdminSession
                  .getGroup(companyAclNameList.get(i).getGroupName());
              IDfGroup idf_Group_Company2 = (IDfGroup) idfAdminSession
                  .getGroup(companyAclNameList.get(i).getGroupName2());
              idf_Group_Company.removeUser(dto.getUserId());
              idf_Group_Company2.removeUser(dto.getUserId());
              idf_Group_Company.save();
              idf_Group_Company2.save();
            }
          } else {
            // 그룹관리자에만 속해있는 경우
            // 사별 dm_group acl list(선택한 사용자가 전사문서관리자에 속해있지 않은경우)
            List<RoleAuth> companyAclNameList = roleAuthDao.selectCompanyAclName(null);

            // 전사관리자 dm_group에서 각각 삭제
            for (int i = 0; i < companyAclNameList.size(); i++) {
              IDfGroup idf_Group_Company = (IDfGroup) idfAdminSession
                  .getGroup(companyAclNameList.get(i).getGroupName());
              IDfGroup idf_Group_Company2 = (IDfGroup) idfAdminSession
                  .getGroup(companyAclNameList.get(i).getGroupName2());
              idf_Group_Company.removeUser(dto.getUserId());
              idf_Group_Company2.removeUser(dto.getUserId());
              idf_Group_Company.save();
              idf_Group_Company2.save();
            }
          }
          // 그룹관리자 mgr 데이터 삭제
          IDfPersistentObject idf_PObj = idf_Sess.getObject(new DfId(dto.getRObjectId()));
          idf_PObj.destroy();

          // redis 세션값 수정          
          if (userSess != null) {
            if (userSess.getUser().getMgr().getGroupComCode() != null) {
              userSess.getUser().getMgr().setGroupComCode(null);
              redisRepository.put(Commons.SESSION_PREFIX + dto.getUserId(), userSess, Commons.REDIS_TIMEOUT_SEC);
              redisRepository.put(Commons.LOGOUT_PREFIX + userSess.getToken(), "LOGOUT TOKEN", Commons.REDIS_TIMEOUT_SEC);
            }
          }
        }
      } else {
        // 전사문서관리자
        if ((dto.getUGroupScope().equals("C") && dto.getUConfigFlag().equals("1"))) {
          List<RoleAuth> DeleteMgrUser = roleAuthDao.selectCompanyMgrUsers(dto.getUserId());

          // mgr에서 삭제(한번만)
          if (DeleteMgrUser.size() != 0) {
            IDfPersistentObject idf_PObj = idf_Sess.getObject(new DfId(DeleteMgrUser.get(0).getRObjectId()));
            idf_PObj.destroy();

            // redis 세션 값 변경
            if (userSess != null) {
              if (userSess.getUser().getMgr().getCompanyComCode() != null) {
                userSess.getUser().getMgr().setCompanyComCode(null);
                redisRepository.put(Commons.SESSION_PREFIX + dto.getUserId(), userSess, Commons.REDIS_TIMEOUT_SEC);
                redisRepository.put(Commons.LOGOUT_PREFIX + userSess.getToken(), "LOGOUT TOKEN", Commons.REDIS_TIMEOUT_SEC);
              }
            }
          }
          // 그룹문서관리자인지 조회
          List<RoleAuth> GroupMgrUser = roleAuthDao.selectDeleteMgrUser("G", dto.getUserId());

          // 삭제하려는 대상이 그룹문서관리자 아닐경우 전사 dm_group acl 삭제
          if (GroupMgrUser.size() == 0) {
            idf_Group.removeUser(dto.getUserId());
            idf_Group.save();
          }
        } else {
          // 전사 프로젝트, 연구과제 / 감사 / Chairman
          idf_Group.removeUser(dto.getUserId());
          idf_Group.save();
        }
      }
    } catch (Exception e) {
      throw e;
    } finally {
      idfAdminSession.disconnect();
      sessionRelease(userSession.getUser().getUserId(), idf_Sess);
    }
  }
}
