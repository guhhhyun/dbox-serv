package com.dongkuksystems.dbox.services.userpreset;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.daos.table.etc.gwdept.GwDeptDao;
import com.dongkuksystems.dbox.daos.type.code.CodeDao;
import com.dongkuksystems.dbox.daos.type.user.preset.UserPresetDao;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.code.CodeFilterDto;
import com.dongkuksystems.dbox.models.dto.type.user.UserPresetDetailDto;
import com.dongkuksystems.dbox.models.dto.type.user.UserPresetFilterDto;
import com.dongkuksystems.dbox.models.dto.type.user.UserPresetRepeatingDto;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.type.auth.AuthBase;
import com.dongkuksystems.dbox.models.type.code.Code;
import com.dongkuksystems.dbox.models.type.user.UserPreset;
import com.dongkuksystems.dbox.models.type.user.UserPresetRepeating;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.code.CodeService;

@Service
public class UserPresetServiceImpl extends AbstractCommonService implements UserPresetService {

	private final UserPresetDao userPresetDao;
	private final CodeDao codeDao;
	private final GwDeptDao gwDeptDao;
	private final CodeService codeService;

	public UserPresetServiceImpl(UserPresetDao userPresetDao, CodeDao codeDao, GwDeptDao gwDeptDao, CodeService codeService) {
		this.codeDao = codeDao;
		this.userPresetDao = userPresetDao;
		this.gwDeptDao = gwDeptDao;
		this.codeService = codeService;
	}


	@Override
	public Optional<UserPreset> selectOneByUserId(UserSession userSession) {
		return userPresetDao.selectOneByUserId(userSession.getUser().getUserId());
	}


  @Override
	public List<UserPreset> selectList(UserSession userSession) throws Exception {
		// TODO Auto-generated method stub
		UserPresetFilterDto userPresetFilterDto = new UserPresetFilterDto();
		userPresetFilterDto.setUUserId(userSession.getUser().getUserId());
		List<UserPreset> result = userPresetDao.selectList(userPresetFilterDto);
		
		CodeFilterDto codeFilterDto = new CodeFilterDto();
		
		//보안등급
		codeFilterDto.setUCodeType("SEC_LEVEL");
		
		result.forEach(item -> {
			codeFilterDto.setUCodeVal1(item.getUSecLevel());
			List<Code> codeList = codeDao.selectList(codeFilterDto);
			item.setLevelName(codeList.get(0).getUCodeName1());
			
	
		});
		return result;
	}

	@Override
	public List<UserPreset> selectDetailList(UserSession userSession) {
		final String currentUserId = userSession.getUser().getUserId();
		
		// 현재 사용자에 대한 UserPreset 조회
		UserPresetFilterDto userPresetFilterDto = UserPresetFilterDto.builder()
				.uUserId(currentUserId)
				.uSecBaseFlag(true)
				.build();
		List<UserPreset> userPresetList = userPresetDao.selectList(userPresetFilterDto);
		
		// UserPreset의 Repeating타입 조회
		List<UserPresetRepeating> userPresetRepeatingList = userPresetDao.selectRepeatingDetailList(userPresetFilterDto);
		Map<String, List<UserPresetRepeating>> userPresetRepeatingGroupByMap = userPresetRepeatingList.stream()
				.collect(Collectors.groupingBy(((item) -> item.getRObjectId())));
		
		for (UserPreset userPreset : userPresetList) {
			List<UserPresetRepeating> userPresetRepeatingListFromMap = userPresetRepeatingGroupByMap.get(userPreset.getRObjectId());
			
			if (userPresetRepeatingListFromMap != null) {
				// Live 조회/다운로드 권한 세팅
				List<AuthBase> liveReadAuthorList = userPresetRepeatingListFromMap.stream()
						.filter(item -> item.getULiveReadAuthor() != null)
						.map((item) -> AuthBase.builder()
								.uPermitType(GrantedLevels.READ.getLabel())
								.user(item.getLiveReadUserDetail())
								.dept(item.getLiveReadDeptDetail())
								.build())
						.collect(Collectors.toList());
				userPreset.setULiveReadAuthorList(liveReadAuthorList);
				
				// Live 편집/삭제 권한 세팅
				List<AuthBase> liveDeleteAuthorList = userPresetRepeatingListFromMap.stream()
						.filter(item -> item.getULiveDeleteAuthor() != null)
						.map((item) -> AuthBase.builder()
								.uPermitType(GrantedLevels.DELETE.getLabel())
								.user(item.getLiveDeleteUserDetail())
								.dept(item.getLiveDeleteDeptDetail())
								.build())
						.collect(Collectors.toList());
				userPreset.setULiveDeleteAuthorList(liveDeleteAuthorList);
				
				// Closed 조회/다운로드 권한 세팅
				List<AuthBase> closedReadAuthorList = userPresetRepeatingListFromMap.stream()
						.filter(item -> item.getUClosedReadAuthor() != null)
						.map((item) -> AuthBase.builder()
								.uPermitType(GrantedLevels.READ.getLabel())
								.user(item.getClosedReadUserDetail())
								.dept(item.getClosedReadDeptDetail())
								.build())
						.collect(Collectors.toList());
				userPreset.setUClosedReadAuthorList(closedReadAuthorList);
			}
		}
		
		return userPresetList;
	}

	@Override
	public List<UserPreset> selectAllList(String rObjectId) throws Exception {
		UserPresetFilterDto userPresetFilterDto = new UserPresetFilterDto();
		userPresetFilterDto.setRObjectId(rObjectId);
		List<UserPreset> result = userPresetDao.selectList(userPresetFilterDto);
		List<UserPresetRepeating> detail = userPresetDao.selectDetail(rObjectId);

		CodeFilterDto codeFilterDto = new CodeFilterDto();
		//보안등급
		codeFilterDto.setUCodeType("SEC_LEVEL");
		
		result.forEach(item -> {
			codeFilterDto.setUCodeVal1(item.getUSecLevel());
			List<Code> codeList = codeDao.selectList(codeFilterDto);
			item.setLevelName(codeList.get(0).getUCodeName1());	
		});

		result.get(0).setUserPresetRepeatings(detail);

		return result;
	}
	
	
	
	@Override
	public int getUserPresetDetailCount(String rObjectId, UserPresetRepeatingDto dto) throws Exception {
		int count = userPresetDao.selectUserPresetCount(rObjectId, dto);
		return count;
	}


	@Override
	public void patchUserPreset(String rObjectId, UserSession userSession, UserPresetDetailDto dto) throws Exception {
		IDfSession idfSession = this.getIdfSession(userSession);
		IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId));
		String mode = "";
		if(dto.getUConfigName() != null) {
			 mode = "D";
		}
		
		try {
			if(mode.equals("D")) {
								
					idf_PObj.setString("u_config_name", dto.getUConfigName());
					//idf_PObj.setBoolean("u_open_flag", dto.getUOpenFlag().equals("0") ? true : false);
					idf_PObj.setInt("u_preserve_flag", dto.getUPreserveFlag());
					idf_PObj.setString("u_sec_level", dto.getUSecLevel());
					idf_PObj.setString("u_pc_reg_flag", dto.getUPcRegFlag());
					idf_PObj.setString("u_copy_flag", dto.getUCopyFlag());
					idf_PObj.setString("u_edit_save_flag", dto.getUEditSaveFlag());
					idf_PObj.setString("u_mail_permit_flag", dto.getUMailPermitFlag());
					idf_PObj.setString("u_modify_user", idfSession.getLoginUserName());
					idf_PObj.setString("u_modify_date", (new DfTime()).toString());	
					
					//repeating 삭제
					idf_PObj.truncate("u_live_read_author", 0);
					idf_PObj.truncate("u_live_delete_author", 0);
					idf_PObj.truncate("u_closed_read_author", 0);
					
					//idf_PObj.save();
					
					//repeating insert
					int i = 0;
					Map<String, String> comCodeMap = codeService.getComCodeMap();
					
					for (UserPresetRepeatingDto rp : dto.getLiveRead()) {
					  String groupType ="G";
		        
		        for (String key : comCodeMap.keySet()) {
		           if(key.equals(rp.getId())) {
		             groupType= "C"; 
		           }
		        }
		        
		        if(rp.getId().substring(0,2).equals("g_")) {
		          groupType= "P"; 
		        }
		        
		            
		        if(rp.getType().equals("D")) {
		          String deptCode = "";
		          if(groupType.equals("C")) {
		            deptCode = "g_" + rp.getId().toLowerCase();
		          }else if(groupType.equals("P")){
                deptCode = rp.getId();
		          }else {
		            VDept vDept = gwDeptDao.selectOneByOrgId(rp.getId()).orElseThrow(() -> new NotFoundException("There is no such dept"));
		            deptCode = "g_" + vDept.getUCabinetCode();
		          }
		          idf_PObj.insertString("u_live_read_author", i, deptCode);
		        } else {
							idf_PObj.insertString("u_live_read_author", i, rp.getId());
						}
						i++;
					}
					
					for (UserPresetRepeatingDto rp : dto.getLiveDelete()) {
					  String groupType ="G";
            
            for (String key : comCodeMap.keySet()) {
               if( key.equals(rp.getId())) {
                 groupType= "C"; 
               }
            }
            
            if(rp.getId().substring(0,2).equals("g_")) {
              groupType= "P"; 
            }
            
            if(rp.getType().equals("D")) {
              String deptCode = "";
              if(groupType.equals("C")) {
                deptCode = "g_" + rp.getId().toLowerCase();
              }else if(groupType.equals("P")){
                deptCode = rp.getId();
              }else {
                VDept vDept = gwDeptDao.selectOneByOrgId(rp.getId()).orElseThrow(() -> new NotFoundException("There is no such dept"));
                deptCode = "g_" + vDept.getUCabinetCode();
              }
              idf_PObj.insertString("u_live_delete_author", i, deptCode);
            } else {
							idf_PObj.insertString("u_live_delete_author", i, rp.getId());
						}
						i++;
					}
					
					for (UserPresetRepeatingDto rp : dto.getClosedRead()) {
					  String groupType ="G";
            
            for (String key : comCodeMap.keySet()) {
               if( key.equals(rp.getId())) {
                 groupType= "C"; 
               }
            }
            
            if(rp.getId().substring(0,2).equals("g_")) {
              groupType= "P"; 
            }
            
            if(rp.getType().equals("D")) {
              String deptCode = "";
              if(groupType.equals("C")) {
                deptCode = "g_" + rp.getId().toLowerCase();
              }else if(groupType.equals("P")){
                deptCode = rp.getId();
              }else {
                VDept vDept = gwDeptDao.selectOneByOrgId(rp.getId()).orElseThrow(() -> new NotFoundException("There is no such dept"));
                deptCode = "g_" + vDept.getUCabinetCode();
              }
              idf_PObj.insertString("u_closed_read_author", i, deptCode);
            } else {
							idf_PObj.insertString("u_closed_read_author", i, rp.getId());
						}
						i++;
					}
				} else {
				  //등록시 기본값
					//idf_PObj.setBoolean("u_reg_base_flag", dto.getURegBaseFlag().equals("1") ? true : false);
					idf_PObj.setBoolean("u_sec_base_flag", dto.getUSecBaseFlag().equals("1") ? true : false);
				}
				
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
	public String createUserPreset(UserPresetDetailDto dto, UserSession userSession) throws Exception {
		IDfSession idfSession = this.getIdfSession(userSession);
		IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_user_preset");
		
		//사용자 설정
		String type = "C";
	
		Map<String, String> comCodeMap = codeService.getComCodeMap();
		
		try {
			idf_PObj.setString("u_user_id", idfSession.getLoginUserName());
			idf_PObj.setString("u_config_name", dto.getUConfigName());
			idf_PObj.setString("u_config_type", type);
			//idf_PObj.setBoolean("u_open_flag", dto.getUOpenFlag() == "0" ? true : false);
			idf_PObj.setInt("u_preserve_flag", dto.getUPreserveFlag());
			idf_PObj.setString("u_sec_level", dto.getUSecLevel());
			idf_PObj.setString("u_pc_reg_flag", dto.getUPcRegFlag());
			idf_PObj.setString("u_copy_flag", dto.getUCopyFlag());
			idf_PObj.setString("u_edit_save_flag", dto.getUEditSaveFlag());
			idf_PObj.setString("u_mail_permit_flag", dto.getUMailPermitFlag());
			idf_PObj.setString("u_create_user", idfSession.getLoginUserName());
			idf_PObj.setString("u_create_date", (new DfTime()).toString());	
						
			int i = 0;
						
			for (UserPresetRepeatingDto rp : dto.getLiveRead()) {
        String groupType ="G";
        
        for (String key : comCodeMap.keySet()) {
           if( key.equals(rp.getId())) {
             groupType= "C"; 
           }
        }
        
			  if(rp.getType().equals("D")) {
			    String deptCode = "";
			    if(groupType.equals("C")) {
			      deptCode = "g_" + rp.getId().toLowerCase();
			    }else {
			      VDept vDept = gwDeptDao.selectOneByOrgId(rp.getId()).orElseThrow(() -> new NotFoundException("There is no such dept"));
			      deptCode = "g_" + vDept.getUCabinetCode();
			    }
					idf_PObj.insertString("u_live_read_author", i, deptCode);
				}	else {
					idf_PObj.insertString("u_live_read_author", i, rp.getId());
				}
				i++;
			}
			
			for (UserPresetRepeatingDto rp : dto.getLiveDelete()) {
			  String groupType ="G";
        
        for (String key : comCodeMap.keySet()) {
           if( key.equals(rp.getId())) {
             groupType= "C"; 
           }
        }
        
        if(rp.getType().equals("D")) {
          String deptCode = "";
          if(groupType.equals("C")) {
            deptCode = "g_" + rp.getId().toLowerCase();
          }else {
            VDept vDept = gwDeptDao.selectOneByOrgId(rp.getId()).orElseThrow(() -> new NotFoundException("There is no such dept"));
            deptCode = "g_" + vDept.getUCabinetCode();
          }
          idf_PObj.insertString("u_live_delete_author", i, deptCode);
        } else {
					idf_PObj.insertString("u_live_delete_author", i, rp.getId());
				}
				i++;
			}
			
			for (UserPresetRepeatingDto rp : dto.getClosedRead()) {
			  String groupType ="G";
        
        for (String key : comCodeMap.keySet()) {
           if( key.equals(rp.getId())) {
             groupType= "C"; 
           }
        }
        
        if(rp.getType().equals("D")) {
          String deptCode = "";
          if(groupType.equals("C")) {
            deptCode = "g_" + rp.getId().toLowerCase();
          }else {
            VDept vDept = gwDeptDao.selectOneByOrgId(rp.getId()).orElseThrow(() -> new NotFoundException("There is no such dept"));
            deptCode = "g_" + vDept.getUCabinetCode();
          }
          idf_PObj.insertString("u_closed_read_author", i, deptCode);
        } else {
					idf_PObj.insertString("u_closed_read_author", i, rp.getId());
				}
				i++;
			}
			
			idf_PObj.save();
		
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
		  if (idfSession != null  && idfSession.isConnected()) {
			  sessionRelease(userSession.getUser().getUserId(), idfSession);
	    }
		}
		
		
		return null;
	}


  @Override
  public void deleteUserPreset(String rObjectId, UserSession userSession) throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);
    try {
      IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId));
      idf_PObj.destroy();
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    
  }
	


	
	
	
	
	
}
