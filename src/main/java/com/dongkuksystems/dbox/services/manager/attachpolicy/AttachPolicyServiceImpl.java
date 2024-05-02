package com.dongkuksystems.dbox.services.manager.attachpolicy;

import java.util.List;

import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfUser;
import com.documentum.fc.common.DfId;
import com.dongkuksystems.dbox.daos.type.manager.attachpolicy.AttachPolicyDao;
import com.dongkuksystems.dbox.daos.type.manager.attachpolicyuser.AttachPolicyUserDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.CreateAttachPolicyDto;
import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.CreateAttachPolicyUserDto;
import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.PatchAttachPolicyDto;
import com.dongkuksystems.dbox.models.type.manager.AttachPolicy;
import com.dongkuksystems.dbox.models.type.manager.AttachPolicyUser;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class AttachPolicyServiceImpl extends AbstractCommonService implements AttachPolicyService {

	private final AttachPolicyDao attachPolicyDao;
	private final AttachPolicyUserDao attachPolicyUserDao;

	public AttachPolicyServiceImpl(AttachPolicyDao attachPolicyDao, AttachPolicyUserDao attachPolicyUserDao) {		
		this.attachPolicyDao = attachPolicyDao;
		this.attachPolicyUserDao = attachPolicyUserDao;
	}

	@Override
	public List<AttachPolicy> selectAll() {		
		return attachPolicyDao.selectAll();
	}
	
	@Override
	public String patchAttachPolicy(String rObjectId, UserSession userSession, PatchAttachPolicyDto dto) throws Exception {		
		IDfSession idfSession = null;		
		try {
		  idfSession = this.getIdfSession(userSession);
      String s_ObjId = rObjectId;
      IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));
      idf_PObj.setBoolean("u_external_flag", dto.isUExternalFlag());
      idf_PObj.setString("u_system_name", dto.getUSystemName());
      idf_PObj.setString("u_system_key1", dto.getUSystemKey1());      
      idf_PObj.setBoolean("u_messenger_flag", dto.isUMessengerFlag());
      idf_PObj.setString("u_attach_type", dto.getUAttachType());
      idf_PObj.setString("u_limit_sec_level", dto.getULimitSecLevel());
      idf_PObj.setString("u_doc_status", dto.getUDocStatus());
      idf_PObj.setBoolean("u_inactive_flag", dto.isUInactiveFlag());
      idf_PObj.setBoolean("u_for_user_flag", dto.isUForUserFlag());
      idf_PObj.setBoolean("u_drm_flag", dto.isUDrmFlag());
      idf_PObj.setBoolean("u_doc_complete", dto.isUDocComplete());
      idf_PObj.save();
    } catch (Exception e) {      
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
		return rObjectId;
  } 			
	
	@Override
	public String createAttachPolicy(CreateAttachPolicyDto dto, UserSession userSession) throws Exception {
    IDfSession idfSession = null;   
    try {
      idfSession = this.getIdfSession(userSession);      
      IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_attach_policy");
      idf_PObj.setString("u_system_name", dto.getUSystemName());
      idf_PObj.setString("u_system_key1", dto.getUSystemKey1());      
      idf_PObj.setString("u_attach_type", dto.getUAttachType());
      idf_PObj.setString("u_limit_sec_level", dto.getULimitSecLevel());
      idf_PObj.setString("u_doc_status", dto.getUDocStatus());
      idf_PObj.setBoolean("u_inactive_flag", dto.isUInactiveFlag());
      idf_PObj.setBoolean("u_external_flag", dto.isUExternalFlag());
      idf_PObj.setBoolean("u_messenger_flag", dto.isUMessengerFlag());
      idf_PObj.setBoolean("u_for_user_flag", dto.isUForUserFlag());
      idf_PObj.setBoolean("u_drm_flag", dto.isUDrmFlag());
      idf_PObj.setBoolean("u_doc_complete", dto.isUDocComplete());
      idf_PObj.save();
    } catch (Exception e) {      
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return userSession.getUser().getUserId();
  }       
	
	@Override
	public String deleteAttachPolicy(String rObjectId, UserSession userSession) throws Exception {
    IDfSession idfSession = null;   
    List<AttachPolicyUser> userList = attachPolicyUserDao.selectDeletePolicyUser(rObjectId);
		try {
      idfSession = this.getIdfSession(userSession);      
			String s_ObjId = rObjectId;
			IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));
			idf_PObj.destroy();						
			
			//attach_policy_user 데이터 삭제
			for(int i=0; i<userList.size(); i++) {			 
			  String s_ObjId2 = userList.get(i).getRObjectId();
			  IDfPersistentObject idf_PObj2 = idfSession.getObject(new DfId(s_ObjId2));
	      idf_PObj2.destroy();
			}
		} catch (Exception e) {			
			throw e;
		} finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
		return rObjectId;
	}	
}
