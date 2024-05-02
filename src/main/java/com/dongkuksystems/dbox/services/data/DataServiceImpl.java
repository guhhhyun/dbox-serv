package com.dongkuksystems.dbox.services.data;
 
import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfTime;
import com.dongkuksystems.dbox.constants.AclTemplate;
import com.dongkuksystems.dbox.constants.AuthObjType;
import com.dongkuksystems.dbox.constants.AuthType;
import com.dongkuksystems.dbox.constants.AuthorType;
import com.dongkuksystems.dbox.constants.CodeType;
import com.dongkuksystems.dbox.constants.DCTMConstants;
import com.dongkuksystems.dbox.constants.DataObjectType;
import com.dongkuksystems.dbox.constants.DboxObjectType;
import com.dongkuksystems.dbox.constants.DocFlag;
import com.dongkuksystems.dbox.constants.DocLogItem;
import com.dongkuksystems.dbox.constants.DocStatus;
import com.dongkuksystems.dbox.constants.FolderStatus;
import com.dongkuksystems.dbox.constants.FolderType;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.constants.HamSearchType;
import com.dongkuksystems.dbox.constants.HamType;
import com.dongkuksystems.dbox.constants.NotiItem;
import com.dongkuksystems.dbox.constants.ProjectType;
import com.dongkuksystems.dbox.constants.SecLevelCode;
import com.dongkuksystems.dbox.constants.UploadFlag;
import com.dongkuksystems.dbox.daos.table.etc.gwdept.GwDeptDao;
import com.dongkuksystems.dbox.daos.table.etc.gwuser.GwUserDao;
import com.dongkuksystems.dbox.daos.table.takeout.ReqTakeoutDao;
import com.dongkuksystems.dbox.daos.type.auth.CommonAuthDao;
import com.dongkuksystems.dbox.daos.type.auth.base.AuthBaseDao;
import com.dongkuksystems.dbox.daos.type.auth.share.AuthShareDao;
import com.dongkuksystems.dbox.daos.type.doc.DocDao;
import com.dongkuksystems.dbox.daos.type.doc.DocImpDao;
import com.dongkuksystems.dbox.daos.type.folder.FolderDao;
import com.dongkuksystems.dbox.daos.type.manager.attachpolicy.AttachPolicyDao;
import com.dongkuksystems.dbox.daos.type.manager.doctemplate.DocTemplateDao;
import com.dongkuksystems.dbox.daos.type.manager.noticonfig.NotiConfigDao;
import com.dongkuksystems.dbox.daos.type.manager.preservationperiod.PreservationPeriodDao;
import com.dongkuksystems.dbox.daos.type.sysobject.SysObjectDao;
import com.dongkuksystems.dbox.daos.type.user.preset.UserPresetDao;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.errors.DupleRequestException;
import com.dongkuksystems.dbox.errors.ForbiddenException;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.errors.upload.UploadAuthException;
import com.dongkuksystems.dbox.errors.upload.UploadExtensionException;
import com.dongkuksystems.dbox.errors.upload.UploadLockException;
import com.dongkuksystems.dbox.errors.upload.UploadNameLengthException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.etc.CustomInputStreamResource;
import com.dongkuksystems.dbox.models.dto.etc.DBoxSearch;
import com.dongkuksystems.dbox.models.dto.type.auth.AuthBaseUpdateDto;
import com.dongkuksystems.dbox.models.dto.type.auth.AuthShareDto;
import com.dongkuksystems.dbox.models.dto.type.auth.AuthShareUpdateDto;
import com.dongkuksystems.dbox.models.dto.type.auth.CheckAuthParam;
import com.dongkuksystems.dbox.models.dto.type.auth.DrmAuthDto;
import com.dongkuksystems.dbox.models.dto.type.auth.FolderAuthDto;
import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult;
import com.dongkuksystems.dbox.models.dto.type.auth.RegistAuthShareDto;
import com.dongkuksystems.dbox.models.dto.type.code.CodeDetailDto;
import com.dongkuksystems.dbox.models.dto.type.code.CodeFilterDto;
import com.dongkuksystems.dbox.models.dto.type.data.DataCreatorDto;
import com.dongkuksystems.dbox.models.dto.type.data.DataDetailDto;
import com.dongkuksystems.dbox.models.dto.type.data.DataUpdateBatchReqDto;
import com.dongkuksystems.dbox.models.dto.type.data.DataUpdateReqDto;
import com.dongkuksystems.dbox.models.dto.type.data.RegistDataDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocDescendantDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocDetailDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocFilterDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocImpFilterDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocLinkListDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocVersionListDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectCreateDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectFilterDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchCreateDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchFilterDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDescendantDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDetailDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderFilterDto;
import com.dongkuksystems.dbox.models.dto.type.folder.RegistFolderDto;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedKUploadFile;
import com.dongkuksystems.dbox.models.dto.type.upload.UploadDocDto;
import com.dongkuksystems.dbox.models.dto.type.user.UserPresetFilterDto;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.type.auth.AuthBase;
import com.dongkuksystems.dbox.models.type.auth.AuthShare;
import com.dongkuksystems.dbox.models.type.code.Code;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.doc.DocImp;
import com.dongkuksystems.dbox.models.type.doc.DocTemplate;
import com.dongkuksystems.dbox.models.type.docbox.Project;
import com.dongkuksystems.dbox.models.type.docbox.Research;
import com.dongkuksystems.dbox.models.type.folder.Folder;
import com.dongkuksystems.dbox.models.type.manager.noticonfig.NotiConfig;
import com.dongkuksystems.dbox.models.type.user.UserPresetDetail;
import com.dongkuksystems.dbox.repositories.redis.RedisRepository;
import com.dongkuksystems.dbox.securities.JWT;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.auth.AuthService;
import com.dongkuksystems.dbox.services.cache.CacheService;
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.dboxsearch.DBoxSearchService;
import com.dongkuksystems.dbox.services.doc.DocImpService;
import com.dongkuksystems.dbox.services.doc.DocService;
import com.dongkuksystems.dbox.services.drm.DrmService;
import com.dongkuksystems.dbox.services.folder.FolderService;
import com.dongkuksystems.dbox.services.manager.lockeddata.LockedDataService;
import com.dongkuksystems.dbox.services.notification.NotificationService;
import com.dongkuksystems.dbox.services.project.ProjectService;
//import com.dongkuksystems.dbox.services.recycle.RecycleService;
import com.dongkuksystems.dbox.services.research.ResearchService;
import com.dongkuksystems.dbox.services.takeout.TakeoutReqService;
import com.dongkuksystems.dbox.services.user.UserService;
import com.dongkuksystems.dbox.utils.CommonUtils;
import com.dongkuksystems.dbox.utils.DboxStringUtils;
import com.dongkuksystems.dbox.utils.dctm.AES256Util;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DataServiceImpl extends AbstractCommonService implements DataService {
  @Value("${jwt.token.header}")
  private String tokenHeader;
  @Value("${kupload.base-path}")
  private String kuploadBasePath;
  
  private final CacheService cacheService;
  private final FolderService folderService;
  //private final RecycleService recycleService;
  private final DocService docService;
  private final ProjectService projectService;
  private final ResearchService researchService;
  private final AuthService authService;
  private final CodeService codeService;
  private final TakeoutReqService takeoutReqService;
  private final SysObjectDao sysObjectDao;
  private final DocTemplateDao docTemplateDao;
  private final GwDeptDao gwDeptDao;
  private final ReqTakeoutDao reqTakeoutDao;
  private final DocImpService docImpService;
  private final DrmService drmService;
  private final DocDao docDao;
  private final FolderDao folderDao;
  private final CommonAuthDao commonAuthDao;
  private final NotificationService notificationService;
  private final GwUserDao gwUserDao;
  private final DocImpDao docImpDao;
  private final AuthBaseDao authBaseDao;
  private final AuthShareDao authShareDao;
  private final UserPresetDao userPresetDao;
  private final AttachPolicyDao attachPolicyDao;
  private final PreservationPeriodDao preservationPeriodDao;
  private final LockedDataService lockedDataService;
  private final NotiConfigDao notiConfigDao;
  private final RedisRepository redisRepository;
  private final JWT jwt;
  private ObjectMapper objectMapper;

  @Autowired
  private DBoxSearchService dBoxSearchService;

  public DataServiceImpl(
      //RecycleService  recycleService,
      CacheService cacheService,
      FolderService folderService,
      DocService docService,
      ProjectService projectService,
      ResearchService researchService,
      AuthService authService,
      CodeService codeService,
      TakeoutReqService takeoutReqService,
      SysObjectDao sysObjectDao,
      DocTemplateDao docTemplateDao,
      GwDeptDao gwDeptDao,
      UserService userSevice,
      ReqTakeoutDao reqTakeoutDao,
      DocImpService docImpService,
      FolderDao folderDao,
      DrmService drmService,
      DocDao docDao,
      CommonAuthDao commonAuthDao,
      NotificationService notificationService,
      GwUserDao gwUserDao,
      DocImpDao docImpDao,
      AuthBaseDao authBaseDao,
      AuthShareDao authShareDao,
      UserPresetDao userPresetDao,
      LockedDataService lockedDataService,
      AttachPolicyDao attachPolicyDao,
      PreservationPeriodDao preservationPeriodDao,
      NotiConfigDao notiConfigDao,
      RedisRepository redisRepository,
      JWT jwt) {
    //this.recycleService =recycleService;
    this.cacheService = cacheService;
    this.folderService = folderService;
    this.docService = docService;
    this.projectService = projectService;
    this.researchService = researchService;
    this.authService = authService;
    this.codeService = codeService;
    this.takeoutReqService = takeoutReqService;
    this.sysObjectDao = sysObjectDao;
    this.docTemplateDao = docTemplateDao;
    this.gwDeptDao = gwDeptDao;
    this.reqTakeoutDao = reqTakeoutDao;
    this.docImpService = docImpService;
    this.drmService = drmService;
    this.redisRepository = redisRepository;
    this.jwt = jwt;
    this.lockedDataService = lockedDataService;
    this.folderDao=folderDao;
    this.docDao = docDao;
    this.commonAuthDao = commonAuthDao;
    this.notificationService = notificationService;
    this.gwUserDao = gwUserDao;
    this.docImpDao = docImpDao;
    this.authBaseDao = authBaseDao;
    this.authShareDao = authShareDao;
    this.userPresetDao = userPresetDao;
    this.attachPolicyDao = attachPolicyDao;
    this.preservationPeriodDao = preservationPeriodDao;
    this.notiConfigDao = notiConfigDao;
  }
 
  @Override
  @Transactional
  public String registData(UserSession userSession, RegistDataDto registDataDto, IDfSession idfSess, HamInfoResult hamInfo, String userIp, String userType, List<UserPresetDetail> userPresetList) throws Exception {
    IDfSession idfSession = idfSess != null ? idfSess : this.getIdfSession(userSession);
    String registObjId = null;
    IDfDocument idfNewDoc = null;
    FolderFilterDto fldrFildter;
    String secLevel = SecLevelCode.TEAM.getValue();
    String prCode = DCTMConstants.DCTM_BLANK;
    String prType = DCTMConstants.DCTM_BLANK;
    String upObjId = DCTMConstants.DCTM_BLANK;
    String normalAclSuffix = null;
    FolderAuthDto folderAuthDto = null;
    IDfPersistentObject obj = null;
    boolean needAuth = false;
    try { 
      if (registDataDto.getDataName().getBytes().length >= 240) {
        throw new UploadNameLengthException("폴더 및 파일명은 영문 240, 한글 80자 이내여야 합니다.");
      }
      
      if (registDataDto.getFolderType() != null) {
        if (FolderType.DFO.getValue().equals(registDataDto.getFolderType())
            || FolderType.PFO.getValue().equals(registDataDto.getFolderType())
            || FolderType.RFO.getValue().equals(registDataDto.getFolderType())) {
          needAuth = true;
        }
      }
      
      if (registDataDto.checkRoot()) {
        // default auth
        if (hamInfo == null) {
          hamInfo = authService.selectHamInfo(registDataDto.getHamId()).orElseThrow(() -> new NotFoundException(CheckAuthParam.class, registDataDto.getHamId()));
        } else {
          secLevel = registDataDto.getSecLevel();
        }
        registDataDto.setHamInfo(hamInfo);
        if (ProjectType.PROJECT.getValue().equals(hamInfo.getHamType())) {
          final String pjtCode = hamInfo.getMyCode();
          Project foundPjt = projectService.selectProjectByUPjtCode(hamInfo.getMyCode())
              .orElseThrow(() -> new NotFoundException(Project.class, pjtCode));
          secLevel = foundPjt.getUSecLevel();
          prCode = pjtCode;
          prType = ProjectType.PROJECT.getValue();
        } else if (ProjectType.RESEARCH.getValue().equals(hamInfo.getHamType())) {
          final String rschCode = hamInfo.getMyCode();
          Research foundRsch = researchService.selectResearchByURschCode(hamInfo.getMyCode())
              .orElseThrow(() -> new NotFoundException(Research.class, rschCode));
          secLevel = foundRsch.getUSecLevel();
          prCode = rschCode;
          prType = ProjectType.RESEARCH.getValue();
        }
        if (needAuth) {
          folderAuthDto = authService.selectRootFolderAuths(userSession, AuthType.findAuthTypeForDocReg(hamInfo), SecLevelCode.LIVE, null, hamInfo);
        }
      } else {
        // 권한 상속
        upObjId = registDataDto.getUpObjectId();
        Folder folder = folderService.selectOne(upObjId).orElseThrow(() -> new NotFoundException("Folder not found"));
 
        if (hamInfo == null) {
          hamInfo = authService.selectHamInfo(" ".equals(folder.getUPrCode()) || folder.getUPrCode() == null ? folder.getUDeptCode() : folder.getUPrCode())
                  .orElseThrow(() -> new NotFoundException(RegistDataDto.class, folder.getUPrCode()));
        } 
        registDataDto.setHamInfo(hamInfo);
        registDataDto.setHamId((registDataDto.hamValidation() ? folder.getUPrCode() : folder.getUDeptCode()));
 
        secLevel = folder.getUSecLevel();
        prCode = folder.getUPrCode();
        prType = folder.getUPrType();
        if (needAuth) {
          folderAuthDto = authService.selectFolderAuths(userSession, upObjId, null);
        }
      }
      registDataDto.setHamInfo(hamInfo);
 
      //권한체크
      boolean gwonhan = false;
      if (needAuth) {
        if (registDataDto.checkRoot()) {
          if (registDataDto.isPrFirst()) {
            gwonhan = true;
          } else {
            gwonhan = authService.isRootAuthenticated(HamType.findByValue(hamInfo.getHamType()), hamInfo.getHamCodeForAuth(), userSession.getUser().getUserId());
          }
        } else {
          gwonhan = authService.checkFolderAuth(upObjId, userSession.getUser().getUserId(), GrantedLevels.DELETE.getLabel());
        }
        if (!gwonhan) {
          throw new RuntimeException("생성 권한이 없는 사용자입니다.");
        }
      }
 
      if (idfSess == null) {
        idfSession.beginTrans();
      } 
      if (DataObjectType.TEMPLATE_FILE.getValue().equals(registDataDto.getDateType())) { 
        DocTemplate template = docTemplateDao.selectOneByTemplateType(registDataDto.getTemplateType())
            .orElseThrow(() -> new NotFoundException(DocTemplate.class, registDataDto.getTemplateType()));
        
        //template 생성
        String dupleName = registDataDto.getDataName();
        while(true) {
          List<Doc> docList = docService.selectList(DocFilterDto.builder()
              .uFolId(upObjId).uCabinetCode(registDataDto.getHamInfo().getUCabinetCode())
              .uPrCode(registDataDto.hamValidation()? prType : null)
              .uFileExt(template.getContentExtension())
              .objectName(dupleName).build());
          if (docList.size() > 0) {
            throw new DupleRequestException(dupleName + " 파일이 존재합니다.");
          } else {
            registDataDto.setDataName(dupleName);
            break;
          }
        } 
        
        //preset 작업
        UserPresetDetail userPreset = null;
        if (userPresetList == null) {
          userPresetList = userPresetDao.selectDetailedOneByFilter(UserPresetFilterDto.builder()
              .uUserId(userSession.getUser().getUserId())
              .uSecBaseFlag(true).build());    
        }
        if (registDataDto.checkRoot()) {
          userPreset = userPresetList.stream().filter(h -> "1".equals(h.getUSecBaseFlag()) && SecLevelCode.TEAM.getValue().equals(h.getUSecLevel()))
//          userPreset = userPresetList.stream().filter(h -> "1".equals(h.getURegBaseFlag()))
              .findFirst()
              .orElseThrow(() -> new IllegalArgumentException("Preset Not found"));
        } else {
          final String compareSecLevel = secLevel;
          userPreset = userPresetList.stream().filter(h -> compareSecLevel.equals(h.getUSecLevel()))
              .findFirst()
              .orElseThrow(() -> new IllegalArgumentException("Preset Not found"));
        }
        if (userPreset.getUSecLevel() != null) {
          secLevel = SecLevelCode.compareSecs(secLevel, userPreset.getUSecLevel()).getValue();
        }
        
        registDataDto.setTemplateObjId(template.getRObjectId());
        idfNewDoc = RegistDataDto.makeTemplate(idfSession, registDataDto, secLevel, template.getContentExtension());
        registObjId = idfNewDoc.getObjectId().toString();
        
        // 추가 권한자 지정 : 기본 권한이나 공유/협업 대상자 등
        idfNewDoc.setString("u_reg_source", "P");//DBox
        idfNewDoc.setInt("u_preserve_flag", userPreset.getUPreserveFlag()); // 보존년한
        //보존연한은 생성시 제외
//        idfNewDoc.setString("u_expired_date", DateTimeUtils.addYears(userPreset.getUPreserveFlag())); // 보존년한
        if (userPreset.getUOpenFlag() != null) {
          idfNewDoc.setString("u_open_flag", userPreset.getUOpenFlag());
        }
        if (userPreset.getUMailPermitFlag() != null) {
          idfNewDoc.setBoolean("u_auto_auth_mail_flag", "Y".equals(userPreset.getUMailPermitFlag())?true:false);
        }
 
        //권한  설정
        if (!registDataDto.checkRoot()) {
          folderAuthDto = authService.selectDocAuths(userSession, hamInfo.getUCabinetCode(), upObjId, registObjId, userPreset);
        } else {
          if (Objects.isNull(folderAuthDto)) {
            folderAuthDto = new FolderAuthDto();
          }
          folderAuthDto = authService.selectRootDocAuths(userSession, AuthType.findAuthTypeForDocReg(hamInfo), SecLevelCode.findByValue(secLevel), registObjId, hamInfo, userPreset);
        }
        normalAclSuffix = authService.selectDocAcl(AuthType.findAuthTypeForDocReg(hamInfo), SecLevelCode.findByValue(secLevel), registDataDto.getHamInfo().getUCabinetCode());
        
        idfNewDoc.setACLDomain(idfSession.getDocbaseOwnerName());
        idfNewDoc.setACLName(normalAclSuffix);
        authService.saveDocAuths(false, idfNewDoc, DocStatus.LIVE.getValue(), folderAuthDto, hamInfo, idfSession);
        idfNewDoc.save();
      } else {
        //folder 생성
 
        fldrFildter = FolderFilterDto.builder().uCabinetCode(registDataDto.getHamInfo().getUCabinetCode())
            .uPrCode(registDataDto.hamValidation()? prType : null)
            .uPrType(registDataDto.hamValidation()? prCode : null)
            .uDeleteStatus(DCTMConstants.DCTM_BLANK)
            .uFolType(registDataDto.getFolderType())
            .uUpFolId(upObjId).uFolName(registDataDto.getDataName()).build();
        List<Folder> folderList = folderService.selectFolderChildren(fldrFildter);
        
        if (folderList.size() > 0) {
          do {
            fldrFildter.setUFolName(DboxStringUtils.addFolderNameNumber(folderList.get(0).getUFolName()));
            folderList = folderService.selectFolderChildren(fldrFildter);
          } while (folderList.size() > 0);
          
          
//          if (idfSess != null) {
//            if (folderList.get(0).isLockFolder()) {
//              return FolderStatus.LOCK.getValue();
//            }
//            return folderList.get(0).getRObjectId();
//          }
//          throw new DupleRequestException(registDataDto.getDataName());
        }
        
        RegistFolderDto dto = RegistFolderDto.builder().uUpFolId(upObjId).uFolName(fldrFildter.getUFolName())
            .uCabinetCode(registDataDto.getHamInfo().getUCabinetCode()).uFolType(registDataDto.getFolderType())
            .uSecLevel(secLevel).uFolStatus(FolderStatus.ORDINARY.getValue())
            .uCreateUser(userSession.getUser().getUserId())
            .uPrCode(registDataDto.hamValidation() ? prCode : null)
            .uPrType(registDataDto.hamValidation() ? prType : null)
            .build();
        registObjId = folderService.createFolder(idfSession, dto);
        authService.saveFolderAuths(registObjId, idfSession, folderAuthDto);
        registDataDto.setSecLevel(secLevel);
      }
      
      if (idfSess == null) {
        idfSession.commitTrans();
      }
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfSession != null) {
        if (idfSession.isTransactionActive() && idfSess == null) {
          idfSession.abortTrans();
        }
        if (idfSession.isConnected() && idfSess == null) {
          sessionRelease(userSession.getUser().getUserId(), idfSession);
        }
      }
    }
    
    //log등록
    if (DataObjectType.TEMPLATE_FILE.getValue().equals(registDataDto.getDateType())) { 
      // 문서 로그
      LogDoc logDoc = LogDoc.builder()
          .uJobCode(DocLogItem.RE.getValue())
          .uDocId(idfNewDoc.getObjectId().getId())
          .uDocKey(idfNewDoc.getObjectId().getId())
          .uDocName(registDataDto.getDataName())
          .uDocVersion("1")
          .uFileSize(idfNewDoc.getContentSize())
          .uOwnDeptcode(hamInfo.getCabinetOrgId().toUpperCase())
          .uActDeptCode(userSession.getUser().getOrgId())
          .uJobUser(userSession.getUser().getUserId())
          .uJobUserType(userType == null ? "P" : userType) //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
          .uDocStatus(DocStatus.LIVE.getValue())
          .uSecLevel(secLevel)
          .uCabinetCode(hamInfo.getUCabinetCode())
          .uJobGubun(UploadFlag.NEW_D.getValue())
          .uUserIp(userIp)
          .build();
      insertLog(logDoc);
    } else {
      
    }
 
    return registObjId;
  }
 
  @Override
  public DataDetailDto getDataOne(UserSession userSession, String dataId, boolean isUDocKey, boolean isMobile) throws Exception {
    // dataId 무결성 확인
    if (DfId.isObjectId(dataId)) {
      // 보안등급 코드 조회
      Map<String, String> secLevelMap = codeService.getSecLevelMap();
      
      // 현재 사용자 아이디
      final String userId = userSession != null ? userSession.getUser().getUserId() : null;
        
      // 폴더일 경우
      Optional<Folder> optFolder = folderService.selectDetailOne(dataId, userId);
      if (optFolder.isPresent()) {
        // 폴더상태 코드 조회
        Map<String, String> folStatusMap = codeService.getFolStatusMap();
            
        Folder folder = optFolder.get();
 
        FolderDetailDto folderDto = getModelMapper().map(folder, FolderDetailDto.class);
        folderDto.setSecLevelName(secLevelMap.get(folder.getUSecLevel()));
        folderDto.setFolStatusName(folStatusMap.get(folder.getUFolStatus()));
 
        // live 권한 리스트
        List<AuthBase> liveAuthBaseList = folder.getAuthBases().stream()
            .filter(item -> DocStatus.LIVE.getValue().equals(item.getUDocStatus())).collect(Collectors.toList());
        folderDto.setLiveAuthBases(liveAuthBaseList);
 
        // closed 권한 리스트
        List<AuthBase> closedAuthBaseList = folder.getAuthBases().stream()
            .filter(item -> DocStatus.CLOSED.getValue().equals(item.getUDocStatus())).collect(Collectors.toList());
        folderDto.setClosedAuthBases(closedAuthBaseList);
        
        // shared 권한 리스트
        folderDto.setLiveAuthShares(folder.getAuthShares());
 
        return DataDetailDto.builder().dataType(DboxObjectType.FOLDER.getValue()).folder(folderDto).build();
      }
 
      // 문서일 경우
      Optional<Doc> optDoc = docService.selectDetailOne(dataId, isUDocKey, userId);
      if (optDoc.isPresent()) {
        // 문서상태 코드 조회
        Map<String, String> docStatusMap = codeService.getDocStatusMap();
        
        Doc doc = optDoc.get();
 
        // 모바일에서 요청한 경우
        if (isMobile) {
          // 특별사용자 리스트 (회장/부회장/각 회사 대표)
          Set<String> specialUserIdSet = codeService.getSpecialUserIdSet();
 
          // 현재 사용자의 특별사용자 여부 확인
          boolean isSpecial = specialUserIdSet.contains(userId);
          String uSecLevel = doc.getUSecLevel();
          String uDocStatus = doc.getUDocStatus();
 
          // 특별사용자가 아니고 제한문서일 경우 에러
          if (!isSpecial && SecLevelCode.SEC.getValue().equals(uSecLevel) && DocStatus.CLOSED.getValue().equals(uDocStatus)) {
            throw new ForbiddenException("모바일에서 제한등급 문서 조회 불가");
          }
        }
        
        // 작성자 리스트
        List<String> uEditorList = doc.getDocRepeatings().stream().filter(predicate -> null != predicate.getUEditor())
            .map(mapper -> mapper.getUEditor()).collect(Collectors.toList());
 
        // 버전 리스트
        List<String> rVersionLabelList = doc.getDocRepeatings().stream()
            .filter(predicate -> null != predicate.getRVersionLabel()).map(mapper -> mapper.getRVersionLabel())
            .collect(Collectors.toList());
 
        DocDetailDto docDto = getModelMapper().map(doc, DocDetailDto.class);
        docDto.setSecLevelName(secLevelMap.get(doc.getUSecLevel()));
        docDto.setDocStatusName(docStatusMap.get(doc.getUDocStatus()));
        docDto.setUEditor(uEditorList);
        docDto.setRVersionLabel(rVersionLabelList);
 
        // 읽기 가능한 경우 상세정보 포함
        if (doc.getMaxLevel() >= GrantedLevels.READ.getLevel()) {
          // 읽기 가능
          docDto.setReadable(true);
          
          // live 권한 리스트
          List<AuthBase> liveAuthBaseList = doc.getAuthBases().stream()
              .filter(item -> DocStatus.LIVE.getValue().equals(item.getUDocStatus()))
              .collect(Collectors.toList());
          docDto.setLiveAuthBases(liveAuthBaseList);
 
          // closed 권한 리스트 (closed 권한에는 DELETE가 없음)
          List<AuthBase> closedAuthBaseList = doc.getAuthBases().stream()
              .filter(item -> DocStatus.CLOSED.getValue().equals(item.getUDocStatus()))
              .filter(item -> GrantedLevels.READ.getLabel().equals(item.getUPermitType()))
              .collect(Collectors.toList());
          docDto.setClosedAuthBases(closedAuthBaseList);
        }
 
        return DataDetailDto.builder().dataType(DboxObjectType.DOCUMENT.getValue()).doc(docDto).build();
      }
      
      // TODO 중요 문서함
      
      // 폴더도 문서도 아닐 경우
      throw new NotFoundException("dataId", dataId);
    } else {
      throw new BadRequestException("dataId is not valid");
    }
  }
 
  @Override
  public List<DataDetailDto> getDataChildren(UserSession userSession, String dataId, String hamType, 
          String folderType, String level, boolean withDoc, boolean checkHasChildren, boolean isMobile) throws Exception {
    boolean isRoot = hamType != null;
    
    // dataId 무결성 확인 (루트 데이터 조회일 경우 체크 안함)
    if (isRoot || DfId.isObjectId(dataId)) {
        // 현재 사용자 아이디
      final String userId = userSession.getUser().getUserId();
      List<DataDetailDto> dataList = new ArrayList<>();
 
      // 검색 필터 설정
      FolderFilterDto folderFilterDto = null;
      if (isRoot) {
        // 프로젝트/투자, 연구과제, 부서 여부 확인
        HamInfoResult hamInfo = commonAuthDao.selectHamInfo(dataId).orElseThrow(() -> new BadRequestException("존재하지 않는 dataId 입니다. (dataId: " + dataId + ")"));
        HamType type = HamType.findByValue(hamInfo.getHamType());
        switch (type) {
          // 부서함일 경우
          case DEPT:
          case COMPANY:
          case COMPANY_M:
            VDept dept = gwDeptDao.selectOneByOrgId(dataId).orElseThrow(() -> new BadRequestException("존재하지 않는 부서코드입니다."));
            folderFilterDto = FolderFilterDto.builder()
               .uCabinetCode(dept.getUCabinetCode())
               .uUpFolId(DCTMConstants.DCTM_BLANK)
               .uFolType(folderType == null ? FolderType.DFO.getValue() : folderType)    // DFO: 부서함 하위, PCL: 프로젝트/투자 분류폴더, RCL: 연구과제 분류폴더
               .build();
            break;
          // 프로젝트/투자일 경우
          case PROJECT:
            Project project = projectService.selectProjectByUPjtCode(dataId).orElseThrow(() -> new BadRequestException("존재하지 않는 프로젝트 코드입니다."));
            folderFilterDto = FolderFilterDto.builder()
                .uCabinetCode(project.getUCabinetCode())
                .uUpFolId(DCTMConstants.DCTM_BLANK)
                .uPrCode(dataId)
                .uPrType(ProjectType.PROJECT.getValue())
                .uFolType(FolderType.PFO.getValue())
                .build();
            break;
          // 연구과제일 경우
          case RESEARCH:
            Research research = researchService.selectResearchByURschCode(dataId).orElseThrow(() -> new BadRequestException("존재하지 않는 연구과제 코드입니다."));
            folderFilterDto = FolderFilterDto.builder()
                .uCabinetCode(research.getUCabinetCode())
                .uUpFolId(DCTMConstants.DCTM_BLANK)
                .uPrCode(dataId)
                .uPrType(ProjectType.RESEARCH.getValue())
                .uFolType(FolderType.RFO.getValue())
                .build();
            break;
          default:
        }
      } else {
        // 상위 폴더 조회
        Folder parentFolder = folderService.selectOne(dataId).orElseThrow(() -> new BadRequestException("존재하지 않는 폴더입니다."));
        // 상위 폴더 종류
        String parentUFolType = parentFolder.getUFolType();
        // 폴더 검색조건 설정
        folderFilterDto = FolderFilterDto.builder()
            .uUpFolId(dataId)
            .uFolType(parentUFolType)
            .build();
      }
 
      // 권한있는 폴더 상세 리스트 조회
      final String orgId = userSession.getUser().getOrgId();    // 사용자 부서코드
      List<FolderDetailDto> folderDetailDtoList = folderService.selectAuthorizedDetailChildren(folderFilterDto, userId, checkHasChildren ? orgId : null);
 
      // 전자결재 Temp 리스트 조회
      if (!isRoot && FolderType.DWY.getValue().equals(folderFilterDto.getUFolType())) {
        FolderFilterDto dwyFolderFilterDto = FolderFilterDto.builder()
            .uUpFolId(folderFilterDto.getUUpFolId())
            .uFolType(FolderType.DWT.getValue())
            .build();
        List<FolderDetailDto> dwtFolderDetailDtoList = folderService.selectAuthorizedDetailChildren(dwyFolderFilterDto, userId, level);
        folderDetailDtoList = Stream.concat(dwtFolderDetailDtoList.stream(), folderDetailDtoList.stream()).collect(Collectors.toList());
      }
 
      // 리턴할 자료 리스트에 추가
      dataList.addAll(folderDetailDtoList.stream()
          .map(item -> DataDetailDto.builder().dataType(DboxObjectType.FOLDER.getValue()).folder(item).build())
          .collect(Collectors.toList()));
 
      // 문서 포함 여부가 true일 경우
      if (withDoc) {
        // 검색 필터 설정
        DocFilterDto docFilterDto = null;
        if (isRoot) {
          // 프로젝트/투자, 연구과제, 부서 여부 확인
          HamInfoResult hamInfo = commonAuthDao.selectHamInfo(dataId).orElseThrow(() -> new BadRequestException("존재하지 않는 dataId 입니다. (dataId: " + dataId + ")"));
          HamType type = HamType.findByValue(hamInfo.getHamType());
          switch (type) {
            // 부서함일 경우
            case DEPT:
            case COMPANY:
            case COMPANY_M:
              VDept dept = gwDeptDao.selectOneByOrgId(dataId).orElseThrow(() -> new BadRequestException("존재하지 않는 부서코드입니다."));
              docFilterDto = DocFilterDto.builder()
                  .uCabinetCode(dept.getUCabinetCode())
                  .uFolId(DCTMConstants.DCTM_BLANK)
                  .uPrCode(DCTMConstants.DCTM_BLANK)
                  .build();
              break;
            // 프로젝트/투자일 경우
            case PROJECT:
              Project project = projectService.selectProjectByUPjtCode(dataId).orElseThrow(() -> new BadRequestException("존재하지 않는 프로젝트 코드입니다."));
              docFilterDto = DocFilterDto.builder()
                  .uCabinetCode(project.getUCabinetCode())
                  .uFolId(DCTMConstants.DCTM_BLANK)
                  .uPrCode(dataId)
                  .uPrType(ProjectType.PROJECT.getValue())
                  .build();
              break;
            // 연구과제일 경우
            case RESEARCH:
              Research research = researchService.selectResearchByURschCode(dataId).orElseThrow(() -> new BadRequestException("존재하지 않는 연구과제 코드입니다."));
              docFilterDto = DocFilterDto.builder()
                  .uCabinetCode(research.getUCabinetCode())
                  .uFolId(DCTMConstants.DCTM_BLANK)
                  .uPrCode(dataId)
                  .uPrType(ProjectType.RESEARCH.getValue())
                  .build();
              break;
            default:
          }
        } else {
          docFilterDto = DocFilterDto.builder().uFolId(dataId).build();
        }
 
        // 검색(2) 이상의 권한을 가진 문서 리스트 조회
        List<DocDetailDto> docDetailDtoList = docService.selectAuthorizedDetailList(docFilterDto, userId, level == null ? GrantedLevels.BROWSE.getLevel() : GrantedLevels.findByLabel(level));
 
        // 모바일에서 요청한 경우
        if (isMobile) {
          // 특별사용자 리스트 (회장/부회장/각 회사 대표)
          Set<String> specialUserIdSet = codeService.getSpecialUserIdSet();
 
          docDetailDtoList = docDetailDtoList.stream().filter(item -> {
            // 현재 사용자의 특별사용자 여부 확인
            String currentUserId = userSession.getUser().getUserId();
            boolean isSpecial = specialUserIdSet.contains(currentUserId);
            String uSecLevel = item.getUSecLevel();
            String uDocStatus = item.getUDocStatus();
 
            // 특별사용자가 아니고 제한문서일 경우 제외
            if (!isSpecial && SecLevelCode.SEC.getValue().equals(uSecLevel) && DocStatus.CLOSED.getValue().equals(uDocStatus)) {
              return false;
            } else {
              return true;
            }
          }).collect(Collectors.toList());
        }
 
        // 리턴할 자료 리스트에 추가
        dataList.addAll(docDetailDtoList.stream()
            .map(item -> DataDetailDto.builder().dataType(DboxObjectType.DOCUMENT.getValue()).doc(item).build())
            .collect(Collectors.toList()));
        
        // TODO 중요 문서함
      }
 
      return dataList;
    } else {
      throw new BadRequestException("dataId is not valid");
    }
  }
  
  @Override
  public List<DocDescendantDto> getDataDescendants(UserSession userSession, String dataId, boolean isMobile, boolean download) throws Exception {
    // dataId 무결성 확인
    if (DfId.isObjectId(dataId)) {
      // 현재 사용자 아이디
      final String userId = userSession.getUser().getUserId();
        
      final ModelMapper modelMapper = getModelMapper();
 
      // 폴더인지 문서인지 확인
      Optional<Doc> optDoc = docService.selectOne(dataId);
      // 문서일 경우
      if (optDoc.isPresent()) {
        Doc doc = optDoc.get();
        
        // 모바일에서 요청한 경우
        if (isMobile) {
          // 특별사용자 리스트 (회장/부회장/각 회사 대표)
          Set<String> specialUserIdSet = codeService.getSpecialUserIdSet();
 
          // 현재 사용자의 특별사용자 여부 확인
          boolean isSpecial = specialUserIdSet.contains(userId);
          String uSecLevel = doc.getUSecLevel();
          String uDocStatus = doc.getUDocStatus();
 
          // 특별사용자가 아니고 제한문서일 경우 에러
          if (!isSpecial && SecLevelCode.SEC.getValue().equals(uSecLevel) && DocStatus.CLOSED.getValue().equals(uDocStatus)) {
            throw new ForbiddenException("모바일에서 제한등급 문서 조회 불가");
          }
        }
        
        DocDescendantDto docDescendantDto = modelMapper.map(doc, DocDescendantDto.class);
        docDescendantDto.setPath(doc.getObjectName());
        
        // 리스트로 변환
        List<DocDescendantDto> docDescendantDtoList = Arrays.asList(docDescendantDto);
        
        return docDescendantDtoList;
      }
      // 문서가 아닐 경우
      else {
        // 폴더 아이디에 해당하는 전체 자손 폴더 조회 (권한이 있는 폴더만 조회)
        List<FolderDescendantDto> folderDescendantDtoList = folderService.selectDescendants(dataId, userId, true);
        if (folderDescendantDtoList.size() == 0) {
          throw new BadRequestException("권한 있는 폴더가 없습니다.");
        }
        // 폴더 아이디 별 경로 map 생성
        Map<String, String> folderPathMap = folderDescendantDtoList.stream()
                .collect(Collectors.toMap((item) -> item.getRObjectId(), (item) -> item.getPath()));
        
        // 각 폴더의 하위에 있는 문서 조회 (읽기(3) 이상의 권한이 있는 폴더만 조회)
        List<String> folIdList = folderDescendantDtoList.stream().map((item) -> item.getRObjectId()).collect(Collectors.toList());
        //(dooyeon) selectAuthorizedListByFolIds 쿼리 조건에  edms_doc_sp.i_has_folder = 1 조건 추가 (최신 버전만 조회하도록)
        List<Doc> docList = docService.selectAuthorizedListByFolIds(folIdList, userId, GrantedLevels.READ.getLevel());
 
        // 모바일에서 요청한 경우
        if (isMobile) {
          // 특별사용자 리스트 (회장/부회장/각 회사 대표)
          Set<String> specialUserIdSet = codeService.getSpecialUserIdSet();
 
          docList = docList.stream().filter(item -> {
            // 현재 사용자의 특별사용자 여부 확인
            boolean isSpecial = specialUserIdSet.contains(userId);
            String uSecLevel = item.getUSecLevel();
            String uDocStatus = item.getUDocStatus();
 
            // 특별사용자가 아니고 제한문서일 경우 제외
            if (!isSpecial && SecLevelCode.SEC.getValue().equals(uSecLevel) && DocStatus.CLOSED.getValue().equals(uDocStatus)) return false;
            else return true;
          }).collect(Collectors.toList());
        }
        
        // 자손 문서 리스트 생성
        List<DocDescendantDto> docDescendantDtoList = docList.stream().map((item) -> {
          String name = item.getObjectName();
          if (download) { 
            int version = item.getDocRepeatings()==null?1:(int) Double.parseDouble(item.getDocRepeatings().get(item.getDocRepeatings().size()-1).getRVersionLabel());
            VUser editorUser = gwUserDao.selectOneByUserId(item.getULastEditor()==null?userSession.getUser().getUserId():item.getULastEditor()).orElse(userSession.getUser());
            name = DboxStringUtils.getDboxFileNameing(name, item.getUFileExt(), editorUser, String.valueOf(version));
          }
          String path = folderPathMap.get(item.getUFolId()) + "/" + name;    // 세팅할 파일 경로
          modelMapper.map(item, DocDescendantDto.class);
          DocDescendantDto docDescendantdto = modelMapper.map(item, DocDescendantDto.class);
          docDescendantdto.setPath(path);
          
          return docDescendantdto;
        }).collect(Collectors.toList());
 
        return docDescendantDtoList;
      }
    } else {
      throw new BadRequestException("dataId is not valid");
    }
  }
 
  @Override
  public List<DataDetailDto> getDataPaths(UserSession userSession, String dataId, boolean isMobile) throws Exception {
    List<Folder> folderPathList = folderService.getFolderPaths(dataId);
 
    final ModelMapper modelMapper = getModelMapper();
    List<DataDetailDto> dataPathList = folderPathList.stream().map((item) -> {
      FolderDetailDto folderDetailDto = modelMapper.map(item, FolderDetailDto.class);
      return DataDetailDto.builder().folder(folderDetailDto).build();
    }).collect(Collectors.toList());
 
    return dataPathList;
  }
  
  @Override
  public String getDataFullPaths(String folderId, String folderType, String deptCode) {
    return folderDao.selectDsearchFullPath(folderId, folderType, deptCode);
  }
 
    @Override
    public Object downloadData(UserSession userSession, String dataId, boolean isUDocKey, String versionId, String browser, boolean isMobile, boolean noEncrypt, boolean isViewer, String approveId, String syspath, String ip) throws Exception {
      boolean isExistReqTakeOn = false; // 반출
      boolean doDRM            = true ; // 기본이 DRM 실행
      boolean isFolder         = false;
      boolean isDoc            = false;
      boolean isCEO            = false; // CEO
      boolean isDocImp         = false; // 즁요문서
      boolean ignoreAuth       = false;  // 권한 무시
      boolean isClosedSec      = false; // Closed 제한 문서
      
      Map<String, String> formatChkMap = null; // drm적용 대상 확장자
      IDfSession idfSession = null;
      VUser user = userSession != null ? userSession.getUser() : null;
      String currentUserId = user != null ? user.getUserId() : null;
      
      // 특별사용자 리스트 (회장/부회장/각 회사 대표)
      Set<String> specialUserIdSet = codeService.getSpecialUserIdSet();
      isCEO = specialUserIdSet.contains(currentUserId);
 
      Folder folder = new Folder();
      Doc doc = new Doc();
      String rObjectId = null;
      FolderDetailDto folderDto = null;
        
      try {
        if (DfId.isObjectId(dataId)) {
          Optional<Folder> optFolder = folderService.selectOne(dataId);
          isFolder = optFolder.isPresent();
          if (isFolder) {
            folder = optFolder.get();
            rObjectId = folder.getRObjectId();
            folderDto = getModelMapper().map(folder, FolderDetailDto.class);
          } else {
          Optional<Doc> optDoc = docService.selectOne(dataId, isUDocKey);
            isDoc = optDoc.isPresent();
            if ( isDoc ) {
              doc = optDoc.get();
              rObjectId = doc.getRObjectId();
            }
          }
          
          if ( isFolder && isDoc ) throw new RuntimeException("잘못된 대상 ID입니다.");
          if ( !isFolder && !isDoc ) throw new RuntimeException("잘못된 대상 ID입니다.");
          
          Optional<DocImp> optDocImp = docImpService.selectOne(dataId);
          isDocImp = optDocImp.isPresent(); // 중요문서이면 이력 안남김.
  
     
          // 해당하는 형식이 아닐경우 drm실행 X. 반출함일경우도 false로 바꿈
          formatChkMap = codeService.getDrmFormatCodeMap();
          if (!formatChkMap.containsKey(
              !DCTMConstants.DCTM_BLANK.equals(doc.getUFileExt().toUpperCase())? 
                  doc.getUFileExt().toUpperCase() : FilenameUtils.getExtension(doc.getObjectName()).toUpperCase())) { 
            doDRM = false;
          }
          
          // 반출대상 여부 (처리일, 만료일까지 확인)
          isExistReqTakeOn = reqTakeoutDao.checkTakeoutDoc(dataId, approveId);
          
          // 반출된 문서이면 DRM 제외.
          if ((isExistReqTakeOn && noEncrypt) || isViewer) doDRM = false;
          
          // 반출된 문서이고 인증받지 않은 사용자일 경우 권한 무시
          if (isExistReqTakeOn) ignoreAuth = true;

          if (DocStatus.CLOSED.getValue().equals(doc.getUDocStatus())) {
            if (SecLevelCode.SEC.getValue().equals(doc.getUSecLevel())) {
              isClosedSec = true;
            }
          }
          
          // SOFTM - [자료다운로드] - [CHECK POINT] : 모바일이고 CEO이면 체크안함.

          if ( !isCEO || !isMobile ) {
    //          true  || false : true
    //          false || true  : true
    //          false || false : true
    //          true  || true  : false
 
            // 문서 권한 보유 여부 확인
            if (ignoreAuth) {
              idfSession      = DCTMUtils.getAdminSession();
            } else if (authService.checkDocAuth(rObjectId, user.getUserId(), GrantedLevels.READ.getLevel()) ) {
              idfSession = this.getIdfSession(userSession);
            } else {
              // 권한 자동부여 여부 확인
              if (isDoc) {
                idfSession      = DCTMUtils.getAdminSession();
                
                // 최신 버전 조회
                Optional<Doc> newestDoc = docService.selectDetailOne(doc.getUDocKey(), true, null);
                String newestDocRObjectId = newestDoc.get().getRObjectId();
                
                // 읽기 권한 부여
                IDfDocument iDfDoc = (IDfDocument)idfSession.getObject(new DfId(newestDocRObjectId));
                
                // 잠기지 않았을 경우 권한 부여
                if (!iDfDoc.isCheckedOut()) {
                  if (doc.isUAutoAuthMailFlag()) {
                    // 권한설정자에게 권한 추가
                    iDfDoc.grant(currentUserId, GrantedLevels.READ.getLevel(), "");
                    iDfDoc.save();
                    
                    if (DocStatus.LIVE.getValue().equals(doc.getUDocStatus())) {
                      IDfPersistentObject iDfAuthBaseLive = (IDfPersistentObject)idfSession.newObject("edms_auth_base");
                      iDfAuthBaseLive.setString("u_obj_id"      , doc.getUDocKey());
                      iDfAuthBaseLive.setString("u_obj_type"    , AuthObjType.DOCUMENT.getValue());
                      iDfAuthBaseLive.setString("u_doc_status"  , DocStatus.LIVE.getValue());
                      iDfAuthBaseLive.setString("u_permit_type" , GrantedLevels.READ.getLabel());
                      iDfAuthBaseLive.setString("u_own_dept_yn" , "N");
                      iDfAuthBaseLive.setString("u_author_id"   , currentUserId);
                      iDfAuthBaseLive.setString("u_author_type" , AuthorType.USER.getValue());
                      iDfAuthBaseLive.setString("u_create_user" , currentUserId);
                      iDfAuthBaseLive.setString("u_create_date" , (new DfTime()).toString());
                      iDfAuthBaseLive.setString("u_add_gubun"   , "P");
                      
                      iDfAuthBaseLive.save();
                    } else {
                      IDfPersistentObject iDfAuthBaseLive = (IDfPersistentObject)idfSession.newObject("edms_auth_base");
                      iDfAuthBaseLive.setString("u_obj_id"      , doc.getUDocKey());
                      iDfAuthBaseLive.setString("u_obj_type"    , AuthObjType.DOCUMENT.getValue());
                      iDfAuthBaseLive.setString("u_doc_status"  , DocStatus.CLOSED.getValue());
                      iDfAuthBaseLive.setString("u_permit_type" , GrantedLevels.READ.getLabel());
                      iDfAuthBaseLive.setString("u_own_dept_yn" , "N");
                      iDfAuthBaseLive.setString("u_author_id"   , currentUserId);
                      iDfAuthBaseLive.setString("u_author_type" , AuthorType.USER.getValue());
                      iDfAuthBaseLive.setString("u_create_user" , currentUserId);
                      iDfAuthBaseLive.setString("u_create_date" , (new DfTime()).toString());
                      iDfAuthBaseLive.setString("u_add_gubun"   , "P");
                      
                      iDfAuthBaseLive.save();
                    }
                  } else {
                    throw new RuntimeException("자동 권한 부여 대상이 아닙니다.");
                  }
                }
              } else {
                throw new ForbiddenException("자료 다운로드 : " + rObjectId);
              }
            } 
          }
          if ( !isCEO ) {
            // SOFTM - [자료다운로드] - [CHECK POINT] : closed && 제한등급 : CEO가 아니면 체크
            if (isClosedSec) {
              throw new ForbiddenException("제한등급 문서 조회 불가");
            }
          }
          
          // SOFTM - [자료다운로드] - [CHECK POINT] : 중요보관소 하위의 문서일 경우 찾을 수 없음 처리 (404 Not Found)
          if ( isDocImp ) {
            throw new NotFoundException("중요보관소 문서 조회 불가");
          } else { // 중요보관소 하위가 아닐 경우 edms_log_doc에 이력 생성
    //          중요보관소 하위가 아닐 경우 edms_log_doc에 이력 생성
          }
    
          if ( isFolder ) {
            // 폴더 조회
            // 리컬시브로 쿼리 짜야함 부서 쿠러 참고해야함.
            // edms_folder_sp의 where = r_object_id connect by u_up_fol_id(하위폴더인데이름이 이상함) === RECulsive.r_object_id
            // rObjectId:path
    //        
    //        {
    //        { 
    //          rObjectId:'110004d2800085da:',
    //          path : '/테스트폴더 1레벨/IIDS샘플'
    //          } , 
    //        { 
    //            rObjectId:'110004d2800085da:',
    //            path : '/테스트폴더 1레벨/IIDS샘플'
    //        } , 
    //            }
            // TODO SOFTM - [자료다운로드] - /api/data/{dataId}/decendants 처리됨.
              List<Doc> docs = docService.selectListForFolderId(rObjectId);
              return OK(docs);
    //            return DataDetailDto.builder().dataType(DboxObjectType.FOLDER.getValue()).folder(folderDto).build();
          } else {
            /*
                      보안등급과 권한에 맞게 DRM 암호화(DRM파일 chk하여 이미 암호화된 문서면, 복호화 후 진행)
                        반출함 하위의 문서는 암호화하지 않음
                      파일명을 [파일명_팀명_버전_날짜(년-월-일)_작성자.확장자] 패턴으로 설정 (Content-Disposition 헤더에 설정)
            */
            
//            CustomInputStreamResource rst = docService.downloadDocByFile(idfSession, dataId);
            CustomInputStreamResource rst = docService.downloadDoc(idfSession, rObjectId);
            HttpHeaders headers = new HttpHeaders();
            String orginFileName = rst.getFilename();
            String fileExt = FilenameUtils.getExtension(orginFileName);
            String fileNameOnly = FilenameUtils.getBaseName(orginFileName);
            String fileName =  DboxStringUtils.getDboxFileNameing(fileNameOnly, fileExt, doc.getRegUserDetail(), versionId);
            
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Content-Disposition", "attachment; filename=\"" + CommonUtils.getFileNm(browser, fileName) + "\"");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            logger.info("fileName:" + fileName);
            
            File drmFile = null;
            if ( doDRM ) {
              String entCode = user.getComOrgId();
              String entName = codeService.getComCodeDetail(entCode).getUCodeName1();
 
              DrmAuthDto drmAuthorDto = authService.getAuthorsForDrm(user, entCode, entName, doc.getUDocKey(), doc.getUDocStatus(), doc.getUSecLevel());
              
              boolean disableSaveEdit = isClosedSec || doc.getUPrivacyFlag();
              
              drmFile = drmService.encrypt(rst.getInputStream(), drmAuthorDto.getSecLevelType(), drmAuthorDto.getCompany(), 
                  drmAuthorDto.getAuthDeptList(), drmAuthorDto.getAuthUserList(),
                  doc.getRObjectId(), doc.getUDocKey(),
                  rst.getFilename(), rst.contentLength(), currentUserId, user.getDisplayName(), user.getOrgId(),
                  user.getOrgNm(), entCode, entName, disableSaveEdit, ip);
            } 
            
            return ResponseEntity.ok().headers(headers).contentLength(doDRM ? drmFile.length() : rst.contentLength())
              .contentType(MediaType.parseMediaType("application/octet-stream;charset=UTF-8"))
              .body(doDRM ? new UrlResource(drmFile.toURI()) : rst);
          }
        } else {
          throw new BadRequestException("dataId is not valid");
        }
      } finally {
        if (idfSession != null) {
          if (idfSession.isConnected()) {
            if (DCTMConstants.DCTM_ADMIN_ID.equals(idfSession.getLoginUserName())) {
              idfSession.disconnect();
            } else {
              sessionRelease(userSession.getUser().getUserId(), idfSession);
            }
          }
        }
      }
    }
 
    @Override
    public Object updateDataForBatch(UserSession userSession, String versionId, DataUpdateBatchReqDto params, String browser, boolean isMobile,
            boolean isLock, String ip) {

      int successCnt = 0;
      int failCnt = 0;
      ArrayList<String> message = new ArrayList();

      List<DataUpdateReqDto> data = params.getData();

      if (CollectionUtils.isEmpty(data)) {
          throw new BadRequestException("data is not valid");
      }

      for (DataUpdateReqDto param : data) {
        try {
          DocDetailDto doc = param.getDoc();
          FolderDetailDto folder = param.getFolder();
          boolean isDoc = !Objects.isNull(param.getDoc());
          boolean isFolder = !Objects.isNull(param.getFolder());
          String rObjectId = isDoc ? doc.getRObjectId() : isFolder ? folder.getRObjectId() : "";

          if(StringUtils.isEmpty(rObjectId)) {
            throw new BadRequestException("Data is not valid.");
          }

          if(canUpdateData(param)) {
            updateData(userSession, rObjectId, versionId, param, browser, isMobile, isLock, true, true, ip);
            successCnt++;
          }
        } catch (Exception ex) {
            ex.printStackTrace();
            message.add(ex.getMessage());
            failCnt++;
        }
      }
      Map<String, Object> okMap = new HashMap();
      okMap.put("total", successCnt + failCnt);
      okMap.put("success", successCnt);
      okMap.put("fail", failCnt);
      okMap.put("message", message);
      return OK(okMap);
    }

  private boolean canUpdateData(DataUpdateReqDto param) {
    return canUpdateDoc(param.getDoc()) || canUpdateFolder(param.getFolder());
  }

  private boolean canUpdateDoc(DocDetailDto doc) {
    if(Objects.isNull(doc)) {
      return false;
    }
    String uDocStatus = doc.getUDocStatus();
    int maxLevel = doc.getMaxLevel();
    boolean isLive = DocStatus.LIVE.getValue().equals(uDocStatus);
    boolean isClosed = DocStatus.CLOSED.getValue().equals(uDocStatus);
    boolean isLock = doc.getRLockOwner() != null;
    boolean hasNoAuthority =
            (isLive && maxLevel < GrantedLevels.DELETE.getLevel()) ||
            (isClosed && maxLevel < GrantedLevels.READ.getLevel());
    return !hasNoAuthority && !isLock;
  }

  private boolean canUpdateFolder(FolderDetailDto folder) {
    if(Objects.isNull(folder)) {
      return false;
    }
    return GrantedLevels.DELETE.getLevelCode().equals(folder.getMaxPermitType());
  }

  @Override
    @Transactional
    public Object updateData(UserSession userSession, String dataId, String versionId, DataUpdateReqDto params, String browser, boolean isMobile,
            boolean isLock, boolean isUDocKey, boolean isMultipleAuth, String ip) throws Exception {
      // 전달받은 dataId가 비정상일 경우 에러
      if (!DfId.isObjectId(dataId)) throw new BadRequestException("dataId is not valid");
      
      // 현재 사용자 정보
      final String currentUserId = userSession.getUser().getUserId();
      final String currentOrgId = userSession.getUser().getOrgId();
      
      // 현재 시간
      LocalDateTime now = LocalDateTime.now();
      
      // 세션 생성
      IDfSession idfSession = DCTMUtils.getAdminSession();
        
      /* 변수 초기화 시작 */
      
      // 상태 변수 선언
      boolean isFolder            = false;    // 폴더 여부
      boolean isDoc               = false;    // 문서 여부
      boolean isDocImp            = false;    // 즁요문서 여부
      boolean isCommonCabinetDept = false;    // 조직함 하위 여부
      boolean isBelowPR           = false;    // 프로젝트/투자 또는 연구과제 하위 여부
 
      // 처리 여부 변수 선언
      boolean doUpdateDoc         = false;    // 문서 수정 필요 여부
//      boolean doUpdateFolder      = false;    // 폴더 수정 필요 여부
      boolean doGrantLiveAuth     = false;    // Live 권한 추가 필요 여부 
      boolean doRevokeLiveAuth    = false;    // Live 권한 삭제 필요 여부
      boolean doGrantClosedAuth   = false;    // Closed 권한 추가 필요 여부 
      boolean doRevokeClosedAuth  = false;    // Closed 권한 삭제 필요 여부 
      boolean doGrantShare        = false;    // 공유/협업 추가 필요 여부  
      boolean doRevokeShare       = false;    // 공유/협업 삭제 필요 여부
    
      // 전달받은 파라미터 변수 초기화
      DocDetailDto pDoc = params.getDoc();
      FolderDetailDto pFolder = params.getFolder();
      List<AuthBaseUpdateDto> pGrantLiveAuthList = params.getGrantLiveAuths();
      List<AuthBaseUpdateDto> pRevokeLiveAuthList = params.getRevokeLiveAuths();
      List<AuthBaseUpdateDto> pGrantClosedAuthList = params.getGrantClosedAuths();
      List<AuthBaseUpdateDto> pRevokeClosedAuthList = params.getRevokeClosedAuths();
      List<AuthShareUpdateDto> pAuthShareList = params.getGrantShares();
      List<AuthShareUpdateDto> pRevokeShareList = params.getRevokeShares();
      
      // 아무 데이터도 받지 않았을 경우 잘못된 요청 처리 (400 Bad Request)
      if (Objects.isNull(pDoc)
          && Objects.isNull(pFolder)
          && Objects.isNull(pGrantLiveAuthList)
          && Objects.isNull(pRevokeLiveAuthList)
          && Objects.isNull(pGrantClosedAuthList)
          && Objects.isNull(pRevokeClosedAuthList)
          && Objects.isNull(pAuthShareList)
          && Objects.isNull(pRevokeShareList)
      ) {
        throw new BadRequestException("변경할 데이터가 없습니다.");
      } else {
        pDoc                  = Optional.ofNullable(pDoc).orElse(new DocDetailDto());
        pFolder               = Optional.ofNullable(pFolder).orElse(new FolderDetailDto());
        pGrantLiveAuthList    = Optional.ofNullable(pGrantLiveAuthList).orElse(new ArrayList<>());
        pRevokeLiveAuthList   = Optional.ofNullable(pRevokeLiveAuthList).orElse(new ArrayList<>());
        pGrantClosedAuthList  = Optional.ofNullable(pGrantClosedAuthList).orElse(new ArrayList<>());
        pRevokeClosedAuthList = Optional.ofNullable(pRevokeClosedAuthList).orElse(new ArrayList<>());
        pAuthShareList        = Optional.ofNullable(pAuthShareList).orElse(new ArrayList<>());
        pRevokeShareList      = Optional.ofNullable(pRevokeShareList).orElse(new ArrayList<>());
      }
    
      // 기존 폴더 데이터 조회
      Optional<Folder> optFolder = folderService.selectOne(dataId);
      Folder folder = optFolder.orElse(null);
    
      // 폴더 여부 확인
      isFolder = optFolder.isPresent();
 
      // 기존 문서 데이터 조회
      Optional<Doc> optDoc = docService.selectOne(dataId, isUDocKey);
      Doc doc = optDoc.orElse(null);
 
      // 문서 여부 확인
      isDoc = optDoc.isPresent();
    
      // 기존 중요문서 데이터 조회
      Optional<DocImp> optDocImp = docImpService.selectOne(dataId);
//      DocImp docImp = optDocImp.orElse(null);
      
      // 중요문서 여부 확인
      isDocImp = optDocImp.isPresent();
 
      // 폴더와 문서, 중요문서에 모두 해당될 경우 데이터 불량 에러
      if ( isFolder && isDoc && !isDocImp ) throw new RuntimeException("isFolder, isDoc, isDocImp의 값이 모두 true입니다.");
      // 폴더도 문서도 중요문서도 아닐 경우 찾을 수 없음 처리 (404 Not Found)
      if ( !isFolder && !isDoc && !isDocImp ) throw new NotFoundException("존재하지 않는 대상입니다.", dataId);
      
      // 자료 키값
      String objId = isDoc ? doc.getUDocKey() : dataId;
 
      // 조직함 하위 여부 확인
      Map<String, Code> commonCabinetDeptMap = codeService.getCommonCabinetDeptMap();
      isCommonCabinetDept = commonCabinetDeptMap.containsKey(isDoc ? doc.getUDeptCode() : folder.getUDeptCode());
      
      // 프로젝트/투자 또는 연구과제 하위 여부 확인
      isBelowPR = isDoc ? !DCTMConstants.DCTM_BLANK.equals(doc.getUPrCode()) : !DCTMConstants.DCTM_BLANK.equals(folder.getUPrCode());
 
      // 폴더 또는 문서에 대한 처리 필요 여부 확인
      doUpdateDoc    = isDoc    && !Objects.isNull(pDoc   );
//      doUpdateFolder = isFolder && !Objects.isNull(pFolder);
 
      // 개인정보포함여부 에 따른 권한 처리 여부 확인
      boolean isDocPrivacyFlagNotPrivacy = isDoc && ((pDoc.getUPrivacyFlag() != null && !pDoc.getUPrivacyFlag()) || !doc.getUPrivacyFlag());    // 개인정보포함여부가 미포함로 넘어왔거나 원본이 미포함인지 여부
      boolean doGrantRevoke = isFolder || isDocPrivacyFlagNotPrivacy;
        
      // 권한 처리 필요 여부 확인
      doGrantLiveAuth    = pGrantLiveAuthList.size()    > 0 && doGrantRevoke;
      doRevokeLiveAuth   = pRevokeLiveAuthList.size()   > 0 && doGrantRevoke;
      doGrantClosedAuth  = pGrantClosedAuthList.size()  > 0 && doGrantRevoke;
      doRevokeClosedAuth = pRevokeClosedAuthList.size() > 0 && doGrantRevoke;
      doGrantShare       = pAuthShareList.size()        > 0 && doGrantRevoke;
      doRevokeShare      = pRevokeShareList.size()      > 0 && doGrantRevoke;
 
      /* 변수 초기화 끝 */
      
      /* 제약조건 처리 시작 */
      
      // 전자결재에 의해 추가된 권한은 제거 금지
//      if (doRevokeLiveAuth || doRevokeClosedAuth) {
//        List<String> addGubunWList = doc.getDocRepeatings().stream()
//            .filter(item -> "W".equals(item.getUAddGubun())).map(item -> item.getUObjId()).collect(Collectors.toList());
//        boolean isLiveAddGubunW = pRevokeLiveAuthList.stream().anyMatch(item -> addGubunWList.stream().filter(item2 -> Objects.equals(item.getTargetId(), item2)).count() > 0);
//        boolean isClosedAddGubunW = pRevokeClosedAuthList.stream().anyMatch(item -> addGubunWList.stream().filter(item2 -> Objects.equals(item.getTargetId(), item2)).count() > 0);
//        if (isLiveAddGubunW || isClosedAddGubunW) throw new RuntimeException("전자결재에 의해 추가된 권한입니다.");
//      }
 
      // SOFTM - [자료수정] - [CHECK POINT .03] : 권한 수정 제한
      //                  기존 보안등급이 제한등급인 경우 다른 등급으로 변경 시도 시 잘못된 요청 처리 (400 Bad Request)
      //                  에러메시지 '제한등급 문서의 하향변경은 결재가 필요합니다. 정보처리의뢰서를 작성해 주시기 바랍니다. (결재선 : 부서장→직속임원)'
      if ( isDoc && pDoc.getUSecLevel() != null ) {
        int originalSecLevelOrder = SecLevelCode.findByValue(doc.getUSecLevel()).getOrder();
        int paramSecLevelOrder = SecLevelCode.findByValue(pDoc.getUSecLevel()).getOrder();
        if (originalSecLevelOrder < paramSecLevelOrder) {
          if ("Y".equals(doc.getUWfDocYn())) {
            throw new BadRequestException("해당 문서는 전자결재 승인이 완료되어 보안등급 조정이 불가합니다.");
          } else if (SecLevelCode.SEC.getValue().equals(doc.getUSecLevel()) && DocStatus.CLOSED.getValue().equals(doc.getUDocStatus())) {
            throw new BadRequestException("제한등급 문서의 하향변경은 결재가 필요합니다. 정보처리의뢰서를 작성해 주시기 바랍니다. (결재선 : 부서장→직속임원)");
          }
        }
      }
      
      // SOFTM - [자료수정] - [CHECK POINT .04] : 조직함 하위는 기본권한으로 고정, 보안등급 사내등급 고정
      // both :> doc , folder 
      // 보안등급(u_sec_level) 또는 권한 변경(grantLiveAuths, revokeLiveAuths, grantClosedAuths, revokeClosedAuths) 내용이 있을 경우
      if ( isCommonCabinetDept ) {
        if (doGrantLiveAuth
            || doRevokeLiveAuth
            || doGrantClosedAuth
            || doRevokeClosedAuth
            || ( isFolder && pFolder.getUSecLevel() != null )
            || ( isDoc && pDoc.getUSecLevel() != null )
        ) {
          throw new BadRequestException("조직함 하위권한 변경불가.");
        }
      }
      
      // 문서에 공유/협업을 추가 또는 삭제하려고 할 경우 잘못된 요청 처리 (400 Bad Request)
      if ( isDoc && (doGrantShare || doRevokeShare) ) {
        throw new BadRequestException( "문서는 공유협업대상을 지정할 수 없습니다." );
      }
 
      // 프로젝트/투자 또는 연구과제 하위일 경우
      if ( isBelowPR ) {
        String uPrType = isDoc ? doc.getUPrType() : folder.getUPrType();
        String uPrCode = isDoc ? doc.getUPrCode() : folder.getUPrCode();
 
        // SOFTM - [자료수정] - [CHECK POINT .06] : 참여부서에 속하지 않은 부서, 사용자 권한 추가할 경우 잘못된 요청 처리 (400 Bad Request)
        //                              - param.type이 U일경우는 targetId가 userId이므로, 사용자에대한 orgId를 구해서, edms_gw_user에서 orgid<= depthCd
        if ( doGrantLiveAuth || doGrantClosedAuth ) {
          // Case 1 : Type == 'D' : 참여부서 체크
          
          // Live Read 권한추가 대상 중 부서
          List<String> grantLiveReadAuthDeptCodes = pGrantLiveAuthList.stream()
              .filter(item -> AuthorType.TEAM.getValue().equals(item.getType()) && GrantedLevels.READ.getLabel().equals(item.getPermitType()))
              .map(item -> item.getTargetId())
              .collect(Collectors.toList());
          
          // Live Delete 권한추가 대상 중 부서
          List<String> grantLiveDelAuthDeptCodes = pGrantLiveAuthList.stream()
              .filter(item -> AuthorType.TEAM.getValue().equals(item.getType()) && GrantedLevels.DELETE.getLabel().equals(item.getPermitType()))
              .map(item -> item.getTargetId())
              .collect(Collectors.toList());
 
          // Closed 권한추가 대상 중 부서
          List<String> grantClosedAuthDeptCodes = pGrantClosedAuthList.stream()
              .filter(predicate -> predicate.getType().equals(AuthorType.TEAM.getValue()))
              .map(item -> item.getTargetId())
              .collect(Collectors.toList());

          // Live Read 권한의 경우 전체 주관부서 참여부서 소속 여부 확인
          for (String deptCode : grantLiveReadAuthDeptCodes) {
            boolean hasAuth = ProjectType.PROJECT.getValue().equals(uPrType)
                ? authService.checkProjectRootAuthByOrgId(uPrCode, deptCode, GrantedLevels.READ.getLabel())
                : authService.checkResearchRootAuthByOrgId(uPrCode, deptCode, GrantedLevels.READ.getLabel());
            if (!hasAuth) throw new RuntimeException("주관부서 또는 참여부서(조회/다운로드) 리스트에 없는 부서의 Live 권한은 추가할 수 없습니다.");
          }

          // Closed 권한의 경우 전체 주관부서 참여부서 소속 여부 확인
          for (String deptCode : grantClosedAuthDeptCodes) {
            boolean hasAuth = ProjectType.PROJECT.getValue().equals(uPrType)
                ? authService.checkProjectRootAuthByOrgId(uPrCode, deptCode, GrantedLevels.READ.getLabel())
                : authService.checkResearchRootAuthByOrgId(uPrCode, deptCode, GrantedLevels.READ.getLabel());
            if (!hasAuth) throw new RuntimeException("주관부서 또는 참여부서(조회/다운로드) 리스트에 없는 부서의 Closed 권한은 추가할 수 없습니다.");
          }
          
          // Live Delete 권한의 경우 주관부서 권한이 있는 참여부서 소속 여부 확인
          for (String deptCode : grantLiveDelAuthDeptCodes) {
            boolean hasAuth = ProjectType.PROJECT.getValue().equals(uPrType)
                ? authService.checkProjectRootAuthByOrgId(uPrCode, deptCode, GrantedLevels.DELETE.getLabel())
                : authService.checkResearchRootAuthByOrgId(uPrCode, deptCode, GrantedLevels.DELETE.getLabel());
            if (!hasAuth) throw new RuntimeException("주관부서 또는 참여부서 리스트에 없는 부서의 Live 권한은 추가할 수 없습니다.");
          }
          
          // Case 2 : Type == 'U' : 사용자의 부서를 이용, 참여부서 체크
 
          // Live Read 권한추가 대상 중 사용자
          List<String> grantLiveReadAuthUserIds = pGrantLiveAuthList.stream()
              .filter(item -> item.getType().equals(AuthorType.USER.getValue()) && GrantedLevels.READ.getLabel().equals(item.getPermitType()))
              .map(item -> item.getTargetId())
              .collect(Collectors.toList());
 
          // Live Delete 권한추가 대상 중 사용자
          List<String> grantLiveDelAuthUserIds = pGrantLiveAuthList.stream()
              .filter(item -> item.getType().equals(AuthorType.USER.getValue()) && GrantedLevels.DELETE.getLabel().equals(item.getPermitType()))
              .map(item -> item.getTargetId())
              .collect(Collectors.toList());
 
          // Closed 권한추가 대상 중 사용자
          List<String> grantClosedAuthUserIds = pGrantClosedAuthList.stream()
              .filter(predicate -> predicate.getType().equals(AuthorType.USER.getValue()))
              .map(item -> item.getTargetId())
              .collect(Collectors.toList());

          // Live Read 권한의 경우 전체 주관부서 참여부서 소속 여부 확인
          for (String userId : grantLiveReadAuthUserIds) {
            boolean hasAuth = ProjectType.PROJECT.getValue().equals(uPrType)
                ? authService.checkProjectRootAuth(uPrCode, userId, GrantedLevels.READ.getLabel())
                : authService.checkResearchRootAuth(uPrCode, userId, GrantedLevels.READ.getLabel());
            if (!hasAuth) throw new RuntimeException("주관부서 또는 참여부서(조회/다운로드) 리스트에 없는 사용자의 Live 권한은 추가할 수 없습니다.");
          }

          // Closed 권한의 경우 전체 주관부서 참여부서 소속 여부 확인
          for (String userId : grantClosedAuthUserIds) {
            boolean hasAuth = ProjectType.PROJECT.getValue().equals(uPrType)
                ? authService.checkProjectRootAuth(uPrCode, userId, GrantedLevels.READ.getLabel())
                : authService.checkResearchRootAuth(uPrCode, userId, GrantedLevels.READ.getLabel());
            if (!hasAuth) throw new RuntimeException("주관부서 또는 참여부서(조회/다운로드) 리스트에 없는 사용자의 Closed 권한은 추가할 수 없습니다.");
          }
          
          // Live Delete 권한의 경우 주관부서 권한이 있는 참여부서 소속 여부 확인
          for (String userId : grantLiveDelAuthUserIds) {
            boolean hasAuth = ProjectType.PROJECT.getValue().equals(uPrType)
                ? authService.checkProjectRootAuth(uPrCode, userId, GrantedLevels.DELETE.getLabel())
                : authService.checkResearchRootAuth(uPrCode, userId, GrantedLevels.DELETE.getLabel());
            if (!hasAuth) throw new RuntimeException("주관부서 또는 참여부서 리스트에 없는 사용자의 Live 권한은 추가할 수 없습니다.");
          }
        }
      }
 
      // SOFTM - [자료수정] - [CHECK POINT .08] : 권한에 따른 처리
      //                                  대상 자료에 대한 DELETE 권한이 없을 경우 권한없음 처리 (403 Forbidden)
      if ( isDoc ) {
        boolean hasDeleteAuth = authService.checkDocAuth(doc.getRObjectId(), currentUserId, GrantedLevels.READ.getLevel());
        
        if (!hasDeleteAuth) throw new ForbiddenException("해당 문서의 권한없음");
      }
      
      if ( isFolder ) {
        // 권한체크 대상인 폴더 타입 확인
        if (FolderType.DFO.getValue().equals(folder.getUFolType())
            || FolderType.PFO.getValue().equals(folder.getUFolType())
            || FolderType.RFO.getValue().equals(folder.getUFolType())) {
          // 폴더의 삭제권한 보유 여부 조회
          boolean hasDeleteAuth = authService.checkFolderAuth(dataId, currentUserId, GrantedLevels.DELETE.getLabel());
          
          // DELETE 권한이 없고 소유부서 또는 주관부서가 아닐 경우 권한없음 처리 (403 Forbidden)
          if (!hasDeleteAuth) throw new ForbiddenException("해당 폴더의 권한없음");
        }
      }
        
      //SOFTM - [자료수정] - [CHECK POINT .20] : 잠금 여부에 따른 처리
      if ( isFolder && FolderStatus.LOCK.getValue().equals(folder.getUFolStatus()) ) {
        // 대상 폴더가 잠겨있을 경우 권한없음 처리 (403 Forbidden)
        throw new BadRequestException("잠금 처리된 폴더입니다.");
      } else if ( isDoc ) {
        // 문서의 상위 폴더 조회
        Optional<Folder> optDocFolder = folderService.selectOne(doc.getUFolId());
        if (optDocFolder.isPresent()) {
          Folder docFolder = optDocFolder.get();
          
          // 상위 폴더가 잠겨있을 경우 권한없음 처리 (403 Forbidden)
          if ( FolderStatus.LOCK.getValue().equals(docFolder.getUFolStatus()) ) {
            throw new BadRequestException("잠금 처리된 폴더에 포함된 자료입니다.");
          }
        }
        
        // 문서가 편집중일 경우 에러 처리
        if (!DCTMConstants.DCTM_BLANK.equals(doc.getRLockOwner())) {
          throw new RuntimeException("편집 중인 문서입니다.");
        }
      }
      
      /* 제약조건 처리 끝 */
      
      try {
        // 트랜잭션 시작
        idfSession.beginTrans();
        
        /* 속성 수정 시작  */
        
        if ( isFolder ) {
          // SOFTM - [자료수정] - [CHECK POINT .11] : 폴더 속성 수정
          IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(dataId));
          if ( pFolder.getUCabinetCode () != null ) idf_PObj.setString("u_cabinet_code" , pFolder.getUCabinetCode ());
          if ( pFolder.getUFolType     () != null ) idf_PObj.setString("u_fol_type"     , pFolder.getUFolType     ());
          if ( pFolder.getUFolName     () != null ) idf_PObj.setString("u_fol_name"     , pFolder.getUFolName     ());
          if ( pFolder.getUUpFolId     () != null ) idf_PObj.setString("u_up_fol_id"    , pFolder.getUUpFolId     ());
          if ( pFolder.getUSecLevel    () != null ) idf_PObj.setString("u_sec_level"    , pFolder.getUSecLevel    ());
          if ( pFolder.getUFolStatus   () != null ) idf_PObj.setString("u_fol_status"   , pFolder.getUFolStatus   ());
          if ( pFolder.getUFolTag      () != null ) idf_PObj.setString("u_fol_tag"      , pFolder.getUFolTag      ());
          if ( pFolder.getUFolClass    () != null ) idf_PObj.setString("u_fol_class"    , pFolder.getUFolClass    ());
          if ( pFolder.getUEditableFlag() != null ) idf_PObj.setString("u_editable_flag", pFolder.getUEditableFlag());
          if ( pFolder.getUDeleteStatus() != null ) idf_PObj.setString("u_delete_status", pFolder.getUDeleteStatus());

          idf_PObj.setString("u_update_user", currentUserId);
          // 업데이트 시간
          idf_PObj.setTime("u_update_date", new DfTime (Timestamp.valueOf(now)));

          idf_PObj.save();
        } else if ( isDoc ) {
          // SOFTM - [자료수정] - [CHECK POINT .12] : 문서 속성 수정
          
          IDfDocument idf_PObj = (IDfDocument)idfSession.getObject(new DfId(doc.getRObjectId()));
          if ( pDoc.getObjectName       () != null ) {
                                                     idf_PObj.setString("object_name"            , pDoc.getObjectName       ());
                                                     idf_PObj.setString("title"                  , pDoc.getObjectName().concat(".").concat(doc.getUFileExt()));
          }
          if ( pDoc.getUCabinetCode     () != null ) idf_PObj.setString("u_cabinet_code"         , pDoc.getUCabinetCode     ());
          if ( pDoc.getUSecLevel        () != null ) idf_PObj.setString("u_sec_level"            , pDoc.getUSecLevel        ());
          
//          if ( pDoc.getUDocStatus       () != null ) {
//            IDfTime closedDate = new DfTime (Timestamp.valueOf(now));
//            
//                                                     idf_PObj.setString("u_doc_status"           , pDoc.getUDocStatus       ());
//                                                     idf_PObj.setTime("u_closed_date"            , closedDate                 );
//                                                     idf_PObj.setString("u_closer"               , currentUserId              );
//          }
          // closed -> live변경시 예외처리 추가 2022-01-27 
          if (pDoc.getUDocStatus() != null) {
            idf_PObj.setString("u_doc_status", pDoc.getUDocStatus());
            if (pDoc.getUDocStatus().equals("C")) {
              IDfTime closedDate = new DfTime(Timestamp.valueOf(now));
              idf_PObj.setTime("u_closed_date", closedDate);
              idf_PObj.setString("u_closer", currentUserId);
            }
          }
          
          if ( pDoc.getUDeleteStatus    () != null ) idf_PObj.setString("u_delete_status"        , pDoc.getUDeleteStatus    ());
          if ( pDoc.getUWfDocYn         () != null ) idf_PObj.setString("u_wf_doc_yn"            , pDoc.getUWfDocYn         ());
          if ( pDoc.getUPrivacyFlag     () != null ) idf_PObj.setBoolean("u_privacy_flag"        , pDoc.getUPrivacyFlag     ());
          if ( pDoc.getUPreserverFlag   () != null ) idf_PObj.setInt("u_preserve_flag"           , pDoc.getUPreserverFlag   ());
          if ( pDoc.getUDocTag          () != null ) idf_PObj.setString("u_doc_tag"              , pDoc.getUDocTag          ());
          if ( pDoc.getUDocClass        () != null ) idf_PObj.setString("u_doc_class"            , pDoc.getUDocClass        ());
          if ( pDoc.getUAutoAuthMailFlag() != null ) idf_PObj.setBoolean("u_auto_auth_mail_flag" , pDoc.getUAutoAuthMailFlag());

          // 업데이트 시간
          idf_PObj.setTime("u_update_date", new DfTime (Timestamp.valueOf(now)));
 
          // Closed로 변경 시 보존연한 만료일 계산
          if (DocStatus.CLOSED.getValue().equals(pDoc.getUDocStatus())) {
            int preserverFlag = pDoc.getUPreserverFlag() != null ? pDoc.getUPreserverFlag() : doc.getUPreserverFlag();
            
            LocalDateTime expireDate = preserverFlag == 0 ? LocalDateTime.of(9999, 12, 31, 0, 0) : now.plusYears(preserverFlag);
            IDfTime dfExpireDate = new DfTime (Timestamp.valueOf(expireDate)) ;
                
                                                     idf_PObj.setTime("u_expired_date"           , dfExpireDate               );
          }
          // 문서 상태(u_doc_status) 변경요청 없이 기존 상태 Closed일 경우
          else if (pDoc.getUDocStatus() == null && DocStatus.CLOSED.getValue().equals(doc.getUDocStatus())) {
            int preserverFlag = pDoc.getUPreserverFlag() != null ? pDoc.getUPreserverFlag() : doc.getUPreserverFlag();
            
            LocalDateTime closedDate = doc.getUClosedDate();
            LocalDateTime expireDate = preserverFlag == 0 ? LocalDateTime.of(9999, 12, 31, 0, 0) : closedDate.plusYears(preserverFlag);
            IDfTime dfExpireDate = new DfTime (Timestamp.valueOf(expireDate)) ;
                
                                                     idf_PObj.setTime("u_expired_date"           , dfExpireDate               );
          }
          if ( pDoc.getUVerKeepFlag() != null )      idf_PObj.setBoolean("u_ver_keep_flag"       , pDoc.getUVerKeepFlag     ());
        
          // Live/Closed여부(u_doc_status), Closed일 경우 보안등급(u_sec_level), 개인정보포함여부(u_privacy_flag), 결재문서여부(u_wf_doc_yn) 변경에 따른 ACL 재적용
          if (pDoc.getUDocStatus() != null
              || pDoc.getUSecLevel() != null
              || pDoc.getUPrivacyFlag() != null
              || pDoc.getUWfDocYn() != null
          ) {
            String  uDocStatus   = pDoc.getUDocStatus() != null                       ? pDoc.getUDocStatus()   : doc.getUDocStatus();           // Live/Closed 상태
            String  uSecLevel    = pDoc.getUSecLevel() != null                        ? pDoc.getUSecLevel()    : doc.getUSecLevel();           // 보안등급
            Boolean uPrivacyFlag = pDoc.getUPrivacyFlag() != null                     ? pDoc.getUPrivacyFlag() : doc.getUPrivacyFlag();         // 개인정보포함여부
            String  uWfDocYn     = pDoc.getUWfDocYn() != null                         ? pDoc.getUWfDocYn()     : doc.getUWfDocYn();             // 결재문서여부
            String  uPrType      = !DCTMConstants.DCTM_BLANK.equals(doc.getUPrType()) ? doc.getUPrType()       : HamType.DEPT.getValue();  // 프로젝트/투자, 연구과제 타입
 
            // 개인정보포함 또는 비공개일 경우 보안등급 제한 고정
            if (uPrivacyFlag) {
              uSecLevel = SecLevelCode.SEC.getValue();
            }
            
            String aclName = MessageFormat.format(
                AclTemplate.DEFAULT.getValue(),
                doc.getUCabinetCode(),
                CommonUtils.getAuthScope(uPrType),
                uPrivacyFlag ? DocFlag.PERSONAL.getValue() : DocFlag.GENERAL.getValue(),
                DocStatus.LIVE.getValue().equals(uDocStatus) ? DocStatus.LIVE.getValue() : uSecLevel,
                uWfDocYn
            ).toLowerCase();
                        
            // 문서에 새 ACL 설정
            idf_PObj.setACLDomain(idfSession.getDocbaseOwnerName());
            idf_PObj.setACLName(aclName);
            
            // 프로젝트/투자 연구과제 책임자 그룹 추가
            if (ProjectType.PROJECT.getValue().equals(doc.getUPrType())) {
              String mgrGroupCode = "g_"+ doc.getUPrCode()+"_pjtmgr";
              idf_PObj.grant(mgrGroupCode, GrantedLevels.DELETE.getLevel(), null);
            } else if (ProjectType.RESEARCH.getValue().equals(doc.getUPrType())) {
              String mgrGroupCode = "g_"+ doc.getUPrCode()+"_rschmgr";
              idf_PObj.grant(mgrGroupCode, GrantedLevels.DELETE.getLevel(), null);
            }
            
            // Closed 변환시 Closed 기준 ACL에 없는 권한 다시 추가, Live 변환 시 Live 기준 ACL에 없는 권한 다시 추가
            if( isDoc && doUpdateDoc && pDoc.getUDocStatus() != null) {
              List<AuthBase> authBaseDocStatusList = authBaseDao.selectLiveCloseList(dataId, pDoc.getUDocStatus());
              
              for (AuthBase authBase : authBaseDocStatusList) {
                String authorId = authBase.getUAuthorId();
                String authorType = authBase.getUAuthorType();
                String addGubun = authBase.getUAddGubun();
                int permitLevel = GrantedLevels.findByLabel(authBase.getUPermitType());

                idf_PObj.grant(authorId, permitLevel, null);    //조회/다운로드 권한(부서)

                // groupid + _sub (하위권한자 포함) 추가
                boolean isClosed = DocStatus.CLOSED.getValue().equals(pDoc.getUDocStatus());
                String secLevel = pDoc.getUSecLevel() != null ? pDoc.getUSecLevel() : doc.getUSecLevel();
                boolean isSecComGroup = SecLevelCode.COMPANY.getValue().equals(secLevel) || SecLevelCode.GROUP.getValue().equals(secLevel);
                if (("P".equals(addGubun) || ("G".equals(addGubun) && isSecComGroup && isClosed)) && "D".equals(authorType)) {
                  idf_PObj.grant(authorId.concat("_sub"), permitLevel, null);
                }
              }
            }
 
            // 기존 auth_base 전체 조회
            List<AuthBase> authBaseList = authBaseDao.selectList(dataId, "A");

            // 백업된 적 있는지 여부
            boolean isBackedUp = authBaseList.stream().anyMatch(item -> AuthObjType.BACKUP.getValue().equals(item.getUObjType()));
            
            // 개인정보 미포함일 경우
            if (!uPrivacyFlag) {
              // 백업된 문서일 경우에만 auth_base 원복
              if (isBackedUp) {
                for (AuthBase authBase : authBaseList) {
                  IDfPersistentObject iDfAuthBase = idfSession.getObject(new DfId(authBase.getRObjectId()));
          
                  // 백업된 권한 다시 살리기
                  if (AuthObjType.BACKUP.getValue().equals(iDfAuthBase.getString("u_obj_type"))) {
                    iDfAuthBase.setString("u_obj_type", AuthObjType.DOCUMENT.getValue());
                    
                    iDfAuthBase.save();  
                  }
                  // 백업이 아닌 권한 삭제
                  else {
                    if (DocStatus.LIVE.getValue().equals(authBase.getUDocStatus())) {
                      iDfAuthBase.destroy(); 
                      pRevokeLiveAuthList = pRevokeLiveAuthList.stream()
                          .filter(item -> !StringUtils.equals(authBase.getRObjectId(), item.getRObjectId()))
                          .collect(Collectors.toList());
                    }
                  }
                }
              }
              
              // DocStatus에 맞는 권한 리스트
              List<AuthBase> filteredAuthBaseList = authBaseList.stream()
                  .filter(item -> uDocStatus.equals(item.getUDocStatus()) && !AuthorType.DEFAULT.getValue().equals(item.getUAuthorType()))
                  .collect(Collectors.toList());
      
              // permitType에 맞는 권한 부여
              if ( isDoc ) {
                for (AuthBase authBase : filteredAuthBaseList) {
                  int permitLevel = GrantedLevels.findByLabel(authBase.getUPermitType());
                  String authorId = authBase.getUAuthorId();
                  
                  idf_PObj.grant(authorId, permitLevel, null);
                }
              }
            }
            // 개인정보 포함일 경우
            else {
              // 개인정보포함일 경우 보안등급 제한 고정
              if (uPrivacyFlag) {
                idf_PObj.setString("u_sec_level", SecLevelCode.SEC.getValue());
              }
              
              // 기존 권한 백업 (authorType 을 B로 변경)
              for (AuthBase authBase : authBaseList) {
                if (DocStatus.LIVE.getValue().equals(authBase.getUDocStatus())) {
                  if (!isBackedUp) {
                    IDfPersistentObject iDfAuthBase = idfSession.getObject(new DfId(authBase.getRObjectId()));
                    iDfAuthBase.setString("u_obj_type", AuthObjType.BACKUP.getValue());
                    
                    iDfAuthBase.save();
                  }
                }
                else {
                  // 개인정보포함일 경우 기존 Closed 권한 삭제 (복구될 일 없음)
                  if (uPrivacyFlag && !isBackedUp) {
                    IDfPersistentObject iDfAuthBase = idfSession.getObject(new DfId(authBase.getRObjectId()));
                    iDfAuthBase.destroy();
                  }
                }
              }
 
              boolean hasSelfLiveAuth = authBaseList.stream()
                  .filter(item -> DocStatus.LIVE.getValue().equals(item.getUDocStatus()))
                  .anyMatch(item -> StringUtils.equals(currentUserId, item.getUAuthorId()));
 
              if (!hasSelfLiveAuth) {
                // 권한설정자에게 Live DELETE 권한 추가
                IDfPersistentObject iDfAuthBaseLive = (IDfPersistentObject)idfSession.newObject("edms_auth_base");
                iDfAuthBaseLive.setString("u_obj_id"      , dataId);
                iDfAuthBaseLive.setString("u_obj_type"    , AuthObjType.DOCUMENT.getValue());
                iDfAuthBaseLive.setString("u_doc_status"  , DocStatus.LIVE.getValue());
                iDfAuthBaseLive.setString("u_permit_type" , GrantedLevels.DELETE.getLabel());
                iDfAuthBaseLive.setString("u_own_dept_yn" , "N");
                iDfAuthBaseLive.setString("u_author_id"   , currentUserId);
                iDfAuthBaseLive.setString("u_author_type" , AuthorType.USER.getValue());
                iDfAuthBaseLive.setString("u_create_user" , currentUserId);
                iDfAuthBaseLive.setString("u_create_date" , (new DfTime()).toString());
                iDfAuthBaseLive.setString("u_add_gubun"   , "P");
                
                iDfAuthBaseLive.save();
              }
 
              boolean hasSelfClosedAuth = authBaseList.stream()
                  .filter(item -> DocStatus.CLOSED.getValue().equals(item.getUDocStatus()))
                  .anyMatch(item -> StringUtils.equals(currentUserId, item.getUAuthorId()));
              
              // 개인정보포함일 경우 권한설정자에게 Closed 권한 추가
              if (uPrivacyFlag && !hasSelfClosedAuth) {
                IDfPersistentObject iDfAuthBaseClosed = (IDfPersistentObject)idfSession.newObject("edms_auth_base");
                iDfAuthBaseClosed.setString("u_obj_id"      , dataId);
                iDfAuthBaseClosed.setString("u_obj_type"    , AuthObjType.DOCUMENT.getValue());
                iDfAuthBaseClosed.setString("u_doc_status"  , DocStatus.CLOSED.getValue());
                iDfAuthBaseClosed.setString("u_permit_type" , GrantedLevels.READ.getLabel());
                iDfAuthBaseClosed.setString("u_own_dept_yn" , "N");
                iDfAuthBaseClosed.setString("u_author_id"   , currentUserId);
                iDfAuthBaseClosed.setString("u_author_type" , AuthorType.USER.getValue());
                iDfAuthBaseClosed.setString("u_create_user" , currentUserId);
                iDfAuthBaseClosed.setString("u_create_date" , (new DfTime()).toString());
                iDfAuthBaseClosed.setString("u_add_gubun"   , "G");
                
                iDfAuthBaseClosed.save();
              }
                
              idf_PObj.grant(currentUserId, GrantedLevels.DELETE.getLevel(), null);
            }
          }
      
          idf_PObj.save();
        }
        
        /* 속성 수정 끝  */
                
        /* 개별 권한 적용 시작  */
        
        // Live 권한 해제
        if ( doRevokeLiveAuth ) {
          for (AuthBaseUpdateDto item : pRevokeLiveAuthList) {
            // 대상 author_id 설정
            String authorId = item.getTargetId();
            // 부서일 경우 캐비닛 코드로 변경
            if (AuthorType.TEAM.getValue().equals(item.getType())) {
              Optional<VDept> dept = gwDeptDao.selectOneByOrgId(item.getTargetId());
              if (dept.isPresent()) authorId = "g_" + dept.get().getUCabinetCode();
            }
            
            String s_Dql = "DELETE edms_auth_base OBJECTS WHERE u_doc_status = 'L' AND (u_author_id = '" + authorId + "' OR u_author_id = '" + authorId + "_sub') AND u_obj_id ='" + objId + "'";

            IDfCollection idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
            if (idf_Colb != null) idf_Colb.close();
 
            // 문서일 경우 도큐멘텀에서 권한 해제
            if ( isDoc ) {
              boolean isLivePDoc = DocStatus.LIVE.getValue().equals(pDoc.getUDocStatus());
              boolean isLiveDoc = DocStatus.LIVE.getValue().equals(doc.getUDocStatus());
              if ( isLivePDoc || (pDoc.getUDocStatus() == null && isLiveDoc) ) {
                IDfDocument iDfDoc = (IDfDocument)idfSession.getObject(new DfId(doc.getRObjectId()));
                iDfDoc.revoke(authorId, null);
                iDfDoc.revoke(authorId + "_sub", null);
              
                iDfDoc.save();
              }
            }
          }
        }
            
        // Closed 권한 해제
        if ( doRevokeClosedAuth ) {
          for (AuthBaseUpdateDto item : pRevokeClosedAuthList) {
            // 대상 author_id 설정
            String authorId = item.getTargetId();
            // 부서일 경우 캐비닛 코드로 변경
            if (AuthorType.TEAM.getValue().equals(item.getType())) {
              Optional<VDept> dept = gwDeptDao.selectOneByOrgId(item.getTargetId());
              if (dept.isPresent()) authorId = "g_" + dept.get().getUCabinetCode();
            }
            
            String s_Dql = "DELETE edms_auth_base OBJECTS WHERE u_doc_status = 'C' AND (u_author_id = '" + authorId + "' OR u_author_id = '" + authorId + "_sub') AND u_obj_id ='" + objId + "'";

            IDfCollection idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
            if (idf_Colb != null) idf_Colb.close();
 
            // 문서일 경우 도큐멘텀에서 권한 해제
            if ( isDoc ) {
              boolean isClosedPDoc = DocStatus.CLOSED.getValue().equals(pDoc.getUDocStatus());
              boolean isClosedDoc = DocStatus.CLOSED.getValue().equals(doc.getUDocStatus());
              if ( isClosedPDoc || (pDoc.getUDocStatus() == null && isClosedDoc) ) {
                IDfDocument iDfDoc = (IDfDocument)idfSession.getObject(new DfId(doc.getRObjectId()));
                iDfDoc.revoke(authorId, null);
                iDfDoc.revoke(authorId + "_sub", null);
              
                iDfDoc.save();
              }
            }
          }
        }
        
        // 기존 auth_base 전체 조회
        List<AuthBase> authBaseList = authBaseDao.selectList(objId, "A");
 
        if (isMultipleAuth) {
          // 권한 기본값 외에 제거
          authBaseList = authBaseList.stream()
              .filter((item) -> "S".equals(item.getUAuthorType()) && !item.getUAuthorId().contains("_sub"))
              .collect(Collectors.toList());

          // 기존 권한 삭제
          String s_Dql = "DELETE edms_auth_base OBJECTS WHERE (u_author_type != 'S' OR u_author_id like '%_sub%') AND u_obj_id ='" + objId + "'";
          IDfCollection idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
          if (idf_Colb != null) idf_Colb.close();
          
          // 삭제 대상 기존 권한
          List<AuthBase> filtered = authBaseList.stream()
                  .filter((item) -> !"S".equals(item.getUAuthorType()) || item.getUAuthorId().contains("_sub"))
                  .collect(Collectors.toList());
 
          for (AuthBase item : filtered) {
            // 문서일 경우 도큐멘텀에서 권한 해제
            if ( isDoc ) {
              boolean isClosedPDoc = DocStatus.CLOSED.getValue().equals(pDoc.getUDocStatus());
              boolean isClosedDoc = DocStatus.CLOSED.getValue().equals(doc.getUDocStatus());
              if ( isClosedPDoc || (pDoc.getUDocStatus() == null && isClosedDoc) ) {
                String authorId = item.getUAuthorId();
                if(StringUtils.isNotEmpty(authorId)) {
                  IDfDocument iDfDoc = (IDfDocument)idfSession.getObject(new DfId(doc.getRObjectId()));
                  iDfDoc.revoke(authorId, null);
 
                  iDfDoc.save();
                }
              }
            }
          }
        }

        // Live 권한 부여
        if ( doGrantLiveAuth ) {
          List<AuthBase> authBaseListForGrant = authBaseList.stream()
              .filter(item -> DocStatus.LIVE.getValue().equals(item.getUDocStatus()) && !"S".equals(item.getUAuthorType()))
              .collect(Collectors.toList());

          // 권한 해제 대상이었던 공유/협업 아이디 리스트
          List<String> revokeAuthBaseIdList = pRevokeLiveAuthList.stream().map(item2 -> item2.getRObjectId()).collect(Collectors.toList());
          
          for (AuthBaseUpdateDto item : pGrantLiveAuthList) {
            // 대상 author_id 설정
            String authorId = item.getTargetId();
            // 부서일 경우 캐비닛 코드로 변경
            if (AuthorType.TEAM.getValue().equals(item.getType())) {
              Optional<VDept> dept = gwDeptDao.selectOneByOrgId(item.getTargetId());
              if (dept.isPresent()) authorId = "g_" + dept.get().getUCabinetCode();
            }
            
            // 중복권한 체크, 하위권한 보유 시 상위권한으로 변경
            String objType = isDoc ? AuthObjType.DOCUMENT.getValue() : AuthObjType.FOLDER.getValue();
            String authorType = item.getType();
            String ownDeptYn = AuthorType.TEAM.getValue().equals(authorType) && currentOrgId.equals(item.getTargetId()) ? "Y" : "N";
            String addGubun = item.getAddGubun() == null ? "P" : item.getAddGubun();

            final String finalAuthorId = authorId;
            Optional<AuthBase> currentAuthBase = authBaseListForGrant.stream()
                .filter(item2 -> !revokeAuthBaseIdList.stream().anyMatch((item3) -> Objects.equals(item2.getRObjectId(), item3)))
                .filter(item2 -> StringUtils.equals(item2.getUAuthorId(), finalAuthorId))
                .findFirst();

            // 기존 권한이 이미 있을 경우 권한이 높은 경우에 대해서만 업그레이드
            if (currentAuthBase.isPresent()) {
              int pPermitTypeLevel = GrantedLevels.findByLabel(item.getPermitType());
              int currentPermitTypeLevel = GrantedLevels.findByLabel(currentAuthBase.get().getUPermitType());
              if (pPermitTypeLevel > currentPermitTypeLevel) {
                IDfPersistentObject iDfDescentantAuthShare = (IDfPersistentObject)idfSession.getObject(new DfId(currentAuthBase.get().getRObjectId()));
                iDfDescentantAuthShare.setString("u_permit_type", item.getPermitType());
                iDfDescentantAuthShare.setString("u_add_gubun", addGubun);
                
                iDfDescentantAuthShare.save();
              }
            }
            // 기존 권한이 없을 경우 새로 생성
            else {
              IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
   
              idf_PObj.setString("u_obj_id"       , objId);
              idf_PObj.setString("u_obj_type"     , objType);
              idf_PObj.setString("u_doc_status"   , DocStatus.LIVE.getValue());
              idf_PObj.setString("u_permit_type"  , item.getPermitType());
              idf_PObj.setString("u_own_dept_yn"  , ownDeptYn);
              idf_PObj.setString("u_author_id"    , authorId);
              idf_PObj.setString("u_author_type"  , authorType);
              idf_PObj.setString("u_create_user"  , currentUserId);
              idf_PObj.setString("u_create_date"  , (new DfTime()).toString());
              idf_PObj.setString("u_add_gubun"    , addGubun);
                    
              idf_PObj.save();
            }
 
            // 문서일 경우 permitType에 맞는 권한 부여
            if ( isDoc ) {
              boolean isLivePDoc = DocStatus.LIVE.getValue().equals(pDoc.getUDocStatus());
              boolean isLiveDoc = DocStatus.LIVE.getValue().equals(doc.getUDocStatus());
              if ( isLivePDoc || (pDoc.getUDocStatus() == null && isLiveDoc) ) {
                int permitLevel = GrantedLevels.findByLabel(item.getPermitType());
                
                IDfDocument iDfDoc = (IDfDocument)idfSession.getObject(new DfId(doc.getRObjectId()));
                iDfDoc.grant(authorId, permitLevel, null);
                
                // groupid + _sub (하위권한자 포함) 추가
                String secLevel = pDoc.getUSecLevel() != null ? pDoc.getUSecLevel() : doc.getUSecLevel();
                boolean isSecComGroup = SecLevelCode.COMPANY.getValue().equals(secLevel) || SecLevelCode.GROUP.getValue().equals(secLevel); 
                if (("P".equals(addGubun) || ("G".equals(addGubun) && isSecComGroup)) && "D".equals(authorType)) {
                  iDfDoc.grant(authorId.concat("_sub"), permitLevel, null);
                }
                iDfDoc.save();
              }
            } else {
              // groupid + _sub (하위권한자 포함) 추가
              String secLevel = pFolder.getUSecLevel() != null ? pFolder.getUSecLevel() : folder.getUSecLevel();
              boolean isSecComGroup = SecLevelCode.COMPANY.getValue().equals(secLevel) || SecLevelCode.GROUP.getValue().equals(secLevel); 
              if (("P".equals(addGubun) || ("G".equals(addGubun) && isSecComGroup)) && "D".equals(authorType)) {
                IDfPersistentObject idf_PObj2 = idfSession.newObject("edms_auth_base");
                
                idf_PObj2.setString("u_obj_id"       , objId);
                idf_PObj2.setString("u_obj_type"     , objType);
                idf_PObj2.setString("u_doc_status"   , DocStatus.LIVE.getValue());
                idf_PObj2.setString("u_permit_type"  , item.getPermitType());
                idf_PObj2.setString("u_own_dept_yn"  , ownDeptYn);
                idf_PObj2.setString("u_author_id"    , authorId.concat("_sub"));
                idf_PObj2.setString("u_author_type"  , AuthorType.DEFAULT.getValue());
                idf_PObj2.setString("u_create_user"  , currentUserId);
                idf_PObj2.setString("u_create_date"  , (new DfTime()).toString());
                idf_PObj2.setString("u_add_gubun"    , addGubun);
                      
                idf_PObj2.save();
              }
            }
          }
        }
            
        // Closed 권한 부여
        if ( doGrantClosedAuth ) {
          List<AuthBase> authBaseListForGrant = authBaseList.stream()
              .filter(item -> DocStatus.CLOSED.getValue().equals(item.getUDocStatus()) && !"S".equals(item.getUAuthorType()))
              .collect(Collectors.toList());

          // 권한 해제 대상이었던 공유/협업 아이디 리스트
          List<String> revokeAuthBaseIdList = pRevokeClosedAuthList.stream().map(item2 -> item2.getRObjectId()).collect(Collectors.toList());
          
          for (AuthBaseUpdateDto item : pGrantClosedAuthList) {
            // 대상 author_id 설정
            String authorId = item.getTargetId();
            // 부서일 경우 캐비닛 코드로 변경
            if (AuthorType.TEAM.getValue().equals(item.getType())) {
              Optional<VDept> dept = gwDeptDao.selectOneByOrgId(item.getTargetId());
              if (dept.isPresent()) authorId = "g_" + dept.get().getUCabinetCode();
            }
            
            // 중복권한 체크, 하위권한 보유 시 상위권한으로 변경
            String objType = isDoc ? AuthObjType.DOCUMENT.getValue() : AuthObjType.FOLDER.getValue();
            String authorType = item.getType();
            String ownDeptYn = AuthorType.TEAM.getValue().equals(authorType) && currentOrgId.equals(item.getTargetId()) ? "Y" : "N";
            String addGubun = item.getAddGubun() == null ? "P" : item.getAddGubun();

            final String finalAuthorId = authorId;
            Optional<AuthBase> currentAuthBase = authBaseListForGrant.stream()
                .filter(item2 -> !revokeAuthBaseIdList.stream().anyMatch((item3) -> Objects.equals(item2.getRObjectId(), item3)))
                .filter(item2 -> StringUtils.equals(item2.getUAuthorId(), finalAuthorId))
                .findFirst();

            // 기존 권한이 이미 있을 경우 권한이 높은 경우에 대해서만 업그레이드
            if (currentAuthBase.isPresent()) {
              int pPermitTypeLevel = GrantedLevels.findByLabel(item.getPermitType());
              int currentPermitTypeLevel = GrantedLevels.findByLabel(currentAuthBase.get().getUPermitType());
              if (pPermitTypeLevel > currentPermitTypeLevel) {
                IDfPersistentObject iDfDescentantAuthShare = (IDfPersistentObject)idfSession.getObject(new DfId(currentAuthBase.get().getRObjectId()));
                iDfDescentantAuthShare.setString("u_permit_type", item.getPermitType());
                iDfDescentantAuthShare.setString("u_add_gubun", addGubun);                
                iDfDescentantAuthShare.save();
              }
            }
            // 기존 권한이 없을 경우 새로 생성
            else {
              IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
              idf_PObj.setString("u_obj_id"       , objId);
              idf_PObj.setString("u_obj_type"     , objType);
              idf_PObj.setString("u_doc_status"   , DocStatus.CLOSED.getValue());
              idf_PObj.setString("u_permit_type"  , GrantedLevels.READ.getLabel());
              idf_PObj.setString("u_own_dept_yn"  , ownDeptYn);
              idf_PObj.setString("u_author_id"    , authorId);
              idf_PObj.setString("u_author_type"  , authorType);
              idf_PObj.setString("u_create_user"  , currentUserId);
              idf_PObj.setString("u_create_date"  , (new DfTime()).toString());
              idf_PObj.setString("u_add_gubun"    , addGubun);
                    
              idf_PObj.save();
            }
 
            // 문서일 경우 permitType에 맞는 권한 부여
            if ( isDoc ) {
              boolean isClosedPDoc = DocStatus.CLOSED.getValue().equals(pDoc.getUDocStatus());
              boolean isClosedDoc = DocStatus.CLOSED.getValue().equals(doc.getUDocStatus());
              if ( isClosedPDoc || (pDoc.getUDocStatus() == null && isClosedDoc) ) {
                int permitLevel = GrantedLevels.findByLabel(item.getPermitType());
                
                IDfDocument iDfDoc = (IDfDocument)idfSession.getObject(new DfId(doc.getRObjectId()));
                iDfDoc.grant(authorId, permitLevel, null);

                // groupid + _sub (하위권한자 포함) 추가
                String secLevel = pDoc.getUSecLevel() != null ? pDoc.getUSecLevel() : doc.getUSecLevel();
                boolean isSecComGroup = SecLevelCode.COMPANY.getValue().equals(secLevel) || SecLevelCode.GROUP.getValue().equals(secLevel); 
                if (("P".equals(addGubun) || ("G".equals(addGubun) && isSecComGroup)) && "D".equals(authorType)) {
                  iDfDoc.grant(authorId.concat("_sub"), permitLevel, null);
                }
                iDfDoc.save();
              }
            } else {
              // groupid + _sub (하위권한자 포함) 추가
              String secLevel = pFolder.getUSecLevel() != null ? pFolder.getUSecLevel() : folder.getUSecLevel();
              boolean isSecComGroup = SecLevelCode.COMPANY.getValue().equals(secLevel) || SecLevelCode.GROUP.getValue().equals(secLevel); 
              if (("P".equals(addGubun) || ("G".equals(addGubun) && isSecComGroup)) && "D".equals(authorType)) {
                IDfPersistentObject idf_PObj2 = idfSession.newObject("edms_auth_base");
                
                idf_PObj2.setString("u_obj_id"       , objId);
                idf_PObj2.setString("u_obj_type"     , objType);
                idf_PObj2.setString("u_doc_status"   , DocStatus.CLOSED.getValue());
                idf_PObj2.setString("u_permit_type"  , GrantedLevels.READ.getLabel());
                idf_PObj2.setString("u_own_dept_yn"  , ownDeptYn);
                idf_PObj2.setString("u_author_id"    , authorId.concat("_sub"));
                idf_PObj2.setString("u_author_type"  , AuthorType.DEFAULT.getValue());
                idf_PObj2.setString("u_create_user"  , currentUserId);
                idf_PObj2.setString("u_create_date"  , (new DfTime()).toString());
                idf_PObj2.setString("u_add_gubun"    , addGubun);
                      
                idf_PObj2.save();
              }
            }
          }
        }
        
        // 문서일 경우 과거 문서에도 최종 ACL 적용
        if (isDoc) {
          IDfDocument iDfDoc = (IDfDocument)idfSession.getObject(new DfId(doc.getRObjectId()));
          String aclName = iDfDoc.getACLName();
          
          List<Doc> docList = docService.selectList(DocFilterDto.builder().iHasFolder(null).uDocKey(dataId).build());
          for (Doc pastDoc : docList) {
            if (!Objects.equals(pastDoc.getRObjectId(), doc.getRObjectId())) {
              IDfDocument iDfPastDoc = (IDfDocument)idfSession.getObject(new DfId(pastDoc.getRObjectId()));
              iDfPastDoc.setACLName(aclName);
              iDfPastDoc.save();
            }
          }
        }
                
        /* 개별 권한 적용 끝  */
        
        // 공유/협업 알림을 위한 문서 리스트
        List<Doc> shareDocList = new ArrayList<>();
                
        /* 공유/협업 적용 시작  */
        
        // SOFTM - [자료수정] - [CHECK POINT .17] : 폴더일 경우 공유/협업 대상으로 추가
        //                  폴더일 경우 공유/협업 대상으로 추가하고 문서 Closed 권한설정 기본에 권한 추가
        //                  폴더가 아닐 경우 잘못된 요청 처리 (400 Bad Request)
        if (isFolder) {
          // 공유/협업 권한 해제
          if ( doRevokeShare ) {
            // 권한있는 하위 폴더 리스트
            List<FolderDescendantDto> folderDescendantList = folderService.selectDescendants(dataId, currentUserId, true);
            
            // 권한있는 하위 문서 리스트
            List<String> folIdListForDescendantDoc = folderDescendantList.stream().map(item -> item.getRObjectId()).collect(Collectors.toList());
            List<Doc> docDescendantList = docService.selectAuthorizedListByFolIds(folIdListForDescendantDoc, currentUserId, GrantedLevels.READ.getLevel());
            
            for (AuthShareUpdateDto item : pRevokeShareList) {
              // 하위(자손)의 권한있는 폴더에서 Live/Closed 권한 제거
              for (FolderDescendantDto descendantFolder : folderDescendantList) {
                // 대상 author_id 설정
                String authorId = item.getTargetId();
                // 부서일 경우 캐비닛 코드로 변경
                if (AuthorType.TEAM.getValue().equals(item.getType())) {
                  Optional<VDept> dept = gwDeptDao.selectOneByOrgId(item.getTargetId());
                  if (dept.isPresent()) authorId = dept.get().getUCabinetCode();
                }
                
                String s_Dql = "DELETE edms_auth_share OBJECTS WHERE u_permit_type = '" + item.getPermitType() + "' AND u_author_id = '" + authorId + "' AND u_obj_id ='" + descendantFolder.getRObjectId() + "'";

                IDfCollection idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
                if (idf_Colb != null) idf_Colb.close();
              }

              // 하위(자손)의 권한있는 문서에 Live/Closed 권한 제거
              for (Doc descendantDoc : docDescendantList) {
                // 대상 author_id 설정
                String authorId = item.getTargetId();
                // 부서일 경우 캐비닛 코드로 변경
                if (AuthorType.TEAM.getValue().equals(item.getType())) {
                  Optional<VDept> dept = gwDeptDao.selectOneByOrgId(item.getTargetId());
                  if (dept.isPresent()) authorId = "g_" + dept.get().getUCabinetCode();
                }
                
                String s_Dql = "DELETE edms_auth_base OBJECTS WHERE u_add_gubun = 'S' AND (u_author_id = '" + authorId + "' OR u_author_id = 'g_" + authorId + "') AND u_obj_id ='" + descendantDoc.getUDocKey() + "'";

                IDfCollection idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
                if (idf_Colb != null) idf_Colb.close();
              }
            }
          }
          
          // 공유/협업 권한 부여
          if ( doGrantShare ) {
            // 권한있는 하위 폴더 리스트
            List<FolderDescendantDto> folderDescendantList = folderService.selectDescendants(dataId, currentUserId, true);
            
            // 권한있는 하위 문서 리스트
            List<String> folIdListForDescendantDoc = folderDescendantList.stream().map(item -> item.getRObjectId()).collect(Collectors.toList());
            List<Doc> docDescendantList = docService.selectAuthorizedListByFolIds(folIdListForDescendantDoc, currentUserId, GrantedLevels.READ.getLevel());
            shareDocList = docDescendantList;

            // 권한 해제 대상이었던 공유/협업 아이디 리스트
            List<String> revokeShareIdList = pRevokeShareList.stream().map(item2 -> item2.getRObjectId()).collect(Collectors.toList());
            
            for (AuthShareUpdateDto item : pAuthShareList) {
              // 대상 author_id 설정
              String authorId = item.getTargetId();
              // 부서일 경우 캐비닛 코드로 변경
              if (AuthorType.TEAM.getValue().equals(item.getType())) {
                Optional<VDept> dept = gwDeptDao.selectOneByOrgId(item.getTargetId());
                if (dept.isPresent()) authorId = dept.get().getUCabinetCode();
              }
              
              // 하위(자손)의 권한있는 폴더에 Live/Closed 권한 부여
              for (FolderDescendantDto descendantFolder : folderDescendantList) {
                List<AuthShare> descentantAuthShareList = authShareDao.selectList(descendantFolder.getRObjectId());
                final String finalAuthorId = authorId;
                Optional<AuthShare> currentAuthShare = descentantAuthShareList.stream()
                    .filter(item2 -> !revokeShareIdList.stream().anyMatch((item3) -> Objects.equals(item2.getRObjectId(), item3)))
                    .filter(item2 -> StringUtils.equals(item2.getUAuthorId(), finalAuthorId))
                    .findFirst();

                // 기존 공유/협업이 이미 있을 경우 권한이 높은 경우에 대해서만 업그레이드
                if (currentAuthShare.isPresent()) {
                  int pPermitTypeLevel = GrantedLevels.findByLabel(item.getPermitType());
                  int currentPermitTypeLevel = GrantedLevels.findByLabel(currentAuthShare.get().getUPermitType());
                  if (pPermitTypeLevel > currentPermitTypeLevel) {
                    IDfPersistentObject iDfDescentantAuthShare = (IDfPersistentObject)idfSession.getObject(new DfId(currentAuthShare.get().getRObjectId()));
                    iDfDescentantAuthShare.setString("u_permit_type", item.getPermitType());
                    
                    iDfDescentantAuthShare.save();
                  }
                }
                // 기존 공유/협업이 없을 경우 새로 생성
                else {
                  IDfPersistentObject iDfDescentantAuthShare = RegistAuthShareDto.toIDfPersistentObject(
                      idfSession, 
                      RegistAuthShareDto.builder()
                          .uObjId(descendantFolder.getRObjectId())
                          .uAuthorId(authorId)
                          .uAuthorType(item.getType())
                          .uPermitType(item.getPermitType())
                          .build()
                  );
                        
                  iDfDescentantAuthShare.save();
                }
              }
              
              // 하위(자손)의 권한있는 문서에 Live/Closed 권한 부여
              for (Doc descendantDoc : docDescendantList) {
                String authorType = item.getType();
                String authAuthorId = AuthorType.TEAM.getValue().equals(authorType) ? "g_" + authorId : authorId;
                String ownDeptYn = AuthorType.TEAM.getValue().equals(authorType) && currentOrgId.equals(item.getTargetId()) ? "Y" : "N";

                List<AuthBase> authBaseListForDescendant = authBaseDao.selectList(descendantDoc.getUDocKey(), "A").stream()
                    .filter(item2 -> !"S".equals(item2.getUAuthorType()))
                    .collect(Collectors.toList());
                
                final String finalAuthorId = authAuthorId;
                List<AuthBase> currentAuthBaseList = authBaseListForDescendant.stream()
                    .filter(item2 -> StringUtils.equals(item2.getUAuthorId(), finalAuthorId))
                    .collect(Collectors.toList());

                // 기존 권한이 이미 있을 경우 권한이 높은 경우에 대해서만 업그레이드
                if (currentAuthBaseList != null && currentAuthBaseList.size() > 0) {
                  for (AuthBase currentAuthBase : currentAuthBaseList) {
                    int pPermitTypeLevel = GrantedLevels.findByLabel(item.getPermitType());
                    int currentPermitTypeLevel = GrantedLevels.findByLabel(currentAuthBase.getUPermitType());
                    if (pPermitTypeLevel > currentPermitTypeLevel) {
                      String permitType = DocStatus.LIVE.getValue().equals(currentAuthBase.getUDocStatus()) ? item.getPermitType() : GrantedLevels.READ.getLabel();
                      
                      IDfPersistentObject iDfDescentantAuthShare = (IDfPersistentObject)idfSession.getObject(new DfId(currentAuthBase.getRObjectId()));
                      iDfDescentantAuthShare.setString("u_permit_type", permitType);
                      iDfDescentantAuthShare.setString("u_add_gubun", "S");
                      
                      iDfDescentantAuthShare.save();
                    }
                  }
                }
                // 기존 권한이 없을 경우 새로 생성
                else {
                  // Live 권한 부여
                  IDfPersistentObject iDfAuthBaseLive = idfSession.newObject("edms_auth_base");
                  iDfAuthBaseLive.setString("u_obj_id"       , descendantDoc.getUDocKey());
                  iDfAuthBaseLive.setString("u_obj_type"     , AuthObjType.DOCUMENT.getValue());
                  iDfAuthBaseLive.setString("u_doc_status"   , DocStatus.LIVE.getValue());
                  iDfAuthBaseLive.setString("u_permit_type"  , item.getPermitType());
                  iDfAuthBaseLive.setString("u_own_dept_yn"  , ownDeptYn);
                  iDfAuthBaseLive.setString("u_author_id"    , authAuthorId);
                  iDfAuthBaseLive.setString("u_author_type"  , authorType);
                  iDfAuthBaseLive.setString("u_create_user"  , currentUserId);
                  iDfAuthBaseLive.setString("u_create_date"  , (new DfTime()).toString());
                  iDfAuthBaseLive.setString("u_add_gubun"    , "S");
                        
                  iDfAuthBaseLive.save();

                  // Closed 권한 부여
                  IDfPersistentObject iDfAuthBaseClosed = idfSession.newObject("edms_auth_base");
                  iDfAuthBaseClosed.setString("u_obj_id"       , descendantDoc.getUDocKey());
                  iDfAuthBaseClosed.setString("u_obj_type"     , AuthObjType.DOCUMENT.getValue());
                  iDfAuthBaseClosed.setString("u_doc_status"   , DocStatus.CLOSED.getValue());
                  iDfAuthBaseClosed.setString("u_permit_type"  , GrantedLevels.READ.getLabel());
                  iDfAuthBaseClosed.setString("u_own_dept_yn"  , ownDeptYn);
                  iDfAuthBaseClosed.setString("u_author_id"    , authAuthorId);
                  iDfAuthBaseClosed.setString("u_author_type"  , authorType);
                  iDfAuthBaseClosed.setString("u_create_user"  , currentUserId);
                  iDfAuthBaseClosed.setString("u_create_date"  , (new DfTime()).toString());
                  iDfAuthBaseClosed.setString("u_add_gubun"    , "S");
                        
                  iDfAuthBaseClosed.save();
                }
     
                // 문서일 경우 permitType에 맞는 권한 부여
                int permitLevel = GrantedLevels.findByLabel(item.getPermitType());
                
                IDfDocument iDfDoc = (IDfDocument)idfSession.getObject(new DfId(descendantDoc.getRObjectId()));
                iDfDoc.grant(authAuthorId, permitLevel, null);
              
                iDfDoc.save();
              }
            }
          }
        }
                
        /* 공유/협업 적용 끝  */
                
        /* 알림 발송 시작  */
                
        // SOFTM - [자료수정] - [CHECK POINT .18_확인필요] : 알림 - 공유/협업에 권한이 추가될 경우 이번에 추가된 사람에게 관리자 화면에서 지정한 알람/통보 방식 기준으로 알림 발송
        
        // 공유/협업 받은 부서/사용자에게 알림 발송
        if ( doGrantShare ) {
          String message = "'" + folder.getUFolName() + "' 폴더의 공유/협업이 설정되었습니다.";
          
          // Case 1 : Type == 'D' : 참여부서
          List<String> deptCodes = pAuthShareList.stream()
              .filter(predicate -> predicate.getType().equals(AuthorType.TEAM.getValue()))
              .map(item -> item.getTargetId())
              .collect(Collectors.toList());
          List<VUser> userListForDeptCodes = deptCodes.size() > 0 ? gwUserDao.selectUserListByDeptCodes(deptCodes) : Collections.emptyList();
          
          // Case 2 : Type == 'U' : 사용자의 부서를 이용, 참여부서 체크
          List<VUser> userList = pAuthShareList.stream()
              .filter(predicate -> predicate.getType().equals(AuthorType.USER.getValue()))
              .parallel()
              .map(item -> gwUserDao.selectOneByUserId(item.getTargetId()).get())
              .collect(Collectors.toList());
          
          List<VUser> notiUserList = Stream.concat(userListForDeptCodes.stream(), userList.stream()).collect(Collectors.toList());
 
          NotiConfig notiData = notiConfigDao.selectOneByCodes(userSession.getUser().getComOrgId(), NotiItem.SH.getValue());
          if("Y".equals(notiData.getUAlarmYn())) {
            for (VUser user : notiUserList) {
              try {
                IDfPersistentObject iDfNoti = (IDfPersistentObject) idfSession.newObject("edms_noti");
                iDfNoti.setString("u_msg_type", NotiItem.SH.getValue());
                iDfNoti.setString("u_sender_id", userSession.getDUserId());
                iDfNoti.setString("u_receiver_id", user.getUserId());
                iDfNoti.setString("u_performer_id", userSession.getDUserId());
                iDfNoti.setString("u_action_yn", "Y");
                iDfNoti.setString("u_action_need_yn", "N");
                iDfNoti.setString("u_msg", message);
                iDfNoti.setString("u_obj_id", folder.getRObjectId());
                iDfNoti.setString("u_sent_date", new DfTime().toString());
                iDfNoti.setString("u_action_date", new DfTime().toString());
                
                iDfNoti.save();
              } catch (Exception e) {
                String errerUserId = user.getUserId() != null ? user.getUserId() : "";
                logger.error(e.getMessage() + "(errorUserId: " + errerUserId + ")");
              }
            }
          }
          if("Y".equals(notiData.getUEmailYn())) {
            List<String> targetEmailList = notiUserList.stream().map(item -> item.getEmail()).collect(Collectors.toList());
            try {
              notificationService.sendMail(
                  "dbox@dongkuk.com",
                  targetEmailList,
                  "[D'Box] " + message,
                  "D'Box에서 알려드립니다.<br/><br/>" + message + "<br/><br/>감사합니다.");
            } catch (Exception e) {
              String errorUserIdList = targetEmailList != null ? targetEmailList.stream().collect(Collectors.joining(",")) : "";
              logger.error(e.getMessage() + "(errorUserIdList: " + errorUserIdList + ")");
            }
          }
          if("Y".equals(notiData.getUMmsYn())) {
            for (VUser user : notiUserList) {
              try {
                String mobileTel = user.getMobileTel().replace("-", "");
                notificationService.sendKakao(user.getUserId(),
                    mobileTel,
                    "dbox_alarm_008",
                    message);
              } catch (Exception e) {
                String errerUserId = user.getUserId() != null ? user.getUserId() : "";
                logger.error(e.getMessage() + "(errorUserId: " + errerUserId + ")");
              }
            }               
          }
        }
        
        // 공유/협업 해제된 부서/사용자에게 알림 발송
        if ( doRevokeShare ) {
          String message = "'" + folder.getUFolName() + "' 폴더의 공유/협업이 해제되었습니다.";
          
          // Case 1 : Type == 'D' : 참여부서
          List<String> deptCodes = pRevokeShareList.stream()
              .filter(predicate -> predicate.getType().equals(AuthorType.TEAM.getValue()))
              .map(item -> item.getTargetId())
              .collect(Collectors.toList());
          
          List<VUser> userListForDeptCodes = deptCodes.size() > 0 ? gwUserDao.selectUserListByDeptCodes(deptCodes) : Collections.emptyList();
          
          // Case 2 : Type == 'U' : 사용자의 부서를 이용, 참여부서 체크
          List<VUser> userList = pRevokeShareList.stream()
              .filter(predicate -> predicate.getType().equals(AuthorType.USER.getValue()))
              .parallel()
              .map(item -> gwUserDao.selectOneByUserId(item.getTargetId()).get())
              .collect(Collectors.toList());
          
          List<VUser> notiUserList = Stream.concat(userListForDeptCodes.stream(), userList.stream()).collect(Collectors.toList());
 
          NotiConfig notiData = notiConfigDao.selectOneByCodes(userSession.getUser().getComOrgId(), NotiItem.SH.getValue());
          if("Y".equals(notiData.getUAlarmYn())) {
            for (VUser user : notiUserList) {
              try {
                IDfPersistentObject iDfNoti = (IDfPersistentObject) idfSession.newObject("edms_noti");
                iDfNoti.setString("u_msg_type", NotiItem.SH.getValue());
                iDfNoti.setString("u_sender_id", userSession.getDUserId());
                iDfNoti.setString("u_receiver_id", user.getUserId());
                iDfNoti.setString("u_performer_id", userSession.getDUserId());
                iDfNoti.setString("u_action_yn", "Y");
                iDfNoti.setString("u_action_need_yn", "N");
                iDfNoti.setString("u_msg", message);
                iDfNoti.setString("u_obj_id", folder.getRObjectId());
                iDfNoti.setString("u_sent_date", new DfTime().toString());
                iDfNoti.setString("u_action_date", new DfTime().toString());
                
                iDfNoti.save();
              } catch (Exception e) {
                String errerUserId = user.getUserId() != null ? user.getUserId() : "";
                logger.error(e.getMessage() + "(errorUserId: " + errerUserId + ")");
              }
            }
          }
          if("Y".equals(notiData.getUEmailYn())) {
            List<String> targetEmailList = notiUserList.stream().map(item -> item.getEmail()).collect(Collectors.toList());
            try {
              notificationService.sendMail(
                  "dbox@dongkuk.com",
                  targetEmailList,
                  "[D'Box] " + message,
                  "D'Box에서 알려드립니다.<br/><br/>" + message + "<br/><br/>감사합니다.");
            } catch (Exception e) {
              String errorUserIdList = targetEmailList != null ? targetEmailList.stream().collect(Collectors.joining(",")) : "";
              logger.error(e.getMessage() + "(errorUserIdList: " + errorUserIdList + ")");
            }
          }
          if("Y".equals(notiData.getUMmsYn())) {
            for (VUser user : notiUserList) {
              try {
                String mobileTel = user.getMobileTel().replace("-", "");
                notificationService.sendKakao(user.getUserId(),
                    mobileTel,
                    "dbox_alarm_009",
                    message);
              } catch (Exception e) {
                String errerUserId = user.getUserId() != null ? user.getUserId() : "";
                logger.error(e.getMessage() + "(errorUserId: " + errerUserId + ")");
              }
            }               
          }
        }
        
        // 문서 보안등급 변경 시 소유부서의 모든 사용자에게 알림 발송
        if ( isDoc && pDoc.getUSecLevel() != null ) {
          String message = "'" + doc.getTitle() + "' 문서의 등급변경이 승인되었습니다.";
 
          List<VUser> userListForDeptCodes = gwUserDao.selectUserListByDeptCodes(Arrays.asList(doc.getUDeptCode()));
 
          NotiConfig notiData = notiConfigDao.selectOneByCodes(userSession.getUser().getComOrgId(), NotiItem.SC.getValue());
          if("Y".equals(notiData.getUAlarmYn())) {
            for (VUser user : userListForDeptCodes) {
              try {
                IDfPersistentObject iDfNoti = (IDfPersistentObject) idfSession.newObject("edms_noti");
                iDfNoti.setString("u_msg_type", NotiItem.SC.getValue());
                iDfNoti.setString("u_sender_id", userSession.getDUserId());
                iDfNoti.setString("u_receiver_id", user.getUserId());
                iDfNoti.setString("u_performer_id", userSession.getDUserId());
                iDfNoti.setString("u_action_yn", "Y");
                iDfNoti.setString("u_action_need_yn", "N");
                iDfNoti.setString("u_msg", message);
                iDfNoti.setString("u_obj_id", doc.getUDocKey());
                iDfNoti.setString("u_sent_date", new DfTime().toString());
                iDfNoti.setString("u_action_date", new DfTime().toString());
   
                iDfNoti.save();
              } catch (Exception e) {
                String errerUserId = user.getUserId() != null ? user.getUserId() : "";
                logger.error(e.getMessage() + "(errorUserId: " + errerUserId + ")");
              }
            }
          }
          if("Y".equals(notiData.getUEmailYn())) {
            List<String> targetEmailList = userListForDeptCodes.stream().map(item -> item.getEmail()).collect(Collectors.toList());
            try {
              notificationService.sendMail(
                  "dbox@dongkuk.com",
                  targetEmailList,
                  "[D'Box] " + message,
                  "D'Box에서 알려드립니다.<br/><br/>" + message + "<br/><br/>감사합니다.");
            } catch (Exception e) {
              String errorUserIdList = targetEmailList != null ? targetEmailList.stream().collect(Collectors.joining(",")) : "";
              logger.error(e.getMessage() + "(errorUserIdList: " + errorUserIdList + ")");
            }
          }
          if("Y".equals(notiData.getUMmsYn())) {
            for (VUser user : userListForDeptCodes) {
              try {
                String mobileTel = user.getMobileTel().replace("-", "");
                notificationService.sendKakao(user.getUserId(),
                    mobileTel,
                    "dbox_alarm_013",
                    message);
              } catch (Exception e) {
                String errerUserId = user.getUserId() != null ? user.getUserId() : "";
                logger.error(e.getMessage() + "(errorUserId: " + errerUserId + ")");
              }
            }
          }
        }
            
        /* 알림 발송 끝  */
                
        /* 이력 생성 시작  */
                
        // SOFTM - [자료수정] - [CHECK POINT .19_확인필요] : 이력 생성
        //                  중요보관소 하위가 아닐 경우
        //                  문서 수정일 경우 edms_log_doc에 이력 생성
            
        // 중요문서가 아닐 경우
        // 중요문서가 아닌 문서일 경우 로그 생성
        if ( isDoc ) {
          boolean isDocPreserveFlagChanged = pDoc.getUPreserverFlag() != null;
          boolean isDocSecLevelChanged = pDoc.getUSecLevel() != null;
          if (isDocPreserveFlagChanged || isDocSecLevelChanged || doGrantLiveAuth || doGrantClosedAuth || doGrantShare ) {
            // 최신 버전값
            List<String> rVersionLabelList = doc.getDocRepeatings().stream()
                .filter(item2 -> null != item2.getRVersionLabel()).map(item2 -> item2.getRVersionLabel())
                .collect(Collectors.toList());
            String version = rVersionLabelList.get(rVersionLabelList.size() - 1);
            
            // 보존연한 변경 시 로그
            if (isDocPreserveFlagChanged) {
              // 보존연한이 커지는 경우 U, 작아지는 경우 D
              String jobGubun = pDoc.getUPreserverFlag() > doc.getUPreserverFlag() ? "U" : "D";
              
              insertLog(LogDoc.builder()
                  .uJobCode(DocLogItem.RC.getValue())
                  .uDocId(doc.getRObjectId())
                  .uDocKey(doc.getUDocKey())
                  .uDocName(doc.getTitle())
                  .uDocVersion(version)
                  .uOwnDeptcode(doc.getUDeptCode())
                  .uActDeptCode(currentOrgId)
                  .uJobUser(currentUserId)
                  .uJobUserType("P")
                  .uDocStatus(doc.getUDocStatus())
                  .uSecLevel(doc.getUSecLevel())
                  .uCabinetCode(doc.getUCabinetCode())
                  .uJobGubun(jobGubun)
                  .uBeforeChangeVal(String.valueOf(doc.getUPreserverFlag()))
                  .uAfterChangeVal(String.valueOf(pDoc.getUPreserverFlag()))
                  .uUserIp(ip)
                  .build());
            }
            
            // 보안등급 변경 시 로그
            if (isDocSecLevelChanged) {
              // 보안등급이 커지는 경우 U, 작아지는 경우 D
              int docOrder = SecLevelCode.findByValue(doc.getUSecLevel()).getOrder();
              int pDocOrder = SecLevelCode.findByValue(pDoc.getUSecLevel()).getOrder();
              
              String jobGubun = docOrder > pDocOrder  ? "U" : "D";
              
              insertLog(LogDoc.builder()
                  .uJobCode(DocLogItem.SC.getValue())
                  .uDocId(doc.getRObjectId())
                  .uDocKey(doc.getUDocKey())
                  .uDocName(doc.getTitle())
                  .uDocVersion(version)
                  .uOwnDeptcode(doc.getUDeptCode())
                  .uActDeptCode(currentOrgId)
                  .uJobUser(currentUserId)
                  .uJobUserType("P")
                  .uDocStatus(doc.getUDocStatus())
                  .uSecLevel(doc.getUSecLevel())
                  .uCabinetCode(doc.getUCabinetCode())
                  .uJobGubun(jobGubun)
                  .uBeforeChangeVal(String.valueOf(doc.getUSecLevel()))
                  .uAfterChangeVal(String.valueOf(pDoc.getUSecLevel()))
                  .uUserIp(ip)
                  .build());
            }
                  
            // 권한 부여
            if (doGrantLiveAuth || doGrantClosedAuth) {
              insertLog(LogDoc.builder()
                  .uJobCode(DocLogItem.PA.getValue())
                  .uDocId(doc.getRObjectId())
                  .uDocKey(doc.getUDocKey())
                  .uDocName(doc.getTitle())
                  .uDocVersion(version)
                  .uOwnDeptcode(doc.getUDeptCode())
                  .uActDeptCode(currentOrgId)
                  .uJobUser(currentUserId)
                  .uJobUserType("P")
                  .uDocStatus(doc.getUDocStatus())
                  .uSecLevel(doc.getUSecLevel())
                  .uCabinetCode(doc.getUCabinetCode())
                  .uUserIp(ip)
                  .build());
            }
          }
        }
        
        if (isFolder) {
          // 공유/협업 추가
          if (doGrantShare) {
            for (Doc shareDoc : shareDocList) {
              // 최신 버전값
              List<String> shareRVersionLabelList = shareDoc.getDocRepeatings().stream()
                  .filter(item2 -> null != item2.getRVersionLabel()).map(item2 -> item2.getRVersionLabel())
                  .collect(Collectors.toList());
              String shareVersion = shareRVersionLabelList.get(shareRVersionLabelList.size() - 1);
              
              insertLog(LogDoc.builder()
                  .uJobCode(DocLogItem.SH.getValue())
                  .uDocId(shareDoc.getRObjectId())
                  .uDocKey(shareDoc.getUDocKey())
                  .uDocName(shareDoc.getTitle())
                  .uDocVersion(shareVersion)
                  .uOwnDeptcode(shareDoc.getUDeptCode())
                  .uActDeptCode(currentOrgId)
                  .uJobUser(currentUserId)
                  .uJobUserType("P")
                  .uDocStatus(shareDoc.getUDocStatus())
                  .uSecLevel(shareDoc.getUSecLevel())
                  .uCabinetCode(shareDoc.getUCabinetCode())
                  .uUserIp(ip)
                  .build());
            }
          }
        }
                
        /* 이력 생성 끝  */
 
        // 트랜잭션 종료
        idfSession.commitTrans();
      } finally {
        if (idfSession != null) {
          if (idfSession.isTransactionActive()) {
            idfSession.abortTrans();
          }
          if (idfSession.isConnected()) {
              idfSession.disconnect();
          }
        }
      }
 
      // 성공 시 결과값 리턴 (개발자 확인용)
      Map<String, String> okMap = new HashMap<>();
      okMap.put("dataId"   , String.valueOf(dataId));
      okMap.put("isDoc"    , String.valueOf(isDoc));
      okMap.put("isFolder" , String.valueOf(isFolder));
      if ( isDoc ) {
          okMap.put("doc.getU_pr_type" , String.valueOf(doc.getUPrType()));
          okMap.put("doc.getU_pr_code" , String.valueOf(doc.getUPrCode()));
      }
      if ( isFolder ) {
          okMap.put("folder.getU_pr_type" , String.valueOf(folder.getUPrType()));
          okMap.put("folder.getU_pr_code" , String.valueOf(folder.getUPrCode()));
      }
      
      return OK(okMap);
    }
 
    @Override
    public boolean checkDocsDuple(DocFilterDto docFilterDto) {
        return docService.checkDocsDuple(docFilterDto);
    }
    @Override
    public String uploadDoc(UserSession userSession, UploadDocDto dto, AttachedKUploadFile aFile, String userIp, String userType) throws Exception {
        String apiKey = null;
        FolderAuthDto folderAuthDto = null;
        IDfSession idfSession = null;
        IDfDocument idfNewDoc = null;
        IDfPersistentObject obj = null;
        String loginId = null;
        String upFolId = null;
        String newObjectId = null;
        String hamId = null;
        boolean gwonhan = false;
        HamInfoResult hamInfo = null;
        try {
        
          Map<String, String> formatChkMap = codeService.getDeniedFormatCodeMap();
          if (formatChkMap.containsKey(aFile.getFileExtention().toUpperCase())) {
              throw new UploadExtensionException("허용된 확장자가 아닙니다.");
          }
          
          idfSession = this.getIdfSession(userSession);
          aFile.setFolderPath(kuploadBasePath);
          
          if (!dto.checkRoot()) {
              Folder fldr = folderService.selectOne(dto.getUpObjectId()).orElseThrow(() -> new NotFoundException(Folder.class, dto.getUpObjectId()));
              if (FolderStatus.LOCK.getValue().equals(fldr.getUFolStatus())) {
                throw new UploadLockException("폴더가 잠금 상태입니다.");
              } else {
                Integer hasLock = folderService.selectAncestorHasLock(dto.getUpObjectId());
                boolean ancestorHasLock = hasLock == null || hasLock == 0 ? false:true; 
                if (ancestorHasLock) {
                  throw new UploadLockException("상위 폴더가 잠금 상태입니다.");
                }
              }
              dto.setSSecLevel(fldr.getUSecLevel());
              hamId = DCTMConstants.DCTM_BLANK.equals(fldr.getUPrCode()) ? fldr.getUDeptCode() : fldr.getUPrCode();
          } else {
              hamId = dto.getUpObjectId();
          }
          
          //폴더가 있을경우 하위폴더있는지 확인
//          hamInfo = authService.selectDeptHamInfo(hamId).orElse(
//              authService.selectHamInfo(hamId).orElseThrow(() -> new NotFoundException(CheckAuthParam.class, dto.toString())));
          hamInfo = authService.selectHamInfo(hamId).orElseThrow(() -> new NotFoundException(CheckAuthParam.class, dto.toString()));
          dto.setHamInfo(hamInfo);
          if (!dto.checkRoot()) {
              upFolId = dto.getUpObjectId();
          } else {
              upFolId = DCTMConstants.DCTM_BLANK;
              if (ProjectType.PROJECT.getValue().equals(hamInfo.getHamType())) {
                final String pjtCode = hamInfo.getMyCode();
                Project foundPjt = projectService.selectProjectByUPjtCode(hamInfo.getMyCode())
                    .orElseThrow(() -> new NotFoundException(Project.class, pjtCode));
                dto.setSSecLevel(foundPjt.getUSecLevel());
              } else if (ProjectType.RESEARCH.getValue().equals(hamInfo.getHamType())) {
                final String rschCode = hamInfo.getMyCode();
                Research foundRsch = researchService.selectResearchByURschCode(hamInfo.getMyCode())
                    .orElseThrow(() -> new NotFoundException(Research.class, rschCode));
                dto.setSSecLevel(foundRsch.getUSecLevel());
              }
          }
  
          //권한 체크
          if (DCTMConstants.DCTM_BLANK.equals(upFolId)) {
              gwonhan = authService.isRootAuthenticated(HamType.findByValue(hamInfo.getHamType()), hamInfo.getHamCodeForAuth(), userSession.getUser().getUserId());
          } else {
              gwonhan = authService.checkFolderAuth(upFolId, userSession.getUser().getUserId(), GrantedLevels.DELETE.getLabel());
          }
          if (!gwonhan) {
              throw new UploadAuthException("사용자 권한 없음");
          }

          //for test

          //업로드 타입 선택 -> V:자동버전업, C:복사본, S:건너뛰기
          UserPresetDetail userPreset = null;
          List<UserPresetDetail> userPresetList = userPresetDao.selectDetailedOneByFilter(UserPresetFilterDto.builder()
              .uUserId(userSession.getUser().getUserId())
              .uSecBaseFlag(true).build());    
          
          AuthBase presetAuthBase = null;
          //폴더 생성
          boolean folderExist = false;
          boolean isIdxZero = false;
          String newFolderName = null;
          if (!Objects.isNull(aFile.getPathList())) {
              for (int idx=0; idx<aFile.getPathList().size()-1; idx++) {
              isIdxZero = !Objects.isNull(dto.getPrType())? idx <= 1 : dto.checkRoot() && idx == 0;
              folderExist = false;
              newFolderName = aFile.getPathList().get(idx);
              
              //prType확인
              if (!Objects.isNull(dto.getPrType()) && idx == 0) {
                  if (ProjectType.PROJECT.getValue().equals(dto.getPrType())) {
                    //project 생성
                    String pjtCode = null;
                    List<Project> pjList = projectService.selectProjectList(ProjectFilterDto.builder()
                        .rDeptCode(hamInfo.getCabinetOrgId().toUpperCase())
                        .uPjtName(newFolderName)
                        .uFinishYn("N").build(), 
                        hamInfo.getCabinetOrgId().toUpperCase(), null);
                    if (pjList.size() > 0) {
                        pjtCode = pjList.get(0).getUPjtCode();
                        dto.setSSecLevel(pjList.get(0).getUSecLevel());
                    } else {
                        idfSession.beginTrans();
                        pjtCode = projectService.createProject(userSession, ProjectCreateDto.builder()
                            .uSecLevel(SecLevelCode.TEAM.getValue())
                            .uOwnDept(dto.getHamInfo().getCabinetOrgId())
                            .uPjtName(newFolderName).build(), idfSession);
                        idfSession.commitTrans();
                    }
                    hamInfo.setHamType(HamType.PROJECT.getValue());
                    hamInfo.setMyCode(pjtCode);
                    dto.setHamType(HamType.PROJECT.getValue());
                  } else {
                    //research 생성
                    String rschCode = null;
                    List<Research> rsList = researchService.selectResearchList(ResearchFilterDto.builder()
                        .rDeptCode(hamInfo.getCabinetOrgId().toUpperCase())
                        .uRschName(newFolderName)
                        .uFinishYn("N").build(), 
                        hamInfo.getCabinetOrgId().toUpperCase(), null);
                    if (rsList.size() > 0) {
                        rschCode = rsList.get(0).getURschCode();
                        dto.setSSecLevel(rsList.get(0).getUSecLevel());
                    } else {
                        idfSession.beginTrans();
                        rschCode = researchService.createResearch(userSession, ResearchCreateDto.builder()
                            .uSecLevel(SecLevelCode.TEAM.getValue())
                            .uRschName(newFolderName).build(), idfSession);
                        idfSession.commitTrans();
                    }
                    hamInfo.setHamType(HamType.RESEARCH.getValue());
                    hamInfo.setMyCode(rschCode);
                    dto.setHamType(HamType.RESEARCH.getValue());
                  }
                  continue;
              }
              
              FolderFilterDto folFilter = FolderFilterDto.builder()
                  .uCabinetCode(hamInfo.getUCabinetCode())
                  .uUpFolId(upFolId)
                  .uFolName(newFolderName)
                  .uDeleteStatus(DCTMConstants.DCTM_BLANK) // 삭제되지 않은것
                  .uPrCode(HamType.RESEARCH.getValue().equals(dto.getHamType()) 
                      || HamType.PROJECT.getValue().equals(dto.getHamType()) ? dto.getHamInfo().getMyCode() : null)
                  .build();
              List<Folder> foundFolder = folderService.selectFolderChildren(folFilter);
              while (foundFolder != null && foundFolder.size() != 0) {
                  if (!authService.checkFolderAuth(foundFolder.get(0).getRObjectId(), userSession.getUser().getUserId(), GrantedLevels.DELETE.getLabel())) {
                    //권한 없음
                    //이름에 (1) 붙인 뒤 확인
                    newFolderName = DboxStringUtils.addFolderNameNumber(newFolderName);
                    folFilter.setUFolName(newFolderName);
                    foundFolder = folderService.selectFolderChildren(folFilter);
                  } else {
                    //권한 있음
                    if (foundFolder.get(0).isLockFolder()) {
//                        throw new UploadLockException("잠금된 폴더입니다.");
                      newFolderName = DboxStringUtils.addFolderNameNumber(newFolderName);
                      folFilter.setUFolName(newFolderName);
                      foundFolder = folderService.selectFolderChildren(folFilter);
                      continue;
                    }
                    
                    upFolId = foundFolder.get(0).getRObjectId();
                    dto.setSSecLevel(foundFolder.get(0).getUSecLevel());
                    folderExist = true;
                    break;
                  }
              }
              if (!folderExist) {
                  RegistDataDto registDataDto = RegistDataDto.builder()
                      .dataName(newFolderName)
                      .dateType(DataObjectType.FOLDER.getValue())
                      .hamType(dto.getHamType())
                      .hamInfo(hamInfo)
                      .folderType(dto.makeFolderType())
                      .isPrFirst(!Objects.isNull(dto.getPrType()) && idx <= 1 ? true : false)
                      .build(); 
                  if (isIdxZero) { 
                    registDataDto.setHamId(dto.getHamInfo().getMyCode()); 
                  } else { 
                    registDataDto.setHamType("F"); //folder
                    registDataDto.setUpObjectId(upFolId);
                  }
                  registDataDto.setSecLevel(dto.getSSecLevel());
                  idfSession.beginTrans();
                  upFolId = registData(userSession, registDataDto, idfSession, hamInfo, userIp, userType, userPresetList);
                  dto.setSSecLevel(registDataDto.getSecLevel());
                  idfSession.commitTrans();
              }
            }
          }
          
          //파일 생성 start
          //closed 포맷 확인
          formatChkMap = codeService.getClosedFormatCodeMap();
          if (formatChkMap.containsKey(aFile.getFileExtention().toUpperCase())) {
              dto.setDocStatus(DocStatus.CLOSED.getValue());
          }
          
          //drm 복호화  -> 일부 확장자. 
        /*  formatChkMap = codeService.getDrmFormatCodeMap();
          if (formatChkMap.containsKey(aFile.getFileExtention().toUpperCase())) {
              
              if (drmService.check(new FileInputStream(uplaodFile), aFile.getGuidExtension(), uplaodFile.length())) {
                File decryptedFile = drmService.decrypt(new FileInputStream(uplaodFile), aFile.getGuidExtension(), uplaodFile.length());
                aFile.setNewFileLocation(decryptedFile.getPath());
              }
          }*/
        //drm 복호화 전체 복호화
          File uplaodFile = new File(aFile.getNewFileLocation());
          if (drmService.check(new FileInputStream(uplaodFile), aFile.getGuidExtension(), uplaodFile.length())) {
            File decryptedFile = drmService.decrypt(new FileInputStream(uplaodFile), aFile.getGuidExtension(), uplaodFile.length());
            aFile.setNewFileLocation(decryptedFile.getPath());
          }
          
          //권한 설정
          DocFilterDto docDto = null;
          boolean isRoot = dto.checkRoot() && DCTMConstants.DCTM_BLANK.equals(upFolId);
          dto.setUpObjectId(upFolId);
          
          //preset 설정
          if (userPresetList == null) {
            userPresetList = userPresetDao.selectDetailedOneByFilter(UserPresetFilterDto.builder()
                .uUserId(userSession.getUser().getUserId())
                .uSecBaseFlag(true).build());    
          }
          if (isRoot) {
            userPreset = userPresetList.stream().filter(h -> "1".equals(h.getUSecBaseFlag()) && SecLevelCode.TEAM.getValue().equals(h.getUSecLevel()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Preset Not found"));
          } else {
            final String compareSecLevel = dto.getSSecLevel();
            userPreset = userPresetList.stream().filter(h -> "1".equals(h.getUSecBaseFlag()) && compareSecLevel.equals(h.getUSecLevel()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Preset Not found"));
          }
          if (userPreset.getUSecLevel() != null) {
            dto.setSSecLevel(SecLevelCode.compareSecs(dto.getSSecLevel(), userPreset.getUSecLevel()).getValue());
          }
          
          dto.setUploadFlag(userPreset.getUPcRegFlag());
          dto.getDoc().setUPreserverFlag((userPreset.getUPreserveFlag()));
          dto.getDoc().setURegSource("P");
          if (userPreset.getUMailPermitFlag() != null) {
            dto.getDoc().setUAutoAuthMailFlag("Y".equals(userPreset.getUMailPermitFlag())?true:false);
          }
     
          //파일명 설정 
          aFile.setFileNames(DboxStringUtils.extractFileName(aFile.getOriginalFileName(), aFile.getFileExtention()));
          
          //파일 확인
          if (isRoot) {
            docDto = DocFilterDto.builder()
              .uCabinetCode(hamInfo.getUCabinetCode())
              .uFolId(upFolId)
              .uDeleteStatus(DCTMConstants.DCTM_BLANK) // 삭제되지 않은것
              .hamId(dto.hamValidation()?hamInfo.getMyCode():null)
              .objectName(aFile.getFileNameOnly())
              .uFileExt(aFile.getFileExtention())
              .build();
          } else {
            docDto = DocFilterDto.builder()
              .uCabinetCode(hamInfo.getUCabinetCode())
              .uFolId(upFolId)
              .uDeleteStatus(DCTMConstants.DCTM_BLANK) // 삭제되지 않은것
              .objectName(aFile.getFileNameOnly())
              .uFileExt(aFile.getFileExtention())
              .build();
          } 
     
          UploadFlag createType = UploadFlag.NEW_D;
          List<Doc> foundDoc = docService.selectList(docDto);
          dto.getDoc().setURegUser(userSession.getUser().getUserId());
          //acl 저장
          String docAcl = authService.selectDocAcl(AuthType.findAuthTypeForDocReg(hamInfo), SecLevelCode.findByValue(dto.getSSecLevel()), hamInfo.getUCabinetCode());
          dto.setDocAcl(docAcl);
          
          //문서등록
          idfSession.beginTrans();
          if (Objects.isNull(foundDoc) || foundDoc.size() == 0) {
              idfNewDoc = docService.createDoc(idfSession, dto, aFile);
//              idfNewDoc.save();
              if (isRoot) {
                folderAuthDto = authService.selectRootDocAuths(userSession, AuthType.findAuthTypeForDocReg(hamInfo), SecLevelCode.findByValue(dto.getSSecLevel()), idfNewDoc.getObjectId().getId(), hamInfo, userPreset);
              } else {
                  if (Objects.isNull(folderAuthDto)) {
                    folderAuthDto = new FolderAuthDto();
                  }
                  folderAuthDto = authService.selectDocAuths(userSession, hamInfo.getUCabinetCode(), upFolId, idfNewDoc.getObjectId().getId(), userPreset);
              }
              authService.saveDocAuths(false, idfNewDoc, dto.getDocStatus(), folderAuthDto, hamInfo, idfSession);
              idfNewDoc.save();
          } else { 
            // 문서 closed 체크 
            // closed면 넘버링 -> 부서장, 부서문서관리자는 권한 7 갖고있어서 전부 권한이있는 문제발생
            boolean isNumbering = false;
            do {
              gwonhan = authService.checkDocAuth(foundDoc.get(0).getRObjectId(), userSession.getUser().getUserId(), GrantedLevels.DELETE.getLevel());
              if (!gwonhan || DocStatus.CLOSED.getValue().equals(foundDoc.get(0).getUDocStatus())) {
                String tmpCopyName = DboxStringUtils.addFileNameNumber(aFile.getFileNameOnly(), aFile.getFileExtention());
                aFile.setFileNames(tmpCopyName);
                docDto.setObjectName(aFile.getFileNameOnly());
                foundDoc = docService.selectList(docDto);
                isNumbering = true;
              } else {
                isNumbering = false;
              }
            } while (isNumbering && foundDoc.size() > 0);
            
            if (isNumbering) {
              idfNewDoc = docService.createDoc(idfSession, dto, aFile);
              if (isRoot) {
                folderAuthDto = authService.selectRootDocAuths(userSession, AuthType.findAuthTypeForDocReg(hamInfo), SecLevelCode.findByValue(dto.getSSecLevel()), idfNewDoc.getObjectId().getId(), hamInfo, userPreset);
              } else {
                  if (Objects.isNull(folderAuthDto)) {
                    folderAuthDto = new FolderAuthDto();
                  }
                  folderAuthDto = authService.selectDocAuths(userSession, hamInfo.getUCabinetCode(), upFolId, idfNewDoc.getObjectId().getId(), userPreset);
              }
              authService.saveDocAuths(false, idfNewDoc, dto.getDocStatus(), folderAuthDto, hamInfo, idfSession);
              idfNewDoc.save();
            } else {
              //권한 있음 -> 건너띄기, 버전갱신, ‘[복사본]’ 추가, 덮어쓰기
              if (UploadFlag.SKIP.getValue().equals(dto.getUploadFlag())) {
                  createType = UploadFlag.SKIP;
                  //none
              } else if (UploadFlag.VERSION.getValue().equals(dto.getUploadFlag())) {
                  createType = UploadFlag.VERSION;
                  idfNewDoc = (IDfDocument) idfSession.getObject(new DfId(foundDoc.get(0).getRObjectId()));
                  idfNewDoc.setString("u_last_editor", dto.getDoc().getURegUser()); // 등록자
                  //editors repeating
                  idfNewDoc.appendString("u_editor", dto.getDoc().getURegUser());
                  idfNewDoc.setString("u_update_date", (new DfTime()).toString()); // 등록자
                  docService.versionUp(idfNewDoc, aFile.getNewFileLocation(), aFile.getFileNameOnly());
                  idfNewDoc.save();
                  //TODO: editor repeating추가
              } else if (UploadFlag.COPY.getValue().equals(dto.getUploadFlag())) {
                  createType = UploadFlag.COPY;
                  //파일명 조회
                  int founcDocSize = foundDoc.size();
                  String tmpCopyName = aFile.getFileNameOnly();
                  while(founcDocSize != 0) {
                    tmpCopyName = DboxStringUtils.addFileNameNumber(aFile.getFileNameOnly(), aFile.getFileExtention());
                    aFile.setFileNames(tmpCopyName);
                    docDto.setObjectName(aFile.getFileNameOnly());
                    foundDoc = docService.selectList(docDto);
                    founcDocSize = foundDoc.size();
                  }
                  idfNewDoc = docService.createDoc(idfSession, dto, aFile);
                  if (isRoot) {
                    folderAuthDto = authService.selectRootDocAuths(userSession, AuthType.findAuthTypeForDocReg(hamInfo), SecLevelCode.findByValue(dto.getSSecLevel()), idfNewDoc.getObjectId().getId(), hamInfo, userPreset);
                  } else {
                    if (Objects.isNull(folderAuthDto)) {
                      folderAuthDto = new FolderAuthDto();
                    }
                    folderAuthDto = authService.selectDocAuths(userSession, hamInfo.getUCabinetCode(), upFolId, idfNewDoc.getObjectId().getId(), userPreset);
                  }
                  authService.saveDocAuths(false, idfNewDoc, dto.getDocStatus(), folderAuthDto, hamInfo, idfSession);
                  idfNewDoc.save();
              } else if (UploadFlag.OVERWIRTE.getValue().equals(dto.getUploadFlag())) {
                  //TODO: editor repeating추가
                  idfNewDoc = docService.overWrite(idfSession, foundDoc.get(0).getRObjectId(), aFile);
                  idfNewDoc.setString("u_last_editor", dto.getDoc().getURegUser()); // 등록자
                  //editors repeating
                  idfNewDoc.appendString("u_editor", dto.getDoc().getURegUser());
                  idfNewDoc.setString("u_update_date", (new DfTime()).toString()); // 등록자
                  idfNewDoc.save();
              }
            }
          }
          //TODO : log
          if (idfNewDoc != null) {
              LogDoc logDoc = LogDoc.builder()
                  .uJobCode(DocLogItem.RE.getValue())
                  .uDocId(idfNewDoc.getObjectId().getId())
                  .uDocKey(idfNewDoc.getObjectId().getId())
                  .uDocName(idfNewDoc.getTitle())
//                  .uDocVersion("1") //query에서 insert
                  .uFileSize(idfNewDoc.getContentSize())
                  .uOwnDeptcode(hamInfo.getCabinetOrgId().toUpperCase())
                  .uActDeptCode(userSession.getUser().getOrgId())
                  .uJobUser(userSession.getUser().getUserId())
                  .uJobUserType(userType == null ? "P" : userType) //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
                  .uDocStatus(dto.getDocStatus())
                  .uSecLevel(dto.getSSecLevel())
                  .uCabinetCode(hamInfo.getUCabinetCode())
                  .uJobGubun("P")
                  .uUserIp(userIp)
                  .build();
              switch (createType) {
              case NEW_D:
                break;
              case NEW_P:
                break;
              case OVERWIRTE:
                logDoc.setUJobCode(DocLogItem.ED.getValue());
                logDoc.setUJobGubun(UploadFlag.OVERWIRTE.getValue());
                logDoc.setUDocKey(foundDoc.get(0).getUDocKey());
                break;
              case COPY:
                break;
              case VERSION: 
                logDoc.setUJobCode(DocLogItem.ED.getValue());
                logDoc.setUJobGubun(UploadFlag.VERSION.getValue());
                logDoc.setUDocVersion(String.valueOf((int) Double.parseDouble(idfNewDoc.getVersionLabels().getImplicitVersionLabel())));
                logDoc.setUDocKey(foundDoc.get(0).getUDocKey());
                break;
              default:
                break;
              }
              insertLog(logDoc);
          }
          idfSession.commitTrans();
        } catch (Exception e) {
          e.printStackTrace();
          throw e;
        } finally {
          if (idfSession != null) {
              if (idfSession.isTransactionActive()) {
                idfSession.abortTrans();
              }
              if (idfSession.isConnected()) {
                sessionRelease(userSession.getUser().getUserId(), idfSession);
              }
          }
          aFile.deleteFile();
        }
        return idfNewDoc!=null?idfNewDoc.getObjectId().getId():null;
    }
 
    @Override
    public Map<String, Object> isDataLock(UserSession userSession, String dataId, String dataType, boolean hasWAuth, String sOpenContent) throws NotFoundException, Exception {
      Map<String, Object> rst = new HashMap<>();
      boolean wAuth = false, lock = false;
      if (hasWAuth) {
        wAuth = authService.checkDocAuth(dataId, userSession.getUser().getUserId(), GrantedLevels.DELETE.getLevel());
          rst.put("W_AUTH", wAuth);
        }
        if (DboxObjectType.DOCUMENT.getValue().equals(dataType)) {
          try {
            lock = docService.isLocked(dataId)?true:false;
            rst.put("LOCK", lock);
            if (!Objects.isNull(sOpenContent)) {
              if (hasWAuth) {
                if (!wAuth || lock) {
                  rst.put("DTO", docService.getViewCheck(userSession, dataId, null, sOpenContent, null, null));
                } else {
                  rst.put("DTO", docService.getCheckoutCheck(userSession, dataId, null, sOpenContent));
                }
              }
            }
            return rst;
          } catch (Exception e) {
            e.printStackTrace();
            throw new NotFoundException(Doc.class, dataId);
          }
        } else if (DboxObjectType.FOLDER.getValue().equals(dataType)) {
          rst.put("LOCK", FolderStatus.LOCK.getValue().equals(folderService.selectOne(dataId).orElseThrow(() -> new NotFoundException(Folder.class, dataId)).getUFolStatus()));
          return rst;
        } else {
          throw new BadRequestException("데이터 타입 오류");
        }
    }
    
  //자료(폴더 잠금) 
    @Override
    public String getLockFolderData(UserSession userSession, String dataId) throws Exception {

        IDfSession idfSession = DCTMUtils.getAdminSession();
        final String userId = userSession.getUser().getUserId();    // 현재 사용자 ID
        final String orgId = userSession.getUser().getOrgId();        // 현재 사용자 부서코드
        String rObjectId = dataId;
        IDfPersistentObject idf_PObj = null;
        String result = "";
        
        try {
        // 폴더인지 문서인지 확인
        Folder optFolder = folderService.selectOne(dataId)
                .orElseThrow(() -> new NotFoundException(Folder.class, dataId));
        
        
        // [폴더잠금] : 부서함 소유부서 소속이 아닐 경우 : 로직 확인필요
        if (optFolder.getUPrCode() == null && !optFolder.getUDeptCode().equals(orgId)) {
            throw new ForbiddenException("소유부서 소속이 아닐 경우 권한없음");
        } else if (!DCTMConstants.DCTM_BLANK.equals(optFolder.getUPrCode())) { // [폴더잠금] : 연구, 프로젝트 인 경우 로직
                List<String> deptCodesForFolder = new ArrayList<String>();
                deptCodesForFolder.add(optFolder.getUDeptCode());
                int ownDeptCountForFolder = docDao.selectOwnDeptCount(optFolder.getUPrType(), optFolder.getUPrCode(), deptCodesForFolder);
                    if (ownDeptCountForFolder == 0) {
                        throw new BadRequestException("주관부서 소속이 아님 권한없음");
                    }
                    int joinDeptCount = docDao.selectJoinDeptCount(optFolder.getUPrType(), optFolder.getUPrCode(),
                            deptCodesForFolder);
                    if (joinDeptCount == 0) {
                        throw new BadRequestException("참여부서 소속이 아님 권한없음");
                }
        }
        
        List<FolderDescendantDto> folderDescendantDtoList = folderService.selectListDescendants(rObjectId, userId);
        List<String> folIdList = folderDescendantDtoList.stream().map((item) -> item.getRObjectId())
                .collect(Collectors.toList());
            for(int idx = 0; idx < folIdList.size(); idx++) {
                // 권한 관련 체크 하는 부분
                boolean gwonhan = false;
                // delete 권한 확인.        
                gwonhan = authService.checkFolderAuth(folIdList.get(idx), userSession.getUser().getUserId(), GrantedLevels.DELETE.getLabel());
                if(gwonhan == false) {
                    throw new ForbiddenException("대상 자료에 대한 DELETE 권한이 없을 경우 권한없음");
                }
    
            }
        if (FolderStatus.LOCK.getValue().equals(optFolder.getUFolStatus())) {
        } else {
            List<String> doc_chk = new ArrayList<String>();
            for (int idx = 0; idx < folIdList.size(); idx++) {
                doc_chk = docService.selectDocChk(folIdList.get(idx));
            }
            if (doc_chk.isEmpty()) {
                for (String objId : folIdList) {
                    try {
                        idf_PObj = (IDfPersistentObject)idfSession.getObject(new DfId(objId));
                        idf_PObj.setString("u_fol_status", "C");
                        idf_PObj.save();
                        result = "success";
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }else {
                throw new BadRequestException("문서 '" + doc_chk.get(0) + "'외(" + doc_chk.size() + ")건이 작업중이므로 잠금처리 할 수 없습니다.");
            }
          }
        }catch (Exception e) {
          throw e;
        }finally {
          if (idfSession != null && idfSession.isConnected()) {
            idfSession.disconnect();
          }
          // Session Close
          sessionRelease(userSession.getUser().getUserId(), idfSession);
        }
        return result;
    }
 
    //자료(폴더 잠금 해제)
    @Override
    public String patchUnLockFolder(String dataId, UserSession userSession) throws Exception {

        IDfSession idfSession = DCTMUtils.getAdminSession();
        final String userId = userSession.getUser().getUserId();
        final String orgId = userSession.getUser().getOrgId();        // 현재 사용자 부서코드
        String rObjectId = dataId;
        IDfPersistentObject idf_PObj = null;
        String result = "";
        
        
        try {
        // 폴더인지 문서인지 확인
        Folder optFolder = folderService.selectOne(dataId)
                .orElseThrow(() -> new NotFoundException(Folder.class, dataId));
        
        // [폴더잠금] : 부서함 소유부서 소속이 아닐 경우 : 로직 확인필요
        if (optFolder.getUPrCode() == null && !optFolder.getUDeptCode().equals(orgId)) {
            throw new ForbiddenException("소유부서 소속이 아닐 경우 권한없음");
        } else if (!DCTMConstants.DCTM_BLANK.equals(optFolder.getUPrCode())) { // [폴더잠금] : 연구, 프로젝트 인 경우 로직
                List<String> deptCodesForFolder = new ArrayList<String>();
                deptCodesForFolder.add(optFolder.getUDeptCode());
                int ownDeptCountForFolder = docDao.selectOwnDeptCount(optFolder.getUPrType(), optFolder.getUPrCode(), deptCodesForFolder);
                    if (ownDeptCountForFolder == 0) {
                        throw new BadRequestException("주관부서 소속이 아님 권한없음");
                    }
                    int joinDeptCount = docDao.selectJoinDeptCount(optFolder.getUPrType(), optFolder.getUPrCode(),
                            deptCodesForFolder);
                    if (joinDeptCount == 0) {
                        throw new BadRequestException("참여부서 소속이 아님 권한없음");
                }
        }
        
        List<FolderDescendantDto> folderDescendantDtoList = folderService.selectListDescendants(rObjectId, userId);
        List<String> folIdList = folderDescendantDtoList.stream().map((item) -> item.getRObjectId())
                .collect(Collectors.toList());
            for(int idx = 0; idx < folIdList.size(); idx++) {
                // 권한 관련 체크 하는 부분
                boolean gwonhan = false;
                // delete 권한 확인.        
                gwonhan = authService.checkFolderAuth(folIdList.get(idx), userSession.getUser().getUserId(), GrantedLevels.DELETE.getLabel());
                if(gwonhan == false) {
                    throw new ForbiddenException("대상 자료에 대한 DELETE 권한이 없을 경우 권한없음");
                }
    
            }
        if (FolderStatus.ORDINARY.getValue().equals(optFolder.getUFolStatus())) {
        } else {
 
                for (String objId : folIdList) {
                    try {
                        idf_PObj = (IDfPersistentObject)idfSession.getObject(new DfId(objId));
                        idf_PObj.setString("u_fol_status", "O");
                        idf_PObj.save();
                        result = "success";
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
         
            }
        }catch (Exception e) {
          throw e;
        }finally {
          if (idfSession != null && idfSession.isConnected()) {
            idfSession.disconnect();
          }
          // Session Close
          sessionRelease(userSession.getUser().getUserId(), idfSession);
        }
        return result;
    }
 
 // 문서 Close
    @Override
    public String patchDocClosed(String dataId, UserSession userSession ,String ip)
            throws Exception {
        
        final String userId = userSession.getUser().getUserId();
        String result = null;
 
        //IDfSession idfSession = this.getIdfSession(userSession);
        IDfSession idfSession = DCTMUtils.getAdminSession();
        IDfDocument idfDoc = (IDfDocument)idfSession.getObject(new DfId(dataId));

        
        Optional<Doc> optDoc = docService.selectOne(dataId, true);
        boolean gwonhan = false;
        gwonhan = authService.checkDocAuth(dataId, userSession.getUser().getUserId(), GrantedLevels.DELETE.getLevel());
        try {
          if(gwonhan == false) {
            throw new ForbiddenException("대상 자료에 대한 DELETE 권한없음");
        }
        
        if(!optDoc.equals(null)) {
            if(DocStatus.LIVE.getValue().equals(optDoc.get().getUDocStatus())) {
                 if (idfDoc.isCheckedOut()) {
                     throw new BadRequestException("현재 다른 사용자가 사용하고 있습니다.");
                 }else {
                     DocDetailDto docDetailDto = DocDetailDto.builder().uDocStatus(DocStatus.CLOSED.getValue()).build();
                     DataUpdateReqDto dataUpdateReqDto = DataUpdateReqDto.builder().doc(docDetailDto).build();
                     updateData(userSession, dataId, null, dataUpdateReqDto, null, false, false, true, false, ip);
                     
                     try { 
                         // 버전 리스트
                         List<String> rVersionLabelList = optDoc.get().getDocRepeatings().stream()
                             .filter(item2 -> null != item2.getRVersionLabel()).map(item2 -> item2.getRVersionLabel())
                             .collect(Collectors.toList());
                         String lastVersion = rVersionLabelList.get(rVersionLabelList.size() - 1);
                         insertLog(LogDoc.builder().uJobCode(DocLogItem.CJ.getValue())
                         .uDocId(optDoc.get().getRObjectId())
                         .uDocKey(optDoc.get().getUDocKey())
                         .uDocName(optDoc.get().getObjectName())
                         .uDocVersion(lastVersion)
                         .uFileSize(Long.parseLong(optDoc.get().getRContentSize()))
                         .uOwnDeptcode(optDoc.get().getUDeptCode())
                         .uActDeptCode(userSession.getUser().getOrgId())
                         .uJobUser(userId)
                         .uJobUserType("P") 
                         .uDocStatus("C")
                         .uSecLevel(optDoc.get().getUSecLevel())
                         .uCabinetCode(optDoc.get().getUCabinetCode())
                         .uUserIp(ip).build());  
                   } catch (Exception e) {
                       throw e;
                   }
                 }
                  result = "success";
            }else {
              throw new BadRequestException("문서 상태 'Close'입니다.");
            }
        }else {
            throw new BadRequestException("문서(자료)가 아닙니다.");
        }
        } catch (Exception e) {
          throw e;
        }finally {
          // Admin Session Close
          if (idfSession != null && idfSession.isConnected()) {
            idfSession.disconnect();
          }
          // Session Close
          sessionRelease(userSession.getUser().getUserId(), idfSession);
        }
        return result;
        }
     
    
    
 
    @Override
    public String patchDocUnClosed(String dataId, String docReason, UserSession userSession, String ip) throws Exception {
        
 
        IDfSession idfSession = DCTMUtils.getAdminSession();
        IDfPersistentObject idf_PObj = null;
        final String userId = userSession.getUser().getUserId();
        // 폴더인지 문서인지 확인
        Optional<Doc> optDoc = docService.selectOne(dataId, true);
        IDfDocument idfDoc = (IDfDocument)idfSession.getObject(new DfId(dataId));
        
        
        boolean isElectronicPayment = false;
        // SOFTM - [자료수정] - [CHECK POINT .01] : 전자결재함 하위의 자료일 경우 잘못된 요청 처리 (400 Bad Request)
        isElectronicPayment = folderService.selectAncestorHasFolType(dataId, FolderType.DWY);
        
        String result = "";
        Doc doc = optDoc.get();
        
        try {
              if(DocStatus.CLOSED.getValue().equals(doc.getUDocStatus())) {
               if ( isElectronicPayment) throw new BadRequestException("전자결재함 하위의 자료입니다.");
              
               if (idfDoc.isCheckedOut()) {throw new BadRequestException("현재 다른 사용자가 사용하고 있습니다.");}

                if (optDoc.isPresent()) {
                     if(idfDoc.isCheckedOut()) {
                       throw new BadRequestException("현재 다른 사용자가 사용하고 있습니다.");
                     }else {
                        try {
                            idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_req_closed");
                            idf_PObj.setString("u_req_doc_id", optDoc.get().getRObjectId());
                            idf_PObj.setString("u_req_doc_key", optDoc.get().getUDocKey());
                            idf_PObj.setString("u_req_user", userId);
                            idf_PObj.setString("u_req_dept_code", userSession.getSocialPerId());
                            idf_PObj.setString("u_req_date", (new DfTime()).toString());
                            idf_PObj.setString("u_req_status", "A");
                            idf_PObj.setString("u_own_dept_code", userSession.getSocialPerId());
                            idf_PObj.setString("u_approver",userId);
                            idf_PObj.setString("u_action_date", (new DfTime()).toString());
                            idf_PObj.save();
                        } catch (Exception e) {
                            throw e;
                        }
                        try {  
                              // 버전 리스트
                              List<String> rVersionLabelList = optDoc.get().getDocRepeatings().stream()
                                  .filter(item2 -> null != item2.getRVersionLabel()).map(item2 -> item2.getRVersionLabel())
                                  .collect(Collectors.toList());
                              String lastVersion = rVersionLabelList.get(rVersionLabelList.size() - 1);
                              insertLog(LogDoc.builder().uJobCode(DocLogItem.CL.getValue())
                              .uDocId(optDoc.get().getRObjectId())
                              .uDocKey(optDoc.get().getUDocKey())
                              .uDocName(optDoc.get().getObjectName())
                              .uDocVersion(lastVersion)
                              .uFileSize(Long.parseLong(optDoc.get().getRContentSize()))
                              .uOwnDeptcode(optDoc.get().getUDeptCode())
                              .uActDeptCode(userSession.getUser().getOrgId())
                              .uJobUser(userId)
                              .uJobUserType("P") 
                              .uDocStatus("L")
                              .uSecLevel(optDoc.get().getUSecLevel())
                              .uCabinetCode(optDoc.get().getUCabinetCode())
                              .uUserIp(ip).build()); 
                        } catch (Exception e) {
                            throw e;
                        }
                        try { 
                          DocDetailDto docDetailDto = DocDetailDto.builder().uDocStatus(DocStatus.LIVE.getValue()).build();
                          DataUpdateReqDto dataUpdateReqDto = DataUpdateReqDto.builder().doc(docDetailDto).build();
                          updateData(userSession, dataId, null, dataUpdateReqDto, null, false, false, true, false, ip);
                        } catch (Exception e) {
                            throw e;
                        }
                        result = "success";
                     }
                    }else {
                        throw new BadRequestException("문서(자료)가 아닙니다.");
                    }  
                
            } 
             else {
               throw new BadRequestException("문서 상태 'Live'입니다.");
            }
    
        } catch (Exception e) {
            throw e;
        }finally {
          
       // Admin Session Close
          if (idfSession != null && idfSession.isConnected()) {
            idfSession.disconnect();
          }
          
          // Session Close
          sessionRelease(userSession.getUser().getUserId(), idfSession);
          
        }
         
         return result;
    }
 
    @Override
    public List<DataCreatorDto> getDataCreators(UserSession userSession, String dataType, String dataId) {
      boolean isDoc = "D".equals(dataType);
      boolean isFolder = "F".equals(dataType);
      return isDoc ? docService.selectDocCreators(dataId) : isFolder ? folderService.selectFolderCreators(dataId) : new ArrayList<>();
    }
 
  @Override  // 자료 버전 리스트 조회
  public List<DocVersionListDto> getDataVersionListPaths(UserSession userSession, String dataId, boolean isMobile)
          throws Exception {
    final String userId = userSession.getUser().getUserId();
    Optional<Doc> optDoc = docService.selectDetailOne(dataId, userId);
    Doc doc = optDoc.get();
    String folderId = optDoc.get().getUFolId();
    boolean gwonhan;
    List<DocVersionListDto> DocVersionList = null;
    Optional<DocImp> optDocImp = docImpService.selectOne(dataId);
 
    gwonhan = authService.checkFolderAuth(folderId, userSession.getUser().getUserId(),GrantedLevels.READ.getLabel());
 
//    if(gwonhan == false) {
//      throw new ForbiddenException("대상 자료에 대한 READ 권한없음");
//    }
    // 모바일에서 요청한 경우
    if (isMobile) {
      // 특별사용자 리스트 (회장/부회장/각 회사 대표)
      Set<String> specialUserIdSet = codeService.getSpecialUserIdSet();
 
      // 현재 사용자의 특별사용자 여부 확인
      boolean isSpecial = specialUserIdSet.contains(userId);
      String uSecLevel = doc.getUSecLevel();
      String uDocStatus = doc.getUDocStatus();
 
      // 특별사용자가 아니고 제한문서일 경우 에러
      if (!isSpecial && SecLevelCode.SEC.getValue().equals(uSecLevel) && DocStatus.CLOSED.getValue().equals(uDocStatus)) {
        throw new ForbiddenException("모바일에서 제한등급 문서 조회 불가");
      }
    }
    if(optDoc.isPresent()) {
 
      String DocKey = optDoc.get().getUDocKey();
      DocVersionList = docService.selectDocVersionList(DocKey);
 
    }else if(optDocImp.isPresent()){
      String DocImpKey = optDocImp.get().getUDocKey();
      DocVersionList = docService.selectDocImpVersionList(DocImpKey);
    }
    else {
      throw new BadRequestException("문서(자료)가 아닙니다.");
    }
 
    return DocVersionList;
  }
    
    
    @Override
    public String postDocVersion(UserSession userSession, String dataId, boolean docVersionChck, String ip) throws Exception {
        //문서
        
        Optional<Doc> optDoc = docService.selectOne(dataId);
        Optional<DocImp> optDocImp = docImpService.selectOne(dataId);
        final String userId = userSession.getUser().getUserId();
        
       
        IDfSession idfSession = this.getIdfSession(userSession);
        IDfPersistentObject idf_PObj = null;
        IDfPersistentObject idf_PObj2 = null;
        
        boolean isDoc = false;
        Doc doc = new Doc();
 
        isDoc = optDoc.isPresent();
        
        boolean isDocimp = false;
        DocImp docImp = new DocImp();
        isDocimp = optDocImp.isPresent();
        DocDetailDto docDto = getModelMapper().map(doc, DocDetailDto.class);
        
        String result = "";
 
        
        if(optDoc.isPresent()) {
            //폴더
            doc = optDoc.get();
            String folderId  = optDoc.get().getUFolId();
            Optional<Folder> optFolder = folderService.selectOne(folderId);
            boolean gwonhan = false;
            gwonhan = authService.checkFolderAuth(folderId, userSession.getUser().getUserId(),GrantedLevels.DELETE.getLabel());
            
            if(gwonhan == false) {
                 throw new ForbiddenException("대상 자료에 대한 DELETE 권한없음");
             }
            if (optFolder.get().getUFolStatus().equals(FolderStatus.LOCK.getValue())) {
                    throw new ForbiddenException("잠금 처리된 폴더입니다.");
            } else if (isDoc) {
                Optional<Folder> optDocFolder = folderService.selectOne(doc.getUFolId());
                if (optDocFolder.isPresent()) {
                    Folder docFolder = optDocFolder.get();
                    if (docFolder.getUFolStatus().equals(FolderStatus.LOCK.getValue())) {
                        throw new ForbiddenException("잠금 처리된 폴더에 포함된 자료입니다.");
                    }
                }
            }
            if(doc.isUTakeoutFlag() == true) {
                throw new ForbiddenException("반출된 자료는 버전 유지 체크 불가");
            }
            try {
                idf_PObj = (IDfPersistentObject) idfSession.getObject(new DfId(optDoc.get().getRObjectId()));
                if(docVersionChck == true) {
                    idf_PObj.setBoolean("u_ver_keep_flag", true); // 문서 상태 값 변경
                } else {
                    idf_PObj.setBoolean("u_ver_keep_flag", false); // 문서 상태 값 변경
                }
                idf_PObj.save(); 
            }catch (Exception e) {
                throw e;
            }
            
            // 버전 리스트
              List<String> rVersionLabelList = optDoc.get().getDocRepeatings().stream()
                  .filter(item2 -> null != item2.getRVersionLabel()).map(item2 -> item2.getRVersionLabel())
                  .collect(Collectors.toList());
              String lastVersion = rVersionLabelList.get(rVersionLabelList.size() - 1);
            try {  
                 insertLog(LogDoc.builder().uJobCode(DocLogItem.DL.getValue())
                  .uDocId(doc.getRObjectId())
                  .uDocKey(doc.getUDocKey())
                  .uDocName(doc.getObjectName())
                  .uDocVersion(lastVersion)
                  .uOwnDeptcode(optDoc.get().getUDeptCode())
                  .uActDeptCode(userSession.getUser().getOrgId())
                  .uJobUser(userId).uJobUserType("P") 
                  .uDocStatus(doc.getUDocStatus())
                  .uSecLevel(doc.getUSecLevel())
                  .uCabinetCode(doc.getUCabinetCode())
                  .uJobGubun("V").uUserIp(ip).build());
                  idf_PObj.save();
                  result = "success";                  
                  
            } catch (Exception e) {
                throw e;
            }
 
            // 자료 변경 및 이력 저장
        }else if(optDocImp.isPresent()) {
            docImp = optDocImp.get();
            String folderImpId = optDocImp.get().getUFolId();
            Optional<Folder> opImpFolder = folderService.selectOne(folderImpId);
 
            boolean gwonhan = false;
            gwonhan = authService.checkFolderAuth(folderImpId, userSession.getUser().getUserId(),GrantedLevels.DELETE.getLabel());
            
            if(gwonhan == false) {
                 throw new ForbiddenException("대상 자료에 대한 DELETE 권한없음");
             }
            if (opImpFolder.get().getUFolStatus().equals(FolderStatus.LOCK.getValue())) {
                    throw new ForbiddenException("잠금 처리된 폴더입니다.");
            } else if (isDocimp) {
                Optional<Folder> optDocFolder = folderService.selectOne(docImp.getUFolId());
                if (optDocFolder.isPresent()) {
                    Folder docFolder = optDocFolder.get();
                    if (docFolder.getUFolStatus().equals(FolderStatus.LOCK.getValue())) {
                        throw new ForbiddenException("잠금 처리된 폴더에 포함된 자료입니다.");
                    }
                }
            }
            if(doc.isUTakeoutFlag() == true) {
                throw new ForbiddenException("반출된 자료는 버전 유지 체크 불가");
            }
                try {
                idf_PObj = (IDfPersistentObject) idfSession.getObject(new DfId(optDocImp.get().getUDocKey()));
                    if(docVersionChck == true) {
                        idf_PObj.setBoolean("u_ver_keep_flag", true); // 문서 상태 값 변경
                    } else {
                        idf_PObj.setBoolean("u_ver_keep_flag", false); // 문서 상태 값 변경
                    }
                idf_PObj.save();
                result = "success";
                }catch (Exception e) {
                    throw e;
                }
        }
        else {
            throw new ForbiddenException("문서(자료)가 아님");
        }
       
        return result;
    }
    
    @Override
    public List<DocLinkListDto> getDataLinkListPaths(UserSession userSession, String dataId, boolean isMobile)
            throws Exception {
 
        List<DocLinkListDto> docLinkList = null;
        boolean gwonhan = false;
        final String userId = userSession.getUser().getUserId();
        Optional<Doc> optDoc = docService.selectOne(dataId);
        String folderId = optDoc.get().getUFolId();
 
        gwonhan = authService.checkFolderAuth(folderId, userSession.getUser().getUserId(),
                GrantedLevels.READ.getLabel());
 
        if (gwonhan == false) {
            throw new ForbiddenException("대상 자료에 대한 READ 권한없음");
        }
        
        if (isMobile) {
          // 특별사용자 리스트 (회장/부회장/각 회사 대표)
          Set<String> specialUserIdSet = codeService.getSpecialUserIdSet();
          // 현재 사용자의 특별사용자 여부 확인
          boolean isSpecial = specialUserIdSet.contains(userId);
 
          String uSecLevel = optDoc.get().getUSecLevel();
          String uDocStatus = optDoc.get().getUDocStatus();
            
          // 특별사용자가 아니고 제한문서일 경우 에러
          if (!isSpecial && SecLevelCode.SEC.getValue().equals(uSecLevel) && DocStatus.CLOSED.getValue().equals(uDocStatus)) {
            throw new ForbiddenException("모바일에서 제한등급 문서 조회 불가");
          }
        }
        docLinkList = docService.selectDocLinkList(dataId);
 
        return docLinkList;
    }
   // 일반 검색
    @Override
    public List<DataDetailDto> getDataList(String searchName, String dataId, UserSession userSession, String ip,
            String hamSearchType, String folderType, boolean isMobile, String dataCabinetCode)
       throws Exception {
      
      
         String userDept = userSession.getUser().getComOrgId();
         String searchNameCheck = commonAuthDao.selectsearchNameCheck(userDept, searchName);
         
         if(searchNameCheck != null) {
           throw new ForbiddenException("해당 검색어로 검색 할 수 없습니다.");
         }
        //folderType =  DFO, RCY, RFO, PFO
        //프로젝트 함 지정 dataId 문서함 번호 등등
        //hamSearchType  
        boolean isRoot = hamSearchType != null;
 
            // 현재 사용자 아이디
            final String userId = userSession.getUser().getUserId();
            List<DataDetailDto> dataList = new ArrayList<>();
            // 검색 필터 설정
            FolderFilterDto folderFilterDto = null;
            if (isRoot) {
                // 프로젝트/투자, 연구과제, 부서 여부 확인
                HamInfoResult hamInfo = commonAuthDao.selectHamSearchInfo(hamSearchType)
                        .orElseThrow(() -> new BadRequestException("존재하지 않는 hamSearchType 입니다. (hamSearchType: " + hamSearchType + ")"));
                HamSearchType type = HamSearchType.findByValue(hamInfo.getHamType());
                switch (type) {
                // 부서함일 경우
                case DEPT:
                case COMPANY:
                case COMPANY_M:
                    VDept dept = gwDeptDao.selectOneByOrgId(hamSearchType)
                            .orElseThrow(() -> new BadRequestException("존재하지 않는 부서코드입니다."));
                            folderFilterDto = FolderFilterDto.builder()
                            .uCabinetCode(dept.getUCabinetCode())
                            .uUpFolId(DCTMConstants.DCTM_BLANK)
                            .uFolType(folderType == null ? FolderType.DFO.getValue() : folderType) // DFO: 부서함 하위, PCL:
                                                                                                    // 프로젝트/투자 분류폴더,
                                                                                                    // RCL: 연구과제 분류폴더
                            .build();
                    break;
                // 프로젝트/투자일 경우
                case PROJECT:
                    Project project = projectService.selectProjectByUPjtCode(hamSearchType)
                            .orElseThrow(() -> new BadRequestException("존재하지 않는 프로젝트 코드입니다."));
                            folderFilterDto = FolderFilterDto.builder()
                            .uCabinetCode(project.getUCabinetCode())
                            .uUpFolId(DCTMConstants.DCTM_BLANK).uPrCode(hamSearchType)
                            .uPrType(ProjectType.PROJECT.getValue())
                            .uFolType(FolderType.PFO.getValue()).build();
                    break;
                    
                    // 연구과제일 경우
                case RESEARCH:
                    Research research = researchService.selectResearchByURschCode(hamSearchType)
                            .orElseThrow(() -> new BadRequestException("존재하지 않는 연구과제 코드입니다."));
                            folderFilterDto = FolderFilterDto.builder()
                            .uCabinetCode(research.getUCabinetCode())
                            .uUpFolId(DCTMConstants.DCTM_BLANK).uPrCode(hamSearchType)
                            .uPrType(ProjectType.PROJECT.getValue())
                            .uFolType(FolderType.RFO.getValue()).build();
                    break;
                    
                default:
                }
            }
            else {
                if(StringUtils.isEmpty(dataId)) {
                  if(StringUtils.isEmpty(folderType)){
                    folderFilterDto = FolderFilterDto.builder()
                        .uCabinetCode(dataCabinetCode)
                        .build();
                  }else {
                  String folTp = null;
                  //DFO, RCY, RFO, PFO
                  if(folderType.equals("DFO")) {
                    folTp = "DFO";
                  }else if(folderType.equals("RFO")) {
                    folTp = "RFO";
                  }else if(folderType.equals("PFO")) {
                    folTp = "PFO";
                  }
                  folderFilterDto = FolderFilterDto.builder()
                          .uCabinetCode(dataCabinetCode)
                           .uFolType(folTp)
                          .build();
                  }
                }else {//dataCabinetCode
                  Folder parentFolder =  folderService.selectOne(dataId).orElseThrow(() -> new BadRequestException("존재하지 않는 폴더입니다."));
                  String parentUFolType = parentFolder.getUFolType();
                  folderFilterDto = FolderFilterDto.builder()
                          .uUpFolId(dataId)
                          .uFolType(parentUFolType)
                          .build();
                }
 
            }
            
            final String orgId = userSession.getUser().getOrgId();
            // 권한있는 폴더 상세 리스트 조회
            List<FolderDetailDto> folderDetailDtoList = folderService.selectAuthorizedDetailSearchList(folderFilterDto,
                    userId, orgId , searchName, dataId, dataCabinetCode);
 
            // 리턴할 자료 리스트에 추가
            dataList.addAll(folderDetailDtoList.stream().map(
                    item -> DataDetailDto.builder().dataType(DboxObjectType.FOLDER.getValue()).folder(item).build())
                    .filter(item -> {
                         boolean authorized = GrantedLevels.findByLabel(item.getFolder().getMaxPermitType()) >= GrantedLevels.READ.getLevel();
                         if(authorized == true) {
                             return true;
                         }
                         return false;
                    })
                    .collect(Collectors.toList()));
            // 문서 시작 ======================================================================= 
            // 검색 필터 설정
                DocFilterDto docFilterDto = null;
                if (isRoot) {
                    // 프로젝트/투자, 연구과제, 부서 여부 확인
                     HamInfoResult hamInfo = commonAuthDao.selectHamSearchInfo(hamSearchType)
                            .orElseThrow(() -> new BadRequestException("존재하지 않는 hamSearchType 입니다. (dataCode: " + hamSearchType + ")"));
                    HamSearchType type = HamSearchType.findByValue(hamInfo.getHamType());
                    switch (type) {
                    // 부서함일 경우
                    case DEPT:
                    case COMPANY:
                    case COMPANY_M:
                        VDept dept = gwDeptDao.selectOneByOrgId(hamSearchType)
                                .orElseThrow(() -> new BadRequestException("존재하지 않는 부서코드입니다."));
                                docFilterDto = DocFilterDto.builder().uCabinetCode(dept.getUCabinetCode())
                                .uPrType("E")
                                .uFolId(DCTMConstants.DCTM_BLANK).build();
 
                        break;
                    // 프로젝트/투자일 경우
                    case PROJECT:
                        Project project = projectService.selectProjectByUPjtCode(hamSearchType)
                                .orElseThrow(() -> new BadRequestException("존재하지 않는 프로젝트 코드입니다."));
                                docFilterDto = DocFilterDto.builder()
                                .uCabinetCode(project.getUCabinetCode())
                                .uFolId(DCTMConstants.DCTM_BLANK)
                                .uPrCode(dataId)
                                .uPrType(ProjectType.PROJECT.getValue())
                                .build();
                                
                        break;
                    // 연구과제일 경우
                    case RESEARCH:
                        Research research = researchService.selectResearchByURschCode(hamSearchType)
                                .orElseThrow(() -> new BadRequestException("존재하지 않는 연구과제 코드입니다."));
                                docFilterDto = DocFilterDto.builder()
                                .uCabinetCode(research.getUCabinetCode())
                                .uFolId(DCTMConstants.DCTM_BLANK)
                                .uPrCode(dataId)
                                .uPrType(ProjectType.RESEARCH.getValue())
                                .build();
                        break;
                    default:
                    }
                } else {
                  if(StringUtils.isEmpty(dataId)) {
                    if(StringUtils.isEmpty(folderType)) {
                        docFilterDto = DocFilterDto.builder()
                            .uCabinetCode(dataCabinetCode)
                            .build();
                    }else {
                      String docTp = null;
                      //DFO, RCY, RFO, PFO
                      if(folderType.equals("DFO")) {
                        docTp = "D";
                      }else if(folderType.equals("RFO")) {
                        docTp = "R";
                      }else if(folderType.equals("PFO")) {
                        docTp = "P";
                      }
                      docFilterDto = DocFilterDto.builder()
                              .uCabinetCode(dataCabinetCode)
                              .uPrType(docTp)
                              .build();
                    }
                  }else {//dataCabinetCode
                    Folder parentFolder =  folderService.selectOne(dataId).orElseThrow(() -> new BadRequestException("존재하지 않는 폴더입니다."));
                    docFilterDto = DocFilterDto.builder()
                            .uFolId(dataId)
                            .build();
                  }
                }
                List<DocDetailDto> docDetailDtoList = new ArrayList<>();
                // 검색(2) 이상의 권한을 가진 문서 리스트 조회
                    docDetailDtoList  = docService.selectAuthorizedsearchList(docFilterDto, userId,
                        GrantedLevels.BROWSE.getLevel(), searchName, folderDetailDtoList, dataId, dataCabinetCode);
                // 모바일에서 요청한 경우
                if (isMobile) {
                    // 특별사용자 리스트 (회장/부회장/각 회사 대표)
                    Set<String> specialUserIdSet = codeService.getSpecialUserIdSet();
 
                    docDetailDtoList = docDetailDtoList.stream().filter(item -> {
                      // 현재 사용자의 특별사용자 여부 확인
                      String currentUserId = userSession.getUser().getUserId();
                      boolean isSpecial = specialUserIdSet.contains(currentUserId);
                      String uSecLevel = item.getUSecLevel();
                      String uDocStatus = item.getUDocStatus();
 
                      // 특별사용자가 아니고 제한문서일 경우 제외
                      if (!isSpecial && SecLevelCode.SEC.getValue().equals(uSecLevel) && DocStatus.CLOSED.getValue().equals(uDocStatus)) {
                        return false;
                      } else {
                        return true;
                      }
                    }).collect(Collectors.toList());
                }
                
                // 리턴할 자료 리스트에 추가
                dataList.addAll(docDetailDtoList.stream().map(
                        item -> DataDetailDto.builder().dataType(DboxObjectType.DOCUMENT.getValue()).doc(item).build())
                        .collect(Collectors.toList()));
                
                // 중요문서 시작   개발이 되지 않아 스톱.
                
               /* // 검색 필터 설정
                    DocImpFilterDto docImpFilterDto = null;
                    if (isRoot) {
                        // 프로젝트/투자, 연구과제, 부서 여부 확인
                            HamInfoResult hamInfo = commonAuthDao.selectHamSearchInfo(hamSearchType)
                                 .orElseThrow(() -> new BadRequestException("존재하지 않는 hamSearchType 입니다. (hamSearchType: " + hamSearchType + ")"));
                          HamSearchType type = HamSearchType.findByValue(hamInfo.getHamType());
                        switch (type) {
                        // 부서함일 경우
                        case DEPT:
                        case COMPANY:
                        case COMPANY_M:
                            VDept dept = gwDeptDao.selectOneByOrgId(hamSearchType)
                                    .orElseThrow(() -> new BadRequestException("존재하지 않는 부서코드입니다."));
                                     docImpFilterDto = DocImpFilterDto.builder().uCabinetCode(dept.getUCabinetCode())
                                    .uFolId(DCTMConstants.DCTM_BLANK).build();
 
                            break;
                        // 프로젝트/투자일 경우
                        case PROJECT:
                            Project project = projectService.selectProjectByUPjtCode(hamSearchType)
                                    .orElseThrow(() -> new BadRequestException("존재하지 않는 프로젝트 코드입니다."));
                                    docImpFilterDto = DocImpFilterDto.builder()
                                    .uCabinetCode(project.getUCabinetCode())
                                    .uFolId(DCTMConstants.DCTM_BLANK)
                                    .uPrCode(dataId)
                                    .uPrType("P").build();
                            break;
                            
                        // 연구과제일 경우
                        case RESEARCH:
                            Research research = researchService.selectResearchByURschCode(hamSearchType)
                                    .orElseThrow(() -> new BadRequestException("존재하지 않는 연구과제 코드입니다."));
                                    docImpFilterDto = DocImpFilterDto.builder()
                                    .uCabinetCode(research.getUCabinetCode())
                                    .uFolId(DCTMConstants.DCTM_BLANK)
                                    .uPrType("R").build();
                            break;
                        default:
                       }
                    } else {
                      if(StringUtils.isEmpty(dataId)) {
                        if(StringUtils.isEmpty(folderType)) {
                            docFilterDto = DocFilterDto.builder()
                                .uCabinetCode(dataCabinetCode)
                                .build();
                          }else {
                            String docTp = null;
                            //DFO, RCY, RFO, PFO
                            if(folderType.equals("DFO")) {
                              docTp = "D";
                            }else if(folderType.equals("RFO")) {
                              docTp = "R";
                            }else if(folderType.equals("PFO")) {
                              docTp = "P";
                            }
                            docFilterDto = DocFilterDto.builder()
                                    .uCabinetCode(dataCabinetCode)
                                    .uPrType(docTp)
                                    .build();
                          }
                        }else {//dataCabinetCode
                        Folder parentFolder =  folderService.selectOne(dataId).orElseThrow(() -> new BadRequestException("존재하지 않는 폴더입니다."));
                        docFilterDto = DocFilterDto.builder()
                                .uFolId(dataId)
                                .build();
                      }
 
                    }
                    // 검색(2) 이상의 권한을 가진 문서 리스트 조회
                    List<DocDetailDto> docImpDetailDtoList  = docService.selectImpAuthorizedsearchList(docFilterDto, userId,
                            GrantedLevels.BROWSE.getLevel(), searchName, folderDetailDtoList, dataId, dataCabinetCode);
                    // 모바일에서 요청한 경우
                    if (isMobile) {
                      // 특별사용자 리스트 (회장/부회장/각 회사 대표)
                        Set<String> specialUserIdSet = codeService.getSpecialUserIdSet();
 
                        docImpDetailDtoList = docImpDetailDtoList.stream().filter(item -> {
                        // 현재 사용자의 특별사용자 여부 확인
                        String currentUserId = userSession.getUser().getUserId();
                        boolean isSpecial = specialUserIdSet.contains(currentUserId);
                        String uSecLevel = item.getUSecLevel();
                        String uDocStatus = item.getUDocStatus();
 
                        // 특별사용자가 아니고 제한문서일 경우 제외
                        if (!isSpecial && SecLevelCode.SEC.getValue().equals(uSecLevel) && DocStatus.CLOSED.getValue().equals(uDocStatus)) {
                          return false;
                        } else {
                          return true;
                        }
                      }).collect(Collectors.toList());
                    }
                    
                    // 리턴할 자료 리스트에 추가
                    dataList.addAll(docImpDetailDtoList.stream().map(
                            item -> DataDetailDto.builder().dataType(DboxObjectType.DOCUMENT.getValue()).doc(item).build())
                            .collect(Collectors.toList()));*/
            return dataList;
    }
    @Override
    public List<Map<String, String>> getDocsEApproval(String dataId) {
      return docDao.selectDocsEApproval(dataId);
    }

    // 통합 검색
    @Override
    public List<DataDetailDto> getDataDsearchList(String searchName, String dataId, UserSession userSession, String deptCode, String folderCode, String folderType) throws Exception {
      return dBoxSearchService.getDataFromDBoxSearch(new DBoxSearch(userSession.getDUserId(), searchName, deptCode, folderCode, folderType));
    }

  @Override
    public String patchDocClosedByAttach(String dataId, UserSession userSession, String ip) throws Exception {
        
        //========================================
        // 첨부시 문서 Close 처리
        //    - 첨부시 (파일, 내부, 메신저 아닐경우 호출됨 )
        //
        //     dataId        - 문서 ID
        //    userSession    - Session
        //    ip            - IP
        //========================================
        
        String            result             = "";    
        
        IDfSession         idfAdminSession    = null; 
        IDfSession        idfSession        = this.getIdfSession(userSession);
        IDfSysObject    idf_SObj         = null;
        final String    userId            = userSession.getUser().getUserId();
        boolean            bIsAdminUse        = false;
        
        try { 
        
            // 첨부시 조회권한밖에 없을경우 관리자 세션처
            IDfDocument idfDoc = (IDfDocument) idfSession.getObject(new DfId(dataId));
            if(idfDoc.getPermit() < 6)
            {
                 // 세션 생성
                bIsAdminUse        = true;
                idfAdminSession = DCTMUtils.getAdminSession();
            }
            
            // 폴더인지 문서인지 확인
            Optional<Doc> optDoc     = docService.selectOne(dataId);
            
            //해당 처리 할 문서 폴더 ID 가지고옴.
            String     folderId         = optDoc.get().getUFolId();
            Doc     doc              = optDoc.get();
            
            // 폴더정보
            Optional<Folder> folderCon = folderService.selectOne(folderId);
 
            // 데이터 가져온 값에 대한 문서 상태가 LIVE인 경우
            // 편집중을 이곳 호출 안됨
            if(DocStatus.LIVE.getValue().equals(doc.getUDocStatus())) 
            {
                if(optDoc.isPresent()) {
 
                    // 조회 후 Insert 부분
                    List<AuthShare> authShareList = authShareDao.selectList(folderId);
                    authShareList.stream().forEach(item -> {
                        AuthShareDto  AuthShareDto;
                        try {
                            AuthShareDto = getModelMapper().map(folderId, AuthShareDto.class);
                            IDfPersistentObject idf_PObj3;
                            idf_PObj3 = (IDfPersistentObject) idfSession.newObject("edms_auth_base");
                            idf_PObj3.setString("u_obj_id"        , folderCon.get().getUUpFolId());
                            idf_PObj3.setString("u_obj_type"    , "D");
                            idf_PObj3.setString("u_doc_status"    , "C");
                            idf_PObj3.setString("u_permit_type"    , AuthShareDto.getUPermitType());
                            idf_PObj3.setString("u_own_dept_yn"    , "N");
                            idf_PObj3.setString("u_author_id"    , AuthShareDto.getUAuthorId());
                            idf_PObj3.setString("u_author_type"    , AuthShareDto.getUAuthorType());
                            idf_PObj3.setString("u_create_user"    , idfSession.getLoginUserName());
                            idf_PObj3.setString("u_create_date"    , (new DfTime()).toString());
                            idf_PObj3.save();
                        } catch (Exception e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    });
                    // 삭제 부분
                    authShareList.stream().forEach(item -> {
                            try {
                                IDfPersistentObject idf_PObj3 = idfSession.getObject(new DfId(item.getRObjectId()));
                                idf_PObj3.setString("u_obj_id", folderId);
                                idf_PObj3.destroy();
                            } catch (DfException e) {
                                e.printStackTrace();
                            }
                        });
                    
                    try { 
                          // 버전 리스트
                          List<String> rVersionLabelList = optDoc.get().getDocRepeatings().stream()
                              .filter(item2 -> null != item2.getRVersionLabel()).map(item2 -> item2.getRVersionLabel())
                              .collect(Collectors.toList());
                         
                          String lastVersion = rVersionLabelList.get(rVersionLabelList.size() - 1);
                          
                          insertLog(LogDoc.builder().uJobCode(DocLogItem.DL.getValue()).uDocId(optDoc.get().getRObjectId())
                                  .uDocKey(optDoc.get().getUDocKey()).uDocName(optDoc.get().getObjectName()).uDocVersion(lastVersion)
                                  .uOwnDeptcode(optDoc.get().getUDeptCode()).uActDeptCode(userSession.getUser().getOrgId()).uJobUser(userId).uJobUserType("P") 
                                  .uDocStatus("C").uSecLevel(optDoc.get().getUSecLevel())
                                  .uCabinetCode(doc.getUCabinetCode()).uUserIp(ip).build());
                         
                    } catch (Exception e) {
                        throw e;
                    }
                    
                    try { 
                        
                        // 권한 없을경우 관리자 세션 
                        if(bIsAdminUse)
                        {
                            idf_SObj = (IDfSysObject) idfAdminSession.getObject(new DfId(optDoc.get().getRObjectId()));
                        }
                        else
                        {
                            idf_SObj = (IDfSysObject) idfSession.getObject(new DfId(optDoc.get().getRObjectId()));
                        }
                        
                        idf_SObj.setString("u_doc_status"    , "C");                         // 문서 상태 값 변경
                        idf_SObj.setString("u_closed_date"    , (new DfTime()).toString());     //close 시간
                        idf_SObj.setString("u_closer"        , userId);                         // close 처리 담당자.
                        
                        idf_SObj.save();                                
                            
                        } catch (Exception e) {
                            throw e;
                        }
 
                    }else {
                        
                        // 첨부시 문서만 호출됨.
                    // throw new BadRequestException("문서(자료)가 아닙니다.");
                }
                result = "success";
            }
            
        } 
        catch (Exception e) 
        {
            throw e;
        } finally {
            
            // Admin Session Close
            if (idfAdminSession != null && idfAdminSession.isConnected()) {
                idfAdminSession.disconnect();
            }
            
            // Session Close
            sessionRelease(userSession.getUser().getUserId(), idfSession);
            
        }
            
        return result;
    }

    @Override
    public String unlockDataOne(String dataId, UserSession userSession, String ip) throws Exception {
      String userId = userSession.getDUserId();
      IDfSession adSess = null;

      try { 
        adSess = DCTMUtils.getAdminSession();
        
//        if (!adSess.isTransactionActive()) { adSess.beginTrans(); }
        
        Doc doc = docDao.selectOne(dataId, true).orElse(new Doc());
        VUser userData = gwUserDao.selectOneByUserId(doc.getRLockOwner()).orElse(new VUser());
        VUser userData2 =  gwUserDao.selectOneByUserId(userId).orElse(new VUser());
//      IDfSession idfSession = this.getIdfSession(userSession);
        if(!(userId.equals(doc.getRLockOwner()))) {
          throw new RuntimeException("편집중인 " + userData.getOrgNm() + " " + userData.getDisplayName() + " " +userData.getPstnName() +" 만 편집중 해제 처리 할 수 있습니다.");
        }else {
          String userType = "P"; //개인
          lockedDataService.unlockData(dataId, userSession, ip, userType);
        }
      } catch (Exception e) {
        // Admin Session Close
        if (adSess != null && adSess.isConnected()) {
          adSess.disconnect();
        }
        throw e;
      }
      return "";
    }
  
  
  
}