package com.dongkuksystems.dbox.daos.type.auth;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.etc.DrmAuthorDto;
import com.dongkuksystems.dbox.models.dto.etc.DrmUserDto;
import com.dongkuksystems.dbox.models.dto.type.auth.AuthBaseGroupMembersDto;
import com.dongkuksystems.dbox.models.dto.type.auth.FolderAuthResult;
import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult; 

public interface CommonAuthMapper {
  public List<FolderAuthResult> selectUserAuthOnFolder(@Param("objectId") String objectId, @Param("userId") String userId); 
  public List<String> selectHamDefaultAuths(@Param("userId") String userId);
  public Optional<HamInfoResult> selectHamInfo(@Param("hamId") String hamId);
  public Optional<HamInfoResult> selectHamSearchInfo(@Param("dataCode") String dataCode);
  public Optional<HamInfoResult> selectDeptHamInfo(@Param("hamId") String hamId);
	public boolean checkUserInDepts(@Param("userId") String userId, @Param("deptCodes") List<String> deptCodes);
	public boolean checkFolderAuth(@Param("folderId") String folderId, @Param("userId") String userId, @Param("permitType") String permitType);
	public boolean checkDocAuth(@Param("docId") String docId, @Param("userId") String userId, @Param("level") int level);
  public boolean checkFolderOwner(@Param("folderId") String folderId, @Param("userId") String userId);
  public boolean checkDocOwner(@Param("docId") String docId, @Param("userId") String userId);
	public List<DrmUserDto> selectDocAccesor(@Param("docKey") String docKey, @Param("docStatus") String docStatus);
	public List<AuthBaseGroupMembersDto> selectGroupMembersList(@Param("docKey") String docKey, @Param("docStatus") String docStatus);
	
	public List<DrmAuthorDto> selectDocAdditionalAuthorList(@Param("docKey") String docKey, @Param("docStatus") String docStatus);
  public String selectsearchNameCheck(@Param("userDept")String userDept, @Param("searchName")String searchName);
  public String selectsearchImwonCheck(@Param("dataCabinetCode")String dataCabinetCode);
  public List<String> selectAccesorGroups(@Param("userId") String userId);
}
