package com.dongkuksystems.dbox.services.manager.hisviewuser;

import java.util.List;

import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.daos.type.manager.hisviewuser.HisViewUserDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.hisviewuser.HisViewUserDto;
import com.dongkuksystems.dbox.models.dto.type.manager.hisviewuser.HisViewUserFilterDto;
import com.dongkuksystems.dbox.models.type.manager.hisviewuser.HisViewUser;
import com.dongkuksystems.dbox.services.AbstractCommonService;

@Service
public class HisViewUserServiceImpl extends AbstractCommonService implements HisViewUserService {

	private final HisViewUserDao hisViewUserDao;

	public HisViewUserServiceImpl(HisViewUserDao hisViewUserDao) {
		this.hisViewUserDao = hisViewUserDao;
	}

	@Override
	public List<HisViewUser> selectAll(HisViewUserFilterDto dto) {
		// TODO Auto-generated method stub
		return hisViewUserDao.selectAll(dto);
	}
	
	@Override
	public void registHisViewUser(UserSession userSession, HisViewUserDto dto) throws Exception {
		IDfSession idfSession = this.getIdfSession(userSession);
		
		try {	
			IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_his_view_user");
			
			idf_PObj.setString("u_his_code", dto.getUHisCode());
			idf_PObj.setString("u_com_code", dto.getUComCode());
			idf_PObj.setString("u_user_id", dto.getUUserId());
			idf_PObj.setString("u_create_user", idfSession.getLoginUserName());
			idf_PObj.setString("u_create_date", (new DfTime()).toString());
			
			idf_PObj.save();  
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
		  if (idfSession != null  && idfSession.isConnected()) {
		    sessionRelease(userSession.getUser().getUserId(), idfSession);
	    }
		}
		
	}

	@Override
	public void deleteHisViewUser(UserSession userSession, String rObjectId) throws Exception {
		IDfSession idfSession = this.getIdfSession(userSession);
		try {	
			IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId));
			idf_PObj.destroy();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
		  if (idfSession != null  && idfSession.isConnected()) {
		    sessionRelease(userSession.getUser().getUserId(), idfSession);
	     }
		}
	}

	
	
	
}
