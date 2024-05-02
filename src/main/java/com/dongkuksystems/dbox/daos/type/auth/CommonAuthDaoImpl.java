package com.dongkuksystems.dbox.daos.type.auth;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.etc.DrmAuthorDto;
import com.dongkuksystems.dbox.models.dto.etc.DrmUserDto;
import com.dongkuksystems.dbox.models.dto.type.auth.AuthBaseGroupMembersDto;
import com.dongkuksystems.dbox.models.dto.type.auth.FolderAuthResult;
import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult; 

@Primary
@Repository
public class CommonAuthDaoImpl implements CommonAuthDao {
  private CommonAuthMapper commonAuthMapper;

  public CommonAuthDaoImpl(CommonAuthMapper commonAuthMapper) {
    this.commonAuthMapper = commonAuthMapper;
  }

  @Override
  public List<FolderAuthResult> selectUserAuthOnFolder(String objectId, String userId) {
    return commonAuthMapper.selectUserAuthOnFolder(objectId, userId);
  }

  @Override
  public List<String> selectHamDefaultAuths(String userId) {
    return commonAuthMapper.selectHamDefaultAuths(userId);
  }

  @Override
  public Optional<HamInfoResult> selectHamInfo(String hamId) {
    return commonAuthMapper.selectHamInfo(hamId);
  }

  @Override
  public Optional<HamInfoResult> selectDeptHamInfo(String hamId) {
    return commonAuthMapper.selectDeptHamInfo(hamId);
  }
  
  @Override
  public boolean checkUserInDepts(String userId, List<String> deptCodes) {
  	return commonAuthMapper.checkUserInDepts(userId, deptCodes);
  }
  
  @Override
  public boolean checkFolderAuth(String folderId, String userId, String permitType) {
  	return commonAuthMapper.checkFolderAuth(folderId, userId, permitType);
  }
	
	@Override
	public boolean checkDocAuth(String docId, String userId, int level) {
		return commonAuthMapper.checkDocAuth(docId, userId, level);
	}
	
	@Override
	public boolean checkFolderOwner(String folderId, String userId) {
	  return commonAuthMapper.checkFolderOwner(folderId, userId);
	}
	
	@Override
	public boolean checkDocOwner(String docId, String userId) {
    return commonAuthMapper.checkDocOwner(docId, userId);
	}

  @Override
  public List<DrmUserDto> selectDocAccesor(String docKey, String docStatus) {
    return commonAuthMapper.selectDocAccesor(docKey, docStatus);
  }
  
  @Override
  public List<AuthBaseGroupMembersDto> selectGroupMembersList(String docKey, String docStatus) {
    return commonAuthMapper.selectGroupMembersList(docKey, docStatus);
  }
  
  @Override
  public List<DrmAuthorDto> selectDocAdditionalAuthorList(String docKey, String docStatus) {
    return commonAuthMapper.selectDocAdditionalAuthorList(docKey, docStatus);
  }

  @Override
  public Optional<HamInfoResult> selectHamSearchInfo(String dataCode) {
  	return commonAuthMapper.selectHamSearchInfo(dataCode);
  }

  @Override
  public String selectsearchNameCheck(String userDept, String searchName) {
    return commonAuthMapper.selectsearchNameCheck(userDept, searchName);
  }

  @Override
  public String selectsearchImwonCheck(String dataCabinetCode) {
    return commonAuthMapper.selectsearchImwonCheck(dataCabinetCode);
    
  }
  
  @Override
  public List<String> selectAccesorGroups(String userId) {
    return commonAuthMapper.selectAccesorGroups(userId);
  }
}
