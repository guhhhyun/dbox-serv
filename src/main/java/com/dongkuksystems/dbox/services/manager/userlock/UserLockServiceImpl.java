package com.dongkuksystems.dbox.services.manager.userlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.Commons;
import com.dongkuksystems.dbox.daos.table.etc.gwdept.GwDeptDao;
import com.dongkuksystems.dbox.daos.type.manager.deptinformconfig.DeptInformConfigDao;
import com.dongkuksystems.dbox.daos.type.manager.userlock.UserLockDao;
import com.dongkuksystems.dbox.daos.type.user.UserDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.code.CodeDetailDto;
import com.dongkuksystems.dbox.models.dto.type.manager.deptinformconfig.DeptInformConfigDto;
import com.dongkuksystems.dbox.models.dto.type.user.LockUserDto;
import com.dongkuksystems.dbox.models.dto.type.user.UserFilterDto;
import com.dongkuksystems.dbox.models.dto.type.user.UserLockFilterDto;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.type.manager.deptinformconfig.DeptInformConfig;
import com.dongkuksystems.dbox.models.type.user.User;
import com.dongkuksystems.dbox.models.type.user.UserLock;
import com.dongkuksystems.dbox.repositories.redis.RedisRepository;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class UserLockServiceImpl extends AbstractCommonService implements UserLockService {

	private final UserLockDao userLockDao;
	private final UserDao userDao;
	private final DeptInformConfigDao deptInformConfigDao;
	private final GwDeptDao gwDeptDao;
  private final RedisRepository redisRepository;

	public UserLockServiceImpl(UserLockDao userLockDao, UserDao userDao, DeptInformConfigDao deptInformConfigDao, GwDeptDao gwDeptDao, RedisRepository redisRepository) {
		this.userLockDao = userLockDao;
		this.userDao = userDao;
		this.deptInformConfigDao = deptInformConfigDao;
		this.gwDeptDao = gwDeptDao;
		this.redisRepository = redisRepository;
	}


	@Override
	public List<UserLock> selectUserLocks(UserLockFilterDto dto) {
		// TODO Auto-generated method stub
		return userLockDao.selectAll(dto);
	}


	@Override
	public void patchUserLock(String rObjectId, String userObjectId, UserSession userSession, LockUserDto dto) throws Exception {
		IDfSession idfSession = this.getIdfSession(userSession);
		IDfSession idfAdminSession = DCTMUtils.getAdminSession(); // Superuser

		IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId));
		IDfPersistentObject idf_U_PObj = idfAdminSession.getObject(new DfId(userObjectId));

		String userId = null;
		String user_state = "0";
		String autoType = "Y"; //수동

		try {		
		  userId = idf_PObj.getString("u_user_id");
			if(dto.getULockStatus().equals("L")) { //관리자 잠금
				/*
				 * 잠금처리
				 */
				user_state = "1";
				idf_PObj.setString("u_lock_type", dto.getULockType());
				idf_PObj.setString("u_auto_yn", autoType);
				idf_PObj.setString("u_lock_status", dto.getULockStatus());
				idf_PObj.setString("u_desig_setter", idfSession.getLoginUserName());
				idf_PObj.setString("u_desig_date", (new DfTime()).toString());		
				//TODO: dooyeon.yoo 추가  잠금 사용자 redis추가
				redisRepository.put(Commons.LOCK_PREFIX + userId, "", null);
			} else { 
				/*
				 * 해제
				 */
				idf_PObj.setString("u_lock_status", dto.getULockStatus());
				idf_PObj.setString("u_undesig_reason", dto.getUUndesigReason());
				idf_PObj.setString("u_undesig_setter", idfSession.getLoginUserName());
				idf_PObj.setString("u_undesig_date", (new DfTime()).toString());		

        //TODO: dooyeon.yoo 추가 잠금 사용자 redis 제거
				redisRepository.delete(Commons.LOCK_PREFIX + userId);
			}
			
			idf_PObj.save();  
			    
			idf_U_PObj.setString("user_state", user_state);
			idf_U_PObj.save();  
		} catch(Exception e) {
		  if (userId != null) redisRepository.delete(Commons.LOCK_PREFIX + userId);
			e.printStackTrace();
		} finally {
		  if (idfSession != null  && idfSession.isConnected() && idfAdminSession != null  && idfAdminSession.isConnected()) {
			  sessionRelease(userSession.getUser().getUserId(), idfSession);
			  idfAdminSession.disconnect();
	     }
		}
		
	}
	
	@Override
	public void registUserLock(UserSession userSession, LockUserDto dto) throws Exception {
		IDfSession idfSession = this.getIdfSession(userSession);
		IDfSession idfAdminSession = DCTMUtils.getAdminSession(); // Superuser

		IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_user_lock");

		String user_state = "1"; //잠금
		String autoType = "M"; //자동
		try {	
			idf_PObj.setString("u_user_id", dto.getUUserId());
			idf_PObj.setString("u_lock_type", dto.getULockType());
			idf_PObj.setString("u_auto_yn", autoType);
			idf_PObj.setString("u_lock_status", dto.getULockStatus());
			idf_PObj.setString("u_desig_reason", dto.getUDeigReason());
			idf_PObj.setString("u_desig_setter", idfSession.getLoginUserName());
			idf_PObj.setString("u_desig_date", dto.getUDesigDate());	
			
			idf_PObj.save();  
			
			UserFilterDto userFilterDto = new UserFilterDto();
			userFilterDto.setUserName(dto.getUUserId());
			List<User> rst = userDao.selectAll(userFilterDto);
			
			String rObjectId = rst.get(0).getRObjectId();
			
			IDfPersistentObject idf_U_PObj = idfAdminSession.getObject(new DfId(rObjectId));
			
			idf_U_PObj.setString("user_state", user_state);
			idf_U_PObj.save();  
      //TODO: dooyeon.yoo 추가 잠금 사용자 redis 추가
      redisRepository.put(Commons.LOCK_PREFIX + dto.getUUserId(), "", null);
		} catch(Exception e) {
      redisRepository.delete(Commons.LOCK_PREFIX + dto.getUUserId());
			e.printStackTrace();
		} finally {
		  if (idfSession != null  && idfSession.isConnected() && idfAdminSession != null  && idfAdminSession.isConnected()) {
		    sessionRelease(userSession.getUser().getUserId(), idfSession);
			  idfAdminSession.disconnect();
      }
		}
		
	}
	

	@Override
	public Optional<DeptInformConfig> selectListByOrgId(String uComCode, String uDeptCode) {
		// TODO Auto-generated method stub
		return deptInformConfigDao.selectListByOrgId(uComCode, uDeptCode);
	}


	
	@Override
	public void patchCode(String rObjectId, UserSession userSession, CodeDetailDto dto) throws Exception {
		IDfSession idfSession = this.getIdfSession(userSession);
		IDfSession idfAdminSession = DCTMUtils.getAdminSession(); // Superuser

		IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId));

		try {		
			idf_PObj.setString("u_code_val2", dto.getUCodeVal2());
			idf_PObj.setString("u_code_val3", dto.getUCodeVal3());
			idf_PObj.setString("u_code_name1", dto.getUCodeName1());
			idf_PObj.setString("u_code_name2", dto.getUCodeName2());
			
			idf_PObj.setString("u_update_date", (new DfTime()).toString());		
			
			idf_PObj.save();  

		} catch(Exception e) {
			e.printStackTrace();
		} finally {
		  if (idfSession != null  && idfSession.isConnected() && idfAdminSession != null  && idfAdminSession.isConnected()) {
		    sessionRelease(userSession.getUser().getUserId(), idfSession);
			  idfAdminSession.disconnect();
	      }
		}
		
	}
	
	
	@Override
	public void patchDeptInform(UserSession userSession, DeptInformConfigDto dto) throws Exception {
		Optional<DeptInformConfig> rst = deptInformConfigDao.selectList(dto.getUDeptCode());
		
		DeptInformConfig rs = rst.orElse(null);
		
		if(rs != null) {
			String rObjectId = rs.getRObjectId();
		
			IDfSession idfSession = this.getIdfSession(userSession);
			IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId));
	
			try {		
				idf_PObj.setString("u_count_download", dto.getUCountDownload());
				idf_PObj.setString("u_count_takeout", dto.getUCountTakeout());
				idf_PObj.setString("u_count_req_permit", dto.getUCountReqPermit());
				idf_PObj.setString("u_count_print", dto.getUCountPrint());
				idf_PObj.setString("u_count_delete", dto.getUCountDelete());
				idf_PObj.setString("u_modify_user", idfSession.getLoginUserName());
				idf_PObj.setString("u_modify_date", (new DfTime()).toString());
				
				idf_PObj.save();  
	
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
			  if (idfSession != null  && idfSession.isConnected()) {
			    sessionRelease(userSession.getUser().getUserId(), idfSession);
		    }
			}
		
		}else{
			if(dto.getType().equals("D"))
			{
				IDfSession idfSession = this.getIdfSession(userSession);
				IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_dept_inform_config");
				
				try {	
					idf_PObj.setString("u_com_code", dto.getUComCode());
					idf_PObj.setString("u_dept_code", dto.getUDeptCode());
					idf_PObj.setString("u_count_download", dto.getUCountDownload());
					idf_PObj.setString("u_count_takeout", dto.getUCountTakeout());
					idf_PObj.setString("u_count_req_permit", dto.getUCountReqPermit());
					idf_PObj.setString("u_count_print", dto.getUCountPrint());
					idf_PObj.setString("u_count_delete", dto.getUCountDelete());
					idf_PObj.setString("u_create_date", (new DfTime()).toString());
					idf_PObj.setString("u_create_user", idfSession.getLoginUserName());
					

					idf_PObj.save();  
					
				} catch(Exception e) {
					e.printStackTrace();
				} finally {
				  if (idfSession != null  && idfSession.isConnected() ) {
				    sessionRelease(userSession.getUser().getUserId(), idfSession);
			    }
				}
								
			}
		}
	}
	
	
	
	@Override
	public List<DeptInformConfigDto> selectListDept(String orgId) {
		List<DeptInformConfigDto> result = new ArrayList<>();
		List<VDept> list = gwDeptDao.selectDeptChildrenByOrgId(orgId);
		
		for (VDept dept : list) {
			Optional<DeptInformConfig> deptInformConfig = deptInformConfigDao.selectListByOrgId(orgId, dept.getOrgId());
		      DeptInformConfigDto tmp = DeptInformConfigDto.builder()
		    		  .deptName(dept.getOrgNm())
		    		  .uCountDownload(deptInformConfig.get().getUCountDownload())
		    		  .uCountTakeout(deptInformConfig.get().getUCountTakeout())
		    		  .uCountReqPermit(deptInformConfig.get().getUCountReqPermit())
		    		  .uCountPrint(deptInformConfig.get().getUCountPrint())
		    		  .uCountDelete(deptInformConfig.get().getUCountDelete())
		    		  .build();
		      result.add(tmp);
		 }
		
		return result;
	}

	
	
	
	
	
}
