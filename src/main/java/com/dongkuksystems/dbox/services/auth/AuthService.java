package com.dongkuksystems.dbox.services.auth;

import java.util.List;
import java.util.Optional;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.dongkuksystems.dbox.constants.AuthType;
import com.dongkuksystems.dbox.constants.HamType;
import com.dongkuksystems.dbox.constants.SecLevelCode;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.etc.DrmAuthorDto;
import com.dongkuksystems.dbox.models.dto.etc.DrmUserDto;
import com.dongkuksystems.dbox.models.dto.type.auth.AuthBaseGroupMembersDto;
import com.dongkuksystems.dbox.models.dto.type.auth.DrmAuthDto;
import com.dongkuksystems.dbox.models.dto.type.auth.FolderAuthDto;
import com.dongkuksystems.dbox.models.dto.type.auth.FolderAuthResult;
import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult;
import com.dongkuksystems.dbox.models.dto.type.user.UserDSearchAuthDto;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.type.auth.AuthBase;
import com.dongkuksystems.dbox.models.type.auth.AuthShare;
import com.dongkuksystems.dbox.models.type.user.UserPresetDetail;

public interface AuthService {
  public List<AuthShare> selectAuthShareList(String folderObjId) throws Exception;
  public List<AuthBase> selectDefaultFolderAuth(String cabinetCode, String folderId, UserSession userSession) throws Exception;
  public List<AuthBase> selectDefaultFolderAuth(HamInfoResult hamInfo,  VUser user) throws Exception;
  public List<AuthBase> makeJoinPrGroup(String objType, HamInfoResult ham, VUser user);
  public List<AuthBase> selectDefaultFolderRootAuth(HamInfoResult hamInfo, VUser user) throws Exception;
  public List<AuthBase> selectDefaultDocRootAuth(String objType, HamInfoResult ham, VUser user) throws Exception;
  public IDfPersistentObject makeAuthBaseObj(IDfSession idfSession, String registObjId, AuthBase authBase,
      UserSession userSession, boolean isDoc) throws DfException;
  public FolderAuthDto selectFolderAuth(String objectId, String authType) throws Exception;
  public IDfPersistentObject makeAuthShareObj(IDfSession idfSession, String registObjId, String cabinetCode,
      AuthShare authShare, UserSession userSession, boolean isDoc) throws DfException;
  public List<FolderAuthResult> selectUserAuthOnFolder(String objectId, String userId); 
  public List<String> selectHamDefaultAuths(String userId); 
  public List<DrmUserDto> selectDocAccesor(String docKey, String docStatus); 
  public List<AuthBaseGroupMembersDto> selectGroupMembersList(String docKey, String docStatus);
  public List<DrmAuthorDto> selectDocAdditionalAuthorList(String docKey, String docStatus);
  public Optional<HamInfoResult> selectHamInfo(String hamId); 
  public Optional<HamInfoResult> selectDeptHamInfo(String hamId);

  public void makeDocAuths(boolean isVersionUp, String newVersionObjId, String docStatus, IDfDocument idfNewDoc, HamInfoResult hamInfo, FolderAuthDto folderAuthDto, IDfSession idfSession, UserSession userSession) throws DfException;
  public void makeFolderAuths(String registObjId, String cabinetCode, FolderAuthDto folderAuthDto, IDfSession idfSession, UserSession userSession) throws DfException;
  
  public void saveDocAuths(boolean isVersionUp, IDfDocument idfNewDoc, String docStatus, FolderAuthDto folderAuthDto, HamInfoResult hamInfo, IDfSession idfSession) throws Exception;
  public void saveFolderAuths(IDfSession idfSession, FolderAuthDto folderAuthDto) throws Exception;
  public void saveFolderAuths(String registObjId, IDfSession idfSession, FolderAuthDto folderAuthDto) throws Exception;
  

  /**
   * 문서 -> root일 경우 acl 만들기  (부서함, 프로젝트함, 리서치, 중요문서함)
   * 
   * @param hamId 부서 코드, 프로젝트/투자 코드, 연구과제 코드
   * @param userId 사용자 아이디
   * @return auth_base
   */
  public String selectDocAcl(AuthType authType, SecLevelCode secCode, String targetCode) throws Exception;
  
  /**
   * 폴더 -> root일 경우 기본 권한 만들기  (부서함, 프로젝트함, 리서치, 중요문서함, 공용문서함)
   * 
   * @param hamId 부서 코드, 프로젝트/투자 코드, 연구과제 코드
   * @param userId 사용자 아이디
   * @return auth_base, auth_share
   */
  public FolderAuthDto selectRootFolderAuths(UserSession userSession, AuthType authType, SecLevelCode secCode, String cabinetCode, String objectId) throws Exception;
  public FolderAuthDto selectRootFolderAuths(UserSession userSession, AuthType authType, SecLevelCode secCode, String objectId, HamInfoResult hamInfo) throws Exception;

  /**
   * 문서 -> root일 경우 기본 권한 만들기  (부서함, 프로젝트함, 리서치, 중요문서함), preset 포함
   * 
   * @param hamId 부서 코드, 프로젝트/투자 코드, 연구과제 코드
   * @param userId 사용자 아이디
   * @return auth_base
   */
  public FolderAuthDto selectRootDocAuths(UserSession userSession, AuthType authType, SecLevelCode secCode, String cabinetCode, String objectId, String prCode) throws Exception;
  public FolderAuthDto selectRootDocAuths(UserSession userSession, AuthType authType, SecLevelCode secCode, String objectId, HamInfoResult hamInfo) throws Exception;
  public FolderAuthDto selectRootDocAuths(UserSession userSession, AuthType authType, SecLevelCode secCode, String objectId, HamInfoResult hamInfo, UserPresetDetail UserPresetDetail) throws Exception;
  
  /**
   * 폴더 -> 상위 폴더 있을 경우  권한 만들기  (부서함, 프로젝트함, 리서치, 중요문서함)
   * 
   * @param hamId 부서 코드, 프로젝트/투자 코드, 연구과제 코드
   * @param userId 사용자 아이디
   * @return auth_base, auth_share
   */
  public FolderAuthDto selectFolderAuths(UserSession userSession, String upObjectId, String objectId) throws Exception;
  
  /**
   * 문서 -> 상위 폴더 있을 경우  권한 만들기  (부서함, 프로젝트함, 리서치, 중요문서함), preset 포함
   * 
   * @param hamId 부서 코드, 프로젝트/투자 코드, 연구과제 코드
   * @param userId 사용자 아이디
   * @return auth_base
   */
  public FolderAuthDto selectDocAuths(UserSession userSession, String cabinetCode, String upObjectId, String objectId) throws Exception;
  public FolderAuthDto selectDocAuths(UserSession userSession, String cabinetCode, String upObjectId, String objectId, UserPresetDetail UserPresetDetail) throws Exception;
  
  /**
   * 기존 권한이 있는 폴더에 대한 authType, secCode에 따른 권한 만들기  (부서함, 프로젝트함, 리서치, 중요문서함
   * 
   * @param hamId 부서 코드, 프로젝트/투자 코드, 연구과제 코드
   * @param userId 사용자 아이디
   * @return auth_base
   */
  public FolderAuthDto selectFolderAuthsBySecLevel(UserSession userSession, AuthType authType, SecLevelCode secCode, String myCode, String objectId) throws Exception;
  
  /**
   * 기존 권한이 있는 문서에 대한 authType, secCode에 따른 권한 만들기  (부서함, 프로젝트함, 리서치, 중요문서함
   * 
   * @param hamId 부서 코드, 프로젝트/투자 코드, 연구과제 코드
   * @param userId 사용자 아이디
   * @return auth_base
   */
  public FolderAuthDto selectDocAuthsBySecLevel(UserSession userSession, AuthType authType, SecLevelCode secCode, String myCode, String objectId) throws Exception;
  
  DrmAuthDto getAuthorsForDrm(VUser user, String entCode, String entName, String docKey, String docStatus, String docSecLevel) throws Exception;
  
  /**
   * 부서함, 프로젝트/투자, 연구과제, 조직공용함(부서)의 루트에 대한 권한 보유 여부  확인 
   * (HamType 자동 조회)
   * 
   * @param hamId 부서 코드, 프로젝트/투자 코드, 연구과제 코드
   * @param userId 사용자 아이디
   * @return 권한 보유 여부
   */
  public boolean isRootAuthenticated(String hamId, String userId) throws Exception;

  
  
  /**
   * 부서함, 프로젝트/투자, 연구과제, 조직공용함의 루트에 대한 권한 보유 여부  확인 
   * 
   * @param hamType 부서함, 프로젝트/투자, 연구과제, 조직공용함 분류 
   * @param hamId 부서 코드, 프로젝트/투자 코드, 연구과제 코드
   * @param userId 사용자 아이디
   * @return 권한 보유 여부
   */
  public boolean isRootAuthenticated(HamType hamType, String hamId, String userId) throws Exception;

  /**
   * 부서함 루트에 대한 쓰기 권한 보유 여부 확인
   * 
   * @param deptCode 부서 코드
   * @param userId 사용자 아이디
   * @return 권한 보유 여부
   */
	public boolean checkDeptRootWriteAuth(String deptCode, String userId) throws Exception;

  /**
   * 조직공용함 루트에 대한 쓰기 권한 보유 여부 확인
   * 
   * @param deptCode 부서 코드
   * @param userId 사용자 아이디
   * @return 권한 보유 여부
   */
	public boolean checkCommonDeptRootWriteAuth(String deptCode, String userId) throws Exception;
  
  /**
   * 프로젝트/투자 루트에 대한 쓰기 권한 보유 여부 확인
   * 
   * @param pjtCode 프로젝트/투자 코드
   * @param userId 사용자 아이디
   * @param permitType 폴더 권한 (R(조회), D(삭제))
   * @return 권한 보유 여부
   */
  public boolean checkProjectRootAuth(String pjtCode, String userId, String permitType) throws Exception;
  
  /**
   * 프로젝트/투자 루트에 대한 쓰기 권한 보유 여부 확인
   * 
   * @param pjtCode 프로젝트/투자 코드
   * @param orgId 부서코드
   * @param permitType 폴더 권한 (R(조회), D(삭제))
   * @return 권한 보유 여부
   */
  public boolean checkProjectRootAuthByOrgId(String pjtCode, String orgId, String permitType) throws Exception;
  
  /**
   * 연구과제 루트에 대한 쓰기 권한 보유 여부 확인
   * 
   * @param rschCode 연구과제 코드
   * @param userId 사용자 아이디
   * @param permitType 폴더 권한 (R(조회), D(삭제))
   * @return 권한 보유 여부
   */
  public boolean checkResearchRootAuth(String rschCode, String userId, String permitType) throws Exception;
  
  /**
   * 연구과제 루트에 대한 쓰기 권한 보유 여부 확인
   * 
   * @param rschCode 연구과제 코드
   * @param orgId 부서코드
   * @param permitType 폴더 권한 (R(조회), D(삭제))
   * @return 권한 보유 여부
   */
  public boolean checkResearchRootAuthByOrgId(String rschCode, String orgId, String permitType) throws Exception;
	
	/**
	 * 폴더 권한 보유 여부 확인 - userId가 folderId에 대해 permitType 이상의 권한을 갖는지 여부<br/>
	 * (폴더 하위에 대한 쓰기 권한은 폴더에 대한 삭제 권한이 있는지 여부로 판단)
	 * 
	 * @param folderId 폴더의 r_object_id
	 * @param userId 사용자 아이디
	 * @param permitType 폴더 권한 (B(검색), R(조회), D(삭제))
	 * @return 권한 보유 여부
	 */
	public boolean checkFolderAuth(String folderId, String userId, String permitType) throws Exception;
	
	/**
	 * 문서 권한 보유 여부 확인 - userId가 docId에 대해 level 이상의 권한을 갖는지 여부
	 * 
	 * @param docId 문서의 r_object_id
	 * @param userId 사용자 아이디
	 * @param level 문서 권한 (2(검색), 3(다운로드), D(삭제))
	 * @return 권한 보유 여부
	 */
	public boolean checkDocAuth(String docId, String userId, int level) throws Exception;
  
	/**
	 * 폴더 소유부서 또는 주관부서 여부
	 * 
	 * @param folderId 폴더의 r_object_id
	 * @param userId 사용자 아이디
	 * @return 권한 보유 여부
	 */
  public boolean checkFolderOwner(String folderId, String userId) throws Exception;
  
  /**
   * 문서 소유부서 또는 주관부서 여부
   * 
   * @param docId 문서의 r_object_id
   * @param userId 사용자 아이디
   * @return 권한 보유 여부
   */
  public boolean checkDocOwner(String docId, String userId) throws Exception;
  
  public UserDSearchAuthDto selectDSearchUserAuth(String userId) throws Exception;
}
 