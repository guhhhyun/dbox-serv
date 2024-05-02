package com.dongkuksystems.dbox.daos.type.auth;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.etc.DrmAuthorDto;
import com.dongkuksystems.dbox.models.dto.etc.DrmUserDto;
import com.dongkuksystems.dbox.models.dto.type.auth.AuthBaseGroupMembersDto;
import com.dongkuksystems.dbox.models.dto.type.auth.FolderAuthResult;
import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult;

public interface CommonAuthDao {
  public List<FolderAuthResult> selectUserAuthOnFolder(String objectId, String userId); 
  public List<String> selectHamDefaultAuths(String userId); 
  public Optional<HamInfoResult> selectHamInfo(String hamId); 
  public Optional<HamInfoResult> selectDeptHamInfo(String hamId); 
	public boolean checkUserInDepts(String userId,List<String> deptCodes);
	public boolean checkFolderAuth(String folderId, String userId, String permitType);
	public boolean checkDocAuth(String docId, String userId, int level);
  public boolean checkFolderOwner(String folderId, String userId);
  public boolean checkDocOwner(String docId, String userId);
  public List<DrmUserDto> selectDocAccesor(String docKey, String docStatus);
  public List<AuthBaseGroupMembersDto> selectGroupMembersList(String docKey, String docStatus);
  public List<DrmAuthorDto> selectDocAdditionalAuthorList(String docKey, String docStatus);
  public Optional<HamInfoResult> selectHamSearchInfo(String dataCode);
  public String selectsearchNameCheck(String userDept, String searchName);
  public String selectsearchImwonCheck(String dataCabinetCode);
  public List<String> selectAccesorGroups(String userId);
}
