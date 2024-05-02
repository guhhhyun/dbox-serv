package com.dongkuksystems.dbox.services.auth;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.dongkuksystems.dbox.constants.AclTemplate;
import com.dongkuksystems.dbox.constants.AuthType;
import com.dongkuksystems.dbox.constants.AuthorType;
import com.dongkuksystems.dbox.constants.DCTMConstants;
import com.dongkuksystems.dbox.constants.DboxObjectType;
import com.dongkuksystems.dbox.constants.DocStatus;
import com.dongkuksystems.dbox.constants.DrmCompanyId;
import com.dongkuksystems.dbox.constants.DrmSecLevelType;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.constants.HamType;
import com.dongkuksystems.dbox.constants.SecLevelCode;
import com.dongkuksystems.dbox.daos.table.etc.gwdept.GwDeptDao;
import com.dongkuksystems.dbox.daos.type.auth.CommonAuthDao;
import com.dongkuksystems.dbox.daos.type.auth.base.AuthBaseDao;
import com.dongkuksystems.dbox.daos.type.auth.share.AuthShareDao;
import com.dongkuksystems.dbox.daos.type.docbox.project.ProjectDao;
import com.dongkuksystems.dbox.daos.type.docbox.research.ResearchDao;
import com.dongkuksystems.dbox.daos.type.user.preset.UserPresetDao;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.etc.DrmAuthorDto;
import com.dongkuksystems.dbox.models.dto.etc.DrmCompanyDto;
import com.dongkuksystems.dbox.models.dto.etc.DrmDeptDto;
import com.dongkuksystems.dbox.models.dto.etc.DrmUserDto;
import com.dongkuksystems.dbox.models.dto.type.auth.AuthBaseGroupMembersDto;
import com.dongkuksystems.dbox.models.dto.type.auth.CheckAuthParam;
import com.dongkuksystems.dbox.models.dto.type.auth.DrmAuthDto;
import com.dongkuksystems.dbox.models.dto.type.auth.FolderAuthDto;
import com.dongkuksystems.dbox.models.dto.type.auth.FolderAuthResult;
import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectRepeatDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchRepeatDto;
import com.dongkuksystems.dbox.models.dto.type.manager.managerconfig.ManagerConfigDto;
import com.dongkuksystems.dbox.models.dto.type.user.UserDSearchAuthDto;
import com.dongkuksystems.dbox.models.dto.type.user.UserPresetFilterDto;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.type.auth.AuthBase;
import com.dongkuksystems.dbox.models.type.auth.AuthShare;
import com.dongkuksystems.dbox.models.type.code.Code;
import com.dongkuksystems.dbox.models.type.docbox.ProjectRepeating;
import com.dongkuksystems.dbox.models.type.docbox.ResearchRepeating;
import com.dongkuksystems.dbox.models.type.user.UserPresetDetail;
import com.dongkuksystems.dbox.models.type.user.UserPresetRDto;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.manager.managerconfig.ManagerConfigService;
import com.dongkuksystems.dbox.services.project.ProjectService;
import com.dongkuksystems.dbox.services.research.ResearchService;

@Service
public class AuthServiceImpl extends AbstractCommonService implements AuthService {

  private final CommonAuthDao commonAuthDao;
  private final GwDeptDao deptDao;
  private final ProjectDao projectDao;
  private final ResearchDao researchDao;
  private final AuthBaseDao authBaseDao;
  private final AuthShareDao authShareDao;
  private final CodeService codeService;
  private final UserPresetDao userPresetDao;
  private final ProjectService projectService;
  private final ResearchService researchService;
  private final ManagerConfigService managerConfigService;

  public AuthServiceImpl(CommonAuthDao commonAuthDao,  ProjectDao projectDao,
      ResearchDao researchDao, AuthBaseDao authBaseDao, AuthShareDao authShareDao,
      GwDeptDao deptDao, CodeService codeService, UserPresetDao userPresetDao, ProjectService projectService, ResearchService researchService,
      ManagerConfigService managerConfigService) {
    this.commonAuthDao = commonAuthDao;
    this.projectDao = projectDao;
    this.researchDao = researchDao;
    this.authBaseDao = authBaseDao;
    this.authShareDao = authShareDao;
    this.deptDao = deptDao;
    this.codeService = codeService;
    this.userPresetDao = userPresetDao;
    this.projectService = projectService;
    this.researchService = researchService;
    this.managerConfigService = managerConfigService;
  }

  @Override
  public boolean isRootAuthenticated(String hamId, String userId) throws Exception {
  	HamType hamType = null;
  	
  	// 조직공용함 여부 확인
  	Map<String, Code> commonCabinetDeptMap = codeService.getCommonCabinetDeptMap();
  	if (commonCabinetDeptMap.containsKey(hamId)) {
  		String hamTypeStr = commonCabinetDeptMap.get(hamId).getUCodeVal1();
  		hamType = HamType.findByValue(hamTypeStr);
  	}
  	// 프로젝트/투자, 연구과제, 부서 여부 확인
  	else {
    	HamInfoResult hamInfo = commonAuthDao.selectHamInfo(hamId).orElseThrow(() -> new RuntimeException("존재하지 않는 hamId 입니다. (hamId: " + hamId + ")"));
    	hamType = HamType.findByValue(hamInfo.getHamType());
  	}
  	
  	return isRootAuthenticated(hamType, hamId, userId);
  }
  
  @Override
  public boolean isRootAuthenticated(HamType hamType, String hamId, String userId) throws Exception {
  	boolean isGranted = false;
  	
  	switch (hamType) {
  		// 부서함일 경우
  		case DEPT:
  			isGranted = checkDeptRootWriteAuth(hamId, userId);
  			break;
  		// 조직공용함일 경우
  		case COMPANY:
  		case COMPANY_M:
  			isGranted = checkCommonDeptRootWriteAuth(hamId, userId);
  			break;
  		// 프로젝트/투자일 경우
  		case PROJECT:
  			isGranted = checkProjectRootAuth(hamId, userId, GrantedLevels.DELETE.getLabel());
  			break;
  		// 연구과제일 경우
  		case RESEARCH:
        isGranted = checkResearchRootAuth(hamId, userId, GrantedLevels.DELETE.getLabel());
  			break;
			default:
  	}
  	
  	return isGranted;
  }

  @Override
  public FolderAuthDto selectFolderAuth(String objectId, String authType) throws Exception {
    return FolderAuthDto.builder().authBaseList(authBaseDao.selectList(objectId, authType))
        .authShareList(authShareDao.selectList(objectId)).build();
  }

  @Override
  public List<AuthBase> selectDefaultFolderAuth(String cabinetCode, String folderId, UserSession userSession)
      throws Exception {
    String orgId = deptDao.selectOrgIdByCabinetcode(cabinetCode);
    VDept dept = deptDao.selectOneByOrgIdDefault(orgId);
    String ownDeptYn = userSession.getUser().getOrgId().equals(orgId) ? "Y" : "N";
    List<AuthBase> rst = new ArrayList<>();
    DCTMConstants.DEFAULT_TEAM_AUTH_GROUP_MAP.forEach((mKey, mVal) -> {
      rst.add(AuthBase.builder().uObjId(folderId).uObjType(DboxObjectType.FOLDER.getValue())
          .uDocStatus(DocStatus.LIVE.getValue())
          .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLevelCode()
              : GrantedLevels.DELETE.getLevelCode())
          .uOwnDeptYn(ownDeptYn).uAuthorId(mKey).uAuthorType(AuthorType.DEFAULT.getValue())
          .uCreateUser(userSession.getUser().getUserId()).build()); 
      rst.add(AuthBase.builder().uObjId(folderId).uObjType(DboxObjectType.FOLDER.getValue())
          .uDocStatus(DocStatus.CLOSED.getValue())
          .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLabel()
              : GrantedLevels.READ.getLabel())
          .uOwnDeptYn(ownDeptYn).uAuthorId(mKey).uAuthorType(AuthorType.DEFAULT.getValue())
          .uCreateUser(userSession.getUser().getUserId()).build());
    });

    DCTMConstants.DEFAULT_TEAM_AUTH_COM_MAP.forEach((mKey, mVal) -> {
      rst.add(AuthBase.builder().uObjId(folderId).uObjType(DboxObjectType.FOLDER.getValue())
          .uDocStatus(DocStatus.LIVE.getValue())
          .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLevelCode()
              : GrantedLevels.DELETE.getLevelCode())
          .uOwnDeptYn(ownDeptYn).uAuthorId(MessageFormat.format(mKey, dept.getComOrgId()))
          .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(userSession.getUser().getUserId()).build());
      rst.add(AuthBase.builder().uObjId(folderId).uObjType(DboxObjectType.FOLDER.getValue())
          .uDocStatus(DocStatus.CLOSED.getValue())
          .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLabel()
              : GrantedLevels.READ.getLabel())
          .uOwnDeptYn(ownDeptYn).uAuthorId(MessageFormat.format(mKey, dept.getComOrgId()))
          .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(userSession.getUser().getUserId()).build());
    });

    DCTMConstants.DEFAULT_TEAM_AUTH_MAP.forEach((mKey, mVal) -> {
      rst.add(AuthBase.builder().uObjId(folderId).uObjType(DboxObjectType.FOLDER.getValue())
          .uDocStatus(DocStatus.LIVE.getValue())
          .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLevelCode()
              : GrantedLevels.DELETE.getLevelCode())
          .uOwnDeptYn(ownDeptYn).uAuthorId(MessageFormat.format(mKey, cabinetCode))
          .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(userSession.getUser().getUserId()).build());
      rst.add(AuthBase.builder().uObjId(folderId).uObjType(DboxObjectType.FOLDER.getValue())
          .uDocStatus(DocStatus.CLOSED.getValue())
          .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLabel()
              : GrantedLevels.READ.getLabel())
          .uOwnDeptYn(ownDeptYn).uAuthorId(MessageFormat.format(mKey, cabinetCode))
          .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(userSession.getUser().getUserId()).build());
    });
    DCTMConstants.DEFAULT_TEAM_AUTH_TEAM_MAP.forEach((mKey, mVal) -> {
      rst.add(AuthBase.builder().uObjId(folderId).uObjType(DboxObjectType.FOLDER.getValue())
          .uDocStatus(DocStatus.LIVE.getValue())
          .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLevelCode()
              : GrantedLevels.DELETE.getLevelCode())
          .uOwnDeptYn(ownDeptYn).uAuthorId(MessageFormat.format(mKey, cabinetCode))
          .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(userSession.getUser().getUserId()).build());
      rst.add(AuthBase.builder().uObjId(folderId).uObjType(DboxObjectType.FOLDER.getValue())
          .uDocStatus(DocStatus.CLOSED.getValue())
          .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLabel()
              : GrantedLevels.READ.getLabel())
          .uOwnDeptYn(ownDeptYn).uAuthorId(MessageFormat.format(mKey, cabinetCode))
          .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(userSession.getUser().getUserId()).build());
    });
    return rst;
  }

  @Override
  public List<AuthBase> selectDefaultFolderRootAuth(HamInfoResult hamInfo, VUser user) throws Exception {
    List<AuthBase> rst = new ArrayList<>();
    rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
        .uDocStatus(DocStatus.LIVE.getValue())
        .uPermitType(GrantedLevels.DELETE.getLevelCode())
        .uOwnDeptYn("y").uAuthorId(hamInfo.getCabinetOrgId())
        .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
    rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
        .uDocStatus(DocStatus.CLOSED.getValue())
        .uPermitType(GrantedLevels.READ.getLabel())
        .uOwnDeptYn("y").uAuthorId(hamInfo.getCabinetOrgId())
        .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
    return rst;
  }
  
  @Override
  public List<AuthBase> selectDefaultFolderAuth(HamInfoResult hamInfo, VUser user) throws Exception {
    List<AuthBase> rst = new ArrayList<>();

    String ownDeptYn = user.getDeptCabinetcode().equals(hamInfo.getUCabinetCode()) ? "y" : "n";
    rst.addAll(this.makeDefaultGroup(ownDeptYn, hamInfo.getMyCode(), user));
    rst.addAll(this.makeDefaultCompany(ownDeptYn, hamInfo, user));
    if (!HamType.COMPANY.getValue().equals(hamInfo.getHamType()) && !HamType.COMPANY_M.getValue().equals(hamInfo.getHamType())) {
      rst.addAll(this.makeDefaultTeam(ownDeptYn, hamInfo.getUCabinetCode(), user));
    }
    if (HamType.PROJECT.getValue().equals(hamInfo.getHamType()) || HamType.RESEARCH.getValue().equals(hamInfo.getHamType())) {
      rst.addAll(this.makeJoinPrGroup(DboxObjectType.FOLDER.getValue(), hamInfo, user));
    }
    return rst;
  }

  @Override
  public List<AuthBase> makeJoinPrGroup(String objType, HamInfoResult ham, VUser user) {
    String ownDeptYn = user.getDeptCabinetcode().equals(ham.getUCabinetCode()) ? "y" : "n";
    List<AuthBase> rst = new ArrayList<>();
    // 주관부서 권한
    rst.add(AuthBase.builder().uObjType(objType)
        .uDocStatus(DocStatus.LIVE.getValue())
        .uPermitType(GrantedLevels.DELETE.getLevelCode())
        .uOwnDeptYn("y")
        .uAuthorId(MessageFormat.format("g_{0}", ham.getUCabinetCode()))
        .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
    if (DboxObjectType.FOLDER.getValue().equals(objType)) {
      rst.add(AuthBase.builder().uObjType(objType)
          .uDocStatus(DocStatus.CLOSED.getValue())
          .uPermitType(GrantedLevels.READ.getLabel())
          .uOwnDeptYn("y")
          .uAuthorId(MessageFormat.format("g_{0}", ham.getUCabinetCode()))
          .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
    }
    if (HamType.PROJECT.getValue().equals(ham.getHamType())) {
      
      DCTMConstants.DEFAULT_TEAM_AUTH_COM_PROJECT_MAP.forEach((mKey, mVal) -> {
        rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
            .uDocStatus(DocStatus.LIVE.getValue())
            .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLevelCode()
                : GrantedLevels.DELETE.getLevelCode())
            .uOwnDeptYn(ownDeptYn)
            .uAuthorId(MessageFormat.format(mKey, ham.getMyCode()))
            .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(user.getUserId()).build());
        rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
            .uDocStatus(DocStatus.CLOSED.getValue())
            .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLabel()
                : GrantedLevels.READ.getLabel())
            .uOwnDeptYn(ownDeptYn)
            .uAuthorId(MessageFormat.format(mKey, ham.getMyCode()))
            .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(user.getUserId()).build());
      });
      
      List<ProjectRepeating> prs = projectDao.selectRepeatingListByCode(ham.getMyCode());
      if (!Objects.isNull(prs)) {
        for (ProjectRepeating pr : prs) {
          if (!Objects.isNull(pr.getUJoinDeptRead())) {
            rst.add(AuthBase.builder().uObjType(objType)
                .uDocStatus(DocStatus.LIVE.getValue())
                .uPermitType(GrantedLevels.READ.getLevelCode())
                .uOwnDeptYn(ownDeptYn)
                .uAuthorId(MessageFormat.format("g_{0}", pr.getUJoinDeptReadOrgId())) //문서에는 g_제거
                .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
            rst.add(AuthBase.builder().uObjType(objType)
                .uDocStatus(DocStatus.CLOSED.getValue())
                .uPermitType(GrantedLevels.READ.getLevelCode())
                .uOwnDeptYn(ownDeptYn)
                .uAuthorId(MessageFormat.format("g_{0}", pr.getUJoinDeptReadOrgId())) //문서에는 g_제거
                .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
          }
          if (!Objects.isNull(pr.getUJoinDeptDel())) {
            rst.add(AuthBase.builder().uObjType(objType)
                .uDocStatus(DocStatus.LIVE.getValue())
                .uPermitType(GrantedLevels.DELETE.getLevelCode())
                .uOwnDeptYn(ownDeptYn)
                .uAuthorId(MessageFormat.format("g_{0}", pr.getUJoinDeptDelOrgId())) //문서에는 g_제거
                .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
            rst.add(AuthBase.builder().uObjType(objType)
                .uDocStatus(DocStatus.CLOSED.getValue())
                .uPermitType(GrantedLevels.READ.getLabel())
                .uOwnDeptYn(ownDeptYn)
                .uAuthorId(MessageFormat.format("g_{0}", pr.getUJoinDeptDelOrgId())) //문서에는 g_제거
                .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
          }
        }
      }
    } else if (HamType.RESEARCH.getValue().equals(ham.getHamType())) {
      DCTMConstants.DEFAULT_TEAM_AUTH_COM_RESEARCH_MAP.forEach((mKey, mVal) -> {
        rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
            .uDocStatus(DocStatus.LIVE.getValue())
            .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLevelCode()
                : GrantedLevels.DELETE.getLevelCode())
            .uOwnDeptYn(ownDeptYn)
            .uAuthorId(MessageFormat.format(mKey, ham.getMyCode()))
            .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(user.getUserId()).build());
        rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
            .uDocStatus(DocStatus.CLOSED.getValue())
            .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLabel()
                : GrantedLevels.READ.getLabel())
            .uOwnDeptYn(ownDeptYn)
            .uAuthorId(MessageFormat.format(mKey, ham.getMyCode()))
            .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(user.getUserId()).build());
      });
      List<ResearchRepeating> rschs = researchDao.selectRepeatingListByCode(ham.getMyCode());
      if (!Objects.isNull(rschs)) {
        for (ResearchRepeating pr : rschs) {
          if (!Objects.isNull(pr.getUJoinDeptRead())) {
            rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
                .uDocStatus(DocStatus.LIVE.getValue())
                .uPermitType(GrantedLevels.READ.getLevelCode())
                .uOwnDeptYn(ownDeptYn)
                .uAuthorId(MessageFormat.format("g_{0}", pr.getUJoinDeptReadOrgId())) //문서에는 g_제거
                .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
            rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
                .uDocStatus(DocStatus.CLOSED.getValue())
                .uPermitType(GrantedLevels.READ.getLevelCode())
                .uOwnDeptYn(ownDeptYn)
                .uAuthorId(MessageFormat.format("g_{0}", pr.getUJoinDeptReadOrgId())) //문서에는 g_제거
                .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
          }
          if (!Objects.isNull(pr.getUJoinDeptDel())) {
            rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
                .uDocStatus(DocStatus.LIVE.getValue())
                .uPermitType(GrantedLevels.DELETE.getLevelCode())
                .uOwnDeptYn(ownDeptYn)
                .uAuthorId(MessageFormat.format("g_{0}", pr.getUJoinDeptDelOrgId())) //문서에는 g_제거
                .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
            rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
                .uDocStatus(DocStatus.CLOSED.getValue())
                .uPermitType(GrantedLevels.READ.getLabel())
                .uOwnDeptYn(ownDeptYn)
                .uAuthorId(MessageFormat.format("g_{0}", pr.getUJoinDeptDelOrgId())) //문서에는 g_제거
                .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
          }
        }
      }
    } 
    return rst;
  }
  
  private List<AuthBase> makeDefaultGroup(String ownDeptYn, String orgId, VUser user) {
    List<AuthBase> rst = new ArrayList<>();
    DCTMConstants.DEFAULT_TEAM_AUTH_GROUP_MAP.forEach((mKey, mVal) -> {
      rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
          .uDocStatus(DocStatus.LIVE.getValue())
          .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLevelCode()
              : GrantedLevels.DELETE.getLevelCode())
          .uOwnDeptYn(ownDeptYn).uAuthorId(mKey).uAuthorType(AuthorType.DEFAULT.getValue())
          .uCreateUser(user.getUserId()).build());
    });
    DCTMConstants.DEFAULT_TEAM_AUTH_GROUP_MAP.forEach((mKey, mVal) -> {
      rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
          .uDocStatus(DocStatus.CLOSED.getValue())
          .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLabel()
              : GrantedLevels.READ.getLabel())
          .uOwnDeptYn(ownDeptYn).uAuthorId(mKey).uAuthorType(AuthorType.DEFAULT.getValue())
          .uCreateUser(user.getUserId()).build());
    });
    return rst;
  }

  private List<AuthBase> makeDefaultCompany(String ownDeptYn, HamInfoResult ham, VUser user) {
    List<AuthBase> rst = new ArrayList<>();
    Map<String, Integer> groups = new HashMap<>();
    
    if (HamType.PROJECT.getValue().equals(ham.getHamType())) {
      groups.putAll(DCTMConstants.DEFAULT_TEAM_AUTH_COM_MAP);
//      groups.putAll(DCTMConstants.DEFAULT_TEAM_AUTH_COM_PROJECT_MAP);
      DCTMConstants.DEFAULT_TEAM_AUTH_COM_PROJECT_MAP.forEach((mKey, mVal) -> {
        rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
            .uDocStatus(DocStatus.LIVE.getValue())
            .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLevelCode()
                : GrantedLevels.DELETE.getLevelCode())
            .uOwnDeptYn(ownDeptYn)
            .uAuthorId(MessageFormat.format(mKey, ham.getMyCode()))
            .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(user.getUserId()).build());
        rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
            .uDocStatus(DocStatus.CLOSED.getValue())
            .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLabel()
                : GrantedLevels.READ.getLabel())
            .uOwnDeptYn(ownDeptYn)
            .uAuthorId(MessageFormat.format(mKey, ham.getMyCode()))
            .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(user.getUserId()).build());
      });
    } else if (HamType.RESEARCH.getValue().equals(ham.getHamType())) {
      groups.putAll(DCTMConstants.DEFAULT_TEAM_AUTH_COM_MAP);
//      groups.putAll(DCTMConstants.DEFAULT_TEAM_AUTH_COM_RESEARCH_MAP);
      DCTMConstants.DEFAULT_TEAM_AUTH_COM_RESEARCH_MAP.forEach((mKey, mVal) -> {
        rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
            .uDocStatus(DocStatus.LIVE.getValue())
            .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLevelCode()
                : GrantedLevels.DELETE.getLevelCode())
            .uOwnDeptYn(ownDeptYn)
            .uAuthorId(MessageFormat.format(mKey, ham.getMyCode()))
            .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(user.getUserId()).build());
        rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
            .uDocStatus(DocStatus.CLOSED.getValue())
            .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLabel()
                : GrantedLevels.READ.getLabel())
            .uOwnDeptYn(ownDeptYn)
            .uAuthorId(MessageFormat.format(mKey, ham.getMyCode()))
            .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(user.getUserId()).build());
      });
    } else if (HamType.DEPT.getValue().equals(ham.getHamType())) {
      groups.putAll(DCTMConstants.DEFAULT_TEAM_AUTH_COM_MAP);
    } else {
      groups.putAll(DCTMConstants.DEFAULT_AUTH_COM_HAM_MAP);
      rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
          .uDocStatus(DocStatus.CLOSED.getValue())
          .uPermitType(GrantedLevels.READ.getLabel())
          .uOwnDeptYn(ownDeptYn)
          .uAuthorId(MessageFormat.format("g_{0}", HamType.COMPANY.getValue().equals(ham.getHamType())?ham.getComOrgId():ham.getMyCode()))
          .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(user.getUserId()).build());
      rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
          .uDocStatus(DocStatus.LIVE.getValue())
          .uPermitType(GrantedLevels.DELETE.getLevelCode())
          .uOwnDeptYn(ownDeptYn)
          .uAuthorId(MessageFormat.format("g_{0}", HamType.COMPANY.getValue().equals(ham.getHamType())?ham.getComOrgId():ham.getMyCode()))
          .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(user.getUserId()).build());
    }
    groups.forEach((mKey, mVal) -> {
      rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
          .uDocStatus(DocStatus.LIVE.getValue())
          .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLevelCode()
              : GrantedLevels.DELETE.getLevelCode())
          .uOwnDeptYn(ownDeptYn).uAuthorId(MessageFormat.format(mKey, ham.getComOrgId()))
          .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(user.getUserId()).build());
      rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
          .uDocStatus(DocStatus.CLOSED.getValue())
          .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLabel()
              : GrantedLevels.READ.getLabel())
          .uOwnDeptYn(ownDeptYn).uAuthorId(MessageFormat.format(mKey, ham.getComOrgId()))
          .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(user.getUserId()).build());
    });
    return rst;
  }

  private List<AuthBase> makeDefaultTeam(String ownDeptYn, String cabinetCode, VUser user) {
    List<AuthBase> rst = new ArrayList<>();
    DCTMConstants.DEFAULT_TEAM_AUTH_MAP.forEach((mKey, mVal) -> {
    rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
        .uDocStatus(DocStatus.LIVE.getValue())
        .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLevelCode()
            : GrantedLevels.DELETE.getLevelCode())
        .uOwnDeptYn(ownDeptYn).uAuthorId(MessageFormat.format(mKey, cabinetCode))
        .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(user.getUserId()).build());
//    rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
//        .uDocStatus(DocStatus.CLOSED.getValue())
//        .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLevelCode()
//            : GrantedLevels.DELETE.getLevelCode())
//        .uOwnDeptYn(ownDeptYn).uAuthorId(MessageFormat.format(mKey, cabinetCode))
//        .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(user.getUserId()).build());
    });
    DCTMConstants.DEFAULT_TEAM_AUTH_TEAM_MAP.forEach((mKey, mVal) -> {
    rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
        .uDocStatus(DocStatus.LIVE.getValue())
        .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLevelCode()
            : GrantedLevels.DELETE.getLevelCode())
        .uOwnDeptYn(ownDeptYn).uAuthorId(MessageFormat.format(mKey, cabinetCode))
        .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
//    rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
//        .uDocStatus(DocStatus.CLOSED.getValue())
//        .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLevelCode()
//            : GrantedLevels.DELETE.getLevelCode())
//        .uOwnDeptYn(ownDeptYn).uAuthorId(MessageFormat.format(mKey, cabinetCode))
//        .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
    });
    return rst;
  }
  
  @Override
  public IDfPersistentObject makeAuthBaseObj(IDfSession idfSession, String registObjId, AuthBase authBase,
      UserSession userSession, boolean isDoc) throws DfException {
    return FolderAuthDto.setAuthBaseObj(idfSession,
        AuthBase.builder().uObjId(registObjId)
            .uObjType(isDoc ? DboxObjectType.DOCUMENT.getValue() : DboxObjectType.FOLDER.getValue())
            .uDocStatus(authBase.getUDocStatus()).uPermitType(authBase.getUPermitType())
            .uAuthorId(authBase.getUAuthorId())
            .uAuthorType(authBase.getUAuthorType())
            .uOwnDeptYn(authBase.getUOwnDeptYn())
            .uAddGubun(authBase.getUAddGubun())
            .uCreateUser(userSession.getUser().getUserId()).build());
  }

  @Override
  public IDfPersistentObject makeAuthShareObj(IDfSession idfSession, String registObjId, String cabinetCode,
      AuthShare authShare, UserSession userSession, boolean isDoc) throws DfException {
    if (isDoc) {
      return FolderAuthDto.setAuthBaseObjByAuthShare(idfSession,
          AuthShare.builder().uObjId(registObjId).uAuthorId(authShare.getUAuthorId())
            .docStatus(authShare.getDocStatus())
            .uAuthorType(authShare.getUAuthorType()).uPermitType(authShare.getUPermitType())
						.uCreateUser(userSession.getUser().getUserId()).build()); /* .uCabinetCode(cabinetCode) */
    } else {
      return FolderAuthDto.setAuthShareObj(idfSession,
          AuthShare.builder().uObjId(registObjId).uAuthorId(authShare.getUAuthorId().replace("g_", ""))
              .uAuthorType(authShare.getUAuthorType()).uPermitType(authShare.getUPermitType())
						.uCreateUser(userSession.getUser().getUserId()).build()); /* .uCabinetCode(cabinetCode) */
    }
  }

  @Override
  public void saveDocAuths(boolean isVersionUp, IDfDocument idfNewDoc, String docStatus, FolderAuthDto folderAuthDto, HamInfoResult hamInfo, IDfSession idfSession) throws Exception {
    //프로젝트 기본 관리자
    if ("P".equals(hamInfo.getHamType())) {
      if(!isVersionUp) idfNewDoc.grant("g_".concat(hamInfo.getMyCode()).concat("_pjtmgr"), GrantedLevels.DELETE.getLevel(), "");
    } else if ("R".equals(hamInfo.getHamType())) {
      if(!isVersionUp) idfNewDoc.grant("g_".concat(hamInfo.getMyCode()).concat("_rschmgr"), GrantedLevels.DELETE.getLevel(), "");
    }
    if (folderAuthDto != null) {
      IDfPersistentObject obj = null;
      if (folderAuthDto.getAuthBaseList() != null) {
        for (AuthBase authBase : folderAuthDto.getAuthBaseList()) {
          if (!AuthorType.DEFAULT.getValue().equals(authBase.getUAuthorType())) {
            boolean isIn = false;
            if (folderAuthDto.getAuthShareList() != null) {
              for (AuthShare authShare : folderAuthDto.getAuthShareList()) {
                String authorId = AuthorType.TEAM.getValue().equals(authShare.getUAuthorType())? "g_".concat(authShare.getUAuthorId()):authShare.getUAuthorId();
                if (authorId.equals(authBase.getUAuthorId())) {
                  if (GrantedLevels.isFirstBiggerOrSame(authShare.getUPermitType(), authBase.getUPermitType())) {
                    isIn = true;
                    break;
                  }
                }
              }
            }
            if (!isIn) {
              obj = FolderAuthDto.setAuthBaseObj(idfSession, authBase);
              obj.save();
              if (docStatus.equals(authBase.getUDocStatus())) {
                 if(!isVersionUp) idfNewDoc.grant(authBase.getUAuthorId(), GrantedLevels.findByLevel(authBase.getUPermitType()), "");
                 String secLevel = idfNewDoc.getString("u_sec_level");
                 boolean isG = SecLevelCode.COMPANY.getValue().equals(secLevel) || SecLevelCode.GROUP.getValue().equals(secLevel);
                 if ("P".equals(authBase.getUAddGubun()) && AuthorType.TEAM.getValue().equals(authBase.getUAuthorType())) {
                   if (!authBase.getUAuthorId().contains("_sub"))
                     idfNewDoc.grant(authBase.getUAuthorId() + "_sub", GrantedLevels.findByLevel(authBase.getUPermitType()), "");
                 } else if ("G".equals(authBase.getUAddGubun()) && isG) {
                   if (!authBase.getUAuthorId().contains("_sub"))
                     idfNewDoc.grant(authBase.getUAuthorId() + "_sub", GrantedLevels.findByLevel(authBase.getUPermitType()), "");
                 }
              }
            }
          }
        }
      }
      
      if (folderAuthDto.getAuthShareList() != null) {
        if (folderAuthDto.getAuthBaseList() != null) {
          for (AuthShare authShare : folderAuthDto.getAuthShareList()) {
            boolean isLiveIn = false, isClosedIn = false;
            String livePermit = null;
            String authorId = AuthorType.TEAM.getValue().equals(authShare.getUAuthorType())? "g_".concat(authShare.getUAuthorId()):authShare.getUAuthorId();
            for (AuthBase authBase : folderAuthDto.getAuthBaseList()) {
              if (DocStatus.LIVE.getValue().equals(authBase.getUDocStatus())) {
//                if (authorId.equals(authBase.getUAuthorId()) && authShare.getUPermitType().equals(authBase.getUPermitType())) {
                if (authorId.equals(authBase.getUAuthorId())) {
                  if (GrantedLevels.isFirstBigger(authBase.getUPermitType(), authShare.getUPermitType())) {
                    isLiveIn = true;
                    break;
                  } else {
                    livePermit = authBase.getUPermitType();
                  }
                }
              }
            }
            if (!isLiveIn) {
              if (livePermit != null) authShare.setUPermitType(livePermit);
              authShare.setDocStatus(DocStatus.LIVE.getValue());
              obj = FolderAuthDto.setAuthBaseObjByAuthShare(idfSession, authShare);
              obj.save();
              if (docStatus.equals(DocStatus.LIVE.getValue())) {
                if(!isVersionUp) idfNewDoc.grant(authorId, GrantedLevels.findByLevel(authShare.getUPermitType()), "");
              }
            }

            for (AuthBase authBase : folderAuthDto.getAuthBaseList()) {
              if (DocStatus.CLOSED.getValue().equals(authBase.getUDocStatus())) {
                if (authorId.equals(authBase.getUAuthorId())) {
//                if (authorId.equals(authBase.getUAuthorId()) && authShare.getUPermitType().equals(authBase.getUPermitType())) {
                  authShare.setDocStatus(DocStatus.CLOSED.getValue());
                  authShare.setUPermitType(GrantedLevels.READ.getLabel());
                  authShare.setUCreateUser(idfSession.getLoginUserName());
                  obj = FolderAuthDto.setAuthBaseObjByAuthShare(idfSession, authShare);
                  obj.save();
                  if (docStatus.equals(DocStatus.CLOSED.getValue())) {
                    if(!isVersionUp) idfNewDoc.grant(authorId, GrantedLevels.findByLevel(authShare.getUPermitType()), "");
                  }
                  isClosedIn = true;
                  break;
                }
              }
            }
            if (!isClosedIn) {
              authShare.setDocStatus(DocStatus.CLOSED.getValue());
              authShare.setUPermitType(GrantedLevels.READ.getLabel());
              authShare.setUCreateUser(idfSession.getLoginUserName());
              obj = FolderAuthDto.setAuthBaseObjByAuthShare(idfSession, authShare);
              obj.save();
              if (docStatus.equals(DocStatus.CLOSED.getValue())) {
                if(!isVersionUp) idfNewDoc.grant(authorId, GrantedLevels.findByLevel(authShare.getUPermitType()), "");
              }
            } 
          }
        }
      }
//      if (folderAuthDto.getAuthShareList() != null) {
//        for (AuthShare authShare : folderAuthDto.getAuthShareList()) {
//          authShare.setDocStatus(DocStatus.LIVE.getValue());
//          obj = FolderAuthDto.setAuthBaseObjByAuthShare(idfSession, authShare);
//          obj.save();
//          authShare.setDocStatus(DocStatus.CLOSED.getValue());
//          obj = FolderAuthDto.setAuthBaseObjByAuthShare(idfSession, authShare);
//          obj.save();
//          if(!isVersionUp) idfNewDoc.grant(authShare.getUAuthorId(), GrantedLevels.findByLevel(authShare.getUPermitType()), "");
//        }
//      }
    }
  }

  @Override
  public void saveFolderAuths(IDfSession idfSession, FolderAuthDto folderAuthDto) throws Exception {
    IDfPersistentObject obj = null;
    if (folderAuthDto != null) {
      if (folderAuthDto.getAuthBaseList() != null) {
        for (AuthBase authBase : folderAuthDto.getAuthBaseList()) {
          obj = FolderAuthDto.setAuthBaseObj(idfSession, authBase);
          obj.save();
        }
      }
      if (folderAuthDto.getAuthShareList() != null) {
        for (AuthShare authShare : folderAuthDto.getAuthShareList()) {
          obj = FolderAuthDto.setAuthShareObj(idfSession, authShare);
          obj.save();
        }
      }
    }
  }
  
  @Override
  public void saveFolderAuths(String registObjId, IDfSession idfSession, FolderAuthDto folderAuthDto) throws Exception {
    IDfPersistentObject obj = null;
    if (folderAuthDto != null) {
      if (folderAuthDto.getAuthBaseList() != null) {
        for (AuthBase authBase : folderAuthDto.getAuthBaseList()) {
          authBase.setUObjId(registObjId);
          obj = FolderAuthDto.setAuthBaseObj(idfSession, authBase);
          obj.save();
//          if ("D".equals(authBase.getUAuthorType()) && !"G".equals(authBase.getUAddGubun())) {
//            IDfPersistentObject obj2 = FolderAuthDto.setAuthBaseObj(idfSession, authBase);
//            obj2.setString("u_author_id", authBase.getUAuthorId().concat("_sub"));
//            obj2.save();
//          }
        }
      }
      if (folderAuthDto.getAuthShareList() != null) {
        for (AuthShare authShare : folderAuthDto.getAuthShareList()) {
          authShare.setUObjId(registObjId);
          obj = FolderAuthDto.setAuthShareObj(idfSession, authShare);
          obj.save();
//          if ("D".equals(authShare.getUAuthorType())) {
//            obj.setString("u_author_id", "g_" + authShare.getUAuthorId().equals("_sub"));
//            obj.save();
//          }
        }
      }
    }
  }
  
  @Override
  public void makeDocAuths(boolean isVersionUp, String newVersionObjId, String docStatus, IDfDocument idfNewDoc, HamInfoResult hamInfo, FolderAuthDto folderAuthDto, IDfSession idfSession, UserSession userSession) throws DfException {
    IDfPersistentObject obj = null;
    String registObjId = newVersionObjId == null ? idfNewDoc.getObjectId().getId() : newVersionObjId;
    List<String> pjtRschCompare = new ArrayList<>();
    DCTMConstants.DEFAULT_AUTH_COM_ITG_HAM_MAP.forEach((mKey, mVal) -> {
      pjtRschCompare.add( MessageFormat.format(mKey, hamInfo.getMyCode()));
    });
    boolean isDefaultOrgIn = false;
    String gCabinet = "g_".concat(hamInfo.getUCabinetCode());
    if (folderAuthDto != null) {
      if (folderAuthDto.getAuthBaseList() != null) {
        for (AuthBase authBase : folderAuthDto.getAuthBaseList()) {
          if (gCabinet.equals(authBase.getUAuthorId())) isDefaultOrgIn = true; 
          if (!AuthorType.DEFAULT.getValue().equals(authBase.getUAuthorType())) {
            obj = makeAuthBaseObj(idfSession, registObjId, authBase, userSession, true);
            obj.save();
            if (docStatus.equals(authBase.getUDocStatus())) {
              if(!isVersionUp) idfNewDoc.grant(authBase.getUAuthorId(), GrantedLevels.findByLevel(authBase.getUPermitType()), "");
            }
          } else {
            if (pjtRschCompare.contains(authBase.getUAuthorId())) {
              if (docStatus.equals(authBase.getUDocStatus())) {
                if(!isVersionUp) idfNewDoc.grant(authBase.getUAuthorId(), GrantedLevels.findByLevel(authBase.getUPermitType()), "");
              }
            }
          }
        }
      }
      if (folderAuthDto.getAuthShareList() != null) {
        for (AuthShare authShare : folderAuthDto.getAuthShareList()) {
          if (hamInfo.getUCabinetCode().equals(authShare.getUAuthorId())) isDefaultOrgIn = true; 
          if (AuthorType.TEAM.getValue().equals(authShare.getUAuthorType())) {
            authShare.setUAuthorId("g_".concat(authShare.getUAuthorId()));
          } 
          authShare.setDocStatus(DocStatus.LIVE.getValue());
          obj = makeAuthShareObj(idfSession, registObjId, hamInfo.getUCabinetCode(),
              authShare, userSession, true);
          obj.save();
          authShare.setDocStatus(DocStatus.CLOSED.getValue());
          authShare.setUPermitType(GrantedLevels.READ.getLabel());
          obj = makeAuthShareObj(idfSession, registObjId, hamInfo.getUCabinetCode(),
              authShare, userSession, true);
          obj.save();
          if(!isVersionUp) idfNewDoc.grant(authShare.getUAuthorId(), GrantedLevels.findByLevel(authShare.getUPermitType()), "");
        }
      }
      if (!isDefaultOrgIn) idfNewDoc.revoke(gCabinet, null);
    }
  }
  
  @Override
  public void makeFolderAuths(String registObjId, String cabinetCode, FolderAuthDto folderAuthDto, IDfSession idfSession, UserSession userSession) throws DfException {
    IDfPersistentObject obj = null;
    if (folderAuthDto != null) {
      if (folderAuthDto.getAuthBaseList() != null) {
        for (AuthBase authBase : folderAuthDto.getAuthBaseList()) {
          obj = makeAuthBaseObj(idfSession, registObjId, authBase, userSession, false);
          obj.save();
        }
      }
      if (folderAuthDto.getAuthShareList() != null) {
        for (AuthShare authShare : folderAuthDto.getAuthShareList()) {
          obj = makeAuthShareObj(idfSession, registObjId, cabinetCode,
              authShare, userSession, false);
          obj.save();
        }
      }
    }
  }
  
  
  @Override
  public List<FolderAuthResult> selectUserAuthOnFolder(String objectId, String userId) {
    return commonAuthDao.selectUserAuthOnFolder(objectId, userId);
  }

  @Override
  public List<String> selectHamDefaultAuths(String userId) {
    return commonAuthDao.selectHamDefaultAuths(userId);
  }
  
  @Override
  public List<DrmUserDto> selectDocAccesor(String docKey, String docStatus) {
    return commonAuthDao.selectDocAccesor(docKey, docStatus);
  }
  
  @Override
  public List<AuthBaseGroupMembersDto> selectGroupMembersList(String docKey, String docStatus) {
    return commonAuthDao.selectGroupMembersList(docKey, docStatus);
  }
  
  @Override
  public List<DrmAuthorDto> selectDocAdditionalAuthorList(String docKey, String docStatus) {
    return commonAuthDao.selectDocAdditionalAuthorList(docKey, docStatus);
  }

  @Override
  public Optional<HamInfoResult> selectHamInfo(String hamId) {
    return commonAuthDao.selectHamInfo(hamId);
  }

  @Override
  public Optional<HamInfoResult> selectDeptHamInfo(String hamId) {
    return commonAuthDao.selectDeptHamInfo(hamId);
  }
  
  @Override
  public boolean checkDeptRootWriteAuth(String deptCode, String userId) throws Exception {
  	boolean result = commonAuthDao.checkUserInDepts(userId, Arrays.asList(deptCode));
  	
  	return result;
  }
  
  @Override
  public boolean checkCommonDeptRootWriteAuth(String deptCode, String userId) throws Exception {
  	List<String> deptCodeList = deptDao.selectOrgIdRecursiveUsable(deptCode);
  	boolean result =  commonAuthDao.checkUserInDepts(userId, deptCodeList);
  	
  	return result;
  }
  
  @Override
  public boolean checkProjectRootAuth(String pjtCode, String userId, String permitType) throws Exception {
    String lowerPjtCode = pjtCode.toLowerCase();
    List<String> deptCodeList = projectDao.selectDeptCodeListByPjtCode(lowerPjtCode, permitType);
    boolean isChief = projectDao.checkProjectChief(lowerPjtCode, userId);
    boolean isInDept = commonAuthDao.checkUserInDepts(userId, deptCodeList);
    
    return isChief || isInDept;
  }
  
  @Override
  public boolean checkProjectRootAuthByOrgId(String pjtCode, String orgId, String permitType) throws Exception {
    String lowerPjtCode = pjtCode.toLowerCase();
    List<String> deptCodeList = projectDao.selectDeptCodeListByPjtCode(lowerPjtCode, permitType);
    boolean result = deptCodeList.stream().anyMatch(item -> StringUtils.equals(orgId, item));
    
    return result;
  }
  
  @Override
  public boolean checkResearchRootAuth(String rschCode, String userId, String permitType) throws Exception {
    String lowerRschCode = rschCode.toLowerCase();
    List<String> deptCodeList = researchDao.selectDeptCodeListByRschCode(lowerRschCode, permitType);
    boolean isChief = researchDao.checkResearchChief(lowerRschCode, userId);
    boolean isInDept = commonAuthDao.checkUserInDepts(userId, deptCodeList);
    
    return isChief || isInDept;
  }
  
  @Override
  public boolean checkResearchRootAuthByOrgId(String rschCode, String orgId, String permitType) throws Exception {
    String lowerRschCode = rschCode.toLowerCase();
    List<String> deptCodeList = researchDao.selectDeptCodeListByRschCode(lowerRschCode, permitType);
    boolean result = deptCodeList.stream().anyMatch(item -> StringUtils.equals(orgId, item));
    
    return result;
  }

  @Override
  public boolean checkFolderAuth(String folderId, String userId, String permitType) throws Exception {
  	return commonAuthDao.checkFolderAuth(folderId, userId, permitType);
  }
    
  @Override
  public boolean checkDocAuth(String docId, String userId, int level) throws Exception {
  	return commonAuthDao.checkDocAuth(docId, userId, level);
  }
  
  @Override
  public boolean checkFolderOwner(String folderId, String userId) throws Exception {
    return commonAuthDao.checkFolderOwner(folderId, userId);
  }
  
  @Override
  public boolean checkDocOwner(String docId, String userId) throws Exception {
    return commonAuthDao.checkDocOwner(docId, userId);
  }
  
  @Override
  public UserDSearchAuthDto selectDSearchUserAuth(String userId) throws Exception {
    UserDSearchAuthDto userDSearchAuthDto = new UserDSearchAuthDto();
    List<String> groups = commonAuthDao.selectAccesorGroups(userId);
    ManagerConfigDto mgr = managerConfigService.selectManagerConfig(userId);
    
    userDSearchAuthDto.setGroups(groups);
    userDSearchAuthDto.setMgr(mgr);
    
    return userDSearchAuthDto;
  }

  @Override
  public List<AuthShare> selectAuthShareList(String folderObjId) throws Exception {
    return authShareDao.selectDetailList(folderObjId);
  }
  
  
  
  
  
  
  public List<String> getDocBaseGroupsByOrgId(String orgId, String docStatus, String secLevel, String folderType) {
    List<String> groupList = new ArrayList<>();
    SecLevelCode secCode = SecLevelCode.findByValue(secLevel);
    switch(DocStatus.findByValue(docStatus)) {
    case LIVE:
      switch(secCode) {
      
      }
      break;
    case CLOSED:
      break;
    default:
      throw new RuntimeException("authService.getBaseGroupsByOrgId docStatus is unvalid");
    }
    
    
    return groupList;
  }

  @Override
  public List<AuthBase> selectDefaultDocRootAuth(String objType, HamInfoResult ham, VUser user) throws Exception {
    String ownDeptYn = user.getDeptCabinetcode().equals(ham.getUCabinetCode()) ? "y" : "n";
    List<AuthBase> rst = new ArrayList<>();
    // 주관부서 권한
    rst.add(AuthBase.builder().uObjType(objType)
        .uDocStatus(DocStatus.LIVE.getValue())
        .uPermitType(GrantedLevels.DELETE.getLevelCode())
        .uOwnDeptYn("y")
        .uAuthorId(MessageFormat.format("g_{0}", ham.getUCabinetCode()))
        .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
    rst.add(AuthBase.builder().uObjType(objType)
        .uDocStatus(DocStatus.CLOSED.getValue())
        .uPermitType(GrantedLevels.READ.getLabel())
        .uOwnDeptYn("y")
        .uAuthorId(MessageFormat.format("g_{0}", ham.getUCabinetCode()))
        .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
    
    if (HamType.PROJECT.getValue().equals(ham.getHamType())) {
      DCTMConstants.DEFAULT_TEAM_AUTH_COM_PROJECT_MAP.forEach((mKey, mVal) -> {
        rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
            .uDocStatus(DocStatus.LIVE.getValue())
            .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLevelCode()
                : GrantedLevels.DELETE.getLevelCode())
            .uOwnDeptYn(ownDeptYn)
            .uAuthorId(MessageFormat.format(mKey, ham.getMyCode()))
            .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(user.getUserId()).build());
        rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
            .uDocStatus(DocStatus.CLOSED.getValue())
            .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLabel()
                : GrantedLevels.READ.getLabel())
            .uOwnDeptYn(ownDeptYn)
            .uAuthorId(MessageFormat.format(mKey, ham.getMyCode()))
            .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(user.getUserId()).build());
      });
      
      List<ProjectRepeating> prs = projectDao.selectRepeatingListByCode(ham.getMyCode());
      if (!Objects.isNull(prs)) {
        for (ProjectRepeating pr : prs) {
          if (!Objects.isNull(pr.getUJoinDeptRead())) {
            rst.add(AuthBase.builder().uObjType(objType)
                .uDocStatus(DocStatus.LIVE.getValue())
                .uPermitType(GrantedLevels.READ.getLevelCode())
                .uOwnDeptYn(ownDeptYn)
                .uAuthorId(MessageFormat.format("g_{0}", pr.getUJoinDeptReadOrgId())) //문서에는 g_제거
                .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
            rst.add(AuthBase.builder().uObjType(objType)
                .uDocStatus(DocStatus.CLOSED.getValue())
                .uPermitType(GrantedLevels.READ.getLevelCode())
                .uOwnDeptYn(ownDeptYn)
                .uAuthorId(MessageFormat.format("g_{0}", pr.getUJoinDeptReadOrgId())) //문서에는 g_제거
                .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
          }
          if (!Objects.isNull(pr.getUJoinDeptDel())) {
            rst.add(AuthBase.builder().uObjType(objType)
                .uDocStatus(DocStatus.LIVE.getValue())
                .uPermitType(GrantedLevels.DELETE.getLevelCode())
                .uOwnDeptYn(ownDeptYn)
                .uAuthorId(MessageFormat.format("g_{0}", pr.getUJoinDeptDelOrgId())) //문서에는 g_제거
                .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
            rst.add(AuthBase.builder().uObjType(objType)
                .uDocStatus(DocStatus.CLOSED.getValue())
                .uPermitType(GrantedLevels.READ.getLabel())
                .uOwnDeptYn(ownDeptYn)
                .uAuthorId(MessageFormat.format("g_{0}", pr.getUJoinDeptDelOrgId())) //문서에는 g_제거
                .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
          }
        }
      }
    } else if (HamType.RESEARCH.getValue().equals(ham.getHamType())) {
      DCTMConstants.DEFAULT_TEAM_AUTH_COM_RESEARCH_MAP.forEach((mKey, mVal) -> {
        rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
            .uDocStatus(DocStatus.LIVE.getValue())
            .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLevelCode()
                : GrantedLevels.DELETE.getLevelCode())
            .uOwnDeptYn(ownDeptYn)
            .uAuthorId(MessageFormat.format(mKey, ham.getMyCode()))
            .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(user.getUserId()).build());
        rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
            .uDocStatus(DocStatus.CLOSED.getValue())
            .uPermitType(GrantedLevels.BROWSE.getLevel() == mVal.intValue() ? GrantedLevels.BROWSE.getLabel()
                : GrantedLevels.READ.getLabel())
            .uOwnDeptYn(ownDeptYn)
            .uAuthorId(MessageFormat.format(mKey, ham.getMyCode()))
            .uAuthorType(AuthorType.DEFAULT.getValue()).uCreateUser(user.getUserId()).build());
      });
      List<ResearchRepeating> rschs = researchDao.selectRepeatingListByCode(ham.getMyCode());
      if (!Objects.isNull(rschs)) {
        for (ResearchRepeating pr : rschs) {
          if (!Objects.isNull(pr.getUJoinDeptRead())) {
            rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
                .uDocStatus(DocStatus.LIVE.getValue())
                .uPermitType(GrantedLevels.READ.getLevelCode())
                .uOwnDeptYn(ownDeptYn)
                .uAuthorId(MessageFormat.format("g_{0}", pr.getUJoinDeptReadOrgId())) //문서에는 g_제거
                .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
            rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
                .uDocStatus(DocStatus.CLOSED.getValue())
                .uPermitType(GrantedLevels.READ.getLevelCode())
                .uOwnDeptYn(ownDeptYn)
                .uAuthorId(MessageFormat.format("g_{0}", pr.getUJoinDeptReadOrgId())) //문서에는 g_제거
                .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
          }
          if (!Objects.isNull(pr.getUJoinDeptDel())) {
            rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
                .uDocStatus(DocStatus.LIVE.getValue())
                .uPermitType(GrantedLevels.DELETE.getLevelCode())
                .uOwnDeptYn(ownDeptYn)
                .uAuthorId(MessageFormat.format("g_{0}", pr.getUJoinDeptDelOrgId())) //문서에는 g_제거
                .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
            rst.add(AuthBase.builder().uObjType(DboxObjectType.FOLDER.getValue())
                .uDocStatus(DocStatus.CLOSED.getValue())
                .uPermitType(GrantedLevels.READ.getLabel())
                .uOwnDeptYn(ownDeptYn)
                .uAuthorId(MessageFormat.format("g_{0}", pr.getUJoinDeptDelOrgId())) //문서에는 g_제거
                .uAuthorType(AuthorType.TEAM.getValue()).uCreateUser(user.getUserId()).build());
          }
        }
      }
    } 
    return rst;
  }

  
  //작업
  @Override
  public String selectDocAcl(AuthType authType, SecLevelCode secCode, String targetCode) throws Exception {
    String acl = "";
    switch (authType) {
    case DEPT:
      switch (secCode) {
      case LIVE:
        acl = MessageFormat.format(AclTemplate.DEPT_BASIC_ACL_LIVE.getValue(), targetCode).toLowerCase();
        break;
      case SEC:
        acl = MessageFormat.format(AclTemplate.DEPT_BASIC_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      case TEAM:
        acl = MessageFormat.format(AclTemplate.DEPT_BASIC_ACL_TEAM.getValue(), targetCode).toLowerCase();
        break;
      case COMPANY:
        acl = MessageFormat.format(AclTemplate.DEPT_BASIC_ACL_COM.getValue(), targetCode).toLowerCase();
        break;
      case GROUP:
        acl = MessageFormat.format(AclTemplate.DEPT_BASIC_ACL_GROUP.getValue(), targetCode).toLowerCase();
        break;
      default:
        acl = MessageFormat.format(AclTemplate.DEPT_BASIC_ACL_LIVE.getValue(), targetCode).toLowerCase();
        break;
      }
      break;
    case DEPT_WF:
      switch (secCode) {
      case SEC:
        acl = MessageFormat.format(AclTemplate.DEPT_WF_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      case TEAM:
        acl = MessageFormat.format(AclTemplate.DEPT_WF_ACL_TEAM.getValue(), targetCode).toLowerCase();
        break;
      case COMPANY:
        acl = MessageFormat.format(AclTemplate.DEPT_WF_ACL_COM.getValue(), targetCode).toLowerCase();
        break;
      case GROUP:
        acl = MessageFormat.format(AclTemplate.DEPT_WF_ACL_GROUP.getValue(), targetCode).toLowerCase();
        break;
      default:
        acl = MessageFormat.format(AclTemplate.DEPT_WF_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      }
      break;
    case DEPT_PERSONAL:
      switch (secCode) {
      case SEC:
        acl = MessageFormat.format(AclTemplate.DEPT_PRIVACY_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      case LIVE:
        acl = MessageFormat.format(AclTemplate.DEPT_PRIVACY_ACL_LIVE.getValue(), targetCode).toLowerCase();
        break;
      default:
        acl = MessageFormat.format(AclTemplate.DEPT_PRIVACY_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      } 
      break;
    case DEPT_PERSONAL_WF:
      switch (secCode) {
      case SEC:
        acl = MessageFormat.format(AclTemplate.DEPT_PRIVACY_WF_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      case LIVE:
        acl = MessageFormat.format(AclTemplate.DEPT_PRIVACY_WF_ACL_LIVE.getValue(), targetCode).toLowerCase();
        break;
      default:
        acl = MessageFormat.format(AclTemplate.DEPT_PRIVACY_WF_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      } 
      break;
    case DEPT_IMP:
      MessageFormat.format(AclTemplate.DEPT_IMP_ACL.getValue(), targetCode).toLowerCase();
      break;
    case PJT:
      switch (secCode) {
      case LIVE:
        acl = MessageFormat.format(AclTemplate.PJT_BASIC_ACL_LIVE.getValue(), targetCode).toLowerCase();
        break;
      case SEC:
        acl = MessageFormat.format(AclTemplate.PJT_BASIC_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      case TEAM:
        acl = MessageFormat.format(AclTemplate.PJT_BASIC_ACL_TEAM.getValue(), targetCode).toLowerCase();
        break;
      case COMPANY:
        acl = MessageFormat.format(AclTemplate.PJT_BASIC_ACL_COM.getValue(), targetCode).toLowerCase();
        break;
      case GROUP:
        acl = MessageFormat.format(AclTemplate.PJT_BASIC_ACL_GROUP.getValue(), targetCode).toLowerCase();
        break;
      default:
        acl = MessageFormat.format(AclTemplate.PJT_BASIC_ACL_LIVE.getValue(), targetCode).toLowerCase();
        break;
      }
      break;
    case PJT_WF:
      switch (secCode) {
      case SEC:
        acl = MessageFormat.format(AclTemplate.PJT_WF_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      case TEAM:
        acl = MessageFormat.format(AclTemplate.PJT_WF_ACL_TEAM.getValue(), targetCode).toLowerCase();
        break;
      case COMPANY:
        acl = MessageFormat.format(AclTemplate.PJT_WF_ACL_COM.getValue(), targetCode).toLowerCase();
        break;
      case GROUP:
        acl = MessageFormat.format(AclTemplate.PJT_WF_ACL_GROUP.getValue(), targetCode).toLowerCase();
        break;
      default:
        acl = MessageFormat.format(AclTemplate.PJT_WF_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      }
      break;
    case PJT_PERSONAL:
      switch (secCode) {
      case SEC:
        acl = MessageFormat.format(AclTemplate.PJT_PRIVACY_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      case LIVE:
        acl = MessageFormat.format(AclTemplate.PJT_PRIVACY_ACL_LIVE.getValue(), targetCode).toLowerCase();
        break;
      default:
        acl = MessageFormat.format(AclTemplate.PJT_PRIVACY_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      } 
      break;
    case PJT_PERSONAL_WF:
      switch (secCode) {
      case SEC:
        acl = MessageFormat.format(AclTemplate.PJT_PRIVACY_WF_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      case LIVE:
        acl = MessageFormat.format(AclTemplate.PJT_PRIVACY_WF_ACL_LIVE.getValue(), targetCode).toLowerCase();
        break;
      default:
        acl = MessageFormat.format(AclTemplate.PJT_PRIVACY_WF_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      } 
      break;
    case RSCH:
      switch (secCode) {
      case LIVE:
        acl = MessageFormat.format(AclTemplate.RSCH_BASIC_ACL_LIVE.getValue(), targetCode).toLowerCase();
        break;
      case SEC:
        acl = MessageFormat.format(AclTemplate.RSCH_BASIC_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      case TEAM:
        acl = MessageFormat.format(AclTemplate.RSCH_BASIC_ACL_TEAM.getValue(), targetCode).toLowerCase();
        break;
      case COMPANY:
        acl = MessageFormat.format(AclTemplate.RSCH_BASIC_ACL_COM.getValue(), targetCode).toLowerCase();
        break;
      case GROUP:
        acl = MessageFormat.format(AclTemplate.RSCH_BASIC_ACL_GROUP.getValue(), targetCode).toLowerCase();
        break;
      default:
        acl = MessageFormat.format(AclTemplate.RSCH_BASIC_ACL_LIVE.getValue(), targetCode).toLowerCase();
        break;
      }
      break;
    case RSCH_WF:
      switch (secCode) {
      case SEC:
        acl = MessageFormat.format(AclTemplate.RSCH_WF_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      case TEAM:
        acl = MessageFormat.format(AclTemplate.RSCH_WF_ACL_TEAM.getValue(), targetCode).toLowerCase();
        break;
      case COMPANY:
        acl = MessageFormat.format(AclTemplate.RSCH_WF_ACL_COM.getValue(), targetCode).toLowerCase();
        break;
      case GROUP:
        acl = MessageFormat.format(AclTemplate.RSCH_WF_ACL_GROUP.getValue(), targetCode).toLowerCase();
        break;
      default:
        acl = MessageFormat.format(AclTemplate.RSCH_WF_ACL_TEAM.getValue(), targetCode).toLowerCase();
        break;
      }
      break;
    case RSCH_PERSONAL:
      switch (secCode) {
      case SEC:
        acl = MessageFormat.format(AclTemplate.RSCH_PRIVACY_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      case LIVE:
        acl = MessageFormat.format(AclTemplate.RSCH_PRIVACY_ACL_LIVE.getValue(), targetCode).toLowerCase();
        break;
      default:
        acl = MessageFormat.format(AclTemplate.RSCH_PRIVACY_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      } 
      break;
    case RSCH_PERSONAL_WF:
      switch (secCode) {
      case SEC:
        acl = MessageFormat.format(AclTemplate.RSCH_PRIVACY_WF_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      case LIVE:
        acl = MessageFormat.format(AclTemplate.RSCH_PRIVACY_WF_ACL_LIVE.getValue(), targetCode).toLowerCase();
        break;
      default:
        acl = MessageFormat.format(AclTemplate.RSCH_PRIVACY_WF_ACL_SEC.getValue(), targetCode).toLowerCase();
        break;
      } 
      break;
    case COMPANY:
      acl = MessageFormat.format(AclTemplate.COMMON_ACL.getValue(), targetCode).toLowerCase();
      break;
    case C_DKS:
      acl = AclTemplate.DKSM_COMMON_ACL.getValue();
      break;
    case C_ITG:
      acl = AclTemplate.ITG_COMMON_ACL.getValue();
      break;
    default:
    }
    return acl;
  }
  
  //TODO dyyoo
  private List<AuthBase> selectPRAuths(HamInfoResult hamInfo, List<AuthBase> authBaseList, String objectId, DboxObjectType objType,
      UserSession userSession) throws Exception {
    String ownDeptYn = userSession.getUser().getDeptCabinetcode().equals(hamInfo.getUCabinetCode()) ? "Y" : "N";
    if (HamType.PROJECT.getValue().equals(hamInfo.getHamType())) {
      //주관부서 권한
      if (DboxObjectType.DOCUMENT == objType) {
        authBaseList.add(AuthBase.builder().uObjId(objectId).uObjType(objType.getValue())
            .uDocStatus(DocStatus.LIVE.getValue())
            .uPermitType(GrantedLevels.DELETE.getLabel())
            .uOwnDeptYn(ownDeptYn).uAuthorId("g_".concat(hamInfo.getUCabinetCode()))
            .uAuthorType("D")
            .uAddGubun("G")
            .uCreateUser(userSession.getUser().getUserId()).build()); 
        authBaseList.add(AuthBase.builder().uObjId(objectId).uObjType(objType.getValue())
            .uDocStatus(DocStatus.CLOSED.getValue())
            .uPermitType(GrantedLevels.READ.getLabel())
            .uOwnDeptYn(ownDeptYn).uAuthorId("g_".concat(hamInfo.getUCabinetCode()))
            .uAuthorType("D")
            .uAddGubun("G")
            .uCreateUser(userSession.getUser().getUserId()).build()); 
      }
      List<ProjectRepeatDto> pjtRepeatList = projectService.selectRepeatListByCode(hamInfo.getMyCode());
      for (ProjectRepeatDto dto : pjtRepeatList) {
        if (dto.getUJoinDeptReadCabinet() != null) {
          ownDeptYn = userSession.getUser().getDeptCabinetcode().equals(dto.getUJoinDeptReadCabinet()) ? "Y" : "N";
          authBaseList.add(AuthBase.builder().uObjId(objectId).uObjType(objType.getValue())
              .uDocStatus(DocStatus.LIVE.getValue())
              .uPermitType(GrantedLevels.READ.getLabel())
              .uOwnDeptYn(ownDeptYn).uAuthorId("g_".concat(dto.getUJoinDeptReadCabinet()))
              .uAuthorType("D")
              .uAddGubun("J")
              .uCreateUser(userSession.getUser().getUserId()).build()); 
          authBaseList.add(AuthBase.builder().uObjId(objectId).uObjType(objType.getValue())
              .uDocStatus(DocStatus.CLOSED.getValue())
              .uPermitType(GrantedLevels.READ.getLabel())
              .uOwnDeptYn(ownDeptYn).uAuthorId("g_".concat(dto.getUJoinDeptReadCabinet()))
              .uAuthorType("D")
              .uAddGubun("J")
              .uCreateUser(userSession.getUser().getUserId()).build()); 
        }
        if (dto.getUJoinDeptDelCabinet() != null) {
          ownDeptYn = userSession.getUser().getDeptCabinetcode().equals(dto.getUJoinDeptDelCabinet()) ? "Y" : "N";
          authBaseList.add(AuthBase.builder().uObjId(objectId).uObjType(objType.getValue())
              .uDocStatus(DocStatus.LIVE.getValue())
              .uPermitType(GrantedLevels.DELETE.getLabel())
              .uOwnDeptYn(ownDeptYn).uAuthorId("g_".concat(dto.getUJoinDeptDelCabinet()))
              .uAuthorType("D")
              .uAddGubun("J")
              .uCreateUser(userSession.getUser().getUserId()).build()); 
          authBaseList.add(AuthBase.builder().uObjId(objectId).uObjType(objType.getValue())
              .uDocStatus(DocStatus.CLOSED.getValue())
              .uPermitType(GrantedLevels.READ.getLabel())
              .uOwnDeptYn(ownDeptYn).uAuthorId("g_".concat(dto.getUJoinDeptDelCabinet()))
              .uAuthorType("D")
              .uAddGubun("J")
              .uCreateUser(userSession.getUser().getUserId()).build()); 
        }
      }
    } else {
      //주관부서 권한
      if (DboxObjectType.DOCUMENT == objType) {
        authBaseList.add(AuthBase.builder().uObjId(objectId).uObjType(objType.getValue())
            .uDocStatus(DocStatus.LIVE.getValue())
            .uPermitType(GrantedLevels.DELETE.getLabel())
            .uOwnDeptYn(ownDeptYn).uAuthorId("g_".concat(hamInfo.getUCabinetCode()))
            .uAuthorType("D")
            .uAddGubun("G")
            .uCreateUser(userSession.getUser().getUserId()).build()); 
        authBaseList.add(AuthBase.builder().uObjId(objectId).uObjType(objType.getValue())
            .uDocStatus(DocStatus.CLOSED.getValue())
            .uPermitType(GrantedLevels.READ.getLabel())
            .uOwnDeptYn(ownDeptYn).uAuthorId("g_".concat(hamInfo.getUCabinetCode()))
            .uAuthorType("D")
            .uAddGubun("G")
            .uCreateUser(userSession.getUser().getUserId()).build()); 
      }
      List<ResearchRepeatDto> rschRepeatList = researchService.selectRepeatListByCode(hamInfo.getMyCode());
      for (ResearchRepeatDto dto : rschRepeatList) {
        if (dto.getUJoinDeptReadCabinet() != null) {
          ownDeptYn = userSession.getUser().getDeptCabinetcode().equals(dto.getUJoinDeptReadCabinet()) ? "Y" : "N";
          authBaseList.add(AuthBase.builder().uObjId(objectId).uObjType(objType.getValue())
              .uDocStatus(DocStatus.LIVE.getValue())
              .uPermitType(GrantedLevels.READ.getLabel())
              .uOwnDeptYn(ownDeptYn).uAuthorId("g_".concat(dto.getUJoinDeptReadCabinet()))
              .uAuthorType("D")
              .uAddGubun("P")
              .uCreateUser(userSession.getUser().getUserId()).build()); 
          authBaseList.add(AuthBase.builder().uObjId(objectId).uObjType(objType.getValue())
              .uDocStatus(DocStatus.CLOSED.getValue())
              .uPermitType(GrantedLevels.READ.getLabel())
              .uOwnDeptYn(ownDeptYn).uAuthorId("g_".concat(dto.getUJoinDeptReadCabinet()))
              .uAuthorType("D")
              .uAddGubun("P")
              .uCreateUser(userSession.getUser().getUserId()).build()); 
        }
        if (dto.getUJoinDeptDelCabinet() != null) {
          ownDeptYn = userSession.getUser().getDeptCabinetcode().equals(dto.getUJoinDeptDelCabinet()) ? "Y" : "N";
          authBaseList.add(AuthBase.builder().uObjId(objectId).uObjType(objType.getValue())
              .uDocStatus(DocStatus.LIVE.getValue())
              .uPermitType(GrantedLevels.DELETE.getLabel())
              .uOwnDeptYn(ownDeptYn).uAuthorId("g_".concat(dto.getUJoinDeptDelCabinet()))
              .uAuthorType("D")
              .uAddGubun("P")
              .uCreateUser(userSession.getUser().getUserId()).build()); 
          authBaseList.add(AuthBase.builder().uObjId(objectId).uObjType(objType.getValue())
              .uDocStatus(DocStatus.CLOSED.getValue())
              .uPermitType(GrantedLevels.READ.getLabel())
              .uOwnDeptYn(ownDeptYn).uAuthorId("g_".concat(dto.getUJoinDeptDelCabinet()))
              .uAuthorType("D")
              .uAddGubun("P")
              .uCreateUser(userSession.getUser().getUserId()).build()); 
        }
      }
    }
    
    return authBaseList;
  }
  
  @Override
  public FolderAuthDto selectRootFolderAuths(UserSession userSession, AuthType authType, SecLevelCode secCode, String myCode, String objectId) throws Exception {
    FolderAuthDto rst = new FolderAuthDto();
    HamInfoResult hamInfo = commonAuthDao.selectHamInfo(myCode).orElseThrow(() -> new NotFoundException(CheckAuthParam.class, myCode));
    String ownDeptYn = userSession.getUser().getDeptCabinetcode().equals(hamInfo.getUCabinetCode()) ? "Y" : "N";
    
    List<AuthBase> authBaseList = new ArrayList<>();
    this.selectPRAuths(hamInfo, authBaseList, objectId, DboxObjectType.FOLDER, userSession);
    this.setAuthMap(DboxObjectType.FOLDER, authType, secCode, authBaseList, objectId, true, ownDeptYn, userSession.getUser().getUserId(), hamInfo);
    rst.setAuthBaseList(authBaseList);
    return rst;
  }

  @Override
  public FolderAuthDto selectRootFolderAuths(UserSession userSession, AuthType authType, SecLevelCode secCode, String objectId, HamInfoResult hamInfo) throws Exception {
    FolderAuthDto rst = new FolderAuthDto();
    String ownDeptYn = userSession.getUser().getDeptCabinetcode().equals(hamInfo.getUCabinetCode()) ? "Y" : "N";

    List<AuthBase> authBaseList = new ArrayList<>();
    this.selectPRAuths(hamInfo, authBaseList, objectId, DboxObjectType.FOLDER, userSession);
    this.setAuthMap(DboxObjectType.FOLDER, authType, secCode, authBaseList, objectId, true, ownDeptYn, userSession.getUser().getUserId(), hamInfo);
    rst.setAuthBaseList(authBaseList);
    return rst;
  }

  @Override
  public FolderAuthDto selectRootDocAuths(UserSession userSession, AuthType authType, SecLevelCode secCode, String cabinetCode, String objectId, String prCode) throws Exception {
    FolderAuthDto rst = new FolderAuthDto();

    String ownDeptYn = userSession.getUser().getDeptCabinetcode().equals(cabinetCode) ? "Y" : "N";
    String myCode = prCode == null ? cabinetCode : prCode;
    HamInfoResult hamInfo = commonAuthDao.selectDeptHamInfo(myCode).orElse(
        commonAuthDao.selectHamInfo(myCode).orElseThrow(() -> new NotFoundException(CheckAuthParam.class, myCode)));
    UserPresetDetail UserPresetDetail = null;
    List<UserPresetDetail> userPresetList = userPresetDao.selectDetailedOneByFilter(UserPresetFilterDto.builder()
                                                    .uUserId(userSession.getUser().getUserId())
                                                    .uRegBaseFlag(true).build());    

    List<AuthBase> authBaseList = new ArrayList<>();
    boolean isOpened = true;
    if (userPresetList != null) {
      if (userPresetList.size() > 0) {
        UserPresetDetail = userPresetList.get(0);
        this.checkPreset(userSession, objectId, cabinetCode, UserPresetDetail, authBaseList);
        rst.setUserPreset(UserPresetDetail);
        if (UserPresetDetail.getUOpenFlag() != null) {
          isOpened = "1".equals(UserPresetDetail.getUOpenFlag()) ? true:false;
        }
      }
    }

    this.selectPRAuths(hamInfo, authBaseList, objectId, DboxObjectType.DOCUMENT, userSession);
    this.setAuthMap(DboxObjectType.DOCUMENT, authType, secCode, authBaseList, objectId, isOpened, ownDeptYn, userSession.getUser().getUserId(), hamInfo);
    rst.setAuthBaseList(authBaseList);
    this.removeDupleAuths(rst);
    return rst;
  }
  
  @Override
  public FolderAuthDto selectRootDocAuths(UserSession userSession, AuthType authType, SecLevelCode secCode, String objectId, HamInfoResult hamInfo) throws Exception {
    FolderAuthDto rst = new FolderAuthDto();

    String ownDeptYn = userSession.getUser().getDeptCabinetcode().equals(hamInfo.getUCabinetCode()) ? "Y" : "N";
    UserPresetDetail UserPresetDetail = null;
    List<UserPresetDetail> userPresetList = userPresetDao.selectDetailedOneByFilter(UserPresetFilterDto.builder()
                                                    .uUserId(userSession.getUser().getUserId())
                                                    .uRegBaseFlag(true).build());    

    List<AuthBase> authBaseList = new ArrayList<>();
    boolean isOpened = true;
    if (userPresetList != null) {
      if (userPresetList.size() > 0) {
        UserPresetDetail = userPresetList.get(0);
        this.checkPreset(userSession, objectId, hamInfo.getUCabinetCode(), UserPresetDetail, authBaseList);
        rst.setUserPreset(UserPresetDetail);
        if (UserPresetDetail.getUOpenFlag() != null) {
          isOpened = "1".equals(UserPresetDetail.getUOpenFlag()) ? true:false;
        }
      }
    }
    this.selectPRAuths(hamInfo, authBaseList, objectId, DboxObjectType.DOCUMENT, userSession);
    this.setAuthMap(DboxObjectType.DOCUMENT, authType, secCode, authBaseList, objectId, isOpened, ownDeptYn, userSession.getUser().getUserId(), hamInfo);
    rst.setAuthBaseList(authBaseList);
    rst.setUserPreset(UserPresetDetail);
    this.removeDupleAuths(rst);
    return rst;
  }
  
  @Override
  public FolderAuthDto selectRootDocAuths(UserSession userSession, AuthType authType, SecLevelCode secCode, String objectId, HamInfoResult hamInfo, UserPresetDetail UserPresetDetail) throws Exception {
    FolderAuthDto rst = new FolderAuthDto();

    String ownDeptYn = userSession.getUser().getDeptCabinetcode().equals(hamInfo.getUCabinetCode()) ? "Y" : "N";
    //preset에 비공개면
    //authbase에 -> B,  u_obj_type       CHAR(1) (SET LABEL_TEXT='문서/폴더 구분', SET COMMENT_TEXT='D:문서, F:폴더, B:문서권한백업'),
    boolean isOpened = true;
    if (UserPresetDetail.getUOpenFlag() != null) {
      isOpened = "1".equals(UserPresetDetail.getUOpenFlag()) ? true:false;
    }
    List<AuthBase> authBaseList = new ArrayList<>();
    this.selectPRAuths(hamInfo, authBaseList, objectId, DboxObjectType.DOCUMENT, userSession);
    this.setAuthMap(DboxObjectType.DOCUMENT, authType, secCode, authBaseList, objectId, isOpened, ownDeptYn, userSession.getUser().getUserId(), hamInfo);
    this.checkPreset(userSession, objectId, hamInfo.getUCabinetCode(), UserPresetDetail, authBaseList);
    rst.setAuthBaseList(authBaseList);
    rst.setUserPreset(UserPresetDetail);
    this.removeDupleAuths(rst);
    return rst;
  }

  @Override
  public FolderAuthDto selectFolderAuths(UserSession userSession, String upObjectId, String objectId) throws Exception {
    FolderAuthDto rst = this.selectFolderAuth(upObjectId, AuthorType.ALL.getValue());
    return rst;
  }
  
  @Override
  public FolderAuthDto selectDocAuths(UserSession userSession, String cabinetCode, String upObjectId, String objectId) throws Exception {
    FolderAuthDto rst = this.selectFolderAuth(upObjectId, AuthorType.ALL.getValue());
    UserPresetDetail UserPresetDetail = null;
    List<UserPresetDetail> userPresetList = userPresetDao.selectDetailedOneByFilter(UserPresetFilterDto.builder()
                                                    .uUserId(userSession.getUser().getUserId())
                                                    .uRegBaseFlag(true).build());    
    
    if (rst != null) {
      IDfPersistentObject obj = null;
      if (rst.getAuthBaseList() != null) {
        for (AuthBase authBase : rst.getAuthBaseList()) {
          authBase.setUObjId(objectId);
          authBase.setUObjType(DboxObjectType.DOCUMENT.getValue());
        }
      }
      if (rst.getAuthShareList() != null) {
        for (AuthShare authShare : rst.getAuthShareList()) {
          authShare.setUObjId(objectId);
        }
      }
    }
    
    if (userPresetList != null) {
      if (userPresetList.size() > 0) {
        UserPresetDetail = userPresetList.get(0);
        this.checkPreset(userSession, objectId, cabinetCode, UserPresetDetail, rst.getAuthBaseList());
        rst.setUserPreset(UserPresetDetail);
      }
    }
    this.removeDupleAuths(rst);
    return rst;
  }
  
  @Override
  public FolderAuthDto selectDocAuths(UserSession userSession, String cabinetCode, String upObjectId, String objectId, UserPresetDetail UserPresetDetail) throws Exception {
    FolderAuthDto rst = this.selectFolderAuth(upObjectId, AuthorType.ALL.getValue());
    
    if (rst != null) {
      IDfPersistentObject obj = null;
      if (rst.getAuthBaseList() != null) {
        for (AuthBase authBase : rst.getAuthBaseList()) {
          authBase.setUObjId(objectId);
          authBase.setUObjType(DboxObjectType.DOCUMENT.getValue());
        }
      }
      if (rst.getAuthShareList() != null) {
        for (AuthShare authShare : rst.getAuthShareList()) {
          authShare.setUObjId(objectId);
        }
      }
    }
    
    if (UserPresetDetail != null) {
      this.checkPreset(userSession, objectId, cabinetCode, UserPresetDetail, rst.getAuthBaseList());
      rst.setUserPreset(UserPresetDetail);
    }
    this.removeDupleAuths(rst);
    return rst;
  }

  @Override
  public FolderAuthDto selectFolderAuthsBySecLevel(UserSession userSession, AuthType authType, SecLevelCode secCode, String myCode, String objectId) throws Exception {
    FolderAuthDto rst = FolderAuthDto.builder().authBaseList(authBaseDao.selectList(objectId, AuthorType.ALL.getValue()))
        .authShareList(authShareDao.selectList(objectId)).build();

    DocStatus docStatus = DocStatus.LIVE.getValue().equals(secCode.getValue()) ? DocStatus.LIVE : DocStatus.CLOSED;
    HamInfoResult hamInfo = commonAuthDao.selectHamInfo(myCode).orElseThrow(() -> new NotFoundException(CheckAuthParam.class, myCode));
    String ownDeptYn = userSession.getUser().getDeptCabinetcode().equals(hamInfo.getUCabinetCode()) ? "Y" : "N";
    List<AuthBase> authBaseList = new ArrayList<>();
    List<String> delGroupList = this.findDefaultGroup(DboxObjectType.FOLDER, authType, secCode, hamInfo); 
    if (delGroupList.size() != 0 && rst.getAuthBaseList() != null) {
      for (int idx=0; idx<rst.getAuthBaseList().size(); idx++) {
        AuthBase ab = rst.getAuthBaseList().get(idx);
        if (!DocStatus.LIVE.equals(docStatus)) continue;
        for (String delGroup : delGroupList) {
          if (delGroup.equals(ab.getUAuthorId())) {
            rst.getAuthBaseList().remove(idx--);
          } else if (AuthorType.DEFAULT.getValue().equals(ab.getUAuthorType())) {
            rst.getAuthBaseList().remove(idx--);
          }
        }
      }
    }
    this.setAuthMap(DboxObjectType.FOLDER, authType, secCode, authBaseList, objectId, true, ownDeptYn, userSession.getUser().getUserId(), hamInfo);
    rst.setAuthBaseList(authBaseList);
    this.removeDupleAuths(rst);
    return rst;
  }

  @Override
  public FolderAuthDto selectDocAuthsBySecLevel(UserSession userSession, AuthType authType, SecLevelCode secCode, String myCode, String objectId) throws Exception {
    FolderAuthDto rst = FolderAuthDto.builder().authBaseList(authBaseDao.selectList(objectId, AuthorType.ALL.getValue()))
        .authShareList(authShareDao.selectList(objectId)).build();

    DocStatus docStatus = DocStatus.LIVE.getValue().equals(secCode.getValue()) ? DocStatus.LIVE : DocStatus.CLOSED;
    HamInfoResult hamInfo = commonAuthDao.selectHamInfo(myCode).orElseThrow(() -> new NotFoundException(CheckAuthParam.class, myCode));
    String ownDeptYn = userSession.getUser().getDeptCabinetcode().equals(hamInfo.getUCabinetCode()) ? "Y" : "N";
    List<AuthBase> authBaseList = new ArrayList<>();
    
    UserPresetDetail UserPresetDetail = null;
    List<UserPresetDetail> userPresetList = userPresetDao.selectDetailedOneByFilter(UserPresetFilterDto.builder()
                                                    .uUserId(userSession.getUser().getUserId())
                                                    .uRegBaseFlag(true).build());    
    
    boolean isOpened = true;
    if (userPresetList != null) {
      if (userPresetList.size() == 1) {
        UserPresetDetail = userPresetList.get(0);
        this.checkPreset(userSession, objectId, hamInfo.getUCabinetCode(), UserPresetDetail, authBaseList);
        rst.setUserPreset(UserPresetDetail);
        if (UserPresetDetail.getUOpenFlag() != null) {
          isOpened = "1".equals(UserPresetDetail.getUOpenFlag()) ? true:false;
        }
      }
    }
    
    List<String> delGroupList = this.findDefaultGroup(DboxObjectType.DOCUMENT, authType, secCode, hamInfo); 
    if (delGroupList.size() != 0 && rst.getAuthBaseList() != null) {
      for (int idx=0; idx<rst.getAuthBaseList().size(); idx++) {
        AuthBase ab = rst.getAuthBaseList().get(idx);
        if (!DocStatus.LIVE.equals(docStatus)) continue;
        for (String delGroup : delGroupList) {
          if (docStatus.getValue().equals(ab.getUDocStatus())) {
            if (delGroup.equals(ab.getUAuthorId())) {
              rst.getAuthBaseList().remove(idx--);
            } else if (AuthorType.DEFAULT.getValue().equals(ab.getUAuthorType())) {
              rst.getAuthBaseList().remove(idx--);
            }
          }
        }
      }
    }
    this.setAuthMap(DboxObjectType.DOCUMENT, authType, secCode, authBaseList, objectId, isOpened, ownDeptYn, userSession.getUser().getUserId(), hamInfo);
    this.checkPreset(userSession, objectId, hamInfo.getUCabinetCode(), UserPresetDetail, authBaseList);
    rst.setUserPreset(UserPresetDetail);
    this.removeDupleAuths(rst);
    rst.setAuthBaseList(authBaseList);
    return rst;
  }
  
  @Override
  public DrmAuthDto getAuthorsForDrm(VUser user, String entCode, String entName, String docKey, String docStatus, String docSecLevel) throws Exception {
    DrmCompanyDto company = null;
    DrmCompanyId drmComId = DrmCompanyId.findByOrgId(user.getComOrgId());
    company = DrmCompanyDto.builder()
                  .companyId(drmComId.getValue())
                  .companyName(drmComId.name()).build();
    DrmAuthDto drmAuthDto = new DrmAuthDto();
    List<DrmDeptDto> authDeptList = new ArrayList<>();
    List<DrmUserDto> authUserList = new ArrayList<>();
    List<AuthBaseGroupMembersDto> groupMembersList = new ArrayList<>();

    authUserList = this.selectDocAccesor(docKey, docStatus);
    groupMembersList = this.selectGroupMembersList(docKey, docStatus);
    DrmSecLevelType secLevelType = DrmSecLevelType.LIVE;
    if (DocStatus.LIVE.getValue().equals(docStatus)) {
//      secLevelType = DrmSecLevelType.findByValue(docSecLevel);
//      if (DrmSecLevelType.INDIVIDUAL == secLevelType) {
//        secLevelType = DrmSecLevelType.LIVE_CLOSED;
//      }
      //TODO: dooyeon.yoo
      for (AuthBaseGroupMembersDto dto : groupMembersList) {
        authDeptList.add(DrmDeptDto.builder().orgId(dto.getOrgId()).orgNm(dto.getOrgNm()).build());
        if (dto.getGroupMembers() == null) continue;
        List<String> tmpUserList = new ArrayList<>(Arrays.asList(dto.getGroupMembers().split(",")));
        for (int idx=0; idx<authUserList.size(); idx++) {
          if (tmpUserList.indexOf(authUserList.get(idx).getUserId()) != -1) {
            authUserList.remove(idx--);
          }
        }
      }
    } else {
      secLevelType = DrmSecLevelType.findByValue(docSecLevel);
      if (DrmSecLevelType.GROUP == secLevelType) { 
        
      } else {
        for (AuthBaseGroupMembersDto dto : groupMembersList) {
          authDeptList.add(DrmDeptDto.builder().orgId(dto.getOrgId()).orgNm(dto.getOrgNm()).build());
          List<String> tmpUserList = dto.getGroupMembers() != null ? new ArrayList<>(Arrays.asList(dto.getGroupMembers().split(","))) : new ArrayList<>();
          for (int idx=0; idx<authUserList.size(); idx++) {
            if (tmpUserList.indexOf(authUserList.get(idx).getUserId()) != -1) {
              authUserList.remove(idx--);
            }
          }
        }
      }
//      if (DrmSecLevelType.COMPANY == secLevelType) {
//
//        for (AuthBaseGroupMembersDto dto : groupMembersList) {
//          authDeptList.add(DrmDeptDto.builder().orgId(dto.getOrgId()).orgNm(dto.getOrgNm()).build());
//          List<String> tmpUserList = dto.getGroupMembers() != null ? new ArrayList<>(Arrays.asList(dto.getGroupMembers().split(","))) : new ArrayList<>();
//          for (int idx=0; idx<authUserList.size(); idx++) {
//            if (tmpUserList.indexOf(authUserList.get(idx).getUserId()) != -1) {
//              authUserList.remove(idx--);
//            }
//          }
//        }
//      } else if (DrmSecLevelType.INDIVIDUAL == secLevelType) {
//        authUserList = new ArrayList<>();
//        List<DrmAuthorDto> drmAuthList = this.selectDocAdditionalAuthorList(docKey, docStatus);
//        for (DrmAuthorDto ab : drmAuthList) {
//          if ("D".equals(ab.getAuthorType())) {
//            //부서
//            authDeptList.add(DrmDeptDto.builder().orgId(ab.getId()).orgNm(ab.getName()).build());
//          } else {
//            authUserList.add(DrmUserDto.builder().userId(ab.getId()).displayName(ab.getName()).build());
//          }
//        }
//      } else if (DrmSecLevelType.GROUP == secLevelType) {
//        
//      } else {
//        for (AuthBaseGroupMembersDto dto : groupMembersList) {
//          authDeptList.add(DrmDeptDto.builder().orgId(dto.getOrgId()).orgNm(dto.getOrgNm()).build());
//          List<String> tmpUserList = dto.getGroupMembers() != null ? new ArrayList<>(Arrays.asList(dto.getGroupMembers().split(","))) : new ArrayList<>();
//          for (int idx=0; idx<authUserList.size(); idx++) {
//            if (tmpUserList.indexOf(authUserList.get(idx).getUserId()) != -1) {
//              authUserList.remove(idx--);
//            }
//          }
//        }
//      } 
    }
    drmAuthDto.setAuthDeptList(authDeptList);
    drmAuthDto.setAuthUserList(authUserList);
    drmAuthDto.setCompany(company);
    drmAuthDto.setSecLevelType(secLevelType);
    return drmAuthDto;
  }
  
  
  
  private void makeFolderAuthBaseFromMap(Map<String, Integer> map, List<AuthBase> authBaseList, String objectId, String ownDeptYn, String userId, HamInfoResult hamInfo) {
    map.forEach((mKey, mVal) -> {
      String newKey = mKey;
      String authorType = AuthorType.DEFAULT.getValue();
      if (mKey.contains(DCTMConstants.COM_CODE_STR))  {
        newKey = mKey.replace(DCTMConstants.COM_CODE_STR, hamInfo.getComOrgId());
      } else if (mKey.contains(DCTMConstants.CABINET_CODE_STR)) { 
        newKey = mKey.replace(DCTMConstants.CABINET_CODE_STR, hamInfo.getUCabinetCode());
      } else if (mKey.contains(DCTMConstants.PJT_CODE_STR))  {
        newKey = mKey.replace(DCTMConstants.PJT_CODE_STR, hamInfo.getMyCode());
      } else if (mKey.contains(DCTMConstants.RSCH_CODE_STR))  {
        newKey = mKey.replace(DCTMConstants.RSCH_CODE_STR, hamInfo.getMyCode());
      } else if (mKey.contains(DCTMConstants.DEFAULT_CABINET_STR)) { 
        authorType = AuthorType.TEAM.getValue();
        newKey = mKey.replace(DCTMConstants.DEFAULT_CABINET_STR, hamInfo.getUCabinetCode());
      }
      authBaseList.add(AuthBase.builder().uObjId(objectId).uObjType(DboxObjectType.FOLDER.getValue())
          .uDocStatus(DocStatus.LIVE.getValue())
          .uPermitType(GrantedLevels.findLabelByLevel(mVal.intValue()))
          .uOwnDeptYn(ownDeptYn).uAuthorId(newKey)
          .uAuthorType(authorType)
          .uAddGubun(AuthorType.TEAM.getValue().equals(authorType)?"G":null)
          .uCreateUser(userId).build()); 
      if (!AuthorType.DEFAULT.getValue().equals(authorType)) {
        authBaseList.add(AuthBase.builder().uObjId(objectId).uObjType(DboxObjectType.FOLDER.getValue())
            .uDocStatus(DocStatus.CLOSED.getValue())
            .uPermitType(GrantedLevels.READ.getLabel())
            .uOwnDeptYn(ownDeptYn).uAuthorId(newKey)
            .uAuthorType(authorType)
            .uAddGubun("G")
            .uCreateUser(userId).build());
      }
    });
  } 

  private List<String> makeDefaultGruop(Map<String, Integer> map, HamInfoResult hamInfo) {
    List<String> newKeyList = new ArrayList<>();
    for (String mKey : map.keySet()) {
      if (mKey.contains(DCTMConstants.DEFAULT_GROUP_STR)) { 
        newKeyList.add(mKey.replace(DCTMConstants.DEFAULT_GROUP_STR, "dkg"));
      } else if (mKey.contains(DCTMConstants.DEFAULT_COMCODE_STR)) { 
        newKeyList.add(mKey.replace(DCTMConstants.DEFAULT_COMCODE_STR, hamInfo.getComOrgId()));
      } else if (mKey.contains(DCTMConstants.DEFAULT_CABINET_STR)) { 
        newKeyList.add(mKey.replace(DCTMConstants.DEFAULT_CABINET_STR, hamInfo.getUCabinetCode()));
      } 
    }
    return newKeyList;
  } 
  
  private void makeDocAuthBaseFromMap(Map<String, Integer> map, List<AuthBase> authBaseList, String objectId, boolean isOpened, String ownDeptYn, String userId, HamInfoResult hamInfo) {
    map.forEach((mKey, mVal) -> {
      String newKey = mKey;
      String authorType = AuthorType.DEFAULT.getValue();
      boolean isDefault = false;
      if (mKey.contains(DCTMConstants.DEFAULT_GROUP_STR)) { 
//        isDefault = true;
        authorType = AuthorType.GROUP.getValue();
        newKey = mKey.replace(DCTMConstants.DEFAULT_GROUP_STR, "dkg");
      } else if (mKey.contains(DCTMConstants.DEFAULT_COMCODE_STR)) { 
//        isDefault = true;
        authorType = AuthorType.COMPANY.getValue();
        newKey = mKey.replace(DCTMConstants.DEFAULT_COMCODE_STR, hamInfo.getComOrgId());
      } else if (mKey.contains(DCTMConstants.DEFAULT_CABINET_STR)) { 
//        isDefault = true;
        authorType = AuthorType.TEAM.getValue();
        newKey = mKey.replace(DCTMConstants.DEFAULT_CABINET_STR, hamInfo.getUCabinetCode());
      } else if (mKey.contains(DCTMConstants.COM_CODE_STR))  {
        newKey = mKey.replace(DCTMConstants.COM_CODE_STR, hamInfo.getComOrgId());
      } else if (mKey.contains(DCTMConstants.CABINET_CODE_STR)) { 
        newKey = mKey.replace(DCTMConstants.CABINET_CODE_STR, hamInfo.getUCabinetCode());
      } else if (mKey.contains(DCTMConstants.PJT_CODE_STR))  {
        newKey = mKey.replace(DCTMConstants.PJT_CODE_STR, hamInfo.getMyCode());
      } else if (mKey.contains(DCTMConstants.RSCH_CODE_STR))  {
        newKey = mKey.replace(DCTMConstants.RSCH_CODE_STR, hamInfo.getMyCode());
      }
      authBaseList.add(AuthBase.builder().uObjId(objectId).uObjType(isOpened?DboxObjectType.DOCUMENT.getValue():"B")
          .uDocStatus(DocStatus.LIVE.getValue())
          .uPermitType(GrantedLevels.findLabelByLevel(mVal.intValue()))
          .uOwnDeptYn(ownDeptYn).uAuthorId(newKey)
          .uAuthorType(authorType)
          .uAddGubun("G")
          .uCreateUser(userId).build()); 
//      if (isDefault) {
      if (!AuthorType.DEFAULT.getValue().equals(authorType)) {
        authBaseList.add(AuthBase.builder().uObjId(objectId).uObjType(isOpened?DboxObjectType.DOCUMENT.getValue():"B")
            .uDocStatus(DocStatus.CLOSED.getValue())
            .uPermitType(GrantedLevels.READ.getLabel())
            .uOwnDeptYn(ownDeptYn).uAuthorId(newKey)
            .uAuthorType(authorType)
            .uAddGubun("G")
            .uCreateUser(userId).build());
      }
    });
  }
  
  private void checkPreset(UserSession userSession, String objectId, String cabinetCode, UserPresetDetail UserPresetDetail, List<AuthBase> authBaseList) {
    AuthBase presetAuthBase = null;
    if (UserPresetDetail.getUClosedReadAuthorStrList() != null) {
      for (UserPresetRDto rDto : UserPresetDetail.getUClosedReadAuthorStrList()) {
        presetAuthBase = AuthBase.builder().uObjType(DboxObjectType.DOCUMENT.getValue())
            .uObjId(objectId)
            .uDocStatus(DocStatus.CLOSED.getValue())
            .uPermitType(GrantedLevels.READ.getLevelCode())
            .uOwnDeptYn(rDto.getAuthorCabinetCode().equals(cabinetCode)?"Y":"N")
            .uAuthorId(rDto.getAuthor())
            .uAuthorType(rDto.getAuthor().startsWith("g_")?AuthorType.TEAM.getValue():AuthorType.USER.getValue())
            .uCreateUser(userSession.getUser().getUserId()).build();
        authBaseList.add(presetAuthBase);
      }
    }
    if (UserPresetDetail.getULiveReadAuthorStrList() != null) {
      for (UserPresetRDto rDto : UserPresetDetail.getULiveReadAuthorStrList()) {
        presetAuthBase = AuthBase.builder().uObjType(DboxObjectType.DOCUMENT.getValue())
            .uObjId(objectId)
            .uDocStatus(DocStatus.LIVE.getValue())
            .uPermitType(GrantedLevels.READ.getLevelCode())
            .uOwnDeptYn(rDto.getAuthorCabinetCode().equals(cabinetCode)?"Y":"N")
            .uAuthorId(rDto.getAuthor())
            .uAuthorType(rDto.getAuthor().startsWith("g_")?AuthorType.TEAM.getValue():AuthorType.USER.getValue())
            .uCreateUser(userSession.getUser().getUserId()).build();
        authBaseList.add(presetAuthBase);
      }
    }
    if (UserPresetDetail.getULiveDeleteAuthorStrList() != null) {
      for (UserPresetRDto rDto : UserPresetDetail.getULiveDeleteAuthorStrList()) {
        presetAuthBase = AuthBase.builder().uObjType(DboxObjectType.DOCUMENT.getValue())
            .uObjId(objectId)
            .uDocStatus(DocStatus.LIVE.getValue())
            .uPermitType(GrantedLevels.DELETE.getLevelCode())
            .uOwnDeptYn(rDto.getAuthorCabinetCode().equals(cabinetCode)?"Y":"N")
            .uAuthorId(rDto.getAuthor())
            .uAuthorType(rDto.getAuthor().startsWith("g_")?AuthorType.TEAM.getValue():AuthorType.USER.getValue())
            .uCreateUser(userSession.getUser().getUserId()).build();
        authBaseList.add(presetAuthBase);
      }
    }
  } 
  
  private void removeDupleAuths(FolderAuthDto dto) {
    //중복 제거 
    if (dto.getAuthBaseList() != null) {
      for (int idx=0; idx<dto.getAuthBaseList().size()-1; idx++) {
        AuthBase com1 = dto.getAuthBaseList().get(idx);
        for (int jdx=idx+1; jdx<dto.getAuthBaseList().size(); jdx++) {
          AuthBase com2 = dto.getAuthBaseList().get(jdx);
          if (com1.getUDocStatus().equals(com2.getUDocStatus())) {
            if (com1.getUAuthorId().equals(com2.getUAuthorId())) {
              if (GrantedLevels.findByLabel(com1.getUPermitType()) - GrantedLevels.findByLabel(com2.getUPermitType()) >= 0) {
                dto.getAuthBaseList().remove(jdx--);
              } else {
                dto.getAuthBaseList().remove(idx--);
                break;
              }
            }
          }
        }
      }
    }
  }

  /**
   *  
   * 
   * @param DboxObjectType dateObjectType -> D(document) / F(Folder)
   *        AuthType authType -> 권한 분류 타입
   *        SecLevelCode secCode -> S,T,C,G,  LIVE(해당 함수전용)
   *        List<AuthBase> authBaseList -> 기본 권한자
   *        String makeType -> A: 모두,  L: LIVE만, C: ClOSE만
   *        String objectId -> 대상 아이디
   *        String ownDeptYn -> 소유부서 여부
   *        String userId -> 사용자 아이디
   *        HamInfoResult hamInfo -> D(부서), P(프로젝트), R(리서치), C(공용함), M(동국제강-관리, 인터지스-관리직) (함 아이디)
   * @return void
   */
  private void setAuthMap(DboxObjectType dateObjectType, AuthType authType, SecLevelCode secCode, List<AuthBase> authBaseList, String objectId, boolean isOpened, String ownDeptYn, String userId, HamInfoResult hamInfo) {
    if (DboxObjectType.DOCUMENT.equals(dateObjectType)) {
      switch (authType) {
      case DEPT:
        switch (secCode) {
        case LIVE:
          this.makeDocAuthBaseFromMap(DCTMConstants.TEAM_DEFAULT_LIVE_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case SEC:
          this.makeDocAuthBaseFromMap(DCTMConstants.TEAM_DEFAULT_CLOSED_S_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case TEAM:
          this.makeDocAuthBaseFromMap(DCTMConstants.TEAM_DEFAULT_CLOSED_T_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case COMPANY:
          this.makeDocAuthBaseFromMap(DCTMConstants.TEAM_DEFAULT_CLOSED_C_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case GROUP:
          this.makeDocAuthBaseFromMap(DCTMConstants.TEAM_DEFAULT_CLOSED_G_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        default:
          this.makeDocAuthBaseFromMap(DCTMConstants.TEAM_DEFAULT_LIVE_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        }
        break;
      case DEPT_WF:
        switch (secCode) {
        case SEC:
          this.makeDocAuthBaseFromMap(DCTMConstants.TEAM_WF_CLOSED_S_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case TEAM:
          this.makeDocAuthBaseFromMap(DCTMConstants.TEAM_WF_CLOSED_T_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case COMPANY:
          this.makeDocAuthBaseFromMap(DCTMConstants.TEAM_WF_CLOSED_C_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case GROUP:
          this.makeDocAuthBaseFromMap(DCTMConstants.TEAM_WF_CLOSED_G_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        default:
          this.makeDocAuthBaseFromMap(DCTMConstants.TEAM_WF_CLOSED_T_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        }
        break;
      case DEPT_PERSONAL:
        switch (secCode) {
        case SEC:
          this.makeDocAuthBaseFromMap(DCTMConstants.TEAM_PERSONAL_CLOSED_S_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case LIVE:
          this.makeDocAuthBaseFromMap(DCTMConstants.TEAM_PERSONAL_LIVE_T_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        default:
          this.makeDocAuthBaseFromMap(DCTMConstants.TEAM_PERSONAL_CLOSED_S_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        } 
        break;
      case DEPT_PERSONAL_WF:
        switch (secCode) {
        case SEC:
          this.makeDocAuthBaseFromMap(DCTMConstants.TEAM_PERSONAL_WF_CLOSED_S_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case LIVE:
          this.makeDocAuthBaseFromMap(DCTMConstants.TEAM_PERSONAL_WF_LIVE_T_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        default:
          this.makeDocAuthBaseFromMap(DCTMConstants.TEAM_PERSONAL_WF_CLOSED_S_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        } 
        break;
      case DEPT_IMP:
        this.makeDocAuthBaseFromMap(DCTMConstants.TEAM_IMP_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
        break;
      case PJT:
        switch (secCode) {
        case LIVE:
          this.makeDocAuthBaseFromMap(DCTMConstants.PJT_DEFAULT_LIVE_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case SEC:
          this.makeDocAuthBaseFromMap(DCTMConstants.PJT_DEFAULT_CLOSED_S_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case TEAM:
          this.makeDocAuthBaseFromMap(DCTMConstants.PJT_DEFAULT_CLOSED_T_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case COMPANY:
          this.makeDocAuthBaseFromMap(DCTMConstants.PJT_DEFAULT_CLOSED_C_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case GROUP:
          this.makeDocAuthBaseFromMap(DCTMConstants.PJT_DEFAULT_CLOSED_G_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        default:
          this.makeDocAuthBaseFromMap(DCTMConstants.PJT_DEFAULT_LIVE_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        }
        break;
      case PJT_WF:
        switch (secCode) {
        case SEC:
          this.makeDocAuthBaseFromMap(DCTMConstants.PJT_WF_CLOSED_S_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case TEAM:
          this.makeDocAuthBaseFromMap(DCTMConstants.PJT_WF_CLOSED_T_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case COMPANY:
          this.makeDocAuthBaseFromMap(DCTMConstants.PJT_WF_CLOSED_C_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case GROUP:
          this.makeDocAuthBaseFromMap(DCTMConstants.PJT_WF_CLOSED_G_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        default:
          this.makeDocAuthBaseFromMap(DCTMConstants.PJT_WF_CLOSED_T_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        }
        break;
      case PJT_PERSONAL:
        switch (secCode) {
        case SEC:
          this.makeDocAuthBaseFromMap(DCTMConstants.PJT_PERSONAL_CLOSED_S_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case LIVE:
          this.makeDocAuthBaseFromMap(DCTMConstants.PJT_PERSONAL_LIVE_T_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        default:
          this.makeDocAuthBaseFromMap(DCTMConstants.PJT_PERSONAL_CLOSED_S_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        } 
        break;
      case PJT_PERSONAL_WF:
        switch (secCode) {
        case SEC:
          this.makeDocAuthBaseFromMap(DCTMConstants.PJT_PERSONAL_WF_CLOSED_S_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case LIVE:
          this.makeDocAuthBaseFromMap(DCTMConstants.PJT_PERSONAL_WF_LIVE_T_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        default:
          this.makeDocAuthBaseFromMap(DCTMConstants.PJT_PERSONAL_WF_CLOSED_S_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        } 
        break;
      case RSCH:
        switch (secCode) {
        case LIVE:
          this.makeDocAuthBaseFromMap(DCTMConstants.RSCH_DEFAULT_LIVE_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case SEC:
          this.makeDocAuthBaseFromMap(DCTMConstants.RSCH_DEFAULT_CLOSED_S_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case TEAM:
          this.makeDocAuthBaseFromMap(DCTMConstants.RSCH_DEFAULT_CLOSED_T_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case COMPANY:
          this.makeDocAuthBaseFromMap(DCTMConstants.RSCH_DEFAULT_CLOSED_C_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case GROUP:
          this.makeDocAuthBaseFromMap(DCTMConstants.RSCH_DEFAULT_CLOSED_G_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        default:
          this.makeDocAuthBaseFromMap(DCTMConstants.RSCH_DEFAULT_LIVE_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        }
        break;
      case RSCH_WF:
        switch (secCode) {
        case SEC:
          this.makeDocAuthBaseFromMap(DCTMConstants.RSCH_WF_CLOSED_S_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case TEAM:
          this.makeDocAuthBaseFromMap(DCTMConstants.RSCH_WF_CLOSED_T_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case COMPANY:
          this.makeDocAuthBaseFromMap(DCTMConstants.RSCH_WF_CLOSED_C_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case GROUP:
          this.makeDocAuthBaseFromMap(DCTMConstants.RSCH_WF_CLOSED_G_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        default:
          this.makeDocAuthBaseFromMap(DCTMConstants.RSCH_WF_CLOSED_T_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        }
        break;
      case RSCH_PERSONAL:
        switch (secCode) {
        case SEC:
          this.makeDocAuthBaseFromMap(DCTMConstants.RSCH_PERSONAL_CLOSED_S_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case LIVE:
          this.makeDocAuthBaseFromMap(DCTMConstants.RSCH_PERSONAL_LIVE_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        default:
          this.makeDocAuthBaseFromMap(DCTMConstants.RSCH_PERSONAL_CLOSED_S_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        } 
        break;
      case RSCH_PERSONAL_WF:
        switch (secCode) {
        case SEC:
          this.makeDocAuthBaseFromMap(DCTMConstants.RSCH_PERSONAL_WF_CLOSED_S_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        case LIVE:
          this.makeDocAuthBaseFromMap(DCTMConstants.RSCH_PERSONAL_WF_LIVE_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        default:
          this.makeDocAuthBaseFromMap(DCTMConstants.RSCH_PERSONAL_WF_CLOSED_S_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
          break;
        } 
        break;
      case COMPANY:
        this.makeDocAuthBaseFromMap(DCTMConstants.COM_HAM_AUTH_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
        break;
      case C_DKS:
        this.makeDocAuthBaseFromMap(DCTMConstants.COM_DKS_HAM_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
        break;
      case C_ITG:
        this.makeDocAuthBaseFromMap(DCTMConstants.COM_ITG_HAM_MAP, authBaseList, objectId, isOpened, ownDeptYn, userId, hamInfo);
        break;
      default:
      }
    } else {
      switch (authType) {
      case DEPT:
        this.makeFolderAuthBaseFromMap(DCTMConstants.TEAM_FOLDER_AUTH_MAP, authBaseList, objectId, ownDeptYn, userId, hamInfo);
        break;
      case DEPT_WF:
        this.makeFolderAuthBaseFromMap(DCTMConstants.TEAM_FOLDER_WF_AUTH_MAP, authBaseList, objectId, ownDeptYn, userId, hamInfo);
        break;
      case DEPT_IMP:
        this.makeFolderAuthBaseFromMap(DCTMConstants.TEAM_FOLDER_IMP_AUTH_MAP, authBaseList, objectId, ownDeptYn, userId, hamInfo);
        break;
      case PJT:
        this.makeFolderAuthBaseFromMap(DCTMConstants.PJT_FOLDER_AUTH_MAP, authBaseList, objectId, ownDeptYn, userId, hamInfo);
        break;
      case RSCH:
        this.makeFolderAuthBaseFromMap(DCTMConstants.RSCH_FOLDER_AUTH_MAP, authBaseList, objectId, ownDeptYn, userId, hamInfo);
        break;
      case COMPANY:
        this.makeFolderAuthBaseFromMap(DCTMConstants.COM_FOLDER_HAM_AUTH_MAP, authBaseList, objectId, ownDeptYn, userId, hamInfo);
        break;
      case C_DKS:
        this.makeFolderAuthBaseFromMap(DCTMConstants.DEFAULT_FOLDER_COM_DKS_HAM_MAP, authBaseList, objectId, ownDeptYn, userId, hamInfo);
        break;
      case C_ITG:
        this.makeFolderAuthBaseFromMap(DCTMConstants.DEFAULT_FOLDER_COM_ITG_HAM_MAP, authBaseList, objectId, ownDeptYn, userId, hamInfo);
        break;
      default:
      }
    }
  }
  
  private List<String> findDefaultGroup(DboxObjectType dateObjectType, AuthType authType, SecLevelCode secCode, HamInfoResult hamInfo) {
    if (DboxObjectType.DOCUMENT.equals(dateObjectType)) {
      switch (authType) {
      case DEPT:
        switch (secCode) {
        case LIVE:
          return this.makeDefaultGruop(DCTMConstants.TEAM_DEFAULT_LIVE_AUTH_MAP, hamInfo);
        case SEC:
          return this.makeDefaultGruop(DCTMConstants.TEAM_DEFAULT_CLOSED_S_AUTH_MAP, hamInfo);
        case TEAM:
          return this.makeDefaultGruop(DCTMConstants.TEAM_DEFAULT_CLOSED_T_AUTH_MAP, hamInfo);
        case COMPANY:
          return this.makeDefaultGruop(DCTMConstants.TEAM_DEFAULT_CLOSED_C_AUTH_MAP, hamInfo);
        case GROUP:
          return this.makeDefaultGruop(DCTMConstants.TEAM_DEFAULT_CLOSED_G_AUTH_MAP, hamInfo);
        default:
          return this.makeDefaultGruop(DCTMConstants.TEAM_DEFAULT_LIVE_AUTH_MAP, hamInfo);
        }
      case DEPT_WF:
        switch (secCode) {
        case SEC:
          return this.makeDefaultGruop(DCTMConstants.TEAM_WF_CLOSED_S_AUTH_MAP, hamInfo);
        case TEAM:
          return this.makeDefaultGruop(DCTMConstants.TEAM_WF_CLOSED_T_AUTH_MAP, hamInfo);
        case COMPANY:
          return this.makeDefaultGruop(DCTMConstants.TEAM_WF_CLOSED_C_AUTH_MAP, hamInfo);
        case GROUP:
          return this.makeDefaultGruop(DCTMConstants.TEAM_WF_CLOSED_G_AUTH_MAP, hamInfo);
        default:
          return this.makeDefaultGruop(DCTMConstants.TEAM_WF_CLOSED_T_AUTH_MAP, hamInfo);
        }
      case DEPT_PERSONAL:
        switch (secCode) {
        case SEC:
          return this.makeDefaultGruop(DCTMConstants.TEAM_PERSONAL_CLOSED_S_AUTH_MAP, hamInfo);
        case LIVE:
          return this.makeDefaultGruop(DCTMConstants.TEAM_PERSONAL_LIVE_T_AUTH_MAP, hamInfo);
        default:
          return this.makeDefaultGruop(DCTMConstants.TEAM_PERSONAL_CLOSED_S_AUTH_MAP, hamInfo);
        } 
      case DEPT_PERSONAL_WF:
        switch (secCode) {
        case SEC:
          return this.makeDefaultGruop(DCTMConstants.TEAM_PERSONAL_WF_CLOSED_S_AUTH_MAP, hamInfo);
        case LIVE:
          return this.makeDefaultGruop(DCTMConstants.TEAM_PERSONAL_WF_LIVE_T_AUTH_MAP, hamInfo);
        default:
          return this.makeDefaultGruop(DCTMConstants.TEAM_PERSONAL_WF_CLOSED_S_AUTH_MAP, hamInfo);
        } 
      case DEPT_IMP:
        return this.makeDefaultGruop(DCTMConstants.TEAM_IMP_AUTH_MAP, hamInfo);
      case PJT:
        switch (secCode) {
        case LIVE:
          return this.makeDefaultGruop(DCTMConstants.PJT_DEFAULT_LIVE_AUTH_MAP, hamInfo);
        case SEC:
          return this.makeDefaultGruop(DCTMConstants.PJT_DEFAULT_CLOSED_S_AUTH_MAP, hamInfo);
        case TEAM:
          return this.makeDefaultGruop(DCTMConstants.PJT_DEFAULT_CLOSED_T_AUTH_MAP, hamInfo);
        case COMPANY:
          return this.makeDefaultGruop(DCTMConstants.PJT_DEFAULT_CLOSED_C_AUTH_MAP, hamInfo);
        case GROUP:
          return this.makeDefaultGruop(DCTMConstants.PJT_DEFAULT_CLOSED_G_AUTH_MAP, hamInfo);
        default:
          return this.makeDefaultGruop(DCTMConstants.PJT_DEFAULT_LIVE_AUTH_MAP, hamInfo);
        }
      case PJT_WF:
        switch (secCode) {
        case SEC:
          return this.makeDefaultGruop(DCTMConstants.PJT_WF_CLOSED_S_AUTH_MAP, hamInfo);
        case TEAM:
          return this.makeDefaultGruop(DCTMConstants.PJT_WF_CLOSED_T_AUTH_MAP, hamInfo);
        case COMPANY:
          return this.makeDefaultGruop(DCTMConstants.PJT_WF_CLOSED_C_AUTH_MAP, hamInfo);
        case GROUP:
          return this.makeDefaultGruop(DCTMConstants.PJT_WF_CLOSED_G_AUTH_MAP, hamInfo);
        default:
          return this.makeDefaultGruop(DCTMConstants.PJT_WF_CLOSED_T_AUTH_MAP, hamInfo);
        }
      case PJT_PERSONAL:
        switch (secCode) {
        case SEC:
          return this.makeDefaultGruop(DCTMConstants.PJT_PERSONAL_CLOSED_S_AUTH_MAP, hamInfo);
        case LIVE:
          return this.makeDefaultGruop(DCTMConstants.PJT_PERSONAL_LIVE_T_AUTH_MAP, hamInfo);
        default:
          return this.makeDefaultGruop(DCTMConstants.PJT_PERSONAL_CLOSED_S_AUTH_MAP, hamInfo);
        } 
      case PJT_PERSONAL_WF:
        switch (secCode) {
        case SEC:
          return this.makeDefaultGruop(DCTMConstants.PJT_PERSONAL_WF_CLOSED_S_AUTH_MAP, hamInfo);
        case LIVE:
          return this.makeDefaultGruop(DCTMConstants.PJT_PERSONAL_WF_LIVE_T_AUTH_MAP, hamInfo);
        default:
          return this.makeDefaultGruop(DCTMConstants.PJT_PERSONAL_WF_CLOSED_S_AUTH_MAP, hamInfo);
        } 
      case RSCH:
        switch (secCode) {
        case LIVE:
          return this.makeDefaultGruop(DCTMConstants.RSCH_DEFAULT_LIVE_AUTH_MAP, hamInfo);
        case SEC:
          return this.makeDefaultGruop(DCTMConstants.RSCH_DEFAULT_CLOSED_S_AUTH_MAP, hamInfo);
        case TEAM:
          return this.makeDefaultGruop(DCTMConstants.RSCH_DEFAULT_CLOSED_T_AUTH_MAP, hamInfo);
        case COMPANY:
          return this.makeDefaultGruop(DCTMConstants.RSCH_DEFAULT_CLOSED_C_AUTH_MAP, hamInfo);
        case GROUP:
          return this.makeDefaultGruop(DCTMConstants.RSCH_DEFAULT_CLOSED_G_AUTH_MAP, hamInfo);
        default:
          return this.makeDefaultGruop(DCTMConstants.RSCH_DEFAULT_LIVE_AUTH_MAP, hamInfo);
        }
      case RSCH_WF:
        switch (secCode) {
        case SEC:
          return this.makeDefaultGruop(DCTMConstants.RSCH_WF_CLOSED_S_AUTH_MAP, hamInfo);
        case TEAM:
          return this.makeDefaultGruop(DCTMConstants.RSCH_WF_CLOSED_T_AUTH_MAP, hamInfo);
        case COMPANY:
          return this.makeDefaultGruop(DCTMConstants.RSCH_WF_CLOSED_C_AUTH_MAP, hamInfo);
        case GROUP:
          return this.makeDefaultGruop(DCTMConstants.RSCH_WF_CLOSED_G_AUTH_MAP, hamInfo);
        default:
          return this.makeDefaultGruop(DCTMConstants.RSCH_WF_CLOSED_T_AUTH_MAP, hamInfo);
        }
      case RSCH_PERSONAL:
        switch (secCode) {
        case SEC:
          return this.makeDefaultGruop(DCTMConstants.RSCH_PERSONAL_CLOSED_S_AUTH_MAP, hamInfo);
        case LIVE:
          return this.makeDefaultGruop(DCTMConstants.RSCH_PERSONAL_LIVE_AUTH_MAP, hamInfo);
        default:
          return this.makeDefaultGruop(DCTMConstants.RSCH_PERSONAL_CLOSED_S_AUTH_MAP, hamInfo);
        } 
      case RSCH_PERSONAL_WF:
        switch (secCode) {
        case SEC:
          return this.makeDefaultGruop(DCTMConstants.RSCH_PERSONAL_WF_CLOSED_S_AUTH_MAP, hamInfo);
        case LIVE:
          return this.makeDefaultGruop(DCTMConstants.RSCH_PERSONAL_WF_LIVE_AUTH_MAP, hamInfo);
        default:
          return this.makeDefaultGruop(DCTMConstants.RSCH_PERSONAL_WF_CLOSED_S_AUTH_MAP, hamInfo);
        } 
      case COMPANY:
        return this.makeDefaultGruop(DCTMConstants.COM_HAM_AUTH_MAP, hamInfo);
      case C_DKS:
        return this.makeDefaultGruop(DCTMConstants.COM_DKS_HAM_MAP, hamInfo);
      case C_ITG:
        return this.makeDefaultGruop(DCTMConstants.COM_ITG_HAM_MAP, hamInfo);
      default:
      }
    } else {
      switch (authType) {
      case DEPT:
        return this.makeDefaultGruop(DCTMConstants.TEAM_FOLDER_AUTH_MAP, hamInfo);
      case DEPT_WF:
        return this.makeDefaultGruop(DCTMConstants.TEAM_FOLDER_WF_AUTH_MAP, hamInfo);
      case DEPT_IMP:
        return this.makeDefaultGruop(DCTMConstants.TEAM_FOLDER_IMP_AUTH_MAP, hamInfo);
      case PJT:
        return this.makeDefaultGruop(DCTMConstants.PJT_FOLDER_AUTH_MAP, hamInfo);
      case RSCH:
        return this.makeDefaultGruop(DCTMConstants.RSCH_FOLDER_AUTH_MAP, hamInfo);
      case COMPANY:
        return this.makeDefaultGruop(DCTMConstants.COM_FOLDER_HAM_AUTH_MAP, hamInfo);
      case C_DKS:
        return this.makeDefaultGruop(DCTMConstants.DEFAULT_FOLDER_COM_DKS_HAM_MAP, hamInfo);
      case C_ITG:
        return this.makeDefaultGruop(DCTMConstants.DEFAULT_FOLDER_COM_ITG_HAM_MAP, hamInfo);
      default:
      }
    }
    return new ArrayList<String>();
  }
}















